package edu.virginia.vcgr.genii.container.security.authz.providers;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.WSNTopic;

public interface GamlAclTopics
{
	@WSNTopic(contentsType = GamlAclChangeContents.class)
	static final public TopicPath GAML_ACL_CHANGE_TOPIC = 
		TopicPath.createTopicPath(new QName(GamlAclChangeContents.GAML_ACL_NAMESPACE,
				"GamlAclChange"));
}
