package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.WSNTopic;

public interface RNSTopics
{
	@WSNTopic(contentsType = RNSOperationContents.class)
	static final public TopicPath RNS_OPERATION_TOPIC = 
		TopicPath.createTopicPath(new QName(
			RNSConstants.GENII_RNS_NS, "RNSOperation", "genii-rns"));

	@WSNTopic(contentsType = RNSContentChangeNotification.class)
	static final public TopicPath RNS_CONTENT_CHANGE_TOPIC = 
	TopicPath.createTopicPath(new QName(
			RNSConstants.GENII_RNS_NS, "RNSContentChanged", "genii-rns"));
}