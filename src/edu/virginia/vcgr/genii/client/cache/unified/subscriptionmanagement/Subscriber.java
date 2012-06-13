package edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.cache.unified.CacheConfigurer;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.wsrf.wsn.notification.LightweightNotificationServer;

/*
 * This is a singleton class for collecting requests for subscription from the RPC interceptors -- kind of 
 * a facet between the subscription management module and the rest of the system. As it is a singleton, for
 * convenience, we have delegated to it the responsibility of initiating the subscription-management module too. 
 * */
public class Subscriber {

	private static Log _logger = LogFactory.getLog(Subscriber.class);

	private static Subscriber subscriber;
	
	/*
	 * This is producer-consumer type queue for managing subscription requests. The subscriber acts as a 
	 * producer while the SubscriptionOutcallHandler acts as a consumer.
	 * */
	private LinkedBlockingQueue<PendingSubscription> queue;
	
	private Subscriber() {
		
		/* We need to create an SSL server to get the notifications in a secure way. Current 
		 * HTTPServer is a makeshift implementation as I don't know the standard way of accessing 
		 * and creating SSL certificates.
		 * */ 
		LightweightNotificationServer notificationServer = LightweightNotificationServer.createStandardServer();
		ClientSideNotificationManager notificationMultiplexer = new ClientSideNotificationManager();
		notificationServer.setMultiplexer(notificationMultiplexer);
		NotificationBrokerDirectory.registerNotificationMulitplexer(notificationMultiplexer);
		
		boolean serverStartedSuccessfully = true;
		try {
			notificationServer.start();
		} catch (Exception e) {
			_logger.info("Could not start notification server. Subscriptions will not work.");
			serverStartedSuccessfully = false;
		}
		/*
		 * If we cannot start the notification server then none of the feature related to 
		 * subscription will work. However, we are not going to propagate the problem to
		 * the caller method. Instead the subscription mechanism should fail silently. This
		 * is done to keep the rest of the code unaffected by the failure of subscription
		 * system. Note that if you did not completely isolate the logic of caching from the
		 * rest of the system this would not be a good idea.
		 * */
		if (serverStartedSuccessfully) {
			queue = new LinkedBlockingQueue<PendingSubscription>();
			SubscriptionOutcallHandler handler = new SubscriptionOutcallHandler(
					queue, notificationServer, true);
			handler.start();
			new PollingFrequencyAdjuster().start();
		}
	}
	
	public static Subscriber getInstance() {
		if (subscriber == null) {
			subscriber = new Subscriber();
		}
		return subscriber;
	}
	
	public void requestForSubscription(PendingSubscription request) {
		if (!CacheConfigurer.isSubscriptionEnabled()) return;
		if (queue == null) return;
		
		EndpointReferenceType newsSource = request.getNewsSource();
		if (SubscriptionDirectory.isResourceAlreadySubscribed(newsSource) 
				|| !SubscriptionDirectory.isResourceSubscribable(newsSource) 
				|| !isSubscribableTarget(newsSource)) return;
		
		try {
			queue.put(request);
		} catch (InterruptedException e) {
			_logger.info("could not submit subcription request", e);
		}
	}
	
	private boolean isSubscribableTarget(EndpointReferenceType target) {

		// The target does not represent an implied GenesisII resource; subscription is pointless. 
		WSName wsName = new WSName(target);
		if (!wsName.isValidWSName()) return false;
		
		TypeInformation typeInfo = new TypeInformation(target);
		
		// The target does not support WS-Base Notification.
		if (!typeInfo.hasPortType(WellKnownPortTypes.NOTIFICATION_PRODUCER_PORT_TYPE)) return false;
		
		// The target is not a GenesisII resource. Even if it implements the notification producer
		// port type, we may not be able to interpret the notification messages issued by it. So to
		// remain at the safe side we don't subscribe to this target.
		if (EPRUtils.getGeniiContainerID(target) == null) return false;
		
		// We don't support notification from arbitrary path of a resource fork. In addition we 
		// should not cache information for long for many of the resource forks, e.g. fork for job 
		// working directory or jobs submitted into a queue.
		if (typeInfo.isResourceFork()) return false;
		
		return true;
	}
}
