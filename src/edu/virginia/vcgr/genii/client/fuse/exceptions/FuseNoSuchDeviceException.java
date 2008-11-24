package edu.virginia.vcgr.genii.client.fuse.exceptions;

import fuse.FuseException;

public class FuseNoSuchDeviceException extends FuseException
{
	static final long serialVersionUID = 0L;
	
	public FuseNoSuchDeviceException(String msg)
	{
		this(msg, null);
	}

	public FuseNoSuchDeviceException(String msg, Throwable cause)
	{
		super(msg, cause);

		initErrno(FuseException.ENODEV);
	}
}
