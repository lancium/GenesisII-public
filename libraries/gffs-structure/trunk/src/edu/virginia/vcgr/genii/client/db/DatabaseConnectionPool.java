package edu.virginia.vcgr.genii.client.db;

import java.io.File;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.system.classloader.GenesisClassLoader;

public class DatabaseConnectionPool
{
	static private Log _logger = LogFactory.getLog(DatabaseConnectionPool.class);

	// set this to true if you want more diagnostics for timing printed at debug level.
	public static boolean ENABLE_DB_TIMING_LOGS = false;

	// the minimum number of connections that we need for known peak db use.
	// the actual minimum is probably about 66, but we need breathing room.
	static public final int MINIMUM_POOL_SIZE = 70;

	// how many connections to the database should be pooled?
	static protected final int _DB_POOL_SIZE_DEFAULT = 96;

	// how long should the database user wait for a connection from the pool?
	static public final int MAX_SNOOZE_AWAITING_POOL = 2 * 60 * 1000; // in milliseconds.
	// hmmm: ridiculously large delay allowed now; was 4 seconds originally!
	// currently set to 2 minutes before the db attempt will fail.

	/*
	 * number of milliseconds to snooze between lock attempts. it doesn't seem to help to retry very frequently, kind of seems worse.
	 */
	static public final int TIME_TAKEN_PER_SNOOZE = 240;

	private LinkedList<Connection> _connPool; // all of our pooled db connections.
	private int _poolSize; // how big the pool may grow.
	private String _connectString;
	private String _user;
	private String _password;

	protected DatabaseConnectionPool(DBPropertyNames names, Properties connectionProperties, String specialString)
	{
		setConnPool(new LinkedList<Connection>());
		setPoolSize(Integer.parseInt(connectionProperties.getProperty(names._DB_POOL_SIZE_PROPERTY, "" + _DB_POOL_SIZE_DEFAULT)));
		if (getPoolSize() < MINIMUM_POOL_SIZE) {
			_logger.debug("existing db pool size of " + getPoolSize() + " was below minimum of " + MINIMUM_POOL_SIZE);
			setPoolSize(MINIMUM_POOL_SIZE);
		}
		setUser(connectionProperties.getProperty(names._DB_USER_PROPERTY));
		setPassword(connectionProperties.getProperty(names._DB_PASSWORD_PROPERTY));

		if (_logger.isDebugEnabled()) {
			_logger.debug("database connection pool size: " + getPoolSize());
		}

		setConnectString(replaceMacros(connectionProperties.getProperty(names._DB_CONNECT_STRING_PROPERTY), specialString));
		if (getConnectString() == null)
			throw new IllegalArgumentException("Connect string cannot be null for database connection.");

		for (int i = 0; i < getPoolSize(); i++) {
			try {
				getConnPool().add(createConnection());
			} catch (SQLException e) {
				_logger.error("failed to create initial DB connection", e);
			}
		}
	}

	public static class DBPropertyNames
	{
		public String _DB_CONNECT_STRING_PROPERTY = "edu.virginia.vcgr.genii.container.db.db-connect-string";
		public String _DB_USER_PROPERTY = "edu.virginia.vcgr.genii.container.db.db-user";
		public String _DB_PASSWORD_PROPERTY = "edu.virginia.vcgr.genii.container.db.db-password";
		public String _DB_POOL_SIZE_PROPERTY = "edu.virginia.vcgr.genii.container.db.pool-size";

		// uses the default database property names for containers.
		public DBPropertyNames()
		{
		}

		// swaps the property names over to client side usage.
		public void useClientNames()
		{
			_DB_CONNECT_STRING_PROPERTY = "edu.virginia.vcgr.genii.client.db.db-connect-string";
			_DB_USER_PROPERTY = "edu.virginia.vcgr.genii.client.db.db-user";
			_DB_PASSWORD_PROPERTY = "edu.virginia.vcgr.genii.client.db.db-password";
			_DB_POOL_SIZE_PROPERTY = "edu.virginia.vcgr.genii.client.db.pool-size";
		}
	}

	static private String replaceMacros(String str, String specialString)
	{
		if (str == null)
			return null;
		if (specialString == null)
			return str;

		ConfigurationManager man = ConfigurationManager.getCurrentConfiguration();

		if (man == null)
			return str;

		File dir = man.getUserDirectory();

		int start = str.indexOf(specialString);
		if (start < 0)
			return str;

		return str.substring(0, start) + dir.getAbsolutePath() + str.substring(start + specialString.length());
	}

