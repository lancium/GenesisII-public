package edu.virginia.vcgr.genii.client.fuse.exceptions;

import fuse.FuseException;

public class FuseNoSuchDeviceOrAddressException extends FuseException
{
	static final long serialVersionUID = 0L;
	
	public FuseNoSuchDeviceOrAddressException(String msg)
	{
		this(msg, null);
	}

	public FuseNoSuchDeviceOrAddressException(String msg, Throwable cause)
	{
		super(msg, cause);

		initErrno(FuseException.ENXIO);
	}
}
