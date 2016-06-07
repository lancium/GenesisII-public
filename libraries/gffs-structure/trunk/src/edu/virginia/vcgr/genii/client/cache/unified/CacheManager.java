package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.algorithm.application.ProgramTools;
import edu.virginia.vcgr.genii.client.cache.ResourceAccessMonitor;
import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig.IdentifierType;
import edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement.NotificationBrokerDirectory;
import edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement.Subscriber;
import edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement.SubscriptionDirectory;
import edu.virginia.vcgr.genii.client.comm.axis.security.VcgrSslSocketFactory;
import edu.virginia.vcgr.genii.security.credentials.ClientCredentialTracker;

/*
 * This is the facet that mediates all cache related operation issued from other places of the code. Additionally, when different caches want
 * to interact with each other to get or update required information, instead of directly invoking one another's methods, they invoke methods
 * in CacheManager interface. This is done because each cache is oblivious of the others.
 */
public class CacheManager
{
	static private Log _logger = LogFactory.getLog(CacheManager.class);

	public static Object getItemFromCache(Object cacheKey, Class<?> itemType)
	{
		return getItemFromCache(null, cacheKey, itemType);
	}

	public static Object getItemFromCache(Object target, Object cacheKey, Class<?> itemType)
	{
		if (CacheConfigurer.isCachingEnabled()) {
			/*
			 * Before accessing the cache, this tries to assess the freshness of cached information by investigating the target, whenever
			 * possible. This helps to reduce load on cache management module and to ensure freshness of information when nothing can be
			 * inferred from the retrieved cached item.
			 */
			if (ResourceAccessMonitor.isMonitoredObject(target) && !ResourceAccessMonitor.isCachedContentGuaranteedToBeFresh(target))
				return null;
			try {
				CommonCache cache = findCacheForObject(target, cacheKey, itemType);
				if (cache == null)
					return null;
				Object cachedItem = cache.getItem(cacheKey, target);

				if (_logger.isTraceEnabled()) {
					String result = "cache miss";
					if (cachedItem != null) {
						result = "cache hit";
					}
					_logger.trace(result + " for " + cacheKey + " of type " + itemType.getCanonicalName());
				}

				if (cachedItem == null)
					return null;

				if (cache.isMonitoringEnabled()) {
					ResourceAccessMonitor.reportResourceUsage(cachedItem);
				}

				// Returns item from cache only when the system is satisfied about its freshness.
				if (ResourceAccessMonitor.isCachedContentGuaranteedToBeFresh(cachedItem)) {
					return cachedItem;
				} else {
					if (_logger.isDebugEnabled())
						_logger.debug("Access to cached item has been denied");
					return null;
				}
			} catch (Exception ex) {
				_logger.error("problem while retrieving objects from the cache", ex);
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static Map getMatchingItemsWithKeys(Object cacheKeyWithWildCard, Class<?> itemType)
	{
		return getMatchingItemsWithKeys(null, cacheKeyWithWildCard, itemType);
	}

	@SuppressWarnings("rawtypes")
	public static Map getMatchingItemsWithKeys(Object target, Object cacheKeyWithWildCard, Class<?> itemType)
	{
		if (CacheConfigurer.isCachingEnabled()) {
			try {
				CommonCache cache = findCacheForObject(target, cacheKeyWithWildCard, itemType);
				if (cache == null)
					return null;
				if (!cache.supportRetrievalByWildCard())
					return null;
				return cache.getWildCardMatches(target, cacheKeyWithWildCard);
			} catch (Exception ex) {
				if (_logger.isDebugEnabled())
					_logger.debug("problem while retrieving objects from the cache", ex);
			}
		}
		return null;
	}

	public static void cacheReleventInformation(Object... targets)
	{
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
					if (value == null) {
						_logger.error("cached value is null from a cacheable item!");
					} else {
						if (_logger.isTraceEnabled())
							_logger.trace("caching item of type " + value.getClass().getCanonicalName());
						putItemInCache(targetObject, cacheKey, value);
					}
				}
			} catch (Exception ex) {
				_logger.error("exception occurred while caching items: " + ex.getMessage(), ex);
			}
		}
	}

	public static void putItemInCache(Object cacheKey, Object value)
	{
		putItemInCache(null, cacheKey, value);
	}

