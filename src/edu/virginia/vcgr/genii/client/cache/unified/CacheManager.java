package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cache.ResourceAccessMonitor;
import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig.IdentifierType;
import edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement.NotificationBrokerDirectory;
import edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement.SubscriptionDirectory;

/*
 * This is the facet that mediates all cache related operation issued from other places of the code.
 * Additionally, when different caches want to interact with each other to get or update required 
 * information, instead of directly invoking one another's methods, they invoke methods in CacheManager 
 * interface. This is done because each cache is oblivious of the others.
 */
public class CacheManager {
	
	static private Log _logger = LogFactory.getLog(CacheManager.class);

	public static Object getItemFromCache(Object cacheKey, Class<?> itemType) {
		return getItemFromCache(null, cacheKey, itemType);
	}
	
	public static Object getItemFromCache(Object target, Object cacheKey, Class<?> itemType) {
	
		if (CacheConfigurer.isCachingEnabled()) {
			
			// Before accessing the cache, this tries to assess the freshness of cached information by 
			// investigating the target, whenever possible. This helps to reduce load on cache management
			// module and to ensure freshness of information when nothing can be inferred from the
			// retrieved cached item.
			if (ResourceAccessMonitor.isMonitoredObject(target) 
					&& !ResourceAccessMonitor.isCachedContentGuaranteedToBeFresh(target)) return null;
			try {
				CommonCache cache = findCacheForObject(target, cacheKey, itemType);
				if (cache == null) return null;
				Object cachedItem = cache.getItem(cacheKey, target);
				if (cachedItem != null) {
					_logger.trace("request is satisfied from the cache: " + cacheKey);
				} else {
					_logger.trace("not in cache: " + cacheKey);
				}
				if (cache.isMonitoringEnabled()) {
					ResourceAccessMonitor.reportResourceUsage(cachedItem);
				}
				
				// Returns item from cache only when the system is satisfied about its freshness.
				if (ResourceAccessMonitor.isCachedContentGuaranteedToBeFresh(cachedItem)) {
					return cachedItem;
				} else {
					_logger.debug("Access to cached item has been denied");
					return null;
				}
			} catch (Exception ex) {
				_logger.info("problem while retrieving objects from the cache", ex);
			}
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public static Map getMatchingItemsWithKeys(Object cacheKeyWithWildCard, Class<?> itemType) {
		return getMatchingItemsWithKeys(null, cacheKeyWithWildCard, itemType);
	}
	
	@SuppressWarnings("rawtypes")
	public static Map getMatchingItemsWithKeys(Object target, Object cacheKeyWithWildCard, Class<?> itemType) {
		if (CacheConfigurer.isCachingEnabled()) {
			try {
				CommonCache cache = findCacheForObject(target, cacheKeyWithWildCard, itemType);
				if (cache == null) return null;
				if (!cache.supportRetrievalByWildCard()) return null;
				return cache.getWildCardMatches(target, cacheKeyWithWildCard);
			} catch (Exception ex) {
				_logger.debug("problem while retrieving objects from the cache", ex);
			}
		}
		return null;
	}

	public static void cacheReleventInformation(Object... targets) {
		if (CacheConfigurer.isCachingEnabled()) {
			try {
				Collection<CacheableItemsGenerator> generators = CacheConfigurer.getGenerators();
				Collection<CacheableItem> items = new ArrayList<CacheableItem>();
				
				Class<?>[] targetTypes = new Class<?>[targets.length];
				for (int index = 0; index < targets.length; index++) {
					targetTypes[index] = targets[index].getClass();
				}
				
				for (CacheableItemsGenerator generator : generators) {
					if (generator.isSupported(targetTypes)) {
						items.addAll(generator.generateItems(targets));
					}
				}
				for (CacheableItem item : items) {
					Object cacheKey = item.getKey();
					Object targetObject = item.getTarget();
					Object value = item.getValue();
					putItemInCache(targetObject, cacheKey, value);
				}
			} catch (Exception ex) {
				_logger.debug("Cache: failed to process information", ex);
			}
		}
	}
	
	public static void putItemInCache(Object cacheKey, Object value) {
		putItemInCache(null, cacheKey, value);
	}
	
	public static void putItemInCache(Object target, Object cacheKey, Object value) {
		if (CacheConfigurer.isCachingEnabled()) {
			try {
				CommonCache cache = findCacheForObject(target, cacheKey, value);
				if (cache != null) {
					cache.putItem(cacheKey, target, value);
				} else {
					_logger.trace("Cache: failed to cache item " + cacheKey);
				}
			} catch (Exception ex) {
				_logger.debug("failed to cache item", ex);
			}
		}
	}
	
	public static void removeAllRelevantInfoFromCache(Object target, Class<?> typeOfItem) {
		removeItemFromCache(target, null, typeOfItem);
	}
	
	public static void removeItemFromCache(Object cacheKey, Class<?> typeOfItem) {
		removeItemFromCache(null, cacheKey, typeOfItem);
	}
	
	public static void removeItemFromCache(Object target, Object cacheKey, Class<?> typeOfItem) {
		if (CacheConfigurer.isCachingEnabled()) {
			try {
				Collection<CommonCache> cacheList = CacheConfigurer.getCaches();
				for (CommonCache cache : cacheList) {
					if (!cache.itemTypeMatches(typeOfItem)) continue;
					if (target == null && cache.supportRetrievalWithoutTarget()) {
						if (cache.cacheKeyMatches(cacheKey)) {
							cache.invalidateCachedItem(cacheKey, null);
						}
					} else if (target != null && !cache.supportRetrievalWithoutTarget()) {
						if (cache.targetTypeMatches(target)) {
							if (cacheKey != null) {
								if (cache.cacheKeyMatches(cacheKey)) {
									cache.invalidateCachedItem(cacheKey, target);
								}
							} else {
								cache.invalidateCachedItem(target);
							}
						}
					}
				}
			} catch (Exception ex) {
				_logger.debug("Could not remove cached item", ex);
			}
		}
	}
	
	public static void updateCacheLifeTimeOfRelevantStoredItems(WSResourceConfig resourceConfig) {
		if (CacheConfigurer.isCachingEnabled()) {
			long lifetimeOfCachedItems = resourceConfig.getMillisecondTimeLeftToCallbackExpiry();
			if (lifetimeOfCachedItems <= 0) return;
			try {
				Collection<CommonCache> cacheList = CacheConfigurer.getCaches();
				for (CommonCache cache : cacheList) {
					IdentifierType cachedItemIdentifier = cache.getCachedItemIdentifier();
					if (cachedItemIdentifier == null) continue;
					if (cachedItemIdentifier == WSResourceConfig.IdentifierType.WS_ENDPOINT_IDENTIFIER) {
						cache.updateCacheLifeTimeOfItems(
								resourceConfig.getWsIdentifier(), lifetimeOfCachedItems);
					} else if (resourceConfig.getInodeNumber() != null 
							&& cachedItemIdentifier == WSResourceConfig.IdentifierType.INODE_NUMBER_IDENTIFIER) {
						cache.updateCacheLifeTimeOfItems(
								resourceConfig.getInodeNumber(), lifetimeOfCachedItems);
					} else {
						for (String rnsPath : resourceConfig.getRnsPaths()) {
							cache.updateCacheLifeTimeOfItems(rnsPath, lifetimeOfCachedItems);
						}
					}
				}
			} catch (Exception ex) {
				_logger.debug("Could not update the lifetime of some cached items", ex);
			}
		}
	}
	
	public static void clearCache(Class<?> typeOfItem) {
		if (CacheConfigurer.isCachingEnabled()) {
			try {
				Collection<CommonCache> cacheList = CacheConfigurer.getCaches();
				for (CommonCache cache : cacheList) {
					if (cache.itemTypeMatches(typeOfItem)) {
						cache.invalidateEntireCache();
					}
				}
			} catch (Exception ex) {
				_logger.debug("Could not remove cached item", ex);
			}
		}
	}
	
	public static void resetCachingSystem() {
		try {
			CacheConfigurer.resetCaches();
			NotificationBrokerDirectory.clearDirectory();
			SubscriptionDirectory.clearDirectory();
		} catch (Exception ex) {
			_logger.info("Alarm: couldn't reset the caching system after a failure. " +
				"To avoid seeing, possibly, stale contents, " +
				"restart the grid client: " + ex.getMessage());
		}
	}
	
	private static CommonCache findCacheForObject(Object target, Object cacheKey, Object value) {
		return findCacheForObject(target, cacheKey, value.getClass());
	}
	
	private static CommonCache findCacheForObject(Object target, Object cacheKey, Class<?> typeOfItem) {
		Collection<CommonCache> cacheList = CacheConfigurer.getCaches();
		for (CommonCache cache : cacheList) {
			if (target == null && cache.supportRetrievalWithoutTarget()) {
				if (cache.isRelevent(cacheKey, typeOfItem)) {
					return cache;
				}
			} else if (target != null && !cache.supportRetrievalWithoutTarget()) {
				if (cache.isRelevent(cacheKey, target, typeOfItem)) {
					return cache;
				}
			}
		}
		return null;
	}
}
