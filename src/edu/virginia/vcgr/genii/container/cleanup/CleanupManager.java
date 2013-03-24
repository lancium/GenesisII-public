package edu.virginia.vcgr.genii.container.cleanup;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;

public class CleanupManager
{
	static private Log _logger = LogFactory.getLog(CleanupManager.class);

	static final private String CLEANUP_PROPERTIES_FILENAME = "cleanup.properties";
	static final private String PROPERTY_SUFFIX = "enact-cleanup";

	static private Properties loadProperties()
	{
		File cleanupProperties = Installation.getDeployment(new DeploymentName()).getConfigurationFile(
			CLEANUP_PROPERTIES_FILENAME);

		FileInputStream fin = null;
		try {
			fin = new FileInputStream(cleanupProperties);
			Properties ret = new Properties();
			ret.load(fin);
			return ret;
		} catch (Throwable cause) {
			_logger.warn("Unable to read the cleanup properties.  " + "We're not going to enact anything.", cause);
			return new Properties();
		} finally {
			StreamUtils.close(fin);
		}
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

		for (CleanupHandler handler : ServiceLoader.load(CleanupHandler.class)) {
			boolean succeeded = false;

			try {
				boolean doEnact = enactCleanup
					&& enactCleanup(properties.getProperty(
						String.format("%s.%s", handler.getClass().getName(), PROPERTY_SUFFIX), "true"));
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