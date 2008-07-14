package edu.virginia.vcgr.fuse.exceptions;

import fuse.FuseException;

public class FuseNoSuchEntryException extends FuseException
{
	static final long serialVersionUID = 0L;
	
	public FuseNoSuchEntryException(String msg)
	{
		this(msg, null);
	}

	public FuseNoSuchEntryException(String msg, Throwable cause)
	{
		super(msg, cause);

		initErrno(FuseException.ENOENT);
	}
}
