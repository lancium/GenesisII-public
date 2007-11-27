package org.morgan.ftp;

public class UnauthenticatedException extends FTPException
{
	static final long serialVersionUID = 0L;
	
	public UnauthenticatedException()
	{
		super(451, "Connection has not yet been authenticated.");
	}
}