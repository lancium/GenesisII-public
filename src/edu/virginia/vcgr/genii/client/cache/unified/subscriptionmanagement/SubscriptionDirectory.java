package edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cache.unified.CacheManager;
import edu.virginia.vcgr.genii.client.cache.unified.CacheUtils;
import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.container.notification.NotificationBrokerConstants;
import edu.virginia.vcgr.genii.notification.broker.IndirectSubscriptionEntryType;

/*
 * This tracks all the subscriptions the client has created in different resource containers. If the client
 * lost any resource configuration object from cache because of capacity limitation, it queries this directory
 * to determine the lifetime of cached contents. Furthermore, this protects the system from creating multiple
 * subscriptions for the same resource. 
 * */
public class SubscriptionDirectory {

	// TODO: This should come from some properties file.
	public static final long SUBSCRIBPTION_TIMEOUT_INTERVAL = 30 * 60 * 1000L; // thirty minutes
	
	static final Map<String, Date> SUBSCRIBED_RESOURCE_TO_SUBSCRIPTION_END_TIME_MAP = 
		new ConcurrentHashMap<String, Date>();
	
	static final Map<String, SubscriptionReferenceList> SUBSCRIBED_RESOURCE_TO_SUBSCRIPTION_REFERENCE_MAP = 
		new ConcurrentHashMap<String, SubscriptionReferenceList>();
	
	// This tracks the set of resources for which subscription requests have been failed previously. We adopt 
	// the pessimistic approach that if the subscription request fails for a resource once it is non-subscribable.  
	static final Set<String> UNSUBSCRIBABLE_RESOURCES = new HashSet<String>();
	
	public static boolean isResourceAlreadySubscribed(EndpointReferenceType EPR) {
		WSName wsName = new WSName(EPR);
		if (!wsName.isValidWSName()) return false;
		String EPI = wsName.getEndpointIdentifier().toString();
		return isResourceAlreadySubscribed(EPI);
	}

	public static boolean isResourceAlreadySubscribed(String EPI) {
		if (SUBSCRIBED_RESOURCE_TO_SUBSCRIPTION_END_TIME_MAP.containsKey(EPI)) {
			Date subscriptionEndsAt = SUBSCRIBED_RESOURCE_TO_SUBSCRIPTION_END_TIME_MAP.get(EPI);
			if (System.currentTimeMillis() >= subscriptionEndsAt.getTime()) {
				SUBSCRIBED_RESOURCE_TO_SUBSCRIPTION_END_TIME_MAP.remove(EPI);
				return false;
			}
			return true;
		}
		return false;
	}
	
	/* When we have subscribed a resource we can expect to receive notifications for any update 
	 * performed on that resource as long as our subscription dosn't expire. This means, we 
	 * can safely cache any information related to the concerned resource until that time --
	 * this assumes that we are always able to interpret the notification message correctly.
	 * So this method update the cache configuration of items related to the subscribed resource
	 * along with updating the subscription directory.
	 * */
	public static void notifySubscriptionCreation(EndpointReferenceType EPR, Date subscriptionEndTime, 
			IndirectSubscriptionEntryType[] subscriptionResponse) {
		WSName wsName = new WSName(EPR);
		if (!wsName.isValidWSName()) return;
		URI endpointIdentifier = wsName.getEndpointIdentifier();
		String EPI = endpointIdentifier.toString();
		SUBSCRIBED_RESOURCE_TO_SUBSCRIPTION_END_TIME_MAP.put(EPI, subscriptionEndTime);

		SubscriptionReferenceList referenceList = createSubscriptionReferenceList(subscriptionResponse);
		SUBSCRIBED_RESOURCE_TO_SUBSCRIPTION_REFERENCE_MAP.put(EPI, referenceList);

		WSResourceConfig resourceConfig = 
			(WSResourceConfig) CacheManager.getItemFromCache(endpointIdentifier, WSResourceConfig.class);
		if (resourceConfig == null) {
			resourceConfig = new WSResourceConfig(wsName);
		}
		resourceConfig.setHasRegisteredCallback(true);
		resourceConfig.setCallbackExpiryTime(subscriptionEndTime);
		CacheManager.putItemInCache(endpointIdentifier, resourceConfig);

		CacheManager.updateCacheLifeTimeOfRelevantStoredItems(resourceConfig);
	}
	
	public static void notifySubscriptionFailure(EndpointReferenceType EPR) {
		String EPI = CacheUtils.getEPIString(EPR);
		UNSUBSCRIBABLE_RESOURCES.add(EPI);
	}
	
	public static boolean isResourceSubscribable(EndpointReferenceType EPR) {
		String EPI = CacheUtils.getEPIString(EPR);
		return !UNSUBSCRIBABLE_RESOURCES.contains(EPI);
	}
	
	public static Date getSubscriptionTimeoutTime(String endpointIdentifier) {
		return SUBSCRIBED_RESOURCE_TO_SUBSCRIPTION_END_TIME_MAP.get(endpointIdentifier);
	}
	
	public static void clearDirectory() {
		SUBSCRIBED_RESOURCE_TO_SUBSCRIPTION_END_TIME_MAP.clear();
		SUBSCRIBED_RESOURCE_TO_SUBSCRIPTION_REFERENCE_MAP.clear();
	}
	
	private static SubscriptionReferenceList createSubscriptionReferenceList(IndirectSubscriptionEntryType[] response) {
		
		SubscriptionReferenceList referenceList = new SubscriptionReferenceList();
		
		for (IndirectSubscriptionEntryType entry : response) {
			String subscriptionEPI = CacheUtils.getEPIString(entry.getSubscriptionReference());
			for (MessageElement element : entry.get_any()) {
				QName qName = element.getQName();
				if (qName.equals(NotificationBrokerConstants.INDIRECT_SUBSCRIPTION_TYPE)) {
					String subscriptionType = element.getValue();
					if (subscriptionType.equals(NotificationBrokerConstants.RNS_CONTENT_CHANGE_SUBSCRIPTION)) {
						referenceList.setRnsContentChangeReference(subscriptionEPI); 
					} else if (subscriptionType.equals(
							NotificationBrokerConstants.BYTEIO_ATTRIBUTE_CHANGE_SUBSCRIPTION)) {
						referenceList.setByteIOAttributesUpdateReference(subscriptionEPI);
					} else if (subscriptionType.equals(
							NotificationBrokerConstants.RESOURCE_AUTHORIZATION_CHANGE_SUBSCRIPTION)) {
						referenceList.setPermissionsBitsChangeReference(subscriptionEPI);
					}
					break;
				}
			}
		}
		return referenceList;
	}
}
