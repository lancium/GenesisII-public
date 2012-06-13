package edu.virginia.vcgr.genii.client.cache;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cache.unified.CacheConfigurer;
import edu.virginia.vcgr.genii.client.cache.unified.CacheUtils;
import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig;
import edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement.NotificationBrokerDirectory;
import edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement.NotificationBrokerWrapper;
import edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement.PollingIntervalDirectory;

/*
 * This track the last time when a resource in individual containers was used. This information is 
 * currently been used to adjust the polling interval. It can also be used to destroy the subscriptions
 * and notification-brokers when the resources have not been accessed for a long time. Although this
 * class can generate complicated resource usage statistics like rate of usage and burstiness, we don't
 * need them to adjust polling interval or destroy registered callbacks. However, later if we want to 
 * consider reducing the polling interval bellow the default value then we'll need to collect and use 
 * those statistics.  
 * */
public class ResourceAccessMonitor {
	
	private static Map<String, Date> LAST_CONTAINER_ACCESS_TIME = new ConcurrentHashMap<String, Date>();
	
	private static ThreadLocal<Boolean> hasUnaccountedAccess = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};
	
	// Assigns unaccounted resource access right -- via both RPC and cache access -- to the current thread.
	public static void getUnaccountedAccessRight() {
		ResourceAccessMonitor.hasUnaccountedAccess.set(true);
	}
	
	public static Date getLastContainerUsageTime(String containerId) {
		return LAST_CONTAINER_ACCESS_TIME.get(containerId);
	}
	
	synchronized public static Set<String> getIdsOfContainersInUse() {
		return new HashSet<String>(LAST_CONTAINER_ACCESS_TIME.keySet());
	}
	
	synchronized public static void reportResourceUsage(Object resource) {
		
		boolean hasUnaccountedAccess = ResourceAccessMonitor.hasUnaccountedAccess.get();
		if (hasUnaccountedAccess) return;
		String containerId = getContainerId(resource);
		if (containerId != null) {
			LAST_CONTAINER_ACCESS_TIME.put(containerId, new Date());
		}
	}
	
	/*
	 * This determine whether a cached resource should be passed to the caller for use or the system should
	 * issue an RPC for the resource to get a fresh copy.
	 * */
	public static boolean isCachedContentGuaranteedToBeFresh(Object targetOrCachedResource) {
		
		// Represent some cache-management related thread. For such a thread cached-resource is always accessible
		// as it itself takes part in ensuring freshness.
		boolean hasUnaccountedAccess = ResourceAccessMonitor.hasUnaccountedAccess.get();
		if (hasUnaccountedAccess) return true;
		
		String containerId = getContainerId(targetOrCachedResource);
		
		// When no container ID can be derived from the cached resource we assume the optimistic
		// approach that the attribute (which in this case most likely content type) get just fetched
		// by making RPC on EPR for which cache access was denied previously a moment ago or 
		// corresponding to a container for which direct notification works. 
		if (containerId == null) return true;
		
		NotificationBrokerWrapper broker = 
			NotificationBrokerDirectory.getExistingRepresentativeBroker(containerId);
		
		// If there is no broker running for the container, we can assume that subscription-based caching
		// is turned off or hasn't been started yet, consequently cached resources are prefetched or 
		// piggy-backed timeout based contents, therefore we can declare them as fresh.
		if (broker == null) return true;
		
		// Container is directly notifying the client about update on any cached resource. Hence, resources 
		// are guaranteed to be fresh.
		if (broker.isBrokerActive()) return true;
		
		Date lastPollingTime = PollingIntervalDirectory.getLastPollingTime(containerId);
		
		// The broker should start polling immediately after its creation. Otherwise, it may not be working 
		// properly, and we cannot say whether the cached content is fresh or stale.
		if (lastPollingTime == null) return broker.isBrokerJustCreated();
		
		// Finally, check whether or not a polling request was issued within the acceptable time window.
		Long timePassedSinceLastPolling = System.currentTimeMillis() - lastPollingTime.getTime();
		return (timePassedSinceLastPolling <= CacheConfigurer.DEFAULT_VALIDITY_PERIOD_FOR_CACHED_CONTENT);
	}
	
	public static boolean isMonitoredObject(Object object) {
		if (object == null) return false;
		return (object instanceof EndpointReferenceType) || (object instanceof WSResourceConfig);
	}

	private static String getContainerId(Object resource) {
		String containerId =  null;
		if (resource instanceof WSResourceConfig) {
			WSResourceConfig resourceConfig = (WSResourceConfig) resource;
			containerId = resourceConfig.getContainerId();
		} else if (resource instanceof EndpointReferenceType) {
			EndpointReferenceType epr = (EndpointReferenceType) resource;
			containerId = CacheUtils.getContainerId(epr);
		}
		return containerId;
	}
}
