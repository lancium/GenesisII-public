package edu.virginia.vcgr.genii.client.fuse.exceptions;

import fuse.FuseException;

public class FuseEntryAlreadyExistsException extends FuseException
{
	static final long serialVersionUID = 0L;

	public FuseEntryAlreadyExistsException(String msg)
	{
		this(msg, null);
	}

	public FuseEntryAlreadyExistsException(String msg, Throwable cause)
	{
		super(msg, cause);

		initErrno(FuseException.EEXIST);
	}
}
