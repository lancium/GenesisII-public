package edu.virginia.vcgr.genii.client.configuration;

import org.morgan.util.configuration.ConfigurationException;

public class InvalidDeploymentException extends ConfigurationException
{
	static final long serialVersionUID = 0L;

	public InvalidDeploymentException(String deployment, String reason)
	{
		super(String.format("Deployment \"%s\" is invalid:  %s", deployment, reason));
	}
}
