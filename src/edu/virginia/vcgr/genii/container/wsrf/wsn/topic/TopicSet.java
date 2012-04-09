package edu.virginia.vcgr.genii.container.wsrf.wsn.topic;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ContextException;
import edu.virginia.vcgr.genii.client.wsrf.WSRFConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.WSNTopic;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.xml.TopicForest;

public class TopicSet
{
	static private Log _logger = LogFactory.getLog(TopicSet.class);
	
	private Set<TopicPath> _topics = new HashSet<TopicPath>();

	private void addAllTopics(TopicSet set)
	{
		_topics.addAll(set._topics);
	}
	
	private TopicSet()
	{		
	}
	
	final public void addTopic(TopicPath topic)
	{
		_topics.add(topic);
	}
	
	final public PublisherTopic createPublisherTopic(TopicPath topicPath)
	{
		if (!_topics.contains(topicPath))
			throw new IllegalArgumentException(String.format(
				"Topic path %s has not been declared!", topicPath));
		
		EndpointReferenceType source = null;
		
		try
		{
			source = (EndpointReferenceType)WorkingContext.getCurrentWorkingContext().getProperty(
				WorkingContext.EPR_PROPERTY_NAME);
		}
		catch (ContextException e)
		{
			source = null;
		}
		
		return new PublisherTopic(source, topicPath);
	}
	
	final public MessageElement describe(QName elementName)
	{
		QName root = elementName;
		
		if (root == null)
			root = new QName(WSRFConstants.WSN_TOPIC_NS,
				"TopicSet", "wstop");
		
		MessageElement ret = new MessageElement(root);
		
		TopicForest forest = new TopicForest();
		
		for (TopicPath path : _topics)
			forest.addTopic(path);
		
		forest.describe(ret);
		
		return ret;
	}
	
	final public Collection<TopicPath> knownTopics()
	{
		return Collections.unmodifiableCollection(_topics);
	}
	
	static private Map<Class<?>, TopicSet> _knownTopicSets =
		new HashMap<Class<?>, TopicSet>();
	
	static private void forPublisher(TopicSet set, Class<?> publisher)
	{
		if ((publisher == null) || publisher.equals(Object.class))
			return;
		
		for (Class<?> iface : publisher.getInterfaces())
		{
			TopicSet parentSet = forPublisher(iface);
			set.addAllTopics(parentSet);
		}
		
		TopicSet parentSet = forPublisher(publisher.getSuperclass());
		set.addAllTopics(parentSet);
		
		for (Field field : publisher.getDeclaredFields())
		{
			WSNTopic annot = field.getAnnotation(WSNTopic.class);
			if (annot != null)
			{
				try
				{
					field.setAccessible(true);
					TopicPath tp = (TopicPath)field.get(null);
					field.setAccessible(false);
					set.addTopic(tp);
				}
				catch (IllegalAccessException e)
				{
					_logger.error(String.format(
						"Unable to access topic field %s.", field), e);
				}
			}
		}
	}
	
	static public TopicSet forPublisher(Class<?> publisher)
	{
		TopicSet ret = null;
		
		synchronized(_knownTopicSets)
		{
			ret = _knownTopicSets.get(publisher);
			if (ret == null)
			{
				ret = new TopicSet();
				forPublisher(ret, publisher);
				_knownTopicSets.put(publisher, ret);
			}
		}
		
		return ret;
	}
}