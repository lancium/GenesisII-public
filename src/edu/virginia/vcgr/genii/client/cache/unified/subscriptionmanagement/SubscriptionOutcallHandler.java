package edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cache.ResourceAccessMonitor;
import edu.virginia.vcgr.genii.client.cache.unified.CacheManager;
import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.wsrf.wsn.notification.LightweightNotificationServer;
import edu.virginia.vcgr.genii.notification.broker.EnhancedNotificationBrokerPortType;
import edu.virginia.vcgr.genii.notification.broker.IndirectSubscriptionEntryType;
import edu.virginia.vcgr.genii.notification.broker.IndirectSubscriptionType;
import edu.virginia.vcgr.genii.notification.broker.SubscriptionFailedFaultType;

/*
 * This is the class for creating subscriptions for cache management. This works by running two cooperating
 * threads. The first is responsible for sampling the subscription requests and accepting or filtering them 
 * on the basis of rate of subscription requests. The second is responsible for making subscribe out-calls 
 * to the resource containers.
 * */
class SubscriptionOutcallHandler extends Thread {
	
	private static Log _logger = LogFactory.getLog(SubscriptionOutcallHandler.class);

	// This is not the correct solution. We have to adjust the sampling frequency based on
	// the expected round-trip time for an RPC. 
	private static final long SAMPLING_INTERVAL_FOR_SUBCRIPTION = 10 * 1000L; // ten seconds
	
	private static final int TOLERABLE_BRANCHING_THERESHOLD = 5;
	private static final int TOLERABLE_REQUEST_COUNT_THRESHOLD = 10;
	
	// Indicates whether all subscription requests are accepted or a rate control mechanism 
	// is employed to avoid too many subscriptions creation on unusual (e.g. find or ls -lR) cases. 
	private boolean controlledRateMode;
	
	private LinkedBlockingQueue<PendingSubscription> requestedSubscriptionQueue;
	private LinkedBlockingQueue<PendingSubscription> scheduledSubscriptionQueue;
	private ICallingContext callingContext;
	private EndpointReferenceType localEndpoint;

	public SubscriptionOutcallHandler(LinkedBlockingQueue<PendingSubscription> queue, 
			LightweightNotificationServer notificationServer, boolean controlledRateMode) {
		this.requestedSubscriptionQueue = queue;
		this.scheduledSubscriptionQueue = new LinkedBlockingQueue<PendingSubscription>();
		this.controlledRateMode = controlledRateMode;
		try {
			this.localEndpoint = notificationServer.getEPR();
		} catch (IOException ex) {
			_logger.warn("Could not retrieve the local endpoint referenct.", ex);
		}
		try {
			this.callingContext = ContextManager.getCurrentContext(true);
		} catch (Exception ex) {
			_logger.warn("Could not collect calling context information.", ex);
		}
	}
	
	@Override
	public void run() {
		
		// Every cache management related thread that load or store information from the Cache should have 
		// unaccounted access to both CachedManager and RPCs to avoid getting mingled with Cache access and 
		// RPCs initiated by some user action. This is important to provide accurate statistics on per container
		// resource usage.
		ResourceAccessMonitor.getUnaccountedAccessRight();
		
		new SubscriptionRequestSampler().start();
		while (true) {
			try {
				PendingSubscription subscriptionRequest = scheduledSubscriptionQueue.take();
				createSubscription(subscriptionRequest);
			} catch (InterruptedException e) {
				_logger.debug("interrupted while creating subscriptions");
			}
		}
	}

	private void createSubscription(PendingSubscription subscriptionRequest) {
		try {
			EndpointReferenceType newsSource = subscriptionRequest.getNewsSource();
			
			if (!SubscriptionDirectory.isResourceAlreadySubscribed(newsSource)) {
				
				Closeable assumedContext = ContextManager.temporarilyAssumeContext(callingContext);
				
				NotificationBrokerWrapper brokerWrapper = 
					NotificationBrokerDirectory.getNotificationBrokerForEndpoint(newsSource, localEndpoint);
				if (brokerWrapper == null) {
					_logger.debug("there is no notification-broker for the container; cannot subscribe.");
					return;
				}
				if (!brokerWrapper.isBrokerModeVerified()) {
					brokerWrapper.testBrokerMode();
				}
				long subscriptionDuration = SubscriptionDirectory.SUBSCRIBPTION_TIMEOUT_INTERVAL;
				long remainingLifeTimeOfBroker = brokerWrapper.getBrokerRemainingLifeTime();
				subscriptionDuration = Math.min(subscriptionDuration, remainingLifeTimeOfBroker);
				
				IndirectSubscriptionType request = new IndirectSubscriptionType(newsSource, subscriptionDuration);
				EnhancedNotificationBrokerPortType brokerPortType = brokerWrapper.getBrokerPortType();
				IndirectSubscriptionEntryType[] response = brokerPortType.createIndirectSubscriptions(request);

				Date subscriptionExpiryTime = new Date(System.currentTimeMillis() + subscriptionDuration);
				SubscriptionDirectory.notifySubscriptionCreation(newsSource, subscriptionExpiryTime, response);
				
				assumedContext.close();
			}
		} catch (SubscriptionFailedFaultType e) {
			_logger.info("resource is not subscribable");
			SubscriptionDirectory.notifySubscriptionFailure(subscriptionRequest.getNewsSource());
		} catch (Exception e) {
			_logger.debug("subscription request failed");
		}
	}

