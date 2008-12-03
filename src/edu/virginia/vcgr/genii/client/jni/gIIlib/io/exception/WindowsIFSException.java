package edu.virginia.vcgr.genii.client.jni.gIIlib.io.exception;

public class WindowsIFSException extends Exception
{	
	private static final long serialVersionUID = 1L;
	
	public WindowsIFSException(String msg)
	{
		super(msg);
	}
	
	public WindowsIFSException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
