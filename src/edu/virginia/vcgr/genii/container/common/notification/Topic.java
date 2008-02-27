package edu.virginia.vcgr.genii.container.common.notification;

import java.util.Collection;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.Token;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.common.notification.Notify;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class Topic
{
	private NotificationPool _pool;
	private String _topicName;
	
	// Can only be created by the topic space.
	Topic(NotificationPool pool, String topicName)
	{
		_pool = pool;
		_topicName = topicName;
	}
	
	public String getTopicName()
	{
		return _topicName;
	}
	
	public void notifyAll(MessageElement []payload)
		throws ResourceException, ResourceUnknownFaultType
	{
		EndpointReferenceType source = 
			(EndpointReferenceType)WorkingContext.getCurrentWorkingContext().getProperty(
				WorkingContext.EPR_PROPERTY_NAME);
		ResourceKey rKey = ResourceManager.getTargetResource(source);
		
		Collection<SubscriptionInformation> subscriptions =
			rKey.dereference().matchSubscriptions(_topicName);
		
		for (SubscriptionInformation info : subscriptions)
		{
			Notify notifyMessage = new Notify(
				new Token(_topicName), source,
				info.getUserData(), payload);
			_pool.submitNotificationRequest(
				info.getTarget(), notifyMessage);
		}
	}
}