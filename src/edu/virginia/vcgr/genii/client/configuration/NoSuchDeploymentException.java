package edu.virginia.vcgr.genii.client.configuration;

import org.morgan.util.configuration.ConfigurationException;

public class NoSuchDeploymentException extends ConfigurationException
{
	static final long serialVersionUID = 0L;
	
	public NoSuchDeploymentException(String deploymentName)
	{
		super(String.format(
			"Deployment \"%s\" doesn't exist.", deploymentName));
	}
}