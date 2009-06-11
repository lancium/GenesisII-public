package edu.virginia.vcgr.genii.client;

import java.io.File;

import org.morgan.util.io.GuaranteedDirectory;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;

public class ApplicationBase
{
	static public final String USER_DIR_ENVIRONMENT_VARIABLE =
		"GENII_USER_DIR";

	static private String getUserDirFromEnvironment()
	{
		String value = System.getenv(USER_DIR_ENVIRONMENT_VARIABLE);
		if (value == null || value.length() == 0)
			return null;
		
		return value;
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
		
		if (userDir == null) 
			userDir = String.format("%s/%s",
				System.getProperty("user.home"), 
				GenesisIIConstants.GENESISII_STATE_DIR_NAME);

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