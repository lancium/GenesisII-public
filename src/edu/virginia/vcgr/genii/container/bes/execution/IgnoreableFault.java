package edu.virginia.vcgr.genii.container.bes.execution;

public class IgnoreableFault extends Exception
{
	static final long serialVersionUID = 0L;

	public IgnoreableFault(String msg)
	{
		super(msg);
	}

	public IgnoreableFault(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}