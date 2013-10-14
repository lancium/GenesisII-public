package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.virginia.vcgr.genii.algorithm.structures.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig.IdentifierType;
import edu.virginia.vcgr.genii.client.fuse.UnixDirectory;

public class FuseDirCache extends CommonCache
{

	private TimedOutLRUCache<String, UnixDirectory> cache;

	public FuseDirCache(int priorityLevel, int capacity, long cacheLifeTime, boolean monitoingEnabled)
	{
		super(priorityLevel, capacity, cacheLifeTime, monitoingEnabled);
		cache = new TimedOutLRUCache<String, UnixDirectory>(capacity, cacheLifeTime);
	}

	@Override
	public boolean isRelevent(Object cacheKey, Object target, Class<?> typeOfItem)
	{
		throw new RuntimeException("this method is not relevant to this cache");
	}

	@Override
	public boolean supportRetrievalWithoutTarget()
	{
		return true;
	}

	@Override
	public boolean supportRetrievalByWildCard()
	{
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getWildCardMatches(Object target, Object wildCardCacheKey)
	{

		if (!(wildCardCacheKey instanceof String))
			return null;

		Map<String, UnixDirectory> matchings = new HashMap<String, UnixDirectory>();

		// Instead of directly iterating over the keySet of the cache we replicate the keySet into
		// another set and iterate over that. This is done to avoid misbehavior of the iterator due
		// to the concurrent update made by the cache itself inside the cache.get(argument) method.
		final Set<String> cachedKeys = new HashSet<String>(cache.keySet());

		String pathWithWildCard = (String) wildCardCacheKey;
		for (String rnsPath : cachedKeys) {
			if (rnsPath.matches(pathWithWildCard)) {
				UnixDirectory directory = cache.get(rnsPath);
				if (directory != null) {
					matchings.put(rnsPath, directory);
				}
			}
		}

		return matchings;
	}

	@Override
	public Object getItem(Object cacheKey, Object target)
	{
		String path = (String) cacheKey;
		return cache.get(path);
	}

	@Override
	public void putItem(Object cacheKey, Object target, Object value) throws Exception
	{
		String path = (String) cacheKey;
		long lifeTime = getCacheLifetTime(path);
		if (lifeTime <= 0)
			return;
		UnixDirectory directory = (UnixDirectory) value;
		cache.put(path, directory, lifeTime);
	}

	@Override
	public void invalidateCachedItem(Object target)
	{
		throw new RuntimeException("does not make sense in this cache");
	}

	@Override
	public void invalidateCachedItem(Object cacheKey, Object target)
	{
		String path = (String) cacheKey;
		cache.remove(path);
	}

	@Override
	public void invalidateEntireCache()
	{
		cache.clear();
	}

	@Override
	public boolean targetTypeMatches(Object target)
	{
		throw new RuntimeException("does not make sense in this cache");
	}

	@Override
	public boolean cacheKeyMatches(Object cacheKey)
	{
		return (cacheKey instanceof String);
	}

	@Override
	public boolean itemTypeMatches(Class<?> itemType)
	{
		return itemType.equals(UnixDirectory.class);
	}

	@Override
	public IdentifierType getCachedItemIdentifier()
	{
		return WSResourceConfig.IdentifierType.RNS_PATH_IDENTIFIER;
	}

	@Override
	public void updateCacheLifeTimeOfItems(Object commonIdentifierForItems, long newCacheLifeTime)
	{
		String rnsPath = (String) commonIdentifierForItems;
		UnixDirectory directory = cache.get(rnsPath);
		if (directory != null) {
			cache.put(rnsPath, directory, newCacheLifeTime);
		}
	}

	private long getCacheLifetTime(String rnsPath)
	{
		WSResourceConfig resourceConfig = (WSResourceConfig) CacheManager.getItemFromCache(rnsPath, WSResourceConfig.class);
		if (resourceConfig == null)
			return cacheLifeTime;
		if (resourceConfig.isCacheAccessBlocked())
			return 0;
		return Math.max(cacheLifeTime, resourceConfig.getMillisecondTimeLeftToCallbackExpiry());
	}
}
