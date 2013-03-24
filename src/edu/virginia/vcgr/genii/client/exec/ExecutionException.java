package edu.virginia.vcgr.genii.client.exec;

public class ExecutionException extends Exception
{
	static final long serialVersionUID = 0L;

	public ExecutionException(String msg)
	{
		super(msg);
	}

	public ExecutionException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}