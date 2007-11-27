package org.morgan.ftp;

public class InternalParseException extends FTPException
{
	static final long serialVersionUID = 0L;
	
	public InternalParseException(String msg)
	{
		super(500, msg);
	}
}