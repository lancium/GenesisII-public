package edu.virginia.vcgr.genii.client.configuration;

import edu.virginia.vcgr.genii.client.ContainerProperties;

public class DeploymentName
{
	static public final String DEPLOYMENT_NAME_PROPERTY =
		"edu.virginia.vcgr.genii.client.deployment-name";
	static public final String DEPLOYMENT_NAME_ENVIRONMENT_VARIABLE =
		"GENII_DEPLOYMENT_NAME";
	static private final String DEFAULT_DEPLOYMENT_NAME = "default";
	
	private String _deploymentName;
	
	public DeploymentName(String deploymentName)
	{
		if (deploymentName == null)
		{
			_deploymentName = figureOutDefaultDeploymentName();
		} else
		{
			if (deploymentName.contains("/") || deploymentName.contains("\\"))
				throw new IllegalArgumentException(
					"Deployment name cannot contain path slashes.");
			
			_deploymentName = deploymentName;
		}
	}
	
	public DeploymentName()
	{
		this(null);
	}
	
	public String toString()
	{
		return _deploymentName;
	}
	
	static private String figureOutDefaultDeploymentName()
	{
		String deploymentName = System.getProperty(DEPLOYMENT_NAME_PROPERTY);
		
		// jfk3w - added user config information - including user's deployment path
		if (deploymentName == null)
			deploymentName = System.getenv(DEPLOYMENT_NAME_ENVIRONMENT_VARIABLE);

		if (deploymentName != null)
			return deploymentName;
		
		// try reading user's config file from USER_DIR
		try
		{	
			UserConfig userConfig = UserConfigUtils.getCurrentUserConfig();
			if (userConfig != null && userConfig.getDeploymentName() != null)
			{
				return userConfig.getDeploymentName().toString();
			}
		}
		catch(Throwable t)
		{
		}
			
		deploymentName = 
			ContainerProperties.containerProperties.getDeploymentName();
		
		// if all else fails, try "default"
		if (deploymentName == null)
			deploymentName = DEFAULT_DEPLOYMENT_NAME;
		
		return deploymentName;
	}
}
