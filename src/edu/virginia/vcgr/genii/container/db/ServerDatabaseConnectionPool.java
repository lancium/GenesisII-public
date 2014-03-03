package edu.virginia.vcgr.genii.container.db;

import java.sql.Connection;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.cleanup.CleanupManager;

public class ServerDatabaseConnectionPool extends DatabaseConnectionPool {
	static private Log _logger = LogFactory
			.getLog(ServerDatabaseConnectionPool.class);

	static private final String _SERVER_SPECIAL_STRING = "${server-dir}";
	static private int _poolInstances = 0;

	public ServerDatabaseConnectionPool(Properties connectionProperties)
			throws IllegalAccessException, ClassNotFoundException,
			InstantiationException {
		super(new DBPropertyNames(), connectionProperties,
				_SERVER_SPECIAL_STRING);
		synchronized (ServerDatabaseConnectionPool.class) {
			_poolInstances++;
			if (_poolInstances != 1) {
				_logger.error("We don't have exactly one connection pool instance -- we have "
						+ _poolInstances);
				System.exit(1);
			}
		}
		Connection connection = null;
		try {
			connection = acquire(false);
			CleanupManager.doCleanups(connection);
		} catch (Throwable cause) {
			_logger.error("Unable to create connection for cleanup handlers.",
					cause);
		} finally {
			release(connection);
		}
	}

	// private Connection acquire() throws SQLException
	// {
	// SQLException lastException = null;
	// Connection connection = null;
	// if (_logger.isTraceEnabled())
	// _logger.trace("Acquiring DB connection[" + get_connPool().size() + "]");
	// boolean succeeded = false;
	//
	// int maxSnooze = 4 * 1000; // in milliseconds.
	// int eachSleep = 500; // number of milliseconds to snooze between lock
	// attempts.
	// int attempts = (int) ((double) maxSnooze / (double) eachSleep + 1);
	//
	// for (int lcv = 0; lcv < attempts; lcv++) {
	// try {
	// get_lock().readLock().lock();
	// synchronized (get_connPool()) {
	// if (!get_connPool().isEmpty()) {
	// connection = get_connPool().removeFirst();
	// }
	// }
	//
	// if (connection == null)
	// connection = createConnection();
	//
	// ((DatabaseConnectionInterceptor)
	// Proxy.getInvocationHandler(connection)).setAcquired();
	//
	// succeeded = true;
	// return connection;
	// } catch (SQLException sqe) {
	// _logger.error(String.format("Unable to acquire/create connection to database on attempt %d.",
	// lcv), sqe);
	// lastException = sqe;
	// } finally {
	// if (!succeeded)
	// get_lock().readLock().unlock();
	// }
	//
	// try {
	// Thread.sleep(eachSleep);
	// } catch (Throwable cause) {
	// }
	// }
	//
	// _logger.error("Unable to acquire/create connections in " + maxSnooze /
	// 1000 +
	// " seconds.  Giving up.", lastException);
	// throw lastException;
	// }
	//
	// public Connection acquire(boolean useAutoCommit) throws SQLException
	// {
	// Connection ret = acquire();
	// ret.setAutoCommit(useAutoCommit);
	// return ret;
	// }
	//
	// public void release(Connection conn)
	// {
	// if (_logger.isTraceEnabled())
	// _logger.trace("Releasing a database connection [" + get_connPool().size()
	// + "].");
	//
	// if (conn == null)
	// return;
	//
	// synchronized (get_connPool()) {
	// try {
	// try {
	// conn.rollback();
	// if (get_connPool().size() < get_poolSize()) {
	// get_connPool().addLast(conn);
	// return;
	// }
	// } catch (SQLException sqe) {
	// _logger.error("Exception releasing connection.", sqe);
	// }
	//
	// try {
	// ((DatabaseConnectionInterceptor)
	// Proxy.getInvocationHandler(conn)).getConnection().close();
	// } catch (Throwable t) {
	// _logger.error("Error closing the connection.", t);
	// }
	// } finally {
	// get_lock().readLock().unlock();
	//
	// ((DatabaseConnectionInterceptor)
	// Proxy.getInvocationHandler(conn)).setReleased();
	// }
	// }
	// }
	//
	// protected Connection createConnection() throws SQLException
	// {
	// if (_logger.isTraceEnabled())
	// _logger.trace("Creating a new database connection.");
	//
	// Connection conn = DriverManager.getConnection(get_connectString(),
	// get_user(),
	// get_password());
	// conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
	// return (Connection)
	// Proxy.newProxyInstance(GenesisClassLoader.classLoaderFactory(), new
	// Class[] { Connection.class },
	// new DatabaseConnectionInterceptor(conn));
	// }
}
