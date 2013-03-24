package edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cache.unified.CacheUtils;

/*
 * This is a utility class for processing the message-index for last notification message stored or propagated
 * by different containers. The information regarding current value of the message-index can reach client in two
 * ways: as attributes of the notification messages received and from processed SOAP message headers. This class
 * has the responsibility to adjust the polling interval for different notification brokers that are operating 
 * in polling mode and to initiate fault-confinement related activities when some messages are lost
 * */
public class NotificationMessageIndexProcessor
{

	private static Log _logger = LogFactory.getLog(NotificationMessageIndexProcessor.class);

	// This is used for processing the message-index received by intercepting legitimate calls to
	// container.
	public static void processMessageIndexValue(EndpointReferenceType target, int messageIndex)
	{

		// The logic is to first adjust the polling interval then check for message loss as at the
		// end of message
		// loss evaluation corresponding method updates the message index of the broker.
		adjustNotificationPollingTime(target, messageIndex);
		checkForMessageLoss(target, messageIndex);
	}

	// This is used for processing the message-index that accompanies a notification message.
	public static void checkForMessageLoss(EndpointReferenceType target, int receivedMessageIndex)
	{

		NotificationBrokerWrapper broker = NotificationBrokerDirectory.getExistingRepresentativeBroker(target);

		// This can happen if the client has removed the broker because it was expired and right at
		// that
		// time a notification message has arrived from the container. since we cannot assume
		// anything about
		// client's and container's clocks -- this scenario is not impossible.
		if (broker == null)
			return;

		int messageIndexOfBroker = broker.getLastReceivedMessageIndex();
		if (receivedMessageIndex > messageIndexOfBroker + 1) {
			/*
			 * TODO: Some messages are missing. Need to discard the cached resources of concerned
			 * container. However, we have to consider that the missing notification messages may be
			 * delayed therefore can reach the client shortly. If the broker is in polling mode then
			 * it may be polling notifications or immediately going to poll. Therefore, we need a
			 * careful technique for fault tolerance.
			 */
		}

		// Update the message index of the broker assuming that we have taken necessary steps to
		// avoid returning
		// stale information from client-cache.
		if (receivedMessageIndex > messageIndexOfBroker) {
			broker.setLastReceivedMessageIndex(receivedMessageIndex);
		}
	}

	private static void adjustNotificationPollingTime(EndpointReferenceType target, int receivedMessageIndex)
	{

		NotificationBrokerWrapper broker = NotificationBrokerDirectory.getExistingRepresentativeBroker(target);
		if (broker == null)
			return;

		String containerId = CacheUtils.getContainerId(target);
		int messageIndexOfBroker = broker.getLastReceivedMessageIndex();
		if (receivedMessageIndex > messageIndexOfBroker) {
			// There is an update done that is not reflected in the cache; therefore we request
			// immediate polling
			PollingIntervalDirectory.expediteScheduledPolling(containerId);
			_logger.info("Expedited the next polling");
		} else {
			// No update has been done; therefore we can differ polling.
			PollingIntervalDirectory.notifyAboutPolling(containerId);
		}
	}
}
