package edu.virginia.vcgr.genii.client.nativeq;

public class NativeQueueException extends Exception
{
	static final long serialVersionUID = 0L;

	public NativeQueueException(String msg)
	{
		super(msg);
	}

	public NativeQueueException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}