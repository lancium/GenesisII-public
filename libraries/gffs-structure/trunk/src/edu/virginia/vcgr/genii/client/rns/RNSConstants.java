package edu.virginia.vcgr.genii.client.rns;

import javax.xml.namespace.QName;

public interface RNSConstants
{
	static public final String GENII_RNS_NS = "http://vcgr.cs.virginia.edu/container/2008/04/enhanced-rns";

	static public final String RESOLVED_ENTRY_UNBOUND_PROPERTY = "rns-resolved-entry-unbound-property";

	/*
	 * hmmm: this value is a tweaking point. originally 100, then 500, now back to 100. we are
	 * seeing greater memory usage on clients than we think we should, so it's been cut back to 100.
	 * --cak.
	 */
	static final public int PREFERRED_BATCH_SIZE = 100;

	static final public QName ELEMENT_COUNT_QNAME = new QName(GENII_RNS_NS, "elementCount");
}
