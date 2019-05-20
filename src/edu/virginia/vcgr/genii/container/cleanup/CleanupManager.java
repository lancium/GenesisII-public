package edu.virginia.vcgr.genii.container.cleanup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.ContainerProperties;

public class CleanupManager
{
	static private Log _logger = LogFactory.getLog(CleanupManager.class);

	static final private String PROPERTY_SUFFIX = "enact-cleanup";

	static private Properties loadProperties()
	{
		return ContainerProperties.getContainerProperties();
	}

	static private boolean enactCleanup(String value)
	{
		return (value != null) && (value.equals("true"));
	}

	static private boolean firstTimeStartup(Connection connection)
	{
		ResultSet rs = null;
		Statement stmt = null;

		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT resourceid FROM resources");

			if (!rs.next())
				return true;

			return false;
		} catch (Throwable cause) {
			return true;
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	static public void doCleanups(Connection connection)
	{
		boolean enactCleanup = false;
		Properties properties = loadProperties();

		String value = properties.getProperty(String.format("%s.%s", CleanupManager.class.getName(), PROPERTY_SUFFIX));
		enactCleanup = enactCleanup(value);

		if (firstTimeStartup(connection))
			return;
//System.err.println("About to run doCleanups\n");
		for (CleanupHandler handler : ServiceLoader.load(CleanupHandler.class)) {
			boolean succeeded = false;
//			System.err.println("About to run doCleanups for \n" + handler.getClass().getName());

			try {
				boolean doEnact = enactCleanup
					&& enactCleanup(properties.getProperty(String.format("%s.%s", handler.getClass().getName(), PROPERTY_SUFFIX), "true"));
				handler.doCleanup(connection, doEnact);

				if (doEnact)
					connection.commit();
				else
					connection.rollback();

				succeeded = true;
			} catch (Throwable cause) {
				_logger.error(String.format("Unable to run and commit cleanup handler %s.", handler), cause);
			} finally {
				try {
					if (!succeeded)
						connection.rollback();
				} catch (Throwable cause) {
					_logger.error("Unable to rollback connection.", cause);
				}
			}
		}
	}
}
