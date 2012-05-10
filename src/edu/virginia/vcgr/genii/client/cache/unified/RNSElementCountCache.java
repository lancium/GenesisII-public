package edu.virginia.vcgr.genii.client.cache.unified;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rp.DefaultSingleResourcePropertyTranslator;
import edu.virginia.vcgr.genii.client.rp.SingleResourcePropertyTranslator;

public class RNSElementCountCache extends CommonAttributeCache {

	private SingleResourcePropertyTranslator translator;
	private TimedOutLRUCache<String, Integer> cache;

	public RNSElementCountCache(int priorityLevel, int capacity, long cacheLifeTime, boolean monitoingEnabled) {
		super(priorityLevel, capacity, cacheLifeTime, monitoingEnabled);
		
		cache = new TimedOutLRUCache<String, Integer>(capacity, cacheLifeTime);
		translator = new DefaultSingleResourcePropertyTranslator();
	}

	@Override
	public Object getItem(Object cacheKey, Object target) {
		String EPI = getEPI(target);
		Integer elementCount = cache.get(EPI);
		if (elementCount == null) return null;
		return new MessageElement(RNSConstants.ELEMENT_COUNT_QNAME, elementCount);
	}

	@Override
	public void putItem(Object cacheKey, Object target, Object value) throws Exception {
		URI wsEndpointIdenfierURI = getEndpointIdentifierURI(target);
		String EPI = wsEndpointIdenfierURI.toString();
		long lifetime = getCacheLifeTime(wsEndpointIdenfierURI);
		if (lifetime <= 0) return;
		MessageElement element = (MessageElement) value;
		int elementCount = translator.deserialize(Integer.class, element);
		cache.put(EPI, elementCount, lifetime);
	}

	@Override
	public void invalidateCachedItem(Object target) {
		String EPI = getEPI(target);
		cache.remove(EPI);
	}

	@Override
	public void invalidateCachedItem(Object cacheKey, Object target) {
		invalidateCachedItem(target);
	}

	@Override
	public void invalidateEntireCache() {
		cache.clear();
	}

	@Override
	public boolean cacheKeyMatches(Object cacheKey) {
		if (cacheKey instanceof QName) {
			QName qName = (QName) cacheKey;
			return (qName.equals(RNSConstants.ELEMENT_COUNT_QNAME));
		}
		return false;
	}
	
	@Override
	protected long getCacheLifeTime(URI endpointIdentifierURI) {
		WSResourceConfig resourceConfig = 
			(WSResourceConfig) CacheManager.getItemFromCache(
					endpointIdentifierURI, WSResourceConfig.class);
		if (resourceConfig == null) return cacheLifeTime;
		if (resourceConfig.isCacheAccessBlocked()) return 0;
		return Math.max(cacheLifeTime, 
				resourceConfig.getMillisecondTimeLeftToCallbackExpiry());
	}

	@Override
	public void updateCacheLifeTimeOfItems(Object commonIdentifierForItems, 
			long newCacheLifeTime) {
		URI wsIdentifier = (URI) commonIdentifierForItems;
		String EPI = wsIdentifier.toString();
		Integer elementCount = cache.get(EPI);
		if (elementCount != null) {
			cache.put(EPI, elementCount, newCacheLifeTime);
		}
	}
}
