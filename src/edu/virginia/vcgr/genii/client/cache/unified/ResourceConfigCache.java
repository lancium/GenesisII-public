package edu.virginia.vcgr.genii.client.cache.unified;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig.IdentifierType;

/*
 * This cache allows retrieval of same cache entries using different keys. To be
 * exact the caller can retrieves resource configuration by WS-Identifier, INode 
 * number, and RNSPath string. This unusual behavior is supported because a resource 
 * configuration instance stores the mapping between these identifiers and should
 * be accessible by any of those.
 * */
public class ResourceConfigCache extends CommonCache {

	/*
	 * Directory and file resource configuration caches are kept separate as we don't want
	 * to loose valuable directory configurations if the system tries to flood the cache with
	 * resource configurations of files encountered in a large directory.
	 * */
	private TimedOutLRUCache<String, WSResourceConfig> fileConfigCache;
	private TimedOutLRUCache<String, WSResourceConfig> directoryConfigCache;
	
	public ResourceConfigCache(int priorityLevel, int capacity, long cacheLifeTime, boolean monitoingEnabled) {
		super(priorityLevel, capacity, cacheLifeTime, monitoingEnabled);
		int directoryLookupCacheCapacity = capacity / 10;
		directoryConfigCache = new TimedOutLRUCache<String, WSResourceConfig>(
				directoryLookupCacheCapacity, cacheLifeTime);
		int fileLookupCacheCapacity = capacity - directoryLookupCacheCapacity;
		fileConfigCache = new TimedOutLRUCache<String, WSResourceConfig>(
				fileLookupCacheCapacity, cacheLifeTime);
	}

	@Override
	public boolean isRelevent(Object cacheKey, Object target, Class<?> typeOfItem) {
		throw new RuntimeException("this method is not relevant to this cache");
	}

	@Override
	public boolean supportRetrievalWithoutTarget() {
		return true;
	}

	@Override
	public Object getItem(Object cacheKey, Object target) {
		Object cachedItem = getItem(cacheKey, target, directoryConfigCache);
		if (cachedItem != null) return cachedItem;
		return getItem(cacheKey, target, fileConfigCache);
	}
	
	/*
	 * Although a resource configuration instance can be retrieved using any
	 * of the three identifiers, it can be stored by only WS-identifier. However,
	 * when the cacheKey is different we are not throwing any exception. This
	 * is because often the caller may retrieve a configuration and try to
	 * store it again after updating some identifiers. Note that this scheme 
	 * should work because -- and only because -- initially the resource 
	 * is retrieved from the container using the primary identifier, and we 
	 * expect to have the configuration stored in the cache at that time. This
	 * process may seems unnecessarily complicated, but we do this to provide
	 * transparency: we do not assume the caller handling the INode or RNS 
	 * path identifier understands WS EndpointIdentifier URIs.
	 * */
	@Override
	public void putItem(Object cacheKey, Object target, Object value) throws Exception {
		
		WSResourceConfig newResourceConfig = (WSResourceConfig) value;
		long lifetime = Math.max(cacheLifeTime, newResourceConfig.getMillisecondTimeLeftToCallbackExpiry());
		TimedOutLRUCache<String, WSResourceConfig> cache = 
			(newResourceConfig.isDirectory()) ? directoryConfigCache : fileConfigCache; 
		
		if (cacheKey instanceof URI) {
			
			String primaryIdentifer = cacheKey.toString();
			
			// only try to put if it is not already in the cache. Otherwise,
			// there is a chance that we will missed some identifier mappings
			// by inserting a replacement resource configuration.
			WSResourceConfig wsResourceConfig = cache.get(primaryIdentifer);
			if (wsResourceConfig == null) {
				cache.put(primaryIdentifer, newResourceConfig, lifetime);
			} else {
				// In case we encounter the same resource in a new RNSPath we
				// should update the set of RNSPath within the cached configuration.
				wsResourceConfig.addRNSPaths(newResourceConfig.getRnsPaths());
				
				// This will refresh the timeout interval in cache.
				lifetime = Math.max(lifetime, wsResourceConfig.getMillisecondTimeLeftToCallbackExpiry());
				cache.put(primaryIdentifer, wsResourceConfig, lifetime);
			}
		} else {
			WSResourceConfig wsResourceConfig = newResourceConfig;
			String primaryIdentifier = wsResourceConfig.getWsIdentifier().toString();
			if (primaryIdentifier != null) {
				cache.put(primaryIdentifier, wsResourceConfig, lifetime);
			}
		}
	}

