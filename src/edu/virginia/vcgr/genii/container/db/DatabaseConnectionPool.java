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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
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
	
	static private final String _DB_POOL_SIZE_DEFAULT = "8";
	
	static private Log _logger = LogFactory.getLog(DatabaseConnectionPool.class);
	
	private int _poolSize;
	private LinkedList<Connection> _connPool;
	
	private String _connectString;
	private String _user;
	private String _password;
	
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
		
		String className = connectionProperties.getProperty(
			_DB_CLASS_NAME_PROPERTY);
		if (className == null)
			throw new IllegalArgumentException(
				"Class name for database connection cannot be null.");
		
		_connectString = connectionProperties.getProperty(_DB_CONNECT_STRING_PROPERTY);
		if (_connectString == null)
			throw new IllegalArgumentException(
				"Connect string cannot be null for database connection.");
		
		_connectString = replaceMacros(_connectString);
		
		Class.forName(className).newInstance();
		
		_user = connectionProperties.getProperty(_DB_USER_PROPERTY);
		_password = connectionProperties.getProperty(_DB_PASSWORD_PROPERTY);
	}
	
	public Connection acquire() throws SQLException
	{
		_logger.debug("Acquiring DB connection[" + _connPool.size() + "]");
		synchronized(_connPool)
		{
			if (!_connPool.isEmpty())
				return _connPool.removeFirst();
		}
		
		return createConnection();
	}
	
	public void release(Connection conn)
	{
		_logger.debug("Releasing a database connection [" + _connPool.size() + "].");
		
		if (conn == null)
			return;
		
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
		
		private Object _instance;
		
		public ConnectionInterceptor(Object instance)
		{
			_instance = instance;
		}
		
		public Connection getConnection()
		{
			return (Connection)_instance;
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
