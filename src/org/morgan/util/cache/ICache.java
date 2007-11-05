package org.morgan.util.cache;

public interface ICache<KeyType, DataType>
{
	public DataType lookup(KeyType key) 
		throws CacheException;
	
	public void clear();
	
	public CacheStatistics getCachingStatistics();
	public void clearStatistics();
}