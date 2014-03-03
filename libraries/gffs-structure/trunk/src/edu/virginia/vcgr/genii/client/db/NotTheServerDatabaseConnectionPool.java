package edu.virginia.vcgr.genii.client.db;

import java.util.Properties;

public abstract class NotTheServerDatabaseConnectionPool extends
		DatabaseConnectionPool {
	public NotTheServerDatabaseConnectionPool(Properties connectionProperties,
			String specialString) throws IllegalAccessException,
			ClassNotFoundException, InstantiationException {
		super(new DatabaseConnectionPool.DBPropertyNames(),
				connectionProperties, specialString);
	}
	//
	// //hmmm: more of this could be pushed down into basis class.
	// protected Connection acquire() throws SQLException
	// {
	// SQLException lastException = null;
	// Connection connection = null;
	// if (_logger.isDebugEnabled())
	// _logger.debug("Acquiring DB connection[" + get_connPool().size() + "]");
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
	// if (conn == null)
	// return;
	//
	// synchronized (get_connPool()) {
	// if (_logger.isDebugEnabled())
	// _logger.debug("Releasing a database connection [" + get_connPool().size()
	// + "].");
	//
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
	// if (_logger.isDebugEnabled())
	// _logger.debug("Creating a new database connection.");
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
