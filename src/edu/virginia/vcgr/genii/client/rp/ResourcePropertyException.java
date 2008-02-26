package edu.virginia.vcgr.genii.client.rp;

public class ResourcePropertyException extends Exception
{
	static final long serialVersionUID = 0L;
	
	public ResourcePropertyException(String msg)
	{
		super(msg);
	}
	
	public ResourcePropertyException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
