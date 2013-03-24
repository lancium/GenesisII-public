package edu.virginia.vcgr.genii.container.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.wsn.base.NotificationMessageHolderType;
import org.oasis_open.wsn.base.Notify;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.wsrf.wsn.notification.NotificationMessageHolder;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.container.cservices.wsn.NotificationMessageOutcallContent;

public class NotificationForwarder implements Runnable
{

	private static Log _logger = LogFactory.getLog(NotificationForwarder.class);

	private NotificationMessageOutcallContent message;
	private List<NotificationBrokerDBResource> listOfActiveBrokers;
	private ICallingContext callingContext;

	public NotificationForwarder(NotificationMessageOutcallContent message,
		List<NotificationBrokerDBResource> listOfActiveBrokers, ICallingContext callingContext)
	{
		this.message = message;
		this.listOfActiveBrokers = listOfActiveBrokers;
		this.callingContext = callingContext;
	}

	@Override
	public void run()
	{
		if (listOfActiveBrokers == null || listOfActiveBrokers.isEmpty())
			return;

		MessageElement[] additionalAttributes = message.contents().getAdditionalAttributes();
		NotificationMessageHolder holder = new NotificationMessageHolder(message.subscriptionReference(), message.publisher(),
			message.topic(), message.contents());

		List<MessageElement> messageElements = new ArrayList<MessageElement>();
		if (additionalAttributes != null && additionalAttributes.length > 0) {
			messageElements.addAll(Arrays.asList(additionalAttributes));
		}
		Notify notify = null;
		try {
			notify = new Notify(new NotificationMessageHolderType[] { holder.toAxisType() }, null);
			for (NotificationBrokerDBResource resource : listOfActiveBrokers) {

				// We pass the message index of the notification broker with each notification
				// message sent so that the
				// client can determine whether or not it has missed any notification message.
				int messageIndexOfCurrentBroker = resource.getMessageIndex();
				List<MessageElement> brokerSpecificMessageElements = new ArrayList<MessageElement>(messageElements.size() + 1);

				// A message-attributes-separator element is needed in the attribute list as the
				// common notification helper
				// expects such a marker with each notification message having additional attributes
				// so that it can decide
				// which attribute belongs to which notification message, when it is given with a
				// set of messages at once.
				// Finally, ordering is important here to ensure that attribute separator has been
				// placed at the beginning.
				brokerSpecificMessageElements.add(new MessageElement(
					GenesisIIConstants.NOTIFICATION_MESSAGE_ATTRIBUTES_SEPARATOR, 0));
				brokerSpecificMessageElements.addAll(messageElements);
				brokerSpecificMessageElements.add(new MessageElement(NotificationBrokerConstants.MESSAGE_INDEX_QNAME,
					messageIndexOfCurrentBroker));

				notify.set_any(brokerSpecificMessageElements.toArray(new MessageElement[brokerSpecificMessageElements.size()]));

				EndpointReferenceType forwardingPort = resource.getForwardingPort();
				try {
					GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, forwardingPort, callingContext);
					common.notify(notify);
				} catch (Exception ex) {
					_logger.warn("failed to make notification outcall", ex);
				}
			}
		} catch (Exception e) {
			_logger.warn("an unexpected error while forwarding notifications", e);
		}
	}
}
