package edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationRegistration;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.AuthZConfigUpdateNotification;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ByteIOAttributesUpdateNotification;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ByteIOTopics;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.GenesisIIBaseTopics;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSContentChangeNotification;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSTopics;
import edu.virginia.vcgr.genii.container.notification.NotificationBrokerConstants;
import edu.virginia.vcgr.genii.container.notification.NotificationBrokerTopics;

public class ClientSideNotificationManager implements NotificationMultiplexer {

	private static Log _logger = LogFactory.getLog(ClientSideNotificationManager.class);

	@Override
	public <ContentsType extends NotificationMessageContents> NotificationRegistration registerNotificationHandler(
			TopicQueryExpression topicFilter,
			NotificationHandler<ContentsType> handler) {
		// Do nothing
		return null;
	}

	@Override
	public String notify(TopicPath path, EndpointReferenceType producerReference,
			EndpointReferenceType subscriptionReference,
			Element messageContents, MessageElement[] _additionalAttributes) {
		
		checkForMessageLoss(producerReference, _additionalAttributes);
		
		_logger.debug("Received a notification message");
		
		try {
			if (path.equals(RNSTopics.RNS_CONTENT_CHANGE_TOPIC)) {
				processRNSContentChange(producerReference, messageContents,	_additionalAttributes);
			} else if (path.equals(ByteIOTopics.BYTEIO_ATTRIBUTES_UPDATE_TOPIC)) {
				processByteIOAttributesUpdate(producerReference, messageContents, _additionalAttributes);
			} else if (path.equals(GenesisIIBaseTopics.AUTHZ_CONFIG_UPDATE_TOPIC)) {
				processAuthzConfigUpdate(producerReference, messageContents, _additionalAttributes);
			} else if (path.equals(NotificationBrokerTopics.TEST_NOTIFICAION_TOPIC)) {
				NotificationBrokerDirectory.updateBrokerModeToActive(producerReference);
			}
		} catch (JAXBException e) {
			_logger.warn("error deserializing message contents", e);
		}
		return NotificationConstants.OK;
	}
	
	private void checkForMessageLoss(EndpointReferenceType producerReference, MessageElement[] _additionalAttributes) {
		
		if (_additionalAttributes == null) return;
		for (MessageElement attribute : _additionalAttributes) {
			if (!attribute.getQName().equals(NotificationBrokerConstants.MESSAGE_INDEX_QNAME)) continue;
			int receivedMessageIndex = Integer.parseInt(attribute.getValue());
			NotificationMessageIndexProcessor.checkForMessageLoss(producerReference, receivedMessageIndex);
			break;
		}
	}

	private void processRNSContentChange(EndpointReferenceType producerReference, 
			Element messageContents,
			MessageElement[] _additionalAttributes) throws JAXBException {
		
		JAXBContext context = JAXBContext.newInstance(RNSContentChangeNotification.class);
		Unmarshaller u = context.createUnmarshaller();
		JAXBElement<? extends NotificationMessageContents> jaxbe =
			u.unmarshal(messageContents, RNSContentChangeNotification.class);
		
		RNSContentChangeNotification notification = 
			(RNSContentChangeNotification) jaxbe.getValue();
		notification.setAdditionalAttributes(_additionalAttributes);
		RNSNotificationHandler.handleContentChangeNotification(notification, producerReference);
	}

	private void processByteIOAttributesUpdate(EndpointReferenceType producerReference, 
			Element messageContents,
			MessageElement[] _additionalAttributes) throws JAXBException {
		
		JAXBElement<? extends NotificationMessageContents> jaxbe = getUnmarshaledElement(
				messageContents, ByteIOAttributesUpdateNotification.class);
		ByteIOAttributesUpdateNotification notification = (ByteIOAttributesUpdateNotification) jaxbe.getValue();
		notification.setAdditionalAttributes(_additionalAttributes);
		AttributesUpdateNotificationsHandler.handleByteIOAttributesUpdate(notification, producerReference);
	}
	
	private void processAuthzConfigUpdate(EndpointReferenceType producerReference, 
			Element messageContents, 
			MessageElement[] _additionalAttributes) throws JAXBException {
		
		JAXBElement<? extends NotificationMessageContents> jaxbe = getUnmarshaledElement(
				messageContents, AuthZConfigUpdateNotification.class);
		AuthZConfigUpdateNotification notification = (AuthZConfigUpdateNotification) jaxbe.getValue();
		notification.setAdditionalAttributes(_additionalAttributes);
		AttributesUpdateNotificationsHandler.handleAuthZConfigUpdate(notification, producerReference);
	}
	
	private JAXBElement<? extends NotificationMessageContents> getUnmarshaledElement(
			Element messageContents, 
			Class<? extends NotificationMessageContents> contentType) 
			throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(contentType);
		Unmarshaller u = context.createUnmarshaller();
		return u.unmarshal(messageContents, contentType);
	}
}
