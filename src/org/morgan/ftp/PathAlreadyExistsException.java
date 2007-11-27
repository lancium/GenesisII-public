package org.morgan.ftp;

public class PathAlreadyExistsException extends FTPException
{
	static final long serialVersionUID = 0L;
	
	public PathAlreadyExistsException(String path)
	{
		super(550, String.format("%1$s: Path already exists.", path));
	}
}