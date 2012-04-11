package edu.virginia.vcgr.genii.container.resolver;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.WSNTopic;

public interface ResolverTopics
{
	@WSNTopic(contentsType = ResolverUpdateContents.class)
	static final public TopicPath RESOLVER_UPDATE_TOPIC = 
		TopicPath.createTopicPath(new QName(
			ResolverUpdateContents.RESOLVER_NAMESPACE, "ResolverUpdate"));
}
