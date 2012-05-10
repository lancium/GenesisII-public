package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig.IdentifierType;
import edu.virginia.vcgr.genii.client.naming.WSName;

public class DefaultAttributeCache extends CommonCache {

	private TimedOutLRUCache<String, Collection<MessageElement>> cache;
	
	public DefaultAttributeCache(int priorityLevel, int capacity, long cacheLifeTime, boolean monitoingEnabled) {
		super(priorityLevel, capacity, cacheLifeTime, monitoingEnabled);
		cache = new TimedOutLRUCache<String, Collection<MessageElement>>(capacity, cacheLifeTime);
	}

	@Override
	public boolean isRelevent(Object cacheKey, Class<?> typeOfItem) {
		throw new RuntimeException("this method is irrelevent to this cache");
	}

	@Override
	public boolean supportRetrievalWithoutTarget() {
		return false;
	}

	@Override
	public Object getItem(Object cacheKey, Object target) {
		String EPI = getEPI(target);
		QName propertyName = (QName) cacheKey;
		Collection<MessageElement> allProperties = cache.get(EPI);
		if (allProperties == null) return null;
		Collection<MessageElement> relevantProperties = new ArrayList<MessageElement>();
		synchronized(allProperties) {
			for (MessageElement element : allProperties) {
				if (element.getQName().equals(propertyName)) {
					relevantProperties.add(element);
				}
			}
		}
		return relevantProperties;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void putItem(Object cacheKey, Object target, Object value)
			throws Exception {
		String EPI = getEPI(target);
		Collection<MessageElement> allProperties = cache.get(EPI);
		if (allProperties == null) {
			// It is important that we use a set, not a list, as the holder
			// of attributes. Otherwise we may end up in storing the same
			// message elements multiple times if the put item method is 
			// called from different places.
			allProperties = new HashSet<MessageElement>();
		}
		synchronized (allProperties) {
			if (value instanceof MessageElement) {
				allProperties.add((MessageElement) value);
			}
			else if (value instanceof Collection) {
				allProperties.addAll((Collection<? extends MessageElement>) value);
			} else if (value instanceof MessageElement[]) {
				allProperties.addAll(Arrays.asList((MessageElement[])value));
			}
		}
		cache.put(EPI, allProperties);
	}

	@Override
	public void invalidateCachedItem(Object target) {
		String EPI = getEPI(target);
		cache.remove(EPI);
	}

	@Override
	public void invalidateCachedItem(Object cacheKey, Object target) {
		String EPI = getEPI(target);
		QName propertyName = (QName) cacheKey;
		Collection<MessageElement> allProperties = cache.get(EPI);
		if (allProperties == null) return;
		synchronized(allProperties) {
			Iterator<MessageElement> iterator = allProperties.iterator();
			while (iterator.hasNext()) {
				MessageElement element = iterator.next();
				if (propertyName.equals(element.getQName())) {
					iterator.remove();
				}
			}
		}
		cache.put(EPI, allProperties);
	}

	@Override
	public void invalidateEntireCache() {
		cache.clear();
	}

	@Override
	public boolean targetTypeMatches(Object target) {
		if (target instanceof EndpointReferenceType) {
			WSName name = new WSName((EndpointReferenceType) target);
			if (name.isValidWSName()) return true;
		} else if (target instanceof WSName) {
			if (((WSName)target).isValidWSName()) return true;
		}
		return false;
	}

	@Override
	public boolean cacheKeyMatches(Object cacheKey) {
		return (cacheKey instanceof QName);
	}

	@Override
	public boolean itemTypeMatches(Class<?> itemType) {
		return itemType.equals(MessageElement.class);
	}
	
	@Override
	public IdentifierType getCachedItemIdentifier() {
		return null;
	}
	
	@Override
	public void updateCacheLifeTimeOfItems(Object commonIdentifierForItems, 
			long newCacheLifeTime) {
		//do nothing
	}

	private String getEPI(Object target) {
		if (target instanceof EndpointReferenceType) {
			EndpointReferenceType epr = (EndpointReferenceType) target;
			return new WSName(epr).toString();
		} 
		return ((WSName) target).toString();	
	}
}
