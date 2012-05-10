package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.Collection;

/* This is the interface class for cross-propagation of cache-able
 * items. This is very useful when we can prefetch items in a RPC
 * call that are likely to be used in subsequent calls. For example,
 * we can prefetch attributes of RNS entries during an RNS lookup
 * operation. The user of this interface is supposed to provide an
 * implementation that translate/retrieve the prefetched items from 
 * the result of the current RPC call. Same logic applies when we
 * receive a notification containing some cache-able information 
 * back from a container. The cache manager will ensure that the 
 * items are propagated to appropriate caches if the relevant method 
 * in manager interface is invoked.*/
public interface CacheableItemsGenerator {
 
	boolean isSupported(Class<?>... argumentTypes);
	
	Collection<CacheableItem> generateItems(Object... originalItems);
}
