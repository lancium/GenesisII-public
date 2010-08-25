package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.wsrf.WSRFConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.WSNTopic;

public interface GenesisIIBaseTopics
{
	@WSNTopic(contentsType = ResourceTerminationContents.class)
	static final public TopicPath RESOURCE_TERMINATION_TOPIC =
		TopicPath.createTopicPath(new QName(WSRFConstants.WSRF_RL_NS, 
			"ResourceTermination", "wsrl"));
}