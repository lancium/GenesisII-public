package edu.virginia.vcgr.genii.client.rns;

import javax.xml.namespace.QName;

public interface RNSConstants
{
	static public final String GENII_RNS_NS = "http://vcgr.cs.virginia.edu/container/2008/04/enhanced-rns";

	static public final String RESOLVED_ENTRY_UNBOUND_PROPERTY = "rns-resolved-entry-unbound-property";

	static final public int PREFERRED_BATCH_SIZE = 100;

	static final public QName ELEMENT_COUNT_QNAME = new QName(GENII_RNS_NS, "elementCount");
}
