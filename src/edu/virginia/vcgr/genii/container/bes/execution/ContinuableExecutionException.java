package edu.virginia.vcgr.genii.container.bes.execution;

public class ContinuableExecutionException extends ExecutionException
{
	static final long serialVersionUID = 0L;

	public ContinuableExecutionException(String msg)
	{
		super(msg);
	}

	public ContinuableExecutionException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}