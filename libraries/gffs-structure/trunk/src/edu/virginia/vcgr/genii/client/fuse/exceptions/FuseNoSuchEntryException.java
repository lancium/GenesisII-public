package edu.virginia.vcgr.genii.client.fuse.exceptions;

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

	@Override
	public void printStackTrace()
	{
		/*
		 * we have taken the rare step of silencing the stack trace, since it is hugely annoying to see it continually and it does not provide
		 * any useful information; it just always gets spewed by the fuse-j.jar which we currently cannot change since we don't have source
		 * code.
		 */
		
		System.err.println(": " + getLocalizedMessage());
	}
}
