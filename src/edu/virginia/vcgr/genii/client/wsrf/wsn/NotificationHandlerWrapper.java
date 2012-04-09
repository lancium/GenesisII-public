package edu.virginia.vcgr.genii.client.wsrf.wsn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;

class NotificationHandlerWrapper<ContentsType extends NotificationMessageContents>
{
	static private Log _logger = LogFactory.getLog(
		NotificationHandlerWrapper.class);
	
	private TopicQueryExpression _filter;
	private NotificationHandler<ContentsType> _handler;
	
	NotificationHandlerWrapper(TopicQueryExpression filter,
		NotificationHandler<ContentsType> handler)
	{
		_filter = filter;
		_handler = handler;
	}
	
	final boolean passesFilter(TopicPath topic)
	{
		return (_filter == null) || _filter.matches(topic);
	}
	
	final Class<ContentsType> contentsType()
	{
		return _handler.contentsType();
	}

	final String handleNotification(TopicPath topic,
		EndpointReferenceType producerReference,
		EndpointReferenceType subscriptionReference, Object contents)
	{
		try
		{
			return _handler.handleNotification(
				topic, producerReference, subscriptionReference,
				_handler.contentsType().cast(contents));
		}
		catch (Exception e)
		{
			_logger.warn(
				"A notification handler threw an exception while " +
				"handling a notification.", e);
			return NotificationConstants.FAIL;
		}
	}
}