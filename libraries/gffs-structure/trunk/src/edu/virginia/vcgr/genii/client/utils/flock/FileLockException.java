package edu.virginia.vcgr.genii.client.utils.flock;

public class FileLockException extends Exception
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