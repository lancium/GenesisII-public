package edu.virginia.vcgr.genii.container.wsrf.wsn.topic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.wsn.WSNotificationContainerService;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

class PublisherTopicImpl implements PublisherTopic
{
	static private Log _logger = LogFactory.getLog(PublisherTopicImpl.class);
	
	private EndpointReferenceType _publisherReference;
	private TopicPath _name;
	
	PublisherTopicImpl(EndpointReferenceType publisherReference,
		TopicPath name)
	{
		_publisherReference = publisherReference;
		_name = name;
	}
	
	@Override
	final public TopicPath name()
	{
		return _name;
	}

	@Override
	final public <ContentsType extends NotificationMessageContents>
		void publish(EndpointReferenceType publisherReference,
			ResourceKey rKey, ContentsType contents)
	{
		if (publisherReference == null)
			throw new IllegalArgumentException(
				"Unable to determine the publisher reference EPR.");
		
		WSNotificationContainerService wsnService = ContainerServices.findService(
			WSNotificationContainerService.class);
		
		try
		{
			wsnService.publishNotification(rKey.getResourceKey(),
				publisherReference, _name, contents);
		}
		catch (ResourceException e)
		{
			_logger.warn(
				"Ignoring publish request because publisher doesn't exist.");
		}
	}
	
	@Override
	final public <ContentsType extends NotificationMessageContents>
		void publish(EndpointReferenceType publisherReference,
			ContentsType contents)
	{
		if (publisherReference == null)
			throw new IllegalArgumentException(
				"Unable to determine the publisher reference EPR.");
		
		try
		{
			publish(publisherReference, 
				ResourceManager.getTargetResource(publisherReference),
				contents);
		}
		catch (ResourceUnknownFaultType e)
		{
			_logger.warn(
				"Ignoring publish request because publisher doesn't exist.");
		}
		catch (ResourceException e)
		{
			_logger.warn(
				"Ignoring publish request because publisher doesn't exist.");
		}
	}
	
	@Override
	final public <ContentsType extends NotificationMessageContents>
		void publish(
			ContentsType contents)
	{
		publish(_publisherReference, contents);
	}
}