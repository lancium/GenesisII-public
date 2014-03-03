package edu.virginia.vcgr.genii.client.spmd;

public class SPMDException extends Exception
{
	static final long serialVersionUID = 0L;

	public SPMDException(String msg)
	{
		super(msg);
	}

	public SPMDException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}