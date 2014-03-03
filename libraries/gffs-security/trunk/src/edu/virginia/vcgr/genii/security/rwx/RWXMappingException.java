package edu.virginia.vcgr.genii.security.rwx;

public class RWXMappingException extends RuntimeException
{
	static final long serialVersionUID = 0L;

	public RWXMappingException(String msg)
	{
		super(msg);
	}

	public RWXMappingException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
