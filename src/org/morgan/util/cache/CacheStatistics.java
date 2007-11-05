package org.morgan.util.cache;

public class CacheStatistics
{
	private long _misses;
	private long _hits;
	
	public CacheStatistics(long misses, long hits)
	{
		_misses = misses;
		_hits = hits;
	}
	
	public long getCacheMisses()
	{
		return _misses;
	}
	
	public long getCacheHits()
	{
		return _hits;
	}
}