	protected LinkedList<Connection> getConnPool()
	{
		return _connPool;
	}

	protected void setConnPool(LinkedList<Connection> connPool)
	{
		this._connPool = connPool;
	}

	protected int getPoolSize()
	{
		return _poolSize;
	}

	protected void setPoolSize(int poolSize)
	{
		this._poolSize = poolSize;
	}

	protected String getConnectString()
	{
		return _connectString;
	}

	protected void setConnectString(String connectString)
	{
		this._connectString = connectString;
	}

	protected String getUser()
	{
		return _user;
	}

	protected void setUser(String _user)
	{
		this._user = _user;
	}

	protected String getPassword()
	{
		return _password;
	}

	protected void setPassword(String _password)
	{
		this._password = _password;
	}

	protected Connection acquire() throws SQLException
	{
		Throwable lastException = null;
		Connection connection = null;
		if (_logger.isDebugEnabled()) {
			_logger.debug("Acquiring DB connection with " + getConnPool().size() + " held in pool.");
			// hmmm: remove the below one, too noisy.
//			_logger.debug("dbconn acquire by:" + ProgramTools.showLastFewOnStack(20));
		}

		int attempts = (int) ((double) MAX_SNOOZE_AWAITING_POOL / (double) TIME_TAKEN_PER_SNOOZE + 1);
		for (int lcv = 0; lcv < attempts; lcv++) {
			try {
				synchronized (getConnPool()) {
					if (!getConnPool().isEmpty()) {
						connection = getConnPool().removeFirst();
					}
				}
				// hmmm: this always creates a connection! we need to pause instead if we can't get one, since this is supposed to be a pool.
				// if (connection == null) {
				// connection = createConnection();
				// }
				if (connection != null) {
					// reset any previous auto-commit value; if they want that enabled, they can call the other acquire.
					connection.setAutoCommit(false);

					((DatabaseConnectionInterceptor) Proxy.getInvocationHandler(connection)).setAcquired();
					if (_logger.isDebugEnabled())
						_logger.debug((lcv + 1) + " tries needed to get lock on db.");
					return connection;
				}

			} catch (Throwable sqe) {
				_logger.error(String.format("Unable to acquire/create connection to database on attempt %d.", lcv), sqe);
				lastException = sqe;
			}

			// we failed to get a connection, so snooze a bit and try again.
			try {
				Thread.sleep(TIME_TAKEN_PER_SNOOZE);
			} catch (Throwable cause) {
				// ignore any interruptions. may want to revise if we see us getting stuck on shutdown.
			}
		}

		_logger.error("Unable to acquire/create db connection in " + MAX_SNOOZE_AWAITING_POOL / 1000
			+ " seconds, or an unexpected error occurred.  Giving up.", lastException);
		if (lastException instanceof SQLException) {
			throw (SQLException) lastException;
		} else {
			throw new SQLException("different type of exception found", lastException);
		}
	}

	public Connection acquire(boolean useAutoCommit) throws SQLException
	{
		Connection ret = acquire();
		ret.setAutoCommit(useAutoCommit);
		return ret;
	}

	public void release(Connection conn)
	{
		if (conn == null) {
			return;
		}

		if (_logger.isDebugEnabled())
			_logger.debug("Releasing DB connection with " + getConnPool().size() + " held in pool.");

		synchronized (getConnPool()) {
			try {
				try {
					conn.rollback();
					if (getConnPool().size() < getPoolSize()) {
						getConnPool().addLast(conn);
						return;
					} else {
						_logger.error("tried to release more db connections than should be in pool!");
					}
				} catch (SQLException sqe) {
					_logger.error("Exception releasing connection.", sqe);
				}

				try {
					((DatabaseConnectionInterceptor) Proxy.getInvocationHandler(conn)).getConnection().close();
				} catch (Throwable t) {
					_logger.error("Error closing the connection.", t);
				}
			} finally {
				((DatabaseConnectionInterceptor) Proxy.getInvocationHandler(conn)).setReleased();
			}
		}
	}

	protected Connection createConnection() throws SQLException
	{
		if (_logger.isTraceEnabled())
			_logger.trace("Creating a new database connection.");

		Connection conn = DriverManager.getConnection(getConnectString(), getUser(), getPassword());
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		return (Connection) Proxy.newProxyInstance(GenesisClassLoader.classLoaderFactory(), new Class[] { Connection.class },
			new DatabaseConnectionInterceptor(conn));
	}
}
