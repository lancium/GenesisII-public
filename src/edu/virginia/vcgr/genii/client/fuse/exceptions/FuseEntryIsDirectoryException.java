package edu.virginia.vcgr.genii.client.fuse.exceptions;

import fuse.FuseException;

public class FuseEntryIsDirectoryException extends FuseException
{
	static final long serialVersionUID = 0L;
	
	public FuseEntryIsDirectoryException(String msg)
	{
		this(msg, null);
	}

	public FuseEntryIsDirectoryException(String msg, Throwable cause)
	{
		super(msg, cause);

		initErrno(FuseException.EISDIR);
	}
}
