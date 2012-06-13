package edu.virginia.vcgr.genii.container.notification;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.WSNTopic;

public interface NotificationBrokerTopics {

	@WSNTopic(contentsType = TestNotificationMessageContents.class)
	static final public TopicPath TEST_NOTIFICAION_TOPIC = 
		TopicPath.createTopicPath(new QName(
			GenesisIIConstants.ENHANCED_NOTIFICATION_BROKER_NS, "TestNotification", "genii-enhanced-notification"));
}
