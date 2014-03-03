package edu.virginia.vcgr.fsii.exceptions;

public class FSIllegalAccessException extends FSException
{
	static final long serialVersionUID = 0L;

	public FSIllegalAccessException(String msg)
	{
		super(msg);
	}

	public FSIllegalAccessException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
