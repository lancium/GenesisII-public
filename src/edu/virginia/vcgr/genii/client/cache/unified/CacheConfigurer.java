package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * CacheConfigurer holds the configuration properties (e.g. timeout, size) of all elements of the 
 * entire client-side cache module. There is no reference of any cache, or related classes elsewhere.
 * Therefore, this is also the only place for enabling or disabling the client-side cache or its
 * component properties.
 * */
public class CacheConfigurer
{

	static private Log _logger = LogFactory.getLog(CacheConfigurer.class);

	public static final long DEFAULT_VALIDITY_PERIOD_FOR_CACHED_CONTENT = 30 * 1000L; // thirty
																						// seconds

	private static boolean CACHING_ENABLED = true;
	private static boolean SUBSCRIPTION_BASED_CACHING_ENABLED = true;

	private static List<CommonCache> LIST_OF_CACHES;

	private static void initializeCaches()
	{
		if (LIST_OF_CACHES == null) {
			LIST_OF_CACHES = new ArrayList<CommonCache>();
		}

		LIST_OF_CACHES.add(new ByteIORPCache(0, 2500, 30 * 1000L, false));
		LIST_OF_CACHES.add(new AuthZConfigCache(0, 2500, 30 * 1000L, false));

		LIST_OF_CACHES.add(new RNSElementCountCache(0, 500, 30 * 1000L, false));
		LIST_OF_CACHES.add(new FuseDirCache(0, 500, 30 * 1000L, false));

		// Monitoring is enabled for only RNSLookup and ResourceConfig caches as these two
		// are sufficient to collect per-container usage statistics.
		LIST_OF_CACHES.add(new RNSLookupCache(0, 500, 30 * 1000L, true));

		// Identifiers linker, we need to keep info in the cache as long as possible and
		// as much as possible too. Furthermore, as this cache gets used the most therefore
		// it may be better to always use memory-based cache implementation for this cache
		// even if we change the implementation of other caches in future.
		LIST_OF_CACHES.add(new ResourceConfigCache(0, 2500, 30 * 60 * 1000L, true));

		Collections.sort(LIST_OF_CACHES);
	}

	static {
		initializeCaches();
	}

	private static Collection<CacheableItemsGenerator> CACHEABLE_ITEMS_GENERATORS;

	private static void initializeGenerators()
	{
		if (CACHEABLE_ITEMS_GENERATORS == null) {
			CACHEABLE_ITEMS_GENERATORS = new ArrayList<CacheableItemsGenerator>();
		}
		CACHEABLE_ITEMS_GENERATORS.add(new RNSEntryResponseTranslator());
	}

	static {
		initializeGenerators();
	}

	public static boolean isCachingEnabled()
	{
		return CACHING_ENABLED;
	}

	public static boolean isSubscriptionEnabled()
	{
		return (CACHING_ENABLED && SUBSCRIPTION_BASED_CACHING_ENABLED);
	}

	public static void disableCaching()
	{
		CACHING_ENABLED = false;
		SUBSCRIPTION_BASED_CACHING_ENABLED = false;
		LIST_OF_CACHES.clear();
		CACHEABLE_ITEMS_GENERATORS.clear();
		if (_logger.isDebugEnabled())
			_logger.debug("Caching has been disabled");
	}

	public static void disableSubscriptionBasedCaching()
	{
		SUBSCRIPTION_BASED_CACHING_ENABLED = false;
		_logger.info("Subscription based caching has been disabled");
	}

	public static void setSubscriptionBasedCaching(boolean state)
	{
		SUBSCRIPTION_BASED_CACHING_ENABLED = state;
	}

	public static void enableCaching()
	{
		initializeCaches();
		initializeGenerators();
		CACHING_ENABLED = true;
		SUBSCRIPTION_BASED_CACHING_ENABLED = true;
		if (_logger.isDebugEnabled())
			_logger.debug("Caching has been enabled");
	}

	public static Collection<CommonCache> getCaches()
	{
		return Collections.unmodifiableCollection(LIST_OF_CACHES);
	}

	public static Collection<CacheableItemsGenerator> getGenerators()
	{
		return Collections.unmodifiableCollection(CACHEABLE_ITEMS_GENERATORS);
	}

	public static void resetCaches()
	{
		try {
			boolean subscriptionState = isSubscriptionEnabled();

			disableCaching();
			enableCaching();

			setSubscriptionBasedCaching(subscriptionState);

		} catch (Exception ex) {
			_logger.info("Exception occurred while resetting the cache management system " + ex.getMessage());
		}
	}
}
