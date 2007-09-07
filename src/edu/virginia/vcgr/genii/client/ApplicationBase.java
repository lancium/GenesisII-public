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
	
	static private void prepareApplication(boolean isClient)
	{
		String userDir = getUserDirFromEnvironment();
		if (userDir == null) 
			userDir = System.getProperty("user.home") + "/.genesisII";
		
		ConfigurationManager configurationManager = 
			ConfigurationManager.initializeConfiguration(userDir);

		setupUserDir(configurationManager.getUserDirectory());
		
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
	
	/**
	 * Prepares the static configuration manager
	 */
	static protected void prepareServerApplication()
	{
		prepareApplication(false);
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
		prepareApplication(true);
	}
}