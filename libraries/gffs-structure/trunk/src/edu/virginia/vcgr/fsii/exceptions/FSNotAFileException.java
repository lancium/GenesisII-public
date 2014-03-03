package edu.virginia.vcgr.fsii.exceptions;

public class FSNotAFileException extends FSException
{
	static final long serialVersionUID = 0L;

	public FSNotAFileException(String msg)
	{
		super(msg);
	}

	public FSNotAFileException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}