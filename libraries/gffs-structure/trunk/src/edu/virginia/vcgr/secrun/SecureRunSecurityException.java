package edu.virginia.vcgr.secrun;

public class SecureRunSecurityException extends RuntimeException
{
	static final long serialVersionUID = 0L;

	public SecureRunSecurityException(String msg)
	{
		super(msg);
	}

	public SecureRunSecurityException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}