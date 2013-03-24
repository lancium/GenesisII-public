package edu.virginia.vcgr.genii.client.fuse.exceptions;

import fuse.FuseException;

public class FuseUnknownException extends FuseException
{
	static final long serialVersionUID = 0L;

	public FuseUnknownException(String message)
	{
		this(message, null);
	}

	public FuseUnknownException(String message, Throwable cause)
	{
		super(message, cause);

		initErrno(FuseException.EIO);
	}
}