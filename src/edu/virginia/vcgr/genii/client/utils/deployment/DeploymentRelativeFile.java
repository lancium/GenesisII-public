package edu.virginia.vcgr.genii.client.utils.deployment;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.configuration.UserConfig;
import edu.virginia.vcgr.genii.client.configuration.UserConfigUtils;
import edu.virginia.vcgr.genii.container.certGenerator.CertGeneratorServiceImpl;

public class DeploymentRelativeFile extends File
{
	static private Log _logger = LogFactory.getLog(CertGeneratorServiceImpl.class);
	static final long serialVersionUID = 0L;
	
	static private final String _DEPLOYMENT_NAME_PROPERTY = GenesisIIConstants.DEPLOYMENT_NAME_PROPERTY;
	static private final String _INSTALL_DIR_PROPERTY = GenesisIIConstants.INSTALL_DIR_SYSTEM_PROPERTY;
	static private final String _DEFAULT_DEPLOYMENT_NAME = "default";
	
	static private File _deploymentDirectory;

	static
	{
		evaluateDeploymentDirectory();
	}
	
	static public void evaluateDeploymentDirectory()
	{
		_deploymentDirectory = getDeploymentDirectory();
	}
	
	static public File getDeploymentDirectory()
	{
		File deploymentDirectory = null;
		String installDir = System.getProperty(_INSTALL_DIR_PROPERTY);
		if (installDir == null)
			throw new RuntimeException("Install directory property (" + _INSTALL_DIR_PROPERTY + ") is undefined.");
		
		String deploymentName = System.getProperty(_DEPLOYMENT_NAME_PROPERTY);
		
		// jfk3w - added user config information - including user's deployment path
		if (deploymentName != null)
		{
			deploymentDirectory = new File(installDir, "deployments/" + deploymentName);
			
		}
		else
		{
			// deployment name not set by Container or environment variable.  Try to use user's configuration info
			try 
			{
				UserConfig userConfig = UserConfigUtils.getCurrentUserConfig();
				if (userConfig != null)
				{
					deploymentDirectory = new File(userConfig.getDeploymentPath());
				}
			}
			catch (Throwable t)
			{
				deploymentDirectory = null;
			}
		}
		
		// check if it exists
		if (deploymentDirectory != null && !deploymentDirectory.exists())
		{
			_logger.error("Deployment directory \"" + deploymentDirectory.getAbsolutePath() + "\" does not exist.  Will try default config.");
			deploymentDirectory = null;
		}

		// if all else fails, try "default"
		if (deploymentDirectory == null)
		{
			deploymentDirectory = new File(installDir, "deployments/" + _DEFAULT_DEPLOYMENT_NAME);
		}

		return deploymentDirectory;
	}
	
	public DeploymentRelativeFile(String path)
	{
		super(_deploymentDirectory, path);
	}
}