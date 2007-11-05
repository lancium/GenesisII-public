package org.morgan.util.cache;

public class CacheMissException extends CacheException
{
	static final long serialVersionUID = 0L;
	
	public CacheMissException(Throwable cause)
	{
		super(
			"An exception occurred while trying to resolve a cache miss.",
			cause);
	}
}