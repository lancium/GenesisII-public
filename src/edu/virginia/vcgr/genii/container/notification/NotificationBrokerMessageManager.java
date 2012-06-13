package edu.virginia.vcgr.genii.container.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.wsn.base.GetMessagesResponse;
import org.oasis_open.wsn.base.NotificationMessageHolderType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.notification.NotificationMessageHolder;
import edu.virginia.vcgr.genii.container.cservices.wsn.NotificationMessageOutcallContent;

/*
 * This class holds the notification messages that are addressed to notification brokers that do not support
 * outcalls to client because of interim firewall or NAT in the network. It holds the processed notification 
 * messages, not the original published content. This is done to avoid XML parsing overhead when each broker
 * pulls the notification messages. Finally, it expects the broker to claim its notification messages within
 * five minutes of the generation time. Longer lived messages are get cleaned up by a cleaner thread.
 * */
public class NotificationBrokerMessageManager {

	private static Log _logger = LogFactory.getLog(NotificationBrokerMessageManager.class);
	
	private static final long MESSAGE_CLEANING_INTERVAL = 5 * 60 * 1000L; // five minutes
	
	private Map<String, List<OnHoldNotificationMessage>> BROKER_ID_TO_MESSAGE_LIST_MAP;
	
	private static NotificationBrokerMessageManager manager;
	
	private NotificationBrokerMessageManager() {
		BROKER_ID_TO_MESSAGE_LIST_MAP = new ConcurrentHashMap<String, List<OnHoldNotificationMessage>>();
		new MessageCleaner().start();
	}
	
	public static NotificationBrokerMessageManager getManager() {
		if (manager == null) {
			manager = new NotificationBrokerMessageManager();
		}
		return manager;
	}
	
	public List<OnHoldNotificationMessage> getMessageQueueOfBroker(String resourceKeyOfBroker) {
		return BROKER_ID_TO_MESSAGE_LIST_MAP.remove(resourceKeyOfBroker);
	}
	
	public void setMessageQueueOfBroker(String resourceKeyOfBroker, List<OnHoldNotificationMessage> messages) {
		BROKER_ID_TO_MESSAGE_LIST_MAP.put(resourceKeyOfBroker, messages);
	}
	
	public void placeMessageInBrokerQueues(NotificationMessageOutcallContent message, 
			Collection<String> brokerResourceKeyList) throws JAXBException, SOAPException {
		
		OnHoldNotificationMessage onHoldNotificationMessage = new OnHoldNotificationMessage();
		NotificationMessageHolder holder = new NotificationMessageHolder(message.subscriptionReference(), 
				message.publisher(), message.topic(), message.contents());
		onHoldNotificationMessage.setHolderType(holder.toAxisType());
		onHoldNotificationMessage.setAdditionalAttributes(message.contents().getAdditionalAttributes());
		onHoldNotificationMessage.setMessagePublicationTime(new Date());
		
		synchronized (BROKER_ID_TO_MESSAGE_LIST_MAP) {
			Set<String> brokerKeys = BROKER_ID_TO_MESSAGE_LIST_MAP.keySet();
			for (String resourceKeyOfBroker : brokerResourceKeyList) {
				if (!brokerKeys.contains(resourceKeyOfBroker)) {
					BROKER_ID_TO_MESSAGE_LIST_MAP.put(resourceKeyOfBroker, new ArrayList<OnHoldNotificationMessage>());
				}
			}
			for (String resourceKeyOfBroker : brokerResourceKeyList) {
				BROKER_ID_TO_MESSAGE_LIST_MAP.get(resourceKeyOfBroker).add(onHoldNotificationMessage);
			}
		}
	}

	public GetMessagesResponse getMessagesResponseFromHeldMessages(List<OnHoldNotificationMessage> messages, int brokerMessageIndex) {
		if (messages == null || messages.isEmpty()) return new GetMessagesResponse(); 
		List<MessageElement> attributeList = new ArrayList<MessageElement>();
		NotificationMessageHolderType[] holders = new NotificationMessageHolderType[messages.size()];
		
		// Indexing of individual messages starts from the difference between current message index of the broker and the 
		// size of the message queue. The system is supposed to retain the most recent messages rather than the old when an 
		// overflow occurs.  So, in most of the message-miss scenarios this indexing will behave correctly. In those 
		// unlikely cases where interim messages get lost, the client will be able to detect the problem by comparing the
		// message indices with its stored last-message-index value.
		int messageIndex = brokerMessageIndex - messages.size() + 1;
		
		int iteration = 0;
		for (OnHoldNotificationMessage message : messages) {
			
			holders[iteration] = message.getHolderType();
			
			MessageElement[] additionalAttributes = message.getAdditionalAttributes();
			// If there are additional attributes in the notification message then 
			// retrieve those, add the attributes separator element, and finally 
			// add all the attributes in the collections. The separator is subsequently
			// used to determine which attribute belongs to what message.
			attributeList.add(new MessageElement(GenesisIIConstants.NOTIFICATION_MESSAGE_ATTRIBUTES_SEPARATOR, iteration));
			attributeList.add(new MessageElement(NotificationBrokerConstants.MESSAGE_INDEX_QNAME, messageIndex));
			if (additionalAttributes != null && additionalAttributes.length > 0) {
				attributeList.addAll(Arrays.asList(additionalAttributes));
			}
			iteration++;
			messageIndex++;
		}
		GetMessagesResponse response = new GetMessagesResponse(holders, 
				attributeList.toArray(new MessageElement[attributeList.size()]));
		return response;
	}
	
	private class MessageCleaner extends Thread {

		@Override
		public void run() {
			while (true) {
				try {
					sleep(MESSAGE_CLEANING_INTERVAL);
					long currentTimeInMillis = System.currentTimeMillis();

					Set<String> brokerIds = BROKER_ID_TO_MESSAGE_LIST_MAP.keySet();
					for (String brokerId : brokerIds) {
						List<OnHoldNotificationMessage> messageListOfBroker = 
							BROKER_ID_TO_MESSAGE_LIST_MAP.get(brokerId);
						Iterator<OnHoldNotificationMessage> iterator = messageListOfBroker.iterator();
						while (iterator.hasNext()) {
							OnHoldNotificationMessage message = iterator.next();
							long messagePublicationTimeInMillis = message.getMessagePublicationTime().getTime();
							if (currentTimeInMillis - messagePublicationTimeInMillis > MESSAGE_CLEANING_INTERVAL) {
								iterator.remove();
								_logger.info("cleaned a notification message");
							}
						}
						if (messageListOfBroker.isEmpty()) {
							BROKER_ID_TO_MESSAGE_LIST_MAP.remove(brokerId);
							_logger.info("cleaned a broker message queue");
						} 
					}
				} catch (InterruptedException e) {
					_logger.info("notification message cleaner thread has been interrupted");
				}
			}
		}
	}
}
