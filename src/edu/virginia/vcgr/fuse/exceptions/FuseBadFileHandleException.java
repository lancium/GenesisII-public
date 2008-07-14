package edu.virginia.vcgr.fuse.exceptions;

import fuse.FuseException;

public class FuseBadFileHandleException extends FuseException
{
	static final long serialVersionUID = 0L;
	
	public FuseBadFileHandleException(String msg)
	{
		this(msg, null);
	}

	public FuseBadFileHandleException(String msg, Throwable cause)
	{
		super(msg, cause);

		initErrno(FuseException.EBADF);
	}
}
