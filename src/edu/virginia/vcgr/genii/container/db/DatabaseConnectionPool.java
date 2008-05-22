package edu.virginia.vcgr.genii.container.db;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.stats.ContainerStatistics;
import edu.virginia.vcgr.genii.client.stats.DBConnectionDataPoint;
import edu.virginia.vcgr.genii.container.Container;

public class DatabaseConnectionPool
{
	static private final String _DB_CLASS_NAME_PROPERTY =
		"edu.virginia.vcgr.genii.container.db.db-class-name";
	static private final String _DB_CONNECT_STRING_PROPERTY =
		"edu.virginia.vcgr.genii.container.db.db-connect-string";
	static private final String _DB_USER_PROPERTY =
		"edu.virginia.vcgr.genii.container.db.db-user";
	static private final String _DB_PASSWORD_PROPERTY =
		"edu.virginia.vcgr.genii.container.db.db-password";
	static private final String _DB_POOL_SIZE_PROPERTY =
		"edu.virginia.vcgr.genii.container.db.pool-size";
	
	static private final String _SPECIAL_STRING = "${server-dir}";
	
	static private final String _DB_POOL_SIZE_DEFAULT = "16";
	static private final long REJUVENATION_CYCLE = 1000L * 60 * 5;
	
	static private Log _logger = LogFactory.getLog(DatabaseConnectionPool.class);
	
	private ReadWriteLock _lock = new ReentrantReadWriteLock(true);
	private int _poolSize;
	private LinkedList<Connection> _connPool;
	private String _connectString;
	private String _user;
	private String _password;
	private String _className;
	
	static private String replaceMacros(String str)
	{
		ConfigurationManager man = Container.getConfigurationManager();
		
		if (man == null)
			return str;
		
		File dir = Container.getConfigurationManager().getUserDirectory();
		
		int start = str.indexOf(_SPECIAL_STRING);
		if (start < 0)
			return str;
		
		return str.substring(0, start) +
			dir.getAbsolutePath() + 
			str.substring(start + _SPECIAL_STRING.length());		
	}
	
	public DatabaseConnectionPool(Properties connectionProperties)
		throws IllegalAccessException, ClassNotFoundException, InstantiationException
	{
		_poolSize = Integer.parseInt(connectionProperties.getProperty(
			_DB_POOL_SIZE_PROPERTY, _DB_POOL_SIZE_DEFAULT));
		
		_connPool = new LinkedList<Connection>();
		
		_className = connectionProperties.getProperty(
			_DB_CLASS_NAME_PROPERTY);
		if (_className == null)
			throw new IllegalArgumentException(
				"Class name for database connection cannot be null.");
		
		_connectString = connectionProperties.getProperty(_DB_CONNECT_STRING_PROPERTY);
		if (_connectString == null)
			throw new IllegalArgumentException(
				"Connect string cannot be null for database connection.");
		
		_connectString = replaceMacros(_connectString);
		
		Class.forName(_className).newInstance();
		
		_user = connectionProperties.getProperty(_DB_USER_PROPERTY);
		_password = connectionProperties.getProperty(_DB_PASSWORD_PROPERTY);
		
		Thread t = new Thread(new DBRejuvenator());
		t.setDaemon(true);
		t.setName("DB Rejuvenator Thread");
		t.start();
	}
	
	public Connection acquire() throws SQLException
	{
		Connection connection = null;
		_logger.debug("Acquiring DB connection[" + _connPool.size() + "]");
		
		_lock.readLock().lock();
		synchronized(_connPool)
		{
			if (!_connPool.isEmpty())
			{
				connection = _connPool.removeFirst();
			}
		}
			
		if (connection == null)
			connection = createConnection();
		
		((ConnectionInterceptor)Proxy.getInvocationHandler(
			connection)).setAcquired();
		
		return connection;
	}
	
	public void release(Connection conn)
	{
		_logger.debug("Releasing a database connection [" + _connPool.size() + "].");
		
		if (conn == null)
			return;
		
		try
		{
			try
			{
				conn.rollback();
				synchronized(_connPool)
				{
					if (_connPool.size() < _poolSize)
					{
						_connPool.addLast(conn);
						return;
					}
				}
			}
			catch (SQLException sqe)
			{
				_logger.error("Exception releasing connection.", sqe);
			}
	
			try 
			{ 
				((ConnectionInterceptor)Proxy.getInvocationHandler(
					conn)).getConnection().close();
			}
			catch (Throwable t) 
			{ 
				_logger.error("Error closing the connection.", t); 
			}
		}
		finally
		{
			((ConnectionInterceptor)Proxy.getInvocationHandler(
				conn)).setReleased();
			
			_lock.readLock().unlock();
		}
	}
	
