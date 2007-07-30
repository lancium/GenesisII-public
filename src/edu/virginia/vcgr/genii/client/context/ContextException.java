package edu.virginia.vcgr.genii.client.context;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class ContextException extends ResourceException
{
	static final long serialVersionUID = 0L;
	
	public ContextException(String msg)
	{
		super(msg);
	}
	
	public ContextException(Throwable t)
	{
		super(t.getLocalizedMessage(), t);
	}
	
	public ContextException(String msg, Throwable t)
	{
		super(msg, t);
	}
}