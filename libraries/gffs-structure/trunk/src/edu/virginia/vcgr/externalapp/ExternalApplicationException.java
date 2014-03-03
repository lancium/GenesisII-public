package edu.virginia.vcgr.externalapp;

public class ExternalApplicationException extends Exception
{
	static final long serialVersionUID = 0L;

	public ExternalApplicationException(String msg)
	{
		super(msg);
	}

	public ExternalApplicationException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}