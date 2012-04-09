package edu.virginia.vcgr.genii.client.wsrf.wsn;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;

public interface NotificationHandler<ContentsType extends NotificationMessageContents>
{
	public Class<ContentsType> contentsType();
	
	public String handleNotification(
		TopicPath topic,
		EndpointReferenceType producerReference,
		EndpointReferenceType subscriptionReference,
		ContentsType contents) throws Exception;
}