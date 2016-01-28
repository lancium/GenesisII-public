package edu.virginia.vcgr.genii.container.db;

import java.sql.Connection;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.cleanup.CleanupManager;

public class ServerDatabaseConnectionPool extends DatabaseConnectionPool
{
	static private Log _logger = LogFactory.getLog(ServerDatabaseConnectionPool.class);

	static private final String _SERVER_SPECIAL_STRING = "${server-dir}";
	static private int _poolInstances = 0;

	public ServerDatabaseConnectionPool(Properties connectionProperties)
		throws IllegalAccessException, ClassNotFoundException, InstantiationException
	{
		super(new DBPropertyNames(), connectionProperties, _SERVER_SPECIAL_STRING);
		synchronized (ServerDatabaseConnectionPool.class) {
			_poolInstances++;
			if (_poolInstances != 1) {
				_logger.error("We don't have exactly one connection pool instance -- we have " + _poolInstances);
				System.exit(1);
			}
		}
		Connection connection = null;
		try {
			connection = acquire(false);
			CleanupManager.doCleanups(connection);
		} catch (Throwable cause) {
			_logger.error("Unable to create connection for cleanup handlers.", cause);
		} finally {
			release(connection);
		}
	}
}
