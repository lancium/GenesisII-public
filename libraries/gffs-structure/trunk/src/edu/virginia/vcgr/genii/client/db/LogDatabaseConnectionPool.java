package edu.virginia.vcgr.genii.client.db;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.logging.DLogUtils;

public class LogDatabaseConnectionPool extends NotTheServerDatabaseConnectionPool
{
	private static Log _logger = LogFactory.getLog(LogDatabaseConnectionPool.class);

	private static int _logPoolInstances = 0;

	public LogDatabaseConnectionPool(Properties connectionProperties) throws IllegalAccessException, ClassNotFoundException,
		InstantiationException
	{
		super(connectionProperties, null);
		synchronized (LogDatabaseConnectionPool.class) {
			_logPoolInstances++;
			if (_logPoolInstances != 1) {
				_logger.error("We don't have exactly one log connection pool instance -- we have " + _logPoolInstances);
				System.exit(1);
			}
			DLogUtils.initializeTables(connectionProperties);
		}
	}
}
