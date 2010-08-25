package edu.virginia.vcgr.genii.container.wsrf.wsn.topic;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public interface PublisherTopic
{
	public TopicPath name();
	
	public <ContentsType extends NotificationMessageContents>
		void publish(EndpointReferenceType publisherReference,
			ResourceKey resourceKey, ContentsType contents);
	public <ContentsType extends NotificationMessageContents>
		void publish(EndpointReferenceType publisherReference,
			ContentsType contents);
	public <ContentsType extends NotificationMessageContents>
		void publish(ContentsType contents);
}