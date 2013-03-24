package edu.virginia.vcgr.genii.container.wsrf.wsn.topic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.wsn.WSNotificationContainerService;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class PublisherTopic
{
	static private Log _logger = LogFactory.getLog(PublisherTopic.class);

	private EndpointReferenceType _publisherReference;
	private TopicPath _name;

	PublisherTopic(EndpointReferenceType publisherReference, TopicPath name)
	{
		_publisherReference = publisherReference;
		_name = name;
	}

	final public TopicPath name()
	{
		return _name;
	}

	final public <ContentsType extends NotificationMessageContents> void publish(ContentsType contents)
	{
		publish(contents, null);
	}

	final public <ContentsType extends NotificationMessageContents> void publish(ContentsType contents,
		GeniiAttachment attachment)
	{
		try {
			ResourceKey rKey = ResourceManager.getTargetResource(_publisherReference);

			WSNotificationContainerService wsnService = ContainerServices.findService(WSNotificationContainerService.class);

			wsnService.publishNotification(rKey.getResourceKey(), _publisherReference, _name, contents, attachment);
		} catch (ResourceUnknownFaultType e) {
			_logger.warn("Ignoring publish request because publisher doesn't exist.");
		} catch (ResourceException e) {
			_logger.warn("Ignoring publish request because publisher doesn't exist.");
		}
	}
}
