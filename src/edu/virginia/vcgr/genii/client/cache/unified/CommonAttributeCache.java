package edu.virginia.vcgr.genii.client.cache.unified;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig.IdentifierType;
import edu.virginia.vcgr.genii.client.fuse.DirectoryManager;
import edu.virginia.vcgr.genii.client.naming.WSName;

/*
 * This class is used to extract common behavior of all caches that saves attributes 
 * in a single location.
 * */
public abstract class CommonAttributeCache  extends CommonCache {

	public CommonAttributeCache(int priorityLevel, int capacity, long cacheLifeTime, boolean monitoingEnabled) {
		super(priorityLevel, capacity, cacheLifeTime, monitoingEnabled);
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
	public boolean targetTypeMatches(Object target) {
		if (target instanceof EndpointReferenceType) {
			WSName name = new WSName((EndpointReferenceType) target);
			if (name.isValidWSName()) return true;
		} else if (target instanceof WSName) {
			if (((WSName)target).isValidWSName()) return true;
		} else if (target instanceof URI) {
			return true;
		}
		return false;	
	}

	@Override
	public boolean itemTypeMatches(Class<?> itemType) {
		return itemType.equals(MessageElement.class);
	}

	@Override
	public IdentifierType getCachedItemIdentifier() {
		return WSResourceConfig.IdentifierType.WS_ENDPOINT_IDENTIFIER;
	}
	
	protected String getEPI(Object target) {
		return getEndpointIdentifierURI(target).toString();
	}
	
	protected URI getEndpointIdentifierURI(Object target) {
		if (target instanceof EndpointReferenceType) {
			EndpointReferenceType epr = (EndpointReferenceType) target;
			return new WSName(epr).getEndpointIdentifier();
		} else if (target instanceof WSName) {
			return ((WSName) target).getEndpointIdentifier();
		}
		return (URI) target;
	}
	
	/*
	 * Since we only subscribe to RNS, not to byteIOs, byteIO attribute changes are propagated through subscription
	 * on container directories. As a result, the lifetime of a byteIO attribute depends on subscription on parent RNS
	 * directory. Meanwhile, the lifetime of an RNS directory attribute depends subscription on the RNS itself and 
	 * on the parent directory as we propagate RNS attributes updates through parent directories too. This is done to
	 * avoid making RPCs for attributes of RNS directories that lie on some directory user has traversed, but the not 
	 * the RNS directories themselves. 
	 * */
	protected long getCacheLifeTime(URI endpointIdentifierURI) {
		WSResourceConfig resourceConfig = (WSResourceConfig) CacheManager.getItemFromCache(
				endpointIdentifierURI, WSResourceConfig.class);
		if (resourceConfig == null) return cacheLifeTime;
		
		long attributeLifeTime = cacheLifeTime;
		if (resourceConfig.isDirectory()) {
			attributeLifeTime = Math.max(attributeLifeTime, resourceConfig.getMillisecondTimeLeftToCallbackExpiry());
		} 
		String currentPath = resourceConfig.getRnsPath();
		if (currentPath == null) return attributeLifeTime;
		
		String parentPath = DirectoryManager.getParentPath(currentPath);
		WSResourceConfig parentConfig = (WSResourceConfig) CacheManager.getItemFromCache(
				parentPath, WSResourceConfig.class);

		//An RNS can propagate entry attribute changes only when both of the resources are in the same container.
		if (parentConfig == null || !parentConfig.getContainerId().equals(resourceConfig.getContainerId())) {  
			return attributeLifeTime;
		}
		return Math.max(attributeLifeTime, parentConfig.getMillisecondTimeLeftToCallbackExpiry());
	}
	
	protected Collection<String> getCacheKeysForLifetimeUpdateRequest(URI commonIdentifier) {
		
		WSResourceConfig commonConfig = (WSResourceConfig) CacheManager.getItemFromCache(
				commonIdentifier, WSResourceConfig.class);
		if (commonConfig == null) return Collections.emptyList();
		
		if (!commonConfig.isDirectory()) return Collections.singleton(commonIdentifier.toString());
		
		Collection<String> cacheKeys = new ArrayList<String>();
		cacheKeys.add(commonIdentifier.toString());
		String containerIdOfBaseResource = commonConfig.getContainerId();
		
		for (String rnsPath : commonConfig.getRnsPaths()) {
			String childPaths = DirectoryManager.getPathForDirectoryEntry(rnsPath, "[^//]+");
			
			@SuppressWarnings("unchecked")
			Map<String, WSResourceConfig> matchingChildConfigs = 
				CacheManager.getMatchingItemsWithKeys(childPaths, WSResourceConfig.class);
			
			if (matchingChildConfigs != null) {
				for (Map.Entry<String, WSResourceConfig> entry : matchingChildConfigs.entrySet()) {
					String uriString = entry.getKey();
					WSResourceConfig childConfig = entry.getValue();
					if (containerIdOfBaseResource.equals(childConfig.getContainerId())) {
						cacheKeys.add(uriString);
					}
				}
			}
		}
		_logger.trace("number of entries selected for update: " + cacheKeys.size());
		return cacheKeys;
	}
}