	@Override
	public void invalidateCachedItem(Object target) {
		throw new RuntimeException("does not make sense in this cache");
	}

	@Override
	public void invalidateCachedItem(Object cacheKey, Object target) {
		invalidateCachedItem(cacheKey, target, fileConfigCache);
		invalidateCachedItem(cacheKey, target, directoryConfigCache);
	}

	@Override
	public void invalidateEntireCache() {
		fileConfigCache.clear();
		directoryConfigCache.clear();
	}

	@Override
	public boolean targetTypeMatches(Object target) {
		throw new RuntimeException("does not make sense in this cache");
	}

	@Override
	public boolean cacheKeyMatches(Object cacheKey) {
		return (cacheKey instanceof String 
				|| cacheKey instanceof Integer 
				|| cacheKey instanceof URI);
	}

	@Override
	public boolean itemTypeMatches(Class<?> itemType) {
		return (WSResourceConfig.class.equals(itemType));
	}

	/*
	 * Since this is the store for resource configuration instances we do not expect
	 * it to respond to any identifier lookup query. This is a safety precaution to 
	 * avoid undetected infinite loops.
	 */
	@Override
	public IdentifierType getCachedItemIdentifier() {
		return null;
	}

	/* 
	 * The caller should directly update the resource configuration by first retrieving
	 * it from the cache then reinserting it with the updated lifetime setting.
	 */
	@Override
	public void updateCacheLifeTimeOfItems(Object commonIdentifierForItems, 
			long newCacheLifeTime) {
		//do nothing
	}

	private Object getItem(Object cacheKey, Object target, 
			TimedOutLRUCache<String, WSResourceConfig> cache) {
		if (cacheKey instanceof URI) {
			String primaryIdentifier = cacheKey.toString();
			return cache.get(primaryIdentifier);
		}
		final Set<String> cachedKeys = new HashSet<String>(cache.keySet());
		for (String primaryIdentifier : cachedKeys) {
			WSResourceConfig config = cache.get(primaryIdentifier);
			if (config != null && config.identifierMatches(cacheKey)) {
				return config;
			}
		}
		return null;
	}
	
	@Override
	public boolean supportRetrievalByWildCard() {
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getWildCardMatches(Object target, Object wildCardCacheKey) {
		if (wildCardCacheKey instanceof String) {
			String pathToCompare = (String) wildCardCacheKey;
			Map<String, WSResourceConfig> matches = new HashMap<String, WSResourceConfig>();
			addMatchingEntriesInMap(pathToCompare, matches, directoryConfigCache);
			addMatchingEntriesInMap(pathToCompare, matches, fileConfigCache);
			return matches;
		}
		return Collections.EMPTY_MAP;
	}

	public void invalidateCachedItem(Object cacheKey, Object target, 
			TimedOutLRUCache<String, WSResourceConfig> cache) {
		if (cacheKey instanceof URI) {
			String primaryIdentifier = cacheKey.toString();
			cache.remove(primaryIdentifier);
		} else {
			String searchedIdentifier = null;
			
			// Instead of directly iterating over the keySet of the cache we replicate the keySet into
			// another set and iterate over that. This is done to avoid misbehavior of the iterator due
			// to the concurrent update made by the cache itself inside the cache.get(argument) method. 
			final Set<String> cachedKeys = new HashSet<String>(cache.keySet());
			
			for (String primaryIdentifier : cachedKeys) {
				WSResourceConfig config = cache.get(primaryIdentifier);
				if (config != null && config.identifierMatches(cacheKey)) {
					searchedIdentifier = primaryIdentifier;
					break;
				}
			}
			if (searchedIdentifier != null) {
				cache.remove(searchedIdentifier);
			}
		}
	}
	
	private void addMatchingEntriesInMap(String pathToCompare, Map<String, WSResourceConfig> matches, 
			TimedOutLRUCache<String, WSResourceConfig> cache) {
		final Set<String> cachedKeys = new HashSet<String>(cache.keySet());
		for (String cacheKey : cachedKeys) {
			WSResourceConfig resourceConfig = cache.get(cacheKey);
			if (resourceConfig != null) {
				if (resourceConfig.isMatchingPath(pathToCompare)) {
					matches.put(cacheKey, resourceConfig);
				}
			}
		}
	}
}
