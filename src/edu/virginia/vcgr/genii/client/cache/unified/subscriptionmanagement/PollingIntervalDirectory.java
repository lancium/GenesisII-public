package edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * This class tracks the last polling time, polling interval settings, and next scheduled polling time for
 * notification brokers that are working by polling notification messages from the containers.
 * */
public class PollingIntervalDirectory {
	
	private static final Log _logger = LogFactory.getLog(PollingIntervalDirectory.class);
	
	public static final long DEFAULT_POLLING_INTERVAL = 15 * 1000L; // 15 seconds
	public static final long MINIMUM_POLLING_INTERVAL = 5 * 1000L; // 5 seconds
	public static final long MAXIMUM_POLLING_INTERVAL = 4 * 60 * 1000L; // four minutes
	
	// The poller should test and update its polling interval every now and then. Otherwise 
	// the system will throw exception to block polling requests.
	private static final long MAX_DEVIATION_FROM_MISSED_POLLING_DEADLINE = 15 * 1000L; // fifteen seconds
	
	private static final Map<String, PollingTimeSettings> POLLING_TIME_CONFIGURATIONS = 
		new ConcurrentHashMap<String, PollingTimeSettings>();
	
	public static void storePollingTimeSettings(PollingTimeSettings pollingTimeSettings) {
		String containerId = pollingTimeSettings.getContainerId();
		POLLING_TIME_CONFIGURATIONS.put(containerId, pollingTimeSettings);
	}
	
	public static void deletePollingTimeSettings(String containerId) {
		POLLING_TIME_CONFIGURATIONS.remove(containerId);
	}
	
	/*
	 * This method is used whenever a broker makes an polling outcall or the client receive information 
	 * piggy-backed from a container indicating the client-cache is fresh, which is equivalent to making 
	 * a polling outcall. 
	 * */
	public static void notifyAboutPolling(String containerId) {
		PollingTimeSettings pollingTimeSettings = POLLING_TIME_CONFIGURATIONS.get(containerId);
		
		// This indicates the client is directly receiving notifications from the container.
		if (pollingTimeSettings == null) return;
		
		synchronized (pollingTimeSettings) {
			pollingTimeSettings.notifyAboutPolling();
		}
	}
	
	/*
	 * This method is invoked to schedule an immediate polling once the client knows that there are 
	 * some updates in the container that are not reflected in the cache.
	 * */
	public static void expediteScheduledPolling(String containerId) {
		PollingTimeSettings pollingTimeSettings = POLLING_TIME_CONFIGURATIONS.get(containerId);
		if (pollingTimeSettings == null) return;
		synchronized (pollingTimeSettings) {
			pollingTimeSettings.setNextPollingTime(new Date());
		}
	}
	
	public static boolean isPollingDue(String containerId, long pollingTimeCheckingInterval) {
		PollingTimeSettings pollingTimeSettings = POLLING_TIME_CONFIGURATIONS.get(containerId);
		if (pollingTimeSettings == null) {
			throw new RuntimeException("Missing required polling time settings information.");
		}
		synchronized (pollingTimeSettings) {
			Date nextPollingTime = pollingTimeSettings.getNextPollingTime();
			long scheduledPollingTimeInMillis = nextPollingTime.getTime();
			long millisTillNextPolling = scheduledPollingTimeInMillis - System.currentTimeMillis();
			if (millisTillNextPolling >= 0) {
				return (millisTillNextPolling < pollingTimeCheckingInterval);
			} else {
				final long deviation = MAX_DEVIATION_FROM_MISSED_POLLING_DEADLINE - Math.abs(millisTillNextPolling);
				if (deviation < 0) {
					_logger.warn("Notification poller missed the polling deadline by a significant margin.");
				}
				return true;
			}
		}
	}
	
	public static Date getLastPollingTime(String containerId) {
		PollingTimeSettings pollingTimeSettings = POLLING_TIME_CONFIGURATIONS.get(containerId);
		if (pollingTimeSettings == null) return null;
		return pollingTimeSettings.getMostRecentPollingTime();
	}
	
	public static void changePollingInterval(String containerId, long pollingInterval) {
		PollingTimeSettings pollingTimeSettings = POLLING_TIME_CONFIGURATIONS.get(containerId);
		if (pollingTimeSettings == null) return;
		synchronized (pollingTimeSettings) {
			long verifiedPollingInterval = Math.min(MAXIMUM_POLLING_INTERVAL, 
					Math.max(pollingInterval, MINIMUM_POLLING_INTERVAL));
			pollingTimeSettings.setPollingRPCInterval(verifiedPollingInterval);
			pollingTimeSettings.updateNextPollingTime();
		}
	}
}