	protected Connection createConnection() throws SQLException
	{
		_logger.debug("Creating a new database connection.");
		
		Connection conn = DriverManager.getConnection(_connectString, _user, _password);
		conn.setAutoCommit(false);
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		return (Connection)Proxy.newProxyInstance(
			Thread.currentThread().getContextClassLoader(),
			new Class[] { Connection.class }, new ConnectionInterceptor(conn));
	}
	
	private void rejuvenate()
	{
		_logger.info("Rejuvenating database.");
		Connection connection = null;
		
		try
		{
			_lock.writeLock().lock();
			
			// There's no need to synchronize on the connection pool, 
			// we have the only thread, because of the write lock, that
			// could be in here.
			
			try
			{
				/* I know that this breaks the pluggability of our database,
				 * but I don't have enough time to do this right right now.
				 */
				connection = DriverManager.getConnection(
					"jdbc:derby:;shutdown=true");
			}
			catch (Throwable cause)
			{
				_logger.debug("Expected exception for rejuvenation.", cause);
			}
			finally
			{
				StreamUtils.close(connection);
			}
			
			
			for (Connection conn : _connPool)
			{
				try
				{
					((ConnectionInterceptor)Proxy.getInvocationHandler(
						conn)).getConnection().close();
				}
				catch (Throwable cause)
				{
					_logger.debug(
						"Exception occurred trying to close connection.", cause);
				}
			}
			
			/* Force the GC to unload the database driver.  It'll get reloaded
			 * when the next connection is made. 
			 */
			_connPool.clear();
			
			/* There's no way in our system to "guarantee" that all references
			 * to all connections are released to the system, so we are just
			 * going to hang out for a few seconds to give them a chance.
			 */
			try { Thread.sleep(1000L * 5); } catch (Throwable cause) {}
			System.gc();
			
			try
			{
				Class.forName(_className).newInstance();
			}
			catch (Throwable cause)
			{
				_logger.error("Unable to reload database after rejuvenation.",
					cause);
			}
			
			ContainerStatistics.instance().getDatabaseStatistics().resetDatabase();
		}
		finally
		{
			_lock.writeLock().unlock();
			_logger.info("Done rejuvenating database.");
		}
	}
	
	private class DBRejuvenator implements Runnable
	{
		public void run()
		{
			ContainerStatistics.instance().getDatabaseStatistics().resetDatabase();
			
			while (true)
			{
				try
				{
					Thread.sleep(REJUVENATION_CYCLE);
					rejuvenate();
				}
				catch (Throwable cause)
				{
					_logger.error("DB Rejuvenation Thread caugh an exception.",
						cause);
				}
			}
		}
	}
	
	static protected class ConnectionInterceptor implements InvocationHandler
	{
		static private Method _CLOSE_METHOD;
		
		static
		{
			try
			{
				_CLOSE_METHOD = Connection.class.getDeclaredMethod(
					"close", new Class[0]);
			}
			catch (Throwable t)
			{
				_logger.error("Couldn't load the close method.", t);
			}
		}
		
		private DBConnectionDataPoint _stat = null;
		private Object _instance;
		
		public ConnectionInterceptor(Object instance)
		{
			_instance = instance;
		}
		
		public Connection getConnection()
		{
			return (Connection)_instance;
		}
		
		public void setAcquired()
		{
			_stat = ContainerStatistics.instance(
				).getDatabaseStatistics().openConnection();
		}
		
		public void setReleased()
		{
			if (_stat != null)
				_stat.markClosed();
			_stat = null;
		}
		
		public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable
		{
			if (method.equals(_CLOSE_METHOD))
			{
				_logger.error("Someone tried to close a pooled connection.");
				throw new RuntimeException(
					"Someone tried to close a pooled connection.");
			}
			
			boolean interrupted = Thread.interrupted();
			try
			{
				return method.invoke(_instance, args);
			}
			catch (Throwable cause)
			{
				_logger.error("Error calling method on connection.", cause);
				if (cause instanceof InvocationTargetException)
				{
					throw cause.getCause();
				}
				
				throw cause;
			}
			finally
			{
				if (interrupted)
					Thread.currentThread().interrupt();
			}
		}
	}
}
