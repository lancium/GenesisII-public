package org.morgan.util.cache;

public class CacheException extends Exception
{
	static final long serialVersionUID = 0L;
	
	public CacheException(String message)
	{
		super(message);
	}
	
	public CacheException(String message, Throwable cause)
	{
		super(message, cause);
	}
}