	private PendingSubscription getNextSubscriptionRequest() {
		try {
			if (requestedSubscriptionQueue.size() == 0) return null;
			return requestedSubscriptionQueue.take();
		} catch (InterruptedException e) {
			_logger.debug("interrupted while trying to retrieve subscription requests");
			return null;
		}
	}
	
	private Collection<PendingSubscription> getSubscriptionRequests() {
		if (requestedSubscriptionQueue.size() == 0) return Collections.emptyList();
		List<PendingSubscription> requests = new ArrayList<PendingSubscription>();
		requestedSubscriptionQueue.drainTo(requests);
		
		filterRequestsCorrespondingDeletedResources(requests);
		
		List<String> rnsPathStrings = getTheRNSPathStringsOfToBeSubscribedResources(requests);
		int branchesCount = countBranchesOnPathList(rnsPathStrings);
		
		if (branchesCount > TOLERABLE_BRANCHING_THERESHOLD 
				|| requests.size() > TOLERABLE_REQUEST_COUNT_THRESHOLD) {
			_logger.debug("to many subscription requests.");
			requests.clear();
		}
		return requests;
	}

	private List<String> getTheRNSPathStringsOfToBeSubscribedResources(List<PendingSubscription> requests) {
		List<String> pathStrings = new ArrayList<String>(requests.size());
		for (PendingSubscription request : requests) {
			EndpointReferenceType EPR = request.getNewsSource();
			URI wsEndpointIdentifier = new WSName(EPR).getEndpointIdentifier();
			WSResourceConfig resourceConfig = 
				(WSResourceConfig) CacheManager.getItemFromCache(
						wsEndpointIdentifier, WSResourceConfig.class);
			if (resourceConfig != null) {
				// although a single resource can be mapped to multiple RNS paths, we are
				// assuming only one of them while calculating number of branches within 
				// the list of subscription requests. This is done to reduce the chance of
				// accidental denial of subscriptions on a normal use case.
				String rnsPath = resourceConfig.getRnsPath();
				if (rnsPath != null) {
					pathStrings.add(rnsPath);
				}
			}
		}
		return pathStrings;
	}
	
	/*
	 * This is a predictive operation. We cannot ensure that the resource we are trying to subscribe hasn't been
	 * deleted by other user. However, when the deletion has been done locally, we expect there wouldn't be any
	 * resource configuration instance in the cache for the concerned news source. Based on that assumption we are
	 * filtering the subscription request. Note that, to remove a directory we have to look check the its contents
	 * first, which result in a lookup call, and naturally creates pending subscriptions. 
	 * */
	private void filterRequestsCorrespondingDeletedResources(List<PendingSubscription> requests) {
		Iterator<PendingSubscription> iterator = requests.iterator();
		while (iterator.hasNext()) {
			PendingSubscription request = iterator.next();
			EndpointReferenceType newsSource = request.getNewsSource();
			WSName wsName = new WSName(newsSource);
			if (wsName.isValidWSName()) {
				Object resourceConfig = 
					CacheManager.getItemFromCache(wsName.getEndpointIdentifier(), WSResourceConfig.class);
				if (resourceConfig == null) iterator.remove();
			}
		}
	}
	
	private int countBranchesOnPathList(List<String> rnsPathStrings) {
		String[] pathArray = new String[rnsPathStrings.size()];
		rnsPathStrings.toArray(pathArray);
		int branchCount = 1;
		int numberOfPaths = rnsPathStrings.size();
		for (int i = 0; i < numberOfPaths; i++) {
			String currentPath = pathArray[i];
			boolean enclosingPathFound = false;
			for (int j = 0; j < numberOfPaths; j++) {
				if (j == i) continue;
				String comparedPath = pathArray[j];
				if (comparedPath.contains(currentPath)) {
					enclosingPathFound = true;
					break;
				}
			}
			if (!enclosingPathFound) branchCount++;
		}
		return branchCount;
	}
	
	private class SubscriptionRequestSampler extends Thread {

		@Override
		public void run() {
			
			// Every cache management related thread that load or store information from the Cache should have 
			// unaccounted access to both CachedManager and RPCs to avoid getting mingled with Cache access and 
			// RPCs initiated by some user action. This is important to provide accurate statistics on per container
			// resource usage.
			ResourceAccessMonitor.getUnaccountedAccessRight();
			
			if (controlledRateMode) {
				while (true) {
					try {
						Thread.sleep(SAMPLING_INTERVAL_FOR_SUBCRIPTION);
					} catch (InterruptedException e) {
						_logger.debug("Subscription request sampler has been interrupted.");
					}
					Collection<PendingSubscription> requests = getSubscriptionRequests();
					for (PendingSubscription subscriptionRequest : requests) {
						try {
							scheduledSubscriptionQueue.put(subscriptionRequest);
						} catch (InterruptedException e) {
							_logger.debug("interrupted while scheduling subscription requests");
						}
					}
				}
			} else {
				while (true) {
					PendingSubscription subscriptionRequest = getNextSubscriptionRequest();
					if (subscriptionRequest != null) {
						try {
							scheduledSubscriptionQueue.put(subscriptionRequest);
						} catch (InterruptedException e) {
							_logger.debug("interrupted while scheduling subscription requests");
						}
					}
				}
			}
		}
	}
}