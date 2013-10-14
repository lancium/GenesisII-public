package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.ContainerProperties;

public class Installation
{
	static private Log _logger = LogFactory.getLog(Installation.class);

	// System property to indicate the installation location
	static private final String INSTALL_DIR_SYSTEM_PROPERTY = "edu.virginia.vcgr.genii.install-base-dir";
	static private final String WEBAPPS_DIR_NAME = "webapps/axis";
	static private final String OGRSH_DIRECTORY_NAME = "OGRSH";
	static private final String PROCESS_WRAPPER_DIR_NAME = "ext/pwrapper/bin";

	static private File _installationDirectory;
	static private File _deploymentsDirectory;
	static private File _webAppDirectory;
	static private File _processWrapperDirectory;
	static private OGRSH _ogrsh = null;

	static {
		String installationDirectoryString = System.getProperty(INSTALL_DIR_SYSTEM_PROPERTY);
		if (installationDirectoryString == null)
			throw new RuntimeException("Installation directory property \"" + INSTALL_DIR_SYSTEM_PROPERTY + "\" not defined.");

		_installationDirectory = new File(installationDirectoryString).getAbsoluteFile();
		if (!_installationDirectory.exists())
			throw new RuntimeException("Installation directory \"" + _installationDirectory + "\" does not exist.");
		if (!_installationDirectory.isDirectory())
			throw new RuntimeException("Installation path \"" + _installationDirectory + "\" is not a directory.");
		
		_logger.debug("got installation directory of: " + _installationDirectory.toString());

		// special creation of container properties that's not static...
		ContainerProperties cp = new ContainerProperties();
		_deploymentsDirectory = new File(cp.getDeploymentsDirectory()); 
		_logger.debug("got deployments directory of: " + _deploymentsDirectory.toString());
		
		_webAppDirectory = new File(_installationDirectory, WEBAPPS_DIR_NAME);
		if (!_webAppDirectory.exists())
			throw new RuntimeException("Installation is corrupt -- couldn't find " + WEBAPPS_DIR_NAME + " directory.");
		if (!_webAppDirectory.isDirectory())
			throw new RuntimeException("Installation is corrupt -- " + WEBAPPS_DIR_NAME + " is not a directory.");

		_processWrapperDirectory = new File(_installationDirectory, PROCESS_WRAPPER_DIR_NAME);

		reload();
	}

	static public File getInstallDirectory()
	{
		return _installationDirectory;
	}

	static public Deployment getDeployment(DeploymentName depName)
	{
		return Deployment.getDeployment(_deploymentsDirectory, depName);
	}

	static public OGRSH getOGRSH()
	{
		return _ogrsh;
	}

	static public File axisWebApplicationPath()
	{
		return _webAppDirectory;
	}

	static public File getProcessWrapperBinPath()
	{
		return _processWrapperDirectory;
	}

	static public void reload()
	{
		Deployment.reload();

		_ogrsh = new OGRSH(new File(_installationDirectory, OGRSH_DIRECTORY_NAME));
	}

	static public File getGridCommand()
	{
		File ret;
		boolean isWindows = OperatingSystemType.getCurrent().isWindows();

		if (isWindows)
			ret = new File(_installationDirectory, "grid.bat");
		else
			ret = new File(_installationDirectory, "grid");

		if (!ret.exists())
			throw new ConfigurationException("Unable to locate grid command.");
		if (!ret.canExecute())
			throw new ConfigurationException(String.format("Grid command \"%s\" is not executable.", ret.getAbsolutePath()));

		return ret;
	}
}