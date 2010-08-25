package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.bes.BESConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.WSNTopic;

public interface BESActivityTopics
{
	@WSNTopic(contentsType = BESActivityStateChangedContents.class)
	static final public TopicPath ACTIVITY_STATE_CHANGED_TOPIC = 
		TopicPath.createTopicPath(new QName(
			BESConstants.GENII_BES_NS, "ActivityStateChanged", "genii-bes"));
	
	@WSNTopic(contentsType = BESActivityStateChangedContents.class)
	static final public TopicPath ACTIVITY_STATE_CHANGED_TO_FINAL_TOPIC =
		TopicPath.createTopicPath(ACTIVITY_STATE_CHANGED_TOPIC,
			"ReachedFinalState");
}