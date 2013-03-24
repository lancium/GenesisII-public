package edu.virginia.vcgr.fsii.exceptions;

public class FSException extends Exception
{
	static final long serialVersionUID = 0L;

	public FSException(String msg)
	{
		super(msg);
	}

	public FSException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}