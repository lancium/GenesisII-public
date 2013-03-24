package edu.virginia.vcgr.genii.container.cservices.wsn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ByteIOTopics;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSTopics;

/*
 * This class protects the container from overloading of notification outcalls. It works by tracking the number of 
 * notifications a particular resource has made within a short interval. If that number crosses the allowed threshold, 
 * it blocks the resource from publishing any more notifications for a specific period of time. To notify the 
 * consumer that the resource will not notify again within the defined blockage period, it updates the notification 
 * message with a blockage flag. After the blocked period is over the resource can send notification again. 
 * 
 * This mechanism of blocking notification is intended for prohibiting notification outcalls when a resource is undergoing 
 * rapid changes -- it does not stop notifications when these are simultaneously issued from many resources. 
 * */
public class NotificationRateController
{

	static private Log _logger = LogFactory.getLog(NotificationRateController.class);

	private static final List<TopicPath> CONSTRICTED_NOTIFICATION_TYPES;
	static {
		CONSTRICTED_NOTIFICATION_TYPES = new ArrayList<TopicPath>();
		CONSTRICTED_NOTIFICATION_TYPES.add(RNSTopics.RNS_CONTENT_CHANGE_TOPIC);
		CONSTRICTED_NOTIFICATION_TYPES.add(ByteIOTopics.BYTEIO_ATTRIBUTES_UPDATE_TOPIC);
	}

	private static final long BLOCKAGE_PERIOD_TIME_SPAN = 2 * 60 * 1000L; // two minutes
	private static final long BURST_PERIOD_TIME_SPAN = 30 * 1000L; // thirty seconds

	// This should be around five to ten.
	private static final int ALLOWED_BURSTINESS_THRESHOLD = 10;

	private Map<String, List<Long>> publisherToRecentNotificationTimeMappings;
	private Map<String, Long> blockedPublisherToBlockageExpiryTimeMappings;

	public NotificationRateController()
	{
		publisherToRecentNotificationTimeMappings = Collections.synchronizedMap(new HashMap<String, List<Long>>());
		blockedPublisherToBlockageExpiryTimeMappings = Collections.synchronizedMap(new HashMap<String, Long>());
		new RecentNotificationTraceCleaner().start();
		new BlockedResourceCleaner().start();
	}

	public boolean notificationCanPass(String publisherKey, TopicPath topic, NotificationMessageContents message)
	{
		if (!isRestrictedTopic(topic))
			return true;
		if (isBlockedPublisher(publisherKey))
			return false;
		submitNotificationForRateControl(publisherKey, message);
		return true;
	}

	public boolean isPublisherBlocked(String publisherKey)
	{
		return blockedPublisherToBlockageExpiryTimeMappings.containsKey(publisherKey);
	}

	public Long getBlockadeCreationTime(String publisherKey)
	{
		Long blockExpiryTime = blockedPublisherToBlockageExpiryTimeMappings.get(publisherKey);
		if (blockExpiryTime == null)
			return null;
		return blockExpiryTime - BLOCKAGE_PERIOD_TIME_SPAN;
	}

	private boolean isRestrictedTopic(TopicPath topic)
	{
		for (TopicPath restrictedTopic : CONSTRICTED_NOTIFICATION_TYPES) {
			if (restrictedTopic.equals(topic))
				return true;
		}
		return false;
	}

	private boolean isBlockedPublisher(String publisherKey)
	{
		if (blockedPublisherToBlockageExpiryTimeMappings.containsKey(publisherKey)) {
			Long blockageExpiryTime = blockedPublisherToBlockageExpiryTimeMappings.get(publisherKey);
			if (blockageExpiryTime == null)
				return false;
			long currentTime = System.currentTimeMillis();
			return (blockageExpiryTime >= currentTime);
		}
		return false;
	}

	private void submitNotificationForRateControl(String publisherKey, NotificationMessageContents message)
	{
		List<Long> publishersRecentNotificationTimes = publisherToRecentNotificationTimeMappings.get(publisherKey);
		if (publishersRecentNotificationTimes == null) {
			publishersRecentNotificationTimes = Collections.synchronizedList(new ArrayList<Long>(ALLOWED_BURSTINESS_THRESHOLD));
			publisherToRecentNotificationTimeMappings.put(publisherKey, publishersRecentNotificationTimes);
		}
		long currentTime = System.currentTimeMillis();
		publishersRecentNotificationTimes.add(currentTime);
		if (publishersRecentNotificationTimes.size() == ALLOWED_BURSTINESS_THRESHOLD) {
			blockPublisherAndUpdateMessage(publisherKey, message);
		} else if (publishersRecentNotificationTimes.size() > ALLOWED_BURSTINESS_THRESHOLD) {
			// rate controlling is not working properly.
			_logger.warn("notification rate controller may not be working properly. Letting notification "
				+ "to pass without any rate control marker.");
			return;
		}
	}

	private void blockPublisherAndUpdateMessage(String publisherKey, NotificationMessageContents message)
	{
		message.setPublisherBlockedFromFurtherNotifications(true);
		message.setBlockageTime(BLOCKAGE_PERIOD_TIME_SPAN);
		long blockageExpiryTime = System.currentTimeMillis() + BLOCKAGE_PERIOD_TIME_SPAN;
		blockedPublisherToBlockageExpiryTimeMappings.put(publisherKey, blockageExpiryTime);
		_logger.info("Publisher " + publisherKey + " has been blocked from sending further notifications for some time");
	}

	private class RecentNotificationTraceCleaner extends Thread
	{
		@Override
		public void run()
		{
			while (true) {
				try {
					sleep(1000); // iterate in every second interval
					long tooOldNotificationThreshold = System.currentTimeMillis() - BURST_PERIOD_TIME_SPAN;
					List<String> publishersWithEmptyLists = new ArrayList<String>();
					for (Map.Entry<String, List<Long>> entry : publisherToRecentNotificationTimeMappings.entrySet()) {
						String publisherKey = entry.getKey();
						List<Long> recentNotificationTimes = entry.getValue();
						synchronized (recentNotificationTimes) {
							Iterator<Long> iterator = recentNotificationTimes.iterator();
							while (iterator.hasNext()) {
								long notificationTime = iterator.next();
								if (notificationTime < tooOldNotificationThreshold) {
									iterator.remove();
								}
							}
						}
						if (recentNotificationTimes.isEmpty())
							publishersWithEmptyLists.add(publisherKey);
					}
					for (String publisherKey : publishersWithEmptyLists) {
						publisherToRecentNotificationTimeMappings.remove(publisherKey);
					}
				} catch (Exception ex) {
					_logger.info("notification cleaner thread faced some problem", ex);
				}
			}
		}
	}

	private class BlockedResourceCleaner extends Thread
	{
		@Override
		public void run()
		{
			while (true) {
				try {
					sleep(5000); // iterate in every five seconds
					long currentTime = System.currentTimeMillis();
					List<String> releasedPublishers = new ArrayList<String>();
					for (Map.Entry<String, Long> entry : blockedPublisherToBlockageExpiryTimeMappings.entrySet()) {
						String publisherKey = entry.getKey();
						long blockageExpiryTime = entry.getValue();
						if (blockageExpiryTime < currentTime) {
							releasedPublishers.add(publisherKey);
						}
					}
					for (String publisherKey : releasedPublishers) {
						blockedPublisherToBlockageExpiryTimeMappings.remove(publisherKey);
					}
				} catch (Exception ex) {
					_logger.info("blockade lifter thread faced some problem", ex);
				}
			}
		}
	}
}
