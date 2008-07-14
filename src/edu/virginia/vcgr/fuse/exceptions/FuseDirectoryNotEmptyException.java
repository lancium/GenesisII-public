package edu.virginia.vcgr.fuse.exceptions;

import fuse.FuseException;

public class FuseDirectoryNotEmptyException extends FuseException
{
	static final long serialVersionUID = 0L;
	
	public FuseDirectoryNotEmptyException(String msg)
	{
		this(msg, null);
	}

	public FuseDirectoryNotEmptyException(String msg, Throwable cause)
	{
		super(msg, cause);

		initErrno(FuseException.ENOTEMPTY);
	}
}
