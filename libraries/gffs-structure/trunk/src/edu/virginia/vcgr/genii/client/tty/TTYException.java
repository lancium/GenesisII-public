package edu.virginia.vcgr.genii.client.tty;

public class TTYException extends Exception
{
	static final long serialVersionUID = 0L;

	public TTYException(String msg)
	{
		super(msg);
	}

	public TTYException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}