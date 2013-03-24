package edu.virginia.vcgr.genii.client.fuse.exceptions;

import fuse.FuseException;

public class FuseIOException extends FuseException
{
	static final long serialVersionUID = 0L;

	public FuseIOException(String msg)
	{
		this(msg, null);
	}

	public FuseIOException(String msg, Throwable cause)
	{
		super(msg, cause);

		initErrno(FuseException.EIO);
	}
}
