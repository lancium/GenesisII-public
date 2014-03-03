package edu.virginia.vcgr.genii.client.appdesc;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class DeploymentException extends ResourceException
{
	static final long serialVersionUID = 0L;

	public DeploymentException(String msg)
	{
		super(msg);
	}

	public DeploymentException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}