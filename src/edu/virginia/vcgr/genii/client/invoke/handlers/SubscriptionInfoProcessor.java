package edu.virginia.vcgr.genii.client.invoke.handlers;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSMetadataType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cache.unified.CacheConfigurer;
import edu.virginia.vcgr.genii.client.cache.unified.RNSCacheLookupHandler;
import edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement.NotificationBrokerDirectory;
import edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement.PendingRNSSubscription;
import edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement.Subscriber;
import edu.virginia.vcgr.genii.client.invoke.InvocationContext;
import edu.virginia.vcgr.genii.client.invoke.PipelineProcessor;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;

/*
 * This is the intercepter for retrieving lookup response from cache and submitting subscribe requests
 * for endPoints on which a lookup operation is invoked. 
 * */
public class SubscriptionInfoProcessor {

	/* 
	 * This intercepter method first tries to satisfy a lookup request from the cache. If it fails
	 * then let the RPC to proceed. Then it search the returned RNS entries to retrieve the address
	 * of notification-broker-factory, which is used to create a broker subscriber with pull point
	 * to retrieve updates on the the returned RNS entries, if it deemed useful. Finally, its place
	 * the current target endPoint in the pending subscription queue. 
	 * */ 
	@PipelineProcessor(portType = EnhancedRNSPortType.class)
	public LookupResponseType lookup(InvocationContext ctxt, String []names) throws Throwable {
		
		if (!CacheConfigurer.isSubscriptionEnabled()) {
			return (LookupResponseType) ctxt.proceed();
		}

		LookupResponseType resp = 
			RNSCacheLookupHandler.getCachedLookupResponse(ctxt.getTarget(), names);
		if (resp == null) {
			resp = (LookupResponseType)ctxt.proceed();
			RNSEntryResponseType []initMembers = resp.getEntryResponse();
			if (initMembers != null) {
				for (RNSEntryResponseType member : initMembers) {
					searchAndStoreNotificationBrokerFactoryAddress(member);
				}
			}
		}
		
		EndpointReferenceType target = ctxt.getTarget();
		Subscriber.getInstance().requestForSubscription(new PendingRNSSubscription(target));
		return resp;
	}
	
	private void searchAndStoreNotificationBrokerFactoryAddress(RNSEntryResponseType member) {
		RNSMetadataType metadata = member.getMetadata();
		if (metadata == null || metadata.get_any() == null) return;
		for (MessageElement element : metadata.get_any()) {
			QName qName = element.getQName();
			if (GenesisIIConstants.NOTIFICATION_BROKER_FACTORY_ADDRESS.equals(qName)) {
				NotificationBrokerDirectory.storeBrokerFactoryAddress(element);
				break;
			}
		}
	}
}
