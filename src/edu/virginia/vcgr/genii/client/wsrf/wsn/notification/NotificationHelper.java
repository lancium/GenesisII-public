package edu.virginia.vcgr.genii.client.wsrf.wsn.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.wsn.base.NotificationMessageHolderType;
import org.oasis_open.wsn.base.NotificationMessageHolderTypeMessage;
import org.oasis_open.wsn.base.Notify;
import org.w3c.dom.Element;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;

public class NotificationHelper
{
	static private Log _logger = LogFactory.getLog(NotificationHelper.class);

	static public void notify(Notify notification, NotificationMultiplexer multiplexer)
	{
		NotificationMessageHolderType[] msgs = notification.getNotificationMessage();
		if (msgs != null) {
			int messageIndex = 0;
			Map<Integer, Collection<MessageElement>> messageIndexToAttributesMap = extractAdditionalAttributes(notification);

			for (NotificationMessageHolderType message : msgs) {
				MessageElement[] additionalAttributes = null;
				Collection<MessageElement> attributeCollection = messageIndexToAttributesMap.get(messageIndex);
				if (attributeCollection != null) {
					additionalAttributes = attributeCollection.toArray(new MessageElement[attributeCollection.size()]);
				}

				notifySingleMessage(message, multiplexer, additionalAttributes);

				messageIndex++;
			}
		}
	}

	static public String notifySingleMessage(NotificationMessageHolderType message, NotificationMultiplexer multiplexer)
	{
		return notifySingleMessage(message, multiplexer, null);
	}

	static public String notifySingleMessage(NotificationMessageHolderType message, NotificationMultiplexer multiplexer,
		MessageElement[] additionalAttributes)
	{
		try {
			NotificationMessageHolder holder = new NotificationMessageHolder(null, message);
			EndpointReferenceType producer = holder.publisherReference();
			EndpointReferenceType subscription = holder.subscriptionReference();

			NotificationMessageHolderTypeMessage contents = message.getMessage();
			Element[] eContents = null;
			Element eContent = null;
			if (contents != null) {
				eContents = contents.get_any();
				if (eContents != null) {
					if (eContents.length > 0)
						eContent = eContents[0];
				}
			}
			return multiplexer.notify(holder.topic(), producer, subscription, eContent, additionalAttributes);
		} catch (Throwable cause) {
			_logger.warn("Got a notification message that we can't handle.", cause);
			return NotificationConstants.FAIL;
		}
	}

	private static Map<Integer, Collection<MessageElement>> extractAdditionalAttributes(Notify notify)
	{

		Map<Integer, Collection<MessageElement>> messageIndexToAttributesMap = new HashMap<Integer, Collection<MessageElement>>();
		MessageElement[] _any = notify.get_any();
		if (_any == null || _any.length == 0)
			return messageIndexToAttributesMap;
		int messageIndex = -1;
		for (MessageElement element : _any) {
			QName qName = element.getQName();
			if (GenesisIIConstants.NOTIFICATION_MESSAGE_ATTRIBUTES_SEPARATOR.equals(qName)) {
				messageIndex = Integer.parseInt(element.getValue().trim());
			} else {
				Collection<MessageElement> messageAttributes = messageIndexToAttributesMap.get(messageIndex);
				if (messageAttributes == null) {
					messageAttributes = new ArrayList<MessageElement>();
				}
				messageAttributes.add(element);
				messageIndexToAttributesMap.put(messageIndex, messageAttributes);
			}
		}
		return messageIndexToAttributesMap;
	}

}