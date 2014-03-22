package edu.virginia.vcgr.genii.client.utils.flock;

import java.io.IOException;

public class FileLockException extends IOException
{
	static final long serialVersionUID = 0L;

	public FileLockException(String message)
	{
		super(message);
	}

	public FileLockException(String message, Throwable cause)
	{
		super(message, cause);
	}
}