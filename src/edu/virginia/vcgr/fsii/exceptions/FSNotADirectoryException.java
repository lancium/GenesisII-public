package edu.virginia.vcgr.fsii.exceptions;

public class FSNotADirectoryException extends FSException
{
	static final long serialVersionUID = 0L;
	
	public FSNotADirectoryException(String msg)
	{
		super(msg);
	}
	
	public FSNotADirectoryException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}