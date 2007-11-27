package org.morgan.ftp;

public class AuthorizationFailedException extends FTPException
{
	static final long serialVersionUID = 0L;
	
	public AuthorizationFailedException()
	{
		super(530, "Couldn't authenticate user.");
	}
}