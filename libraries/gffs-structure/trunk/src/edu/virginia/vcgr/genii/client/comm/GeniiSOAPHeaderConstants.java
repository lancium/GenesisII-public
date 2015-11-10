package edu.virginia.vcgr.genii.client.comm;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public interface GeniiSOAPHeaderConstants
{
	static public final String GENII_ENDPOINT_NAME = "genesisII-endpoint";
	static public final String GENII_ENDPOINT_VERSION_NAME = "genesisII-version";

	static public final QName GENII_ENDPOINT_QNAME = new QName(GenesisIIConstants.GENESISII_NS, GENII_ENDPOINT_NAME);
	static public final QName GENII_ENDPOINT_VERSION = new QName(GenesisIIConstants.GENESISII_NS, GENII_ENDPOINT_VERSION_NAME);

	//hmmm: probably change the name of this shorthand thing.  cred streamlining is 'official' name of feature.
	static public final String GENII_CREDENTIAL_SHORTHAND_SUPPORTED_NAME = "cred-shorthand";
	static public final QName GENII_CREDENTIAL_SHORTHAND_SUPPORTED_QNAME = new QName(GenesisIIConstants.GENESISII_NS,
		GENII_CREDENTIAL_SHORTHAND_SUPPORTED_NAME);
}
