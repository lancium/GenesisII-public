package edu.virginia.vcgr.fsii.exceptions;

public class FSBadFileHandleException extends FSException
{
	static final long serialVersionUID = 0L;
	
	public FSBadFileHandleException(String msg)
	{
		super(msg);
	}
	
	public FSBadFileHandleException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}