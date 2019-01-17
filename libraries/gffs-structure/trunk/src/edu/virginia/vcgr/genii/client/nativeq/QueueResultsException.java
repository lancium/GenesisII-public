package edu.virginia.vcgr.genii.client.nativeq;

public class QueueResultsException extends Exception
{
	static final long serialVersionUID = 0L;

	public QueueResultsException(String msg)
	{
		super(msg);
	}

	public QueueResultsException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
