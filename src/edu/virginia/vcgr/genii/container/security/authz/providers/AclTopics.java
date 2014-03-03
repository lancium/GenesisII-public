package edu.virginia.vcgr.genii.container.security.authz.providers;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.WSNTopic;

public interface AclTopics
{
	@WSNTopic(contentsType = AclChangeContents.class)
	static final public TopicPath GENII_ACL_CHANGE_TOPIC = TopicPath.createTopicPath(new QName(
		AclChangeContents.GENII_ACL_NAMESPACE, "SamlAclChange"));
}
