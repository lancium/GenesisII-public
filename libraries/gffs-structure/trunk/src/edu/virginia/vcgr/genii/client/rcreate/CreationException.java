package edu.virginia.vcgr.genii.client.rcreate;

public class CreationException extends Exception
{
	static final long serialVersionUID = 0L;

	public CreationException(String message)
	{
		super(message);
	}

	public CreationException(String message, Throwable cause)
	{
		super(message, cause);
	}
}