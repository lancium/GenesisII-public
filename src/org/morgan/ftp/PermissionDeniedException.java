package org.morgan.ftp;

public class PermissionDeniedException extends FTPException
{
	static final long serialVersionUID = 0L;
	
	public PermissionDeniedException()
	{
		super(530, "Permission denied.");
	}
}