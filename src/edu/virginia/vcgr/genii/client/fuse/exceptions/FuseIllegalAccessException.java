package edu.virginia.vcgr.genii.client.fuse.exceptions;

import fuse.FuseException;

public class FuseIllegalAccessException extends FuseException
{
	static final long serialVersionUID = 0L;
	
	public FuseIllegalAccessException(String msg)
	{
		this(msg, null);
	}

	public FuseIllegalAccessException(String msg, Throwable cause)
	{
		super(msg, cause);

		initErrno(FuseException.EACCES);
	}
}
