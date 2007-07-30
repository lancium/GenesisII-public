package edu.virginia.vcgr.genii.container.common.notification;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.container.resource.IResource;

public interface ISubscriptionResource extends IResource
{
	static public QName TARGET_ENDPOINT_CONSTRUCTION_PARAMTER =
		new QName(GenesisIIConstants.GENESISII_NS, "subscription-target");
	static public QName TOPIC_CONSTRUCTION_PARAMETER =
		new QName(GenesisIIConstants.GENESISII_NS, "subscription-topic");
	static public QName SOURCE_KEY_CONSTRUCTION_PARAMETER =
		new QName(GenesisIIConstants.GENESISII_NS, "source-key");
	static public QName USER_DATA_CONSTRUCTION_PARAMETER =
		new QName(GenesisIIConstants.GENESISII_NS, "user-data");
}