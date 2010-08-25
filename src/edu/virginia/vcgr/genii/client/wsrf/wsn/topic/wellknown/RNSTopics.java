package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.WSNTopic;

public interface RNSTopics
{
	@WSNTopic(contentsType = RNSEntryAddedContents.class)
	static final public TopicPath RNS_ENTRY_ADDED_TOPIC = 
		TopicPath.createTopicPath(new QName(
			RNSConstants.GENII_RNS_NS, "RNSEntryAdded", "genii-rns"));
}