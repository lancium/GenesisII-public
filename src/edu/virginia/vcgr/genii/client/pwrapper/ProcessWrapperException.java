package edu.virginia.vcgr.genii.client.pwrapper;

public class ProcessWrapperException extends Exception
{
	static final long serialVersionUID = 0L;
	
	public ProcessWrapperException(String msg)
	{
		super(msg);
	}
	
	public ProcessWrapperException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}