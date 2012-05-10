package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.rp.DefaultSingleResourcePropertyTranslator;
import edu.virginia.vcgr.genii.client.rp.SingleResourcePropertyTranslator;


public class ByteIORPCache extends CommonAttributeCache {

	/*
	 * When a byteIO is blocked because of rapid write/append operations, we don't want to client to cache 
	 * attributes. However, we are replacing each get-attribute call with an aggregate call for all attributes.
	 * So if you don't cache information at least momentarily, we will miss the opportunity to utilize the 
	 * call aggregation and issue RPC for individual attributes of a blocked ByteIO. To circumvent this problem,
	 * we use a small non-zero cache-life-time interval for blocked byteIOs. 
	 * */
	private static final long BLOCKED_BYTEIO_ATTRIBUTE_LIFETIME = 1000L;
	
	private SingleResourcePropertyTranslator translator;
	
	private TimedOutLRUCache<String, Set<String>> xferAttributeCache;
	private TimedOutLRUCache<String, Long> sizeAttributeCache;
	private TimedOutLRUCache<String, Calendar> createTimeAttributeCache;
	private TimedOutLRUCache<String, Calendar> modTimeAttributeCache;
	private TimedOutLRUCache<String, Calendar> accessTimeAttributeCache;
	
	public ByteIORPCache(int priorityLevel, int capacity, long cacheLifeTime, boolean monitoingEnabled) {
		
		super(priorityLevel, capacity, cacheLifeTime, monitoingEnabled);
		
		sizeAttributeCache = new TimedOutLRUCache<String, Long>(capacity, cacheLifeTime);
		modTimeAttributeCache = new TimedOutLRUCache<String, Calendar>(capacity, cacheLifeTime);
		accessTimeAttributeCache = new TimedOutLRUCache<String, Calendar>(capacity, cacheLifeTime);
		
		// Create time cache has a very long life time for cached entries as once cached 
		// we never have to remove this information from the cache. The same is true for
		// transfer mechanism attributes cache. We could not use Long.MAX_VALUE to simulate
		// an infinite lifetime as in that case the cache implementation we are using fails
		// to safe the attributes.
		long millisecondsInDay = 24 * 60 * 60 * 1000L;
		createTimeAttributeCache = new TimedOutLRUCache<String, Calendar>(capacity, millisecondsInDay);
		xferAttributeCache = new TimedOutLRUCache<String, Set<String>>(capacity, millisecondsInDay);
		
		translator = new DefaultSingleResourcePropertyTranslator();
	}

	@Override
	public Object getItem(Object cacheKey, Object target) {
		String EPI = getEPI(target);
		QName qName = (QName) cacheKey;
		
		@SuppressWarnings("unchecked")
		Object targetProperty = getCacheForProperty(qName).get(EPI);
		
		if (targetProperty == null) return null;
		if (qName.equals(ByteIOConstants.rxferMechs) || qName.equals(ByteIOConstants.sxferMechs)) {
			@SuppressWarnings("unchecked")
			Set<String> xferMechanisms = (Set<String>) targetProperty;
			return getXferMechElements(qName, xferMechanisms);
		}		
		return new MessageElement(qName, targetProperty);
	}

	@Override
	public void putItem(Object cacheKey, Object target, Object value) throws Exception {
		
		URI wsEndpointIdenfierURI = getEndpointIdentifierURI(target);
		String EPI = wsEndpointIdenfierURI.toString();
		long lifetime = getCacheLifeTime(wsEndpointIdenfierURI);
		if (lifetime <= 0) return;
		
		QName qName = (QName) cacheKey;
		MessageElement element = (MessageElement) value;
		
		if (qName.equals(ByteIOConstants.rsize) || qName.equals(ByteIOConstants.ssize)) {
			Long fileSize = translator.deserialize(Long.class, element);
			sizeAttributeCache.put(EPI, fileSize, lifetime);
		} else if (qName.equals(ByteIOConstants.rcreatTime) || qName.equals(ByteIOConstants.screatTime)) {
			Calendar createTime = translator.deserialize(Calendar.class, element);
			createTimeAttributeCache.put(EPI, createTime);
		} else if (qName.equals(ByteIOConstants.rmodTime) || qName.equals(ByteIOConstants.smodTime)) {
			Calendar modificationTime = translator.deserialize(Calendar.class, element);
			modTimeAttributeCache.put(EPI, modificationTime, lifetime);
		} else if (qName.equals(ByteIOConstants.raccessTime) || qName.equals(ByteIOConstants.saccessTime)) {
			Calendar accessTime = translator.deserialize(Calendar.class, element);
			accessTimeAttributeCache.put(EPI, accessTime, lifetime);
		} else if (qName.equals(ByteIOConstants.rxferMechs) || qName.equals(ByteIOConstants.sxferMechs)) {
			Set<String> xferMecahnisms = xferAttributeCache.get(EPI);
			if (xferMecahnisms == null) {
				xferMecahnisms = new HashSet<String>(3);
			}
			URI newXferMechanism = translator.deserialize(URI.class, element);
			addXferMechanismAttributeInSet(xferMecahnisms, newXferMechanism);
			xferAttributeCache.put(EPI, xferMecahnisms);
		}
	}

