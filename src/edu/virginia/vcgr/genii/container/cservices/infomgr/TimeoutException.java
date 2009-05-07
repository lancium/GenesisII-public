package edu.virginia.vcgr.genii.container.cservices.infomgr;

/**
 * A special exception that can be stored with an information result
 * indicating that the last call to update the information timed out
 * before information was made available.
 * 
 * @author mmm2a
 */
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