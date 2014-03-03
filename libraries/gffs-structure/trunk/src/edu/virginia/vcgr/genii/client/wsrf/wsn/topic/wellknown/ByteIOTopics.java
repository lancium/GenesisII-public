package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.WSNTopic;

public interface ByteIOTopics
{
	@WSNTopic(contentsType = ByteIOContentsChangedContents.class)
	static final public TopicPath BYTEIO_CONTENTS_CHANGED_TOPIC = TopicPath.createTopicPath(new QName(
		ByteIOConstants.BYTEIO_NS, "ContentsChanged", "byteio"));

	@WSNTopic(contentsType = ByteIOAttributesUpdateNotification.class)
	static final public TopicPath BYTEIO_ATTRIBUTES_UPDATE_TOPIC = TopicPath.createTopicPath(new QName(
		ByteIOConstants.BYTEIO_NS, "AttributesUpdated", "byteio"));
}