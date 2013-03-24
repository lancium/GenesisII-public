package edu.virginia.vcgr.genii.container.bes.execution;

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