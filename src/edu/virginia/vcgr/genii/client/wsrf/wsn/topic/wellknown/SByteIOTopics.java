package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.byteio.StreamableByteIORP;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.WSNTopic;

public interface SByteIOTopics extends GenesisIIBaseTopics
{
	@WSNTopic(contentsType = ResourceTerminationContents.class)
	static final public TopicPath SBYTEIO_INSTANCE_DYING =
		TopicPath.createTopicPath(RESOURCE_TERMINATION_TOPIC,
			new QName(StreamableByteIORP.STREAMABLE_BYTEIO_NS,
				"SByteIOInstanceDying", "sbyteio"));
}
