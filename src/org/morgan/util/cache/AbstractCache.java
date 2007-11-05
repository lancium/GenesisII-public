package org.morgan.util.cache;

public abstract class AbstractCache<KeyType, DataType> 
	implements ICache<KeyType, DataType>
{
	private long _hits = 0;
	private long _misses = 0;
	
	@Override
    public void clearStatistics()
    {
	    _hits = 0;
	    _misses = 0;
    }

	@Override
    public CacheStatistics getCachingStatistics()
    {
	    return new CacheStatistics(_misses, _hits);
    }

	@Override
    public DataType lookup(KeyType key) throws CacheException
    {
	    DataType ret = tableLookup(key);
	    if (ret == null)
	    {
	    	_misses++;
	    	return miss(key);
	    } else
	    {
	    	_hits++;
	    	return ret;
	    }
    }
	
	protected abstract DataType tableLookup(KeyType key);
	protected abstract DataType miss(KeyType key) 
		throws CacheMissException;
}