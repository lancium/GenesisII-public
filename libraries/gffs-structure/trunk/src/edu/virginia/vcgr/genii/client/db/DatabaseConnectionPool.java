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
import edu.virginia.vcgr.genii.client.locking.GReadWriteLock;
import edu.virginia.vcgr.genii.client.locking.UnfairReadWriteLock;
import edu.virginia.vcgr.genii.system.classloader.GenesisClassLoader;

public class DatabaseConnectionPool {
	static private Log _logger = LogFactory
			.getLog(DatabaseConnectionPool.class);

	static protected final String _DB_POOL_SIZE_DEFAULT = "16";

	private GReadWriteLock _lock = new UnfairReadWriteLock();
	private LinkedList<Connection> _connPool;
	private int _poolSize;
	private String _connectString;
	private String _user;
	private String _password;

	protected DatabaseConnectionPool(DBPropertyNames names,
			Properties connectionProperties, String specialString) {
		set_connPool(new LinkedList<Connection>());
		set_poolSize(Integer.parseInt(connectionProperties.getProperty(
				names._DB_POOL_SIZE_PROPERTY, _DB_POOL_SIZE_DEFAULT)));
		set_user(connectionProperties.getProperty(names._DB_USER_PROPERTY));
		set_password(connectionProperties
				.getProperty(names._DB_PASSWORD_PROPERTY));

		// this is dangerous since it prints db account name and password.
		if (_logger.isTraceEnabled())
			_logger.trace("DB Conn Pool for: " + connectionProperties);

		set_connectString(replaceMacros(
				connectionProperties
						.getProperty(names._DB_CONNECT_STRING_PROPERTY),
				specialString));
		if (get_connectString() == null)
			throw new IllegalArgumentException(
					"Connect string cannot be null for database connection.");
	}

	public static class DBPropertyNames {
		public String _DB_CONNECT_STRING_PROPERTY = "edu.virginia.vcgr.genii.container.db.db-connect-string";
		public String _DB_USER_PROPERTY = "edu.virginia.vcgr.genii.container.db.db-user";
		public String _DB_PASSWORD_PROPERTY = "edu.virginia.vcgr.genii.container.db.db-password";
		public String _DB_POOL_SIZE_PROPERTY = "edu.virginia.vcgr.genii.container.db.pool-size";

		// uses the default database property names for containers.
		public DBPropertyNames() {
		}

		// swaps the property names over to client side usage.
		public void useClientNames() {
			_DB_CONNECT_STRING_PROPERTY = "edu.virginia.vcgr.genii.client.db.db-connect-string";
			_DB_USER_PROPERTY = "edu.virginia.vcgr.genii.client.db.db-user";
			_DB_PASSWORD_PROPERTY = "edu.virginia.vcgr.genii.client.db.db-password";
			_DB_POOL_SIZE_PROPERTY = "edu.virginia.vcgr.genii.client.db.pool-size";
		}
	}

	static private String replaceMacros(String str, String specialString) {
		if (str == null)
			return null;
		if (specialString == null)
			return str;

		ConfigurationManager man = ConfigurationManager
				.getCurrentConfiguration();

		if (man == null)
			return str;

		File dir = man.getUserDirectory();

		int start = str.indexOf(specialString);
		if (start < 0)
			return str;

		return str.substring(0, start) + dir.getAbsolutePath()
				+ str.substring(start + specialString.length());
	}

	protected LinkedList<Connection> get_connPool() {
		return _connPool;
	}

	protected void set_connPool(LinkedList<Connection> _connPool) {
		this._connPool = _connPool;
	}

	protected int get_poolSize() {
		return _poolSize;
	}

	protected void set_poolSize(int _poolSize) {
		this._poolSize = _poolSize;
	}

	protected String get_connectString() {
		return _connectString;
	}

	protected void set_connectString(String _connectString) {
		this._connectString = _connectString;
	}

	protected String get_user() {
		return _user;
	}

	protected void set_user(String _user) {
		this._user = _user;
	}

	protected String get_password() {
		return _password;
	}

	protected void set_password(String _password) {
		this._password = _password;
	}

	protected GReadWriteLock get_lock() {
		return _lock;
	}

	protected void set_lock(GReadWriteLock _lock) {
		this._lock = _lock;
	}

	protected Connection acquire() throws SQLException {
		SQLException lastException = null;
		Connection connection = null;
		if (_logger.isTraceEnabled())
			_logger.trace("Acquiring DB connection[" + get_connPool().size()
					+ "]");
		boolean succeeded = false;

		int maxSnooze = 4 * 1000; // in milliseconds.
		int eachSleep = 500; // number of milliseconds to snooze between lock
								// attempts.
		int attempts = (int) ((double) maxSnooze / (double) eachSleep + 1);

		for (int lcv = 0; lcv < attempts; lcv++) {
			try {
				get_lock().readLock().lock();
				synchronized (get_connPool()) {
					if (!get_connPool().isEmpty()) {
						connection = get_connPool().removeFirst();
					}
				}

				if (connection == null)
					connection = createConnection();

				((DatabaseConnectionInterceptor) Proxy
						.getInvocationHandler(connection)).setAcquired();

				succeeded = true;
				return connection;
			} catch (SQLException sqe) {
				_logger.error(
						String.format(
								"Unable to acquire/create connection to database on attempt %d.",
								lcv), sqe);
				lastException = sqe;
			} finally {
				if (!succeeded)
					get_lock().readLock().unlock();
			}

			try {
				Thread.sleep(eachSleep);
			} catch (Throwable cause) {
			}
		}

		_logger.error("Unable to acquire/create connections in " + maxSnooze
				/ 1000 + " seconds.  Giving up.", lastException);
		throw lastException;
	}

	public Connection acquire(boolean useAutoCommit) throws SQLException {
		Connection ret = acquire();
		ret.setAutoCommit(useAutoCommit);
		return ret;
	}

	public void release(Connection conn) {
		if (_logger.isTraceEnabled())
			_logger.trace("Releasing a database connection ["
					+ get_connPool().size() + "].");

		if (conn == null)
			return;

		synchronized (get_connPool()) {
			try {
				try {
					conn.rollback();
					if (get_connPool().size() < get_poolSize()) {
						get_connPool().addLast(conn);
						return;
					}
				} catch (SQLException sqe) {
					_logger.error("Exception releasing connection.", sqe);
				}

				try {
					((DatabaseConnectionInterceptor) Proxy
							.getInvocationHandler(conn)).getConnection()
							.close();
				} catch (Throwable t) {
					_logger.error("Error closing the connection.", t);
				}
			} finally {
				get_lock().readLock().unlock();

				((DatabaseConnectionInterceptor) Proxy
						.getInvocationHandler(conn)).setReleased();
			}
		}
	}

	protected Connection createConnection() throws SQLException {
		if (_logger.isTraceEnabled())
			_logger.trace("Creating a new database connection.");

		Connection conn = DriverManager.getConnection(get_connectString(),
				get_user(), get_password());
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		return (Connection) Proxy.newProxyInstance(
				GenesisClassLoader.classLoaderFactory(),
				new Class[] { Connection.class },
				new DatabaseConnectionInterceptor(conn));
	}
}