	@Override
	public void invalidateCachedItem(Object target) {
		String EPI = getEPI(target);
		sizeAttributeCache.remove(EPI);
		modTimeAttributeCache.remove(EPI);
		accessTimeAttributeCache.remove(EPI);
		createTimeAttributeCache.remove(EPI);
		xferAttributeCache.remove(EPI);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void invalidateCachedItem(Object cacheKey, Object target) {
		String EPI = getEPI(target);
		QName qName = (QName) cacheKey;
		_logger.trace("invalidating: " + qName);
		getCacheForProperty(qName).remove(EPI);
	}

	@Override
	public void invalidateEntireCache() {
		sizeAttributeCache.clear();
		createTimeAttributeCache.clear();
		modTimeAttributeCache.clear();
		accessTimeAttributeCache.clear();
		xferAttributeCache.clear();
	}
	
	@Override
	public boolean cacheKeyMatches(Object cacheKey) {
		if (cacheKey instanceof QName) {
			QName qName = (QName) cacheKey;
			return (qName.equals(ByteIOConstants.rxferMechs) || qName.equals(ByteIOConstants.sxferMechs) 
					|| qName.equals(ByteIOConstants.rsize) || qName.equals(ByteIOConstants.ssize) 
					|| qName.equals(ByteIOConstants.rcreatTime) || qName.equals(ByteIOConstants.screatTime)
					|| qName.equals(ByteIOConstants.rmodTime) || qName.equals(ByteIOConstants.smodTime) 
					|| qName.equals(ByteIOConstants.raccessTime) || qName.equals(ByteIOConstants.saccessTime));
		}
		return false;
	}

	@Override
	public void updateCacheLifeTimeOfItems(Object commonIdentifierForItems, long newCacheLifeTime) {
		
		URI wsIdentifier = (URI) commonIdentifierForItems;
		Collection<String> epiStrings = getCacheKeysForLifetimeUpdateRequest(wsIdentifier);
		if (epiStrings == null) return;

		for (String EPI : epiStrings) {
			Long fileSize = sizeAttributeCache.get(EPI);
			if (fileSize != null) {
				sizeAttributeCache.put(EPI, fileSize, newCacheLifeTime);
			}
			Calendar modificationTime = modTimeAttributeCache.get(EPI);
			if (modificationTime != null) {
				modTimeAttributeCache.put(EPI, modificationTime, newCacheLifeTime); 
			}
			Calendar accessTime = accessTimeAttributeCache.get(EPI);
			if (accessTime != null) {
				accessTimeAttributeCache.put(EPI, accessTime, newCacheLifeTime);
			}
		}
	}
	
	@Override
	protected long getCacheLifeTime(URI endpointIdentifierURI) {
		WSResourceConfig resourceConfig = (WSResourceConfig) CacheManager.getItemFromCache(
				endpointIdentifierURI, WSResourceConfig.class);
		if (resourceConfig == null) return cacheLifeTime;
		if (resourceConfig.isCacheAccessBlocked()) return BLOCKED_BYTEIO_ATTRIBUTE_LIFETIME;
		return super.getCacheLifeTime(endpointIdentifierURI);
	}

	@SuppressWarnings("rawtypes")
	private TimedOutLRUCache getCacheForProperty(QName qName) {
		if (qName.equals(ByteIOConstants.rsize) || qName.equals(ByteIOConstants.ssize)) {
			return sizeAttributeCache;
		} else if (qName.equals(ByteIOConstants.rcreatTime) || qName.equals(ByteIOConstants.screatTime)) {
			return createTimeAttributeCache;
		} else if (qName.equals(ByteIOConstants.rmodTime) || qName.equals(ByteIOConstants.smodTime)) {
			return modTimeAttributeCache;
		} else if (qName.equals(ByteIOConstants.raccessTime) || qName.equals(ByteIOConstants.saccessTime)) {
			return accessTimeAttributeCache;
		} else if (qName.equals(ByteIOConstants.rxferMechs) || qName.equals(ByteIOConstants.sxferMechs)) {
			return xferAttributeCache;
		}
		throw new RuntimeException("could not recognize the property");
	}
	
	private void addXferMechanismAttributeInSet(Set<String> existingMechanisms, URI newMechanism) {
		if (newMechanism.equals(ByteIOConstants.TRANSFER_TYPE_MTOM_URI)) {
			existingMechanisms.add(ByteIOConstants.TRANSFER_TYPE_MTOM);
		} else if (newMechanism.equals(ByteIOConstants.TRANSFER_TYPE_DIME_URI)) {
			existingMechanisms.add(ByteIOConstants.TRANSFER_TYPE_DIME);
		} else if (newMechanism.equals(ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI)) {
			existingMechanisms.add(ByteIOConstants.TRANSFER_TYPE_SIMPLE);
		} else {
			throw new RuntimeException("unrecognized transfer type");
		}
	}
	
	private Collection<MessageElement> getXferMechElements(QName xferAttributeName, Set<String> xferMechasims) {
		if (xferMechasims == null || xferMechasims.isEmpty()) return null;
		List<MessageElement> elementList = new ArrayList<MessageElement>(3);
		if (xferMechasims.contains(ByteIOConstants.TRANSFER_TYPE_MTOM)) {
			elementList.add(new MessageElement(xferAttributeName, ByteIOConstants.TRANSFER_TYPE_MTOM_URI));
		}
		if (xferMechasims.contains(ByteIOConstants.TRANSFER_TYPE_DIME)) {
			elementList.add(new MessageElement(xferAttributeName, ByteIOConstants.TRANSFER_TYPE_DIME_URI));
		}
		if (xferMechasims.contains(ByteIOConstants.TRANSFER_TYPE_SIMPLE)) {
			elementList.add(new MessageElement(xferAttributeName, ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
		}
		return elementList;
	}
}
