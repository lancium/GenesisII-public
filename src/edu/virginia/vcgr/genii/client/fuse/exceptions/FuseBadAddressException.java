package edu.virginia.vcgr.genii.client.fuse.exceptions;

import fuse.FuseException;

public class FuseBadAddressException extends FuseException
{
	static final long serialVersionUID = 0L;

	public FuseBadAddressException(String msg)
	{
		this(msg, null);
	}

	public FuseBadAddressException(String msg, Throwable cause)
	{
		super(msg, cause);

		initErrno(FuseException.EFAULT);
	}
}
