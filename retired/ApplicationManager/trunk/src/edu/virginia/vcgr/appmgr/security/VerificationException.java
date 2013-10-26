package edu.virginia.vcgr.appmgr.security;

public class VerificationException extends RuntimeException
{
	static final long serialVersionUID = 0L;

	public VerificationException(String msg)
	{
		super(msg);
	}

	public VerificationException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}