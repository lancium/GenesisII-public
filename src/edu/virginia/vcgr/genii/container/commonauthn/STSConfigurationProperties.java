package edu.virginia.vcgr.genii.container.commonauthn;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public interface STSConfigurationProperties
{

	// property that indicates that the resource being created is a replica of some other
	// resource in a different container
	public static QName REPLICA_STS_CONSTRUCTION_PARAM = new QName(GenesisIIConstants.GENESISII_NS, "replica-sts");

	// a flag directing to avoid linking a replicated resource under the service RNS directory
	public static QName LINK_TO_SERVICE_DIR_CONSTRUCTION_PARAM = new QName(GenesisIIConstants.GENESISII_NS,
		"link-sts-to-service-dir");

	// represents the EPR from which the resource certificate should be retrieved instead of
	// creating a new resource certificate for a replica
	public static QName CERTIFICATE_OWNER_EPR = new QName(GenesisIIConstants.GENESISII_NS, "certificate-owner-epr");
}