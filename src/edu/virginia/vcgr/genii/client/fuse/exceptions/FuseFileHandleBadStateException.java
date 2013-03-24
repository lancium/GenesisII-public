package edu.virginia.vcgr.genii.client.fuse.exceptions;

import fuse.FuseException;

public class FuseFileHandleBadStateException extends FuseException
{
	static final long serialVersionUID = 0L;

	public FuseFileHandleBadStateException(String msg)
	{
		this(msg, null);
	}

	public FuseFileHandleBadStateException(String msg, Throwable cause)
	{
		super(msg, cause);

		initErrno(FuseException.EBADFD);
	}
}
