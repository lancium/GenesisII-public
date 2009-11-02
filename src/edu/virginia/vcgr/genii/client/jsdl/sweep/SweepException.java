package edu.virginia.vcgr.genii.client.jsdl.sweep;

public class SweepException extends Exception
{
	static final long serialVersionUID = 0L;
	
	public SweepException(String msg)
	{
		super(msg);
	}
	
	public SweepException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}