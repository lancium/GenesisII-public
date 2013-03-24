package edu.virginia.vcgr.genii.client.wsrf;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.PortType;

public interface WSRFConstants
{
	static public final String WSRF_RP_NS = "http://docs.oasis-open.org/wsrf/rp-2";
	static public final String WSRF_RL_NS = "http://docs.oasis-open.org/wsrf/rl-2";

	static public final String WSRF_RPW_NS = "http://docs.oasis-open.org/wsrf/rpw-2";
	static public final String WSRF_RLW_NS = "http://docs.oasis-open.org/wsrf/rlw-2";

	static public final String WSN_TOPIC_NS = "http://docs.oasis-open.org/wsrf/t-1";
	static public final String WSN_BASE_NOT_XSD_NS = "http://docs.oasis-open.org/wsn/b-2";
	static public final String WSN_BASE_NOT_NS = "http://docs.oasis-open.org/wsn/bw-2";
	static public final String WSN_BROKERED_NOT_NS = "http://docs.oasis-open.org/wsn/brw-2";

	static public final String WSRF_RPW_GET_RP_PORT_NAME = "GetResourceProperty";
	static public final String WSRF_RPW_GET_MULTIPLE_RP_PORT_NAME = "GetMultipleResourceProperties";
	static public final String WSRF_IMMEDIATE_TERMINATE_PORT_NAME = "ImmediateResourceTermination";
	static public final String WSRF_SCHEDULED_TERMINATE_PORT_NAME = "ScheduledResourceTermination";

	static public final PortType WSRF_RPW_GET_RP_PORT = PortType.get(new QName(WSRF_RPW_NS, WSRF_RPW_GET_RP_PORT_NAME));
	static public final PortType WSRF_RPW_GET_MULTIPLE_RP_PORT = PortType.get(new QName(WSRF_RPW_NS,
		WSRF_RPW_GET_MULTIPLE_RP_PORT_NAME));
	static public final PortType WSRF_RLW_IMMEDIATE_TERMINATE_PORT = PortType.get(new QName(WSRF_RLW_NS,
		WSRF_IMMEDIATE_TERMINATE_PORT_NAME));
	static public final PortType WSRF_RLW_SCHEDULED_TERMINATE_PORT = PortType.get(new QName(WSRF_RLW_NS,
		WSRF_SCHEDULED_TERMINATE_PORT_NAME));

	static public final PortType WSN_NOTIFICATION_CONSUMER_PORT = PortType.get(new QName(WSN_BASE_NOT_NS,
		"NotificationConsumer"));
	static public final PortType WSN_NOTIFICATION_PRODUCER_PORT = PortType.get(new QName(WSN_BASE_NOT_NS,
		"NotificationProducer"));
	static public final PortType WSN_PULL_POINT_PORT = PortType.get(new QName(WSN_BASE_NOT_NS, "PullPoint"));
	static public final PortType WSN_CREATE_PULL_POINT_PORT = PortType.get(new QName(WSN_BASE_NOT_NS, "CreatePullPoint"));
	static public final PortType WSN_SUBSCRIPTION_MANAGER_PORT = PortType
		.get(new QName(WSN_BASE_NOT_NS, "SubscriptionManager"));
	static public final PortType WSN_PAUSABLE_SUBSCRIPTION_MANAGER_PORT = PortType.get(new QName(WSN_BASE_NOT_NS,
		"PausableSubscriptionManager"));
	static public final PortType WSN_REGISTER_PUBLISHER_PORT = PortType
		.get(new QName(WSN_BROKERED_NOT_NS, "RegisterPublisher"));
	static public final PortType WSN_BROKERED_NOTIFICATION_PORT = PortType.get(new QName(WSN_BROKERED_NOT_NS,
		"NotificationBroker"));
	static public final PortType WSN_PUBLISHER_REGISTRATION_MANAGER_PORT = PortType.get(new QName(WSN_BROKERED_NOT_NS,
		"PublisherRegistrationManager"));

	static public final QName FIXED_TOPIC_SET_QNAME = new QName(WSN_BASE_NOT_XSD_NS, "FixedTopicSet");

	static public final QName TOPIC_EXPRESSION_DIALECT_RP = new QName(WSN_BASE_NOT_XSD_NS, "TopicExpressionDialect");

	static public final QName TOPIC_EXPRESSION_RP = new QName(WSN_BASE_NOT_XSD_NS, "TopicExpression");

	static public final QName TOPIC_SET_RP = new QName(WSN_BASE_NOT_XSD_NS, "TopicSet");
	static public final String XPATH_QUERY_EXPRESSION_DIALECT_STRING = "http://www.w3.org/TR/1999/REC-xpath-19991116";
}