package org.morgan.ftp;

public class ConnectionCloseException extends FTPException
{
	static final long serialVersionUID = 0L;
	
	public ConnectionCloseException()
	{
		super(400, "The server is about to close the connection.");
	}
}