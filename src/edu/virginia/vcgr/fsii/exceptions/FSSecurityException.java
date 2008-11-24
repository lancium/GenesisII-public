package edu.virginia.vcgr.fsii.exceptions;

public class FSSecurityException extends FSException
{
	static final long serialVersionUID = 0L;
	
	public FSSecurityException(String msg)
	{
		super(msg);
	}
	
	public FSSecurityException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}