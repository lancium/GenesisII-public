package edu.virginia.vcgr.genii.client;

import java.io.File;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;

public class ApplicationBase
{
	static public final String USER_DIR_ENVIRONMENT_VARIABLE =
		"GENII_USER_DIR";

	static public final String CONFIG_DIR_ENVIRONMENT_VARIABLE =
		"GENII_CONFIG_DIR";
	
	static private String getUserDirFromEnvironment()
	{
		String value = System.getenv(USER_DIR_ENVIRONMENT_VARIABLE);
		if (value == null || value.length() == 0)
			return null;
		
		return value;
	}
	
	static private String getConfigDirFromEnvironment()
	{
		String value = System.getenv(CONFIG_DIR_ENVIRONMENT_VARIABLE);
		if (value == null || value.length() == 0)
			return null;
		
		return value;
	}

	static private void prepareApplication(boolean isClient, String explicitConfigDir)
	{
		if (explicitConfigDir == null) {
			explicitConfigDir = getConfigDirFromEnvironment();
		}
		
		String userDir = getUserDirFromEnvironment();
		if (userDir == null) 
		{
			userDir = System.getProperty("user.home") + "/.genesisII";
		}
		
		ConfigurationManager configurationManager = 
			ConfigurationManager.initializeConfiguration(explicitConfigDir, userDir);

		setupUserDir(configurationManager.getUserDirectory());
		setupConfigDir(configurationManager.getConfigDirectory());
		
		if (isClient)
			configurationManager.setRoleClient();
		else
			configurationManager.setRoleServer();
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
	
	static private void setupConfigDir(File basedir)
	{
		if (!basedir.exists())
			throw new RuntimeException("Path \"" + basedir.getAbsolutePath()
				+ "\" does not exist.");
		
		if (!basedir.isDirectory())
			throw new RuntimeException("Path \"" + basedir.getAbsolutePath()
				+ "\" is not a directory.");
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
	static protected void prepareServerApplication(String explicitConfigDir)
	{
		prepareApplication(false, explicitConfigDir);
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
	static protected void prepareClientApplication(String explicitConfigDir)
	{
		prepareApplication(true, explicitConfigDir);
	}
}