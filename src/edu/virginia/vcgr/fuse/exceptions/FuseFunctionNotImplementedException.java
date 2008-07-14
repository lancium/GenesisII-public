package edu.virginia.vcgr.fuse.exceptions;

import fuse.FuseException;

public class FuseFunctionNotImplementedException extends FuseException
{
	static final long serialVersionUID = 0L;
	
	public FuseFunctionNotImplementedException(String msg)
	{
		this(msg, null);
	}

	public FuseFunctionNotImplementedException(String msg, Throwable cause)
	{
		super(msg, cause);

		initErrno(FuseException.ENOSYS);
	}
}
