package edu.virginia.vcgr.genii.client.comm;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public interface GeniiSOAPHeaderConstants
{
	static public final String GENII_ENDPOINT_NAME = "genesisII-endpoint";
	static public final QName GENII_ENDPOINT_QNAME = new QName(GenesisIIConstants.GENESISII_NS, GENII_ENDPOINT_NAME);

	static public final String GENII_ENDPOINT_VERSION_NAME = "genesisII-version";
	static public final QName GENII_ENDPOINT_VERSION_QNAME = new QName(GenesisIIConstants.GENESISII_NS, GENII_ENDPOINT_VERSION_NAME);

	// credential streamlining allows the client to only send the "new" parts of delegation chains, rather than always
	// including the full chains where the container had already seen the beginning pieces.
	static public final String GENII_SUPPORTS_CREDENTIAL_STREAMLINING_NAME = "genesisII-cred-streamline";
	static public final QName GENII_SUPPORTS_CREDENTIAL_STREAMLINING_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, GENII_SUPPORTS_CREDENTIAL_STREAMLINING_NAME);
}
