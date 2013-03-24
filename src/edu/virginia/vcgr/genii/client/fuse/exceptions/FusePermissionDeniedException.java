package edu.virginia.vcgr.genii.client.fuse.exceptions;

import fuse.FuseException;

public class FusePermissionDeniedException extends FuseException
{
	static final long serialVersionUID = 0L;

	public FusePermissionDeniedException(String msg)
	{
		this(msg, null);
	}

	public FusePermissionDeniedException(String msg, Throwable cause)
	{
		super(msg, cause);

		initErrno(FuseException.EPERM);
	}
}
