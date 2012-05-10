package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * This is the common abstraction for all client-side caches. For an external user of cache, everything
 * looks like a CommonCache. Because of being generic, its methods some-times have more parameters than
 * needed to get elements from or put elements in a particular cache. Subclasses are supposed to override 
 * appropriate methods to indicate their method access specificities.
 * */
public abstract class CommonCache implements Comparable<CommonCache> {

	static protected Log _logger = LogFactory.getLog(CommonCache.class);

	// priority levels are used to make distinctions when there exists a default cache for 
	// a particular type of information. For example, when caching RP interface attributes
	// that are not stored by the AuthZConfig and ByteIORP caches are stored in
	// a default cache. We use priority to insure that the caches are contacted in a
	// defined order as default cache cannot justify whether or not an attribute is 
	// irrelevant to the other caches based on the cache key. Finally, caches are ordered
	// in increasing order of the priority level.
	private int priorityLevel;
	
	// This is the default lifetime for a cached item when no explicit lifetime is specified
	// within the resource configuration instance associated with the to be cached item.
	protected long cacheLifeTime;
	
	// When enabled the cache have to report the monitor about the usage of cached resource
	// before returning it to the caller. This is used to get statistics on container-client
	// interaction, then subsequently to use that statistics to manage subscriptions and 
	// polling of individual containers. The second use of this flag is to determine whether
	// or not retrieval from cache is temporarily blocked on per container basis.
	protected boolean monitoringEnabled;
	
	public CommonCache(int priorityLevel, int capacity, long cacheLifeTime, boolean monitoingEnabled) {
		this.priorityLevel = priorityLevel;
		this.cacheLifeTime = cacheLifeTime;
		this.monitoringEnabled = monitoingEnabled;
	}

	public boolean isRelevent(Object cacheKey, Object target, Class<?> typeOfItem) {
		return targetTypeMatches(target) 
			&& cacheKeyMatches(cacheKey) 
			&& itemTypeMatches(typeOfItem);
	}
	
	public boolean isRelevent(Object cacheKey, Class<?> typeOfItem) {
		return cacheKeyMatches(cacheKey) && itemTypeMatches(typeOfItem);
	}

	public boolean isMonitoringEnabled() {
		return monitoringEnabled;
	}

	public abstract boolean supportRetrievalWithoutTarget();
	
	/*
	 * Wildcard matching is useful for caches that store elements by pathnames or something
	 * similar. For example here we used it to retrieve endPoints of all RNS entries of a 
	 * parent directory.
	 * */
	public boolean supportRetrievalByWildCard() {
		return false;
	}
	
	/*
	 * Wild card matchings are used to retrieve descendants of some RNS resource. The caller supplies
	 * an RNSPath expression and gets a map of <cacheKey, cachedItem> pairs that matches the expression. 
	 * Although the search will almost always be performed by RNSPath string, the keys of the returned
	 * Map should reflect the particular cacheKeys used to store the matched items.
	 * */
	@SuppressWarnings("rawtypes")
	public Map getWildCardMatches(Object target, Object wildCardCacheKey) {
		return null;
	}
	
	public abstract Object getItem(Object cacheKey, Object target);
	
	public abstract void putItem(Object cacheKey, Object target, Object value) throws Exception;
	
	public abstract void invalidateCachedItem(Object target);
	
	public abstract void invalidateCachedItem(Object cacheKey, Object target);
	
	public abstract void invalidateEntireCache();
	
	public abstract boolean targetTypeMatches(Object target);
	
	public abstract boolean cacheKeyMatches(Object cacheKey);
	
	public abstract boolean itemTypeMatches(Class<?> itemType);

	/*
	 * This determines how the cache implementation is tied with the resource configuration
	 * object, which is used by the caller to extend or contract the cache lifetime of relevant 
	 * properties or view of any WS-resource. Furthermore, while inserting elements in the 
	 * cache this identifier is used to search resource configuration instance to check whether
	 * or not the item should have a lifetime other than the default.  
	 */
	public abstract WSResourceConfig.IdentifierType getCachedItemIdentifier();
	
	/*
	 * This is used to update the lifetime of cached item when some resource has been subscribed
	 * or un-subscribed. The different caches behaves differently on this. For example, the EPR 
	 * lookup-cache should(and does) update the lifetime of all the EPRs within the directory 
	 * represented by the commonIdentifer, but not the directory EPR itself. On the other hand, 
	 * attribute caches updates the lifetime of attributes for both the directory and its children.
	 * */
	public abstract void updateCacheLifeTimeOfItems(Object commonIdentifierForItems, long newCacheLifeTime);
	
	@Override
	public int compareTo(CommonCache other) {
		return new Integer(priorityLevel).compareTo(other.priorityLevel);
	}
}
