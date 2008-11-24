package edu.virginia.vcgr.fsii.exceptions;

public class FSFileHandleBadStateException extends FSException
{
	static final long serialVersionUID = 0L;
	
	public FSFileHandleBadStateException(String msg)
	{
		super(msg);
	}
	
	public FSFileHandleBadStateException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}