	public static void putItemInCache(Object target, Object cacheKey, Object value)
	{
		if (value == null) {
			String msg = "value to cache is null; ignoring request to cache it.";
			_logger.error(msg);
			return;
		}

		if (_logger.isTraceEnabled()) {
			String msg = "cache put: key type " + cacheKey.getClass().getCanonicalName() + " target ";
			if (target != null)
				msg = msg + "type " + target.getClass().getCanonicalName();
			else
				msg = msg + " is null ";
			msg = msg + " value type " + value.getClass().getCanonicalName();
			_logger.trace(msg);
		}

		if (CacheConfigurer.isCachingEnabled()) {
			try {
				CommonCache cache = findCacheForObject(target, cacheKey, value);
				if (cache != null) {
					cache.putItem(cacheKey, target, value);
				}
			} catch (Exception ex) {
				_logger.warn("could not cache item: " + ex.getLocalizedMessage(), ex);
			}
		}
	}

	public static void removeAllRelevantInfoFromCache(Object target, Class<?> typeOfItem)
	{
		removeItemFromCache(target, null, typeOfItem);
	}

	public static void removeItemFromCache(Object cacheKey, Class<?> typeOfItem)
	{
		removeItemFromCache(null, cacheKey, typeOfItem);
	}

	public static void removeItemFromCache(Object target, Object cacheKey, Class<?> typeOfItem)
	{
		if (CacheConfigurer.isCachingEnabled()) {
			try {
				Collection<CommonCache> cacheList = CacheConfigurer.getCaches();
				for (CommonCache cache : cacheList) {
					if (!cache.itemTypeMatches(typeOfItem))
						continue;
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
				if (_logger.isDebugEnabled())
					_logger.debug("Could not remove cached item", ex);
			}
		}
	}

	public static void updateCacheLifeTimeOfRelevantStoredItems(WSResourceConfig resourceConfig)
	{
		if (CacheConfigurer.isCachingEnabled()) {
			long lifetimeOfCachedItems = resourceConfig.getMillisecondTimeLeftToCallbackExpiry();
			if (lifetimeOfCachedItems <= 0)
				return;
			try {
				Collection<CommonCache> cacheList = CacheConfigurer.getCaches();
				for (CommonCache cache : cacheList) {
					IdentifierType cachedItemIdentifier = cache.getCachedItemIdentifier();
					if (cachedItemIdentifier == null)
						continue;
					if (cachedItemIdentifier == WSResourceConfig.IdentifierType.WS_ENDPOINT_IDENTIFIER) {
						cache.updateCacheLifeTimeOfItems(resourceConfig.getWsIdentifier(), lifetimeOfCachedItems);
					} else if (resourceConfig.getInodeNumber() != null
						&& cachedItemIdentifier == WSResourceConfig.IdentifierType.INODE_NUMBER_IDENTIFIER) {
						cache.updateCacheLifeTimeOfItems(resourceConfig.getInodeNumber(), lifetimeOfCachedItems);
					} else {
						for (String rnsPath : resourceConfig.getRnsPaths()) {
							cache.updateCacheLifeTimeOfItems(rnsPath, lifetimeOfCachedItems);
						}
					}
				}
			} catch (Exception ex) {
				if (_logger.isDebugEnabled())
					_logger.debug("Could not update the lifetime of some cached items", ex);
			}
		}
	}

	public static void clearCache(Class<?> typeOfItem)
	{
		if (CacheConfigurer.isCachingEnabled()) {
			try {
				Collection<CommonCache> cacheList = CacheConfigurer.getCaches();
				for (CommonCache cache : cacheList) {
					if (cache.itemTypeMatches(typeOfItem)) {
						cache.invalidateEntireCache();
					}
				}
			} catch (Exception ex) {
				if (_logger.isDebugEnabled())
					_logger.debug("Could not remove cached item", ex);
			}
		}
	}

	public static void resetCachingForContainer(EndpointReferenceType epr)
	{
		String containerId = CacheUtils.getContainerId(epr);
		if (containerId == null) {
			_logger.debug("nothing has been removed from the cache as container ID has not been found in the epr");
		} else {
			resetCachingForContainer(containerId);
		}
	}

	public static void resetCachingForContainer(String containerId)
	{

		_logger.debug("going to clear all cache information and resources for container: " + containerId);
		NotificationBrokerDirectory.removeBrokerForContainer(containerId);

		CacheConfigurer.getCaches();
		ResourceConfigCache configCache = null;
		Collection<CommonCache> cacheList = CacheConfigurer.getCaches();
		for (CommonCache cache : cacheList) {
			if (cache instanceof ResourceConfigCache) {
				configCache = (ResourceConfigCache) cache;
				break;
			}
		}

		if (configCache != null) {
			Collection<WSResourceConfig> configList = configCache.getAllConfigsForContainer(containerId);
			for (WSResourceConfig config : configList) {
				removeRelevantStoredItems(config);
				URI wsIdentifier = config.getWsIdentifier();
				Collection<String> rnsPaths = config.getRnsPaths();
				if (wsIdentifier != null) {
					configCache.invalidateCachedItem(wsIdentifier, null);
				} else if (rnsPaths != null && !rnsPaths.isEmpty()) {
					for (String rnsPath : rnsPaths) {
						configCache.invalidateCachedItem(rnsPath, null);
					}
				}
				String rnsPath = config.getRnsPath();
				if (rnsPath != null) {
					_logger.debug("removed all cached item for the resource on path: " + rnsPath);
				} else if (wsIdentifier != null) {
					_logger.debug("removed all cached item for the resource with id: " + wsIdentifier);
				}

				SubscriptionDirectory.invalidateSubscription(config);
			}
		} else {
			_logger.debug("could not remove items for container as resource config cache is not configured");
		}
	}

	public static void resetCachingSystem()
	{
		try {
			CacheConfigurer.resetCaches();
			NotificationBrokerDirectory.clearDirectory();
			SubscriptionDirectory.clearDirectory();
			// flush any subscriptions that are still pending.
			Subscriber.getInstance().flushPendingSubscriptions();
			// flush all tracking information on credentials.
			ClientCredentialTracker.flushEntireTracker();

			VcgrSslSocketFactory.closeIdleConnections();

			// try {
			// // drop any connections that are established to avoid keeping session alive with wrong creds.
			// HttpConnectionManager connMgr = CommonsHTTPSender.getConnectionManager();
			// if (connMgr != null) {
			// // we close idle with an idle timeout of 0, which should mean everyone, even active connections.
			// connMgr.closeIdleConnections(0);
			// }
			//
			// } catch (Throwable t) {
			// if (_logger.isTraceEnabled())
			// _logger.debug("screwup from closing idle connections", t);
			// }
		} catch (Exception ex) {
			_logger.error("Alarm: couldn't reset the caching system after a failure. " + "To avoid seeing, possibly, stale contents, "
				+ "restart the grid client: " + ex.getMessage());
		}
	}

	private static void removeRelevantStoredItems(WSResourceConfig resourceConfig)
	{
		if (CacheConfigurer.isCachingEnabled()) {
			try {
				Collection<CommonCache> cacheList = CacheConfigurer.getCaches();
				for (CommonCache cache : cacheList) {
					if (cache instanceof ResourceConfigCache) {
						continue;
					}
					IdentifierType cachedItemIdentifier = cache.getCachedItemIdentifier();
					URI wsIdentifier = resourceConfig.getWsIdentifier();
					Collection<String> rnsPaths = resourceConfig.getRnsPaths();
					if (cachedItemIdentifier == null) {
						continue;
					}
					if (cachedItemIdentifier == WSResourceConfig.IdentifierType.WS_ENDPOINT_IDENTIFIER && wsIdentifier != null) {
						cache.invalidateCachedItem(wsIdentifier);
					} else if (cachedItemIdentifier == WSResourceConfig.IdentifierType.RNS_PATH_IDENTIFIER
						&& (rnsPaths != null && !rnsPaths.isEmpty())) {
						for (String rnsPath : rnsPaths) {
							cache.invalidateCachedItem(rnsPath, null);
						}
					}
				}
			} catch (Exception ex) {
				if (_logger.isDebugEnabled()) {
					_logger.debug("Could not remove items from cache", ex);
				}
			}
		}
	}

	private static CommonCache findCacheForObject(Object target, Object cacheKey, Object value)
	{
		return findCacheForObject(target, cacheKey, value.getClass());
	}

	private static CommonCache findCacheForObject(Object target, Object cacheKey, Class<?> typeOfItem)
	{
		Collection<CommonCache> cacheList = CacheConfigurer.getCaches();
		for (CommonCache cache : cacheList) {
			if (target == null && cache.supportRetrievalWithoutTarget()) {
				if (cache.isRelevent(cacheKey, typeOfItem)) {
					if (_logger.isTraceEnabled())
						_logger.trace("null target found good cache...  " + cache.getClass().getCanonicalName());
					return cache;
				}
			} else if (target != null && !cache.supportRetrievalWithoutTarget()) {
				if (cache.isRelevent(cacheKey, target, typeOfItem)) {
					if (_logger.isTraceEnabled())
						_logger.trace("non-null target found good cache...  " + cache.getClass().getCanonicalName());
					return cache;
				}
			}
		}
		if (_logger.isTraceEnabled())
			_logger.trace("could not find an appropriate cache for key '" + cacheKey.toString() + "' type " + typeOfItem.getCanonicalName()
				+ ", coming from: " + ProgramTools.showLastFewOnStack(20));
		return null;
	}
}
