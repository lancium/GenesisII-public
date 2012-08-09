package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig.IdentifierType;
import edu.virginia.vcgr.genii.client.fuse.DirectoryManager;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;

public class RNSLookupCache extends CommonCache {
	
	private TimedOutLRUCache<String, EndpointReferenceType> fileLookupCache;
	private TimedOutLRUCache<String, EndpointReferenceType> directoryLookupCache;
	
	public RNSLookupCache(int priorityLevel, int capacity, long cacheLifeTime, boolean monitoingEnabled) {
		
		super(priorityLevel, capacity, cacheLifeTime, monitoingEnabled);
		int directoryLookupCacheCapacity = capacity / 5;
		int fileLookupCacheCapacity = capacity - directoryLookupCacheCapacity;

		fileLookupCache = new TimedOutLRUCache<String, EndpointReferenceType>(
				fileLookupCacheCapacity, cacheLifeTime);
		
		directoryLookupCache = new TimedOutLRUCache<String, EndpointReferenceType>(
				directoryLookupCacheCapacity, cacheLifeTime);
	}

	@Override
	public boolean isRelevent(Object cacheKey, Object target, Class<?> typeOfItem) {
		throw new RuntimeException("this method is irrelevent to this cache");
	}

	@Override
	public boolean supportRetrievalWithoutTarget() {
		return true;
	}
	
	@Override
	public boolean supportRetrievalByWildCard() {
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map getWildCardMatches(Object target, Object wildCardCacheKey) {
		Map matchings = new HashMap<String, EndpointReferenceType>();
		if (wildCardCacheKey instanceof String) {
			String pathNamesEndWithWildCard = (String) wildCardCacheKey;
			performWildcardMatching(pathNamesEndWithWildCard, matchings, fileLookupCache);
			performWildcardMatching(pathNamesEndWithWildCard, matchings, directoryLookupCache);
		}
		return matchings;
	}

	@Override
	public Object getItem(Object cacheKey, Object target) {
		String path = (String) cacheKey;
		EndpointReferenceType result = directoryLookupCache.get(path);
		if (result == null) {
			result = fileLookupCache.get(path);
		}
		return result;
	}

	@Override
	public void putItem(Object cacheKey, Object target, Object value) throws Exception {
		
		String path = (String) cacheKey;
		long lifetime = getCacheLifetTime(path);
		EndpointReferenceType EPR = (EndpointReferenceType) value;
		
		// Get rid of all the references to the SOAPMessage associated with this endPoint. 
		EndpointReferenceType sanitizedEPR = Sanitizer.getSanitizedEpr(EPR);
		
		getCacheForTarget(EPR).put(path, sanitizedEPR, lifetime);
	}

	@Override
	public void invalidateCachedItem(Object target) {
		throw new RuntimeException("Does not make sense in this cache");
	}

	@Override
	public void invalidateCachedItem(Object cacheKey, Object target) {
		String path = (String) cacheKey;
		fileLookupCache.remove(path);
		directoryLookupCache.remove(path);
	}

	@Override
	public void invalidateEntireCache() {
		fileLookupCache.clear();
		directoryLookupCache.clear();
	}

	@Override
	public boolean targetTypeMatches(Object target) {
		throw new RuntimeException("Does not make sense in this cache");
	}

	@Override
	public boolean cacheKeyMatches(Object cacheKey) {
		return (cacheKey instanceof String);
	}

	@Override
	public boolean itemTypeMatches(Class<?> itemType) {
		return itemType.equals(EndpointReferenceType.class);
	}

	@Override
	public IdentifierType getCachedItemIdentifier() {
		return WSResourceConfig.IdentifierType.RNS_PATH_IDENTIFIER;
	}

	@Override
	public void updateCacheLifeTimeOfItems(Object commonIdentifierForItems, long newCacheLifeTime) {
		String rnsPathString = (String) commonIdentifierForItems;
		String childPaths = DirectoryManager.getPathForDirectoryEntry(rnsPathString, "[^//]+");
		@SuppressWarnings("unchecked")
		Map<String, EndpointReferenceType> children = getWildCardMatches(null, childPaths);
		if (children != null) {
			for (Map.Entry<String, EndpointReferenceType> child : children.entrySet()) {
				String childRNSPathString = child.getKey();
				EndpointReferenceType childEndpoint = child.getValue();
				getCacheForTarget(childEndpoint).put(childRNSPathString, childEndpoint, newCacheLifeTime);
			}
		} 
	}
	
	private long getCacheLifetTime(String rnsPath) {
		String parentPath = DirectoryManager.getParentPath(rnsPath);
		WSResourceConfig parentResourceConfig = 
			(WSResourceConfig) CacheManager.getItemFromCache(parentPath, WSResourceConfig.class);
		if (parentResourceConfig == null) return cacheLifeTime;
		return Math.max(cacheLifeTime, parentResourceConfig.getMillisecondTimeLeftToCallbackExpiry());
	}
	
	private TimedOutLRUCache<String, EndpointReferenceType> getCacheForTarget(EndpointReferenceType target) {
		TypeInformation typeInfo = new TypeInformation(target);
		return (typeInfo.isRNS()) ? directoryLookupCache : fileLookupCache;
	}
	
	private void performWildcardMatching(String pathWithWildCard, Map<String, EndpointReferenceType> matchings,
			TimedOutLRUCache<String, EndpointReferenceType> cache) {
		
		// Instead of directly iterating over the keySet of the cache we replicate the keySet into
		// another set and iterate over that. This is done to avoid misbehavior of the iterator due
		// to the concurrent update made by the cache itself inside the cache.get(argument) method.
		final Set<String> cachedKeys = new HashSet<String>(cache.keySet());
		
		for (String rnsPath : cachedKeys) {
			if (rnsPath.matches(pathWithWildCard)) {
				EndpointReferenceType matchingEPR = cache.get(rnsPath);
				if (matchingEPR != null) {
					matchings.put(rnsPath, matchingEPR);
				}
			}
		}
	}
}
