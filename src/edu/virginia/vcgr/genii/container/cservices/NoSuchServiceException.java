package edu.virginia.vcgr.genii.container.cservices;

import org.morgan.util.configuration.ConfigurationException;

public class NoSuchServiceException extends ConfigurationException
{
	static final long serialVersionUID = 0L;

	public NoSuchServiceException(String serviceName)
	{
		super(String.format("The container service \"%s\" does not exist.", serviceName));
	}
}
