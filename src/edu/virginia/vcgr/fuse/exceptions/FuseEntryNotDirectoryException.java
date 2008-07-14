package edu.virginia.vcgr.fuse.exceptions;

import fuse.FuseException;

public class FuseEntryNotDirectoryException extends FuseException
{
	static final long serialVersionUID = 0L;
	
	public FuseEntryNotDirectoryException(String msg)
	{
		this(msg, null);
	}

	public FuseEntryNotDirectoryException(String msg, Throwable cause)
	{
		super(msg, cause);

		initErrno(FuseException.ENOTDIR);
	}
}
