package edu.virginia.vcgr.genii.container.axis;

import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.SOAPHeaderElement;

import edu.virginia.vcgr.genii.container.context.ClientConfig;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.wsn.WSNotificationContainerService;
import edu.virginia.vcgr.genii.container.notification.NotificationBrokerConstants;

public class NotificationMessageIndexPropagator extends BasicHandler {

	private static final long serialVersionUID = 0L;

	@Override
	public void invoke(MessageContext msgContext) throws AxisFault {

		ClientConfig clientConfig = ClientConfig.getCurrentClientConfig();
		if (clientConfig != null) {
			String clientId = clientConfig.getClientId();
			WSNotificationContainerService notificationService = ContainerServices.findService(
					WSNotificationContainerService.class);
			Integer messageIndex = notificationService.getMessageIndexOfBroker(clientId);
			if (messageIndex != null) {
				setNotificationMessageIndex(messageIndex, msgContext);
			}
		}
	}
	
	private void setNotificationMessageIndex(int messageIndex, MessageContext msgContext) throws AxisFault {
		SOAPHeaderElement messageIndexHeader = new SOAPHeaderElement(
				NotificationBrokerConstants.MESSAGE_INDEX_QNAME, messageIndex);
		messageIndexHeader.setActor(null);
		messageIndexHeader.setMustUnderstand(false);
		try {
			msgContext.getMessage().getSOAPHeader().addChildElement(messageIndexHeader);
		} catch (SOAPException se) {
			throw new AxisFault(se.getLocalizedMessage());
		}
	}
}
