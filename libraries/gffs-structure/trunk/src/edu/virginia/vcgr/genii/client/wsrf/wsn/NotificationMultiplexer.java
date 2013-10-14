package edu.virginia.vcgr.genii.client.wsrf.wsn;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Element;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;

public interface NotificationMultiplexer
{
	public <ContentsType extends NotificationMessageContents> NotificationRegistration registerNotificationHandler(
		TopicQueryExpression topicFilter, NotificationHandler<ContentsType> handler);

	public String notify(TopicPath path, EndpointReferenceType producerReference, EndpointReferenceType subscriptionReference,
		Element messageContents, MessageElement[] additionalAttributes);
}