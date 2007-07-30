package edu.virginia.vcgr.genii.container.common.notification;

import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.notification.InvalidTopicException;
import edu.virginia.vcgr.genii.client.notification.UnknownTopicException;

public class TopicSpace
{
	static private Pattern _TOPIC_PATTERN =
		Pattern.compile("^[-a-zA-Z0-9_]+(\\.[-a-zA-Z0-9_]+)*$");
	
	static private NotificationPool _notificationPool =
		new NotificationPool();
	
	private HashMap<String, Topic> _space =
		new HashMap<String, Topic>();
	
	public Topic registerTopic(String topicName) throws InvalidTopicException
	{
		Topic ret;
		if (!_TOPIC_PATTERN.matcher(topicName).matches())
			throw new InvalidTopicException(topicName);
		
		_space.put(topicName, (ret = new Topic(_notificationPool, topicName)) );
		return ret;
	}
	
	public Topic getTopic(String topicName) throws UnknownTopicException
	{
		Topic t = _space.get(topicName);
		if (t == null)
			throw new UnknownTopicException(topicName);
		
		return t;
	}
	
	public Collection<Topic> getRegisteredTopics()
	{
		return _space.values();
	}
}