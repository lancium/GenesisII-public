package edu.virginia.vcgr.genii.container.notification;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public class NotificationBrokerConstants
{

	public static final String RNS_CONTENT_CHANGE_SUBSCRIPTION = "rnsContentChangeSubscription";
	public static final String BYTEIO_ATTRIBUTE_CHANGE_SUBSCRIPTION = "byteIOAttributeChangeSubscription";
	public static final String RESOURCE_AUTHORIZATION_CHANGE_SUBSCRIPTION = "resourceAuthChangeSubscription";

	public static final QName INDIRECT_SUBSCRIPTION_TYPE = new QName(GenesisIIConstants.ENHANCED_NOTIFICATION_BROKER_NS,
		"SubscriptionType");

	public static final QName MESSAGE_INDEX_QNAME = new QName(GenesisIIConstants.ENHANCED_NOTIFICATION_BROKER_NS,
		"MessageIndex");
}
