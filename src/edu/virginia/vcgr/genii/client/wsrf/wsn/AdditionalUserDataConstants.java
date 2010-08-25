package edu.virginia.vcgr.genii.client.wsrf.wsn;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public interface AdditionalUserDataConstants
{
	static final public String NS = GenesisIIConstants.GENESISII_NS;
	static final public String ELEMENT_NAME = "AdditionalUserData";
	static final public QName ELEMENT_QNAME = new QName(
		NS, ELEMENT_NAME);
}
