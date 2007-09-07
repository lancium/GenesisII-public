package edu.virginia.vcgr.genii.client.utils.deployment;

import java.io.File;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public class DeploymentRelativeFile extends File
{
	static final long serialVersionUID = 0L;
	
	static private final String _DEPLOYMENT_NAME_PROPERTY = GenesisIIConstants.DEPLOYMENT_NAME_PROPERTY;
	static private final String _INSTALL_DIR_PROPERTY = GenesisIIConstants.INSTALL_DIR_SYSTEM_PROPERTY;
	
	static private File _deploymentDirectory;
	
	static
	{
		String installDir = System.getProperty(_INSTALL_DIR_PROPERTY);
		if (installDir == null)
			throw new RuntimeException("Install directory property (" + _INSTALL_DIR_PROPERTY + ") is undefined.");
		
		String deploymentName = System.getProperty(_DEPLOYMENT_NAME_PROPERTY, "default");
		
		_deploymentDirectory = new File(installDir, "deployments/" + deploymentName);
		if (!_deploymentDirectory.exists())
			throw new RuntimeException("Deployment directory \"" + 
				_deploymentDirectory.getAbsolutePath() + "\" does not exist.");
	}
	
	public DeploymentRelativeFile(String path)
	{
		super(_deploymentDirectory, path);
	}
}