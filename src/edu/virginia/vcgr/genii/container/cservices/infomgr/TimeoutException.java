package edu.virginia.vcgr.genii.container.cservices.infomgr;

public class TimeoutException extends Exception
{
	static final long serialVersionUID = 0L;
	
	public TimeoutException()
	{
		this("Operation timed out.");
	}
	
	public TimeoutException(String msg)
	{
		super(msg);
	}
	
	public TimeoutException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}