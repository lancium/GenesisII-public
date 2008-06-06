package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;

public class Installation
{
	/// System property to indicate the installtion location 
	static private final String INSTALL_DIR_SYSTEM_PROPERTY = 
		"edu.virginia.vcgr.genii.install-base-dir";
	static private final String DEPLOYMENTS_DIRECTORY_NAME =
		"deployments";
	static private final String WEBAPPS_DIR_NAME = "webapps/axis";
	static private final String OGRSH_DIRECTORY_NAME = "OGRSH";
	
	static private File _installationDirectory;
	static private File _deploymentsDirectory;
	static private File _webAppDirectory;
	static private OGRSH _ogrsh = null;
	
	static
	{
		String installationDirectoryString =
			System.getProperty(INSTALL_DIR_SYSTEM_PROPERTY);
		if (installationDirectoryString == null)
			throw new RuntimeException("Installation directory property \"" +
				INSTALL_DIR_SYSTEM_PROPERTY + "\" not defined.");
		
		_installationDirectory = new File(
			installationDirectoryString).getAbsoluteFile();
		if (!_installationDirectory.exists())
			throw new RuntimeException("Installation directory \"" +
				_installationDirectory + "\" does not exist.");
		if (!_installationDirectory.isDirectory())
			throw new RuntimeException("Installation path \"" +
				_installationDirectory + "\" is not a directory.");
		
		_deploymentsDirectory = new File(_installationDirectory, 
			DEPLOYMENTS_DIRECTORY_NAME);
		
		_webAppDirectory = new File(_installationDirectory, WEBAPPS_DIR_NAME);
		if (!_webAppDirectory.exists())
			throw new RuntimeException(
				"Installation is corrupt -- couldn't find " + 
				WEBAPPS_DIR_NAME + " directory.");
		if (!_webAppDirectory.isDirectory())
			throw new RuntimeException(
				"Installation is corrupt -- " + 
				WEBAPPS_DIR_NAME + " is not a directory.");
		
		reload();
	}
	
	static public Deployment getDeployment()
	{
		return getDeployment(null);
	}
	
	static public Deployment getDeployment(String deploymentName)
	{
		return Deployment.getDeployment(_deploymentsDirectory, deploymentName);
	}
	
	static public OGRSH getOGRSH()
	{
		return _ogrsh;
	}
	
	static public File axisWebApplicationPath()
	{
		return _webAppDirectory;
	}
	
	static public void reload()
	{
		Deployment.reload();
		
		_ogrsh = new OGRSH(
			new File(_installationDirectory, OGRSH_DIRECTORY_NAME));
	}
}