package edu.virginia.vcgr.genii.client;

import java.io.File;

import org.morgan.util.io.GuaranteedDirectory;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;

public class ApplicationBase
{
	// the name of the environment variable whose value points at our state directory.
	static public final String USER_DIR_ENVIRONMENT_VARIABLE = "GENII_USER_DIR";
	// the value that can be used in property files (in select places) and is translated into the
	// value of the user state directory variable (see above).
	static public final String USER_DIR_PROPERTY_VALUE = "env-GENII_USER_DIR";

	// environment variable name that contains OSGI storage; might not be set.
	static public final String OSGI_DIR_ENVIRONMENT_VARIABLE = "GENII_OSGI_DIR";

	// loads the value for the genesis user state directory from the environment.
	static public String getUserDirFromEnvironment()
	{
		String value = System.getenv(USER_DIR_ENVIRONMENT_VARIABLE);
		// make this decision across the board, so we don't get caught short without
		// a default user state directory.
		if (value == null || value.length() == 0) {
			value = String.format("%s/%s", System.getProperty("user.home"), GenesisIIConstants.GENESISII_STATE_DIR_NAME);
		}
		return value;
	}

	// loads the value for the OSGI storage area from the environment or returns null if undefined.
	static public String getOSGIDirFromEnvironment()
	{
		return System.getenv(OSGI_DIR_ENVIRONMENT_VARIABLE);
	}
	
	static private void setupUserDir(File userdir)
	{
		if (!userdir.exists())
		{
			if (!userdir.mkdirs())
				throw new RuntimeException("Unable to create directory \"" 
					+ userdir.getAbsolutePath() + "\".");
		}
		
		if (!userdir.isDirectory())
			throw new RuntimeException("Path \"" + userdir.getAbsolutePath()
				+ "\" is not a directory.");
	}
	
	/**
	 * Prepares the static configuration manager
	 */
	static protected void prepareServerApplication()
	{
		ContainerProperties cProperties = 
			ContainerProperties.containerProperties;
		String depName = cProperties.getDeploymentName();
		if (depName != null)
			System.setProperty(
				DeploymentName.DEPLOYMENT_NAME_PROPERTY, depName);
		
		String userDir = getUserDir(cProperties);
		ConfigurationManager configurationManager = 
			ConfigurationManager.initializeConfiguration(userDir);

		setupUserDir(configurationManager.getUserDirectory());
		
		configurationManager.setRoleServer();
	}
	
	/**
	 * Prepares the static configuration manager (using the config files in the 
	 * explicitConfigDir).  If explicitConfigDir is null, the GENII_CONFIG_DIR is
	 * inspected.  If that is not present (or empty), the default configuration location
	 * located in a well-known spot from the installation directory (as per the
	 * installation system property) is used;
	 * 
	 * @param explicitConfigDir
	 */
	static protected void prepareClientApplication()
	{
		String userDir = getUserDir(null);
		ConfigurationManager configurationManager = 
			ConfigurationManager.initializeConfiguration(userDir);

		setupUserDir(configurationManager.getUserDirectory());
		
		configurationManager.setRoleClient();
	}
	
	static public String getUserDir(ContainerProperties cProperties)
	{
		String userDir = null;
		
		if (cProperties != null)
			userDir = cProperties.getUserDirectory();
		
		if (userDir == null)
			userDir = getUserDirFromEnvironment();
		
		try
		{
			File userDirFile = new GuaranteedDirectory(userDir);
			
			return userDirFile.getCanonicalPath();
		}
		catch (Throwable cause)
		{
			throw new RuntimeException(
				"Unable to access or create state directory.", cause);
		}
	}
}
