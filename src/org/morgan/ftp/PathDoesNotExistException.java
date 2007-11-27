package org.morgan.ftp;

public class PathDoesNotExistException extends FTPException
{
	static final long serialVersionUID = 0L;
	
	public PathDoesNotExistException(String path)
	{
		super(550, String.format("%1$s: No such file or directory.", path));
	}
}