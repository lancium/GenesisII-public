package org.morgan.ftp;

public class InternalException extends FTPException
{
	static final long serialVersionUID = 0L;
	
	public InternalException(String msg)
	{
		super(451, msg);
	}
	
	public InternalException(String msg, Throwable t)
	{
		super(451, msg + ": " + t.getLocalizedMessage());
	}
}
