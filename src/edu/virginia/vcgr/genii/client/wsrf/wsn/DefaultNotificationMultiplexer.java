package edu.virginia.vcgr.genii.client.wsrf.wsn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;

public class DefaultNotificationMultiplexer implements NotificationMultiplexer
{
	static private Log _logger = LogFactory.getLog(DefaultNotificationMultiplexer.class);

	private Set<NotificationHandlerWrapper<? extends NotificationMessageContents>> _handlers = new HashSet<NotificationHandlerWrapper<? extends NotificationMessageContents>>();

	private class NotificationRegistrationImpl implements NotificationRegistration
	{
		private NotificationHandlerWrapper<?> _wrapper;

		private NotificationRegistrationImpl(NotificationHandlerWrapper<?> wrapper)
		{
			_wrapper = wrapper;
		}

		@Override
		public void cancel()
		{
			synchronized (_handlers) {
				_handlers.remove(_wrapper);
			}
		}
	}

	@Override
	final public <ContentsType extends NotificationMessageContents> NotificationRegistration registerNotificationHandler(
		TopicQueryExpression topicFilter, NotificationHandler<ContentsType> handler)
	{
		NotificationHandlerWrapper<ContentsType> wrapper = new NotificationHandlerWrapper<ContentsType>(topicFilter, handler);

		synchronized (_handlers) {
			_handlers.add(wrapper);
		}

		return new NotificationRegistrationImpl(wrapper);
	}

	@Override
	final public String notify(TopicPath path, EndpointReferenceType producerReference,
		EndpointReferenceType subscriptionReference, Element messageContents, MessageElement[] additionalAttributes)
	{
		String status = NotificationConstants.UNPROCESSED;

		Collection<NotificationHandlerWrapper<? extends NotificationMessageContents>> handlers;

		synchronized (_handlers) {
			handlers = new ArrayList<NotificationHandlerWrapper<? extends NotificationMessageContents>>(_handlers);
		}

		for (NotificationHandlerWrapper<? extends NotificationMessageContents> handler : handlers) {
			if (handler.passesFilter(path)) {
				try {
					JAXBContext context = JAXBContext.newInstance(handler.contentsType());
					Unmarshaller u = context.createUnmarshaller();
					JAXBElement<? extends NotificationMessageContents> jaxbe = u.unmarshal(messageContents,
						handler.contentsType());

					NotificationMessageContents messageContent = jaxbe.getValue();
					messageContent.setAdditionalAttributes(additionalAttributes);

					// What if a message is handled by multiple handlers?
					// Should we combine the status so far with the new status?
					// Should we abort the loop if the new status is "try again"?
					status = handler.handleNotification(path, producerReference, subscriptionReference, messageContent);

				} catch (JAXBException e) {
					_logger.warn("Error deserializing message contents for " + "notification handler.", e);
				}
			}
		}
		return status;
	}
}