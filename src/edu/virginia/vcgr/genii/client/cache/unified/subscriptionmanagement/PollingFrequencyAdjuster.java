package edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cache.ResourceAccessMonitor;

/*
 * This class periodically access per-container resource access information and adjust the polling interval of the 
 * brokers that are in polling mode. This thread run very frequently, once in each second, to keep the responsiveness
 * of frequency adjuster very high. The motive here is to response quickly to reduce polling interval when dictated 
 * by the usage pattern, to avoid blocking the client-cache for longer terms. 
 * */
public class PollingFrequencyAdjuster extends Thread
{

	private static final Log _logger = LogFactory.getLog(PollingFrequencyAdjuster.class);

	// A step functions where millisecond polling interval times are plotted against the
	// last usage time in minutes
	private static final Map<Integer, Long> USAGE_TIME_TO_POLLING_INTERVAL_MATRIX;
	static {
		// This must be a linked hash map as the ordering of elements is important.
		USAGE_TIME_TO_POLLING_INTERVAL_MATRIX = new LinkedHashMap<Integer, Long>();

		USAGE_TIME_TO_POLLING_INTERVAL_MATRIX.put(0, 10 * 1000L);

		// thirty second is the threshold interval for ensuring freshness of cached information. The
		// cache-management
		// module use that threshold to decides whether or not to return results from cache. Hence,
		// here we kept the
		// interval a little smaller than that, hoping that a polling request will start-and-end
		// within every thirty
		// seconds interval.
		USAGE_TIME_TO_POLLING_INTERVAL_MATRIX.put(2, 25 * 1000L);

		USAGE_TIME_TO_POLLING_INTERVAL_MATRIX.put(5, 60 * 1000L);
		USAGE_TIME_TO_POLLING_INTERVAL_MATRIX.put(7, 2 * 60 * 1000L);

		// last polling interval is four minutes as the container will not store unread notification
		// messages for more
		// than five minutes.
		USAGE_TIME_TO_POLLING_INTERVAL_MATRIX.put(10, 4 * 60 * 1000L);
	}

	private static final long POLLING_ADJUSTMENT_CHECKING_INTERVAL = 100L; // hundred milliseconds

	@Override
	public void run()
	{
		while (true) {
			try {
				sleep(POLLING_ADJUSTMENT_CHECKING_INTERVAL);
				for (String containerId : ResourceAccessMonitor.getIdsOfContainersInUse()) {
					Date timeOfLastUse = ResourceAccessMonitor.getLastContainerUsageTime(containerId);
					long newPollingInterval = getNewPollingInterval(timeOfLastUse);
					PollingIntervalDirectory.changePollingInterval(containerId, newPollingInterval);
				}
			} catch (InterruptedException e) {
				_logger.warn("Polling interval adjuster has been interrupted");
			}
		}
	}

	private long getNewPollingInterval(Date lastResourceUsageTime)
	{
		long timePassedSinceLastUse = System.currentTimeMillis() - lastResourceUsageTime.getTime();
		int roundedLastUsageTimeInMinutes = (int) (timePassedSinceLastUse / (60 * 1000L));
		Long maxExaminedPollingInterval = null;
		for (Map.Entry<Integer, Long> entry : USAGE_TIME_TO_POLLING_INTERVAL_MATRIX.entrySet()) {
			if (roundedLastUsageTimeInMinutes > entry.getKey()) {
				maxExaminedPollingInterval = entry.getValue();
			} else if (roundedLastUsageTimeInMinutes == entry.getKey()) {
				return entry.getValue();
			} else {
				return maxExaminedPollingInterval;
			}
		}
		return maxExaminedPollingInterval;
	}
}
