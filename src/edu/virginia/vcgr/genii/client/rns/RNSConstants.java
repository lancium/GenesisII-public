package edu.virginia.vcgr.genii.client.rns;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.PortType;

public interface RNSConstants
{
	static public final String GENII_RNS_NS =
		"http://vcgr.cs.virginia.edu/container/2008/04/enhanced-rns";
	
	static public final String RESOLVED_ENTRY_UNBOUND_PROPERTY =
		"rns-resolved-entry-unbound-property";
	static public final String RESOLVED_ENTRY_UNBOUND_TRUE =
		"TRUE";
	static public final String RESOLVED_ENTRY_UNBOUND_FALSE =
		"FALSE";
	
	static public PortType RNS_PORT_TYPE =
		PortType.get(
			new QName("http://schemas.ggf.org/rns/2006/05/rns",
			"RNSPortType"));
	static public PortType ENHANCED_RNS_PORT_TYPE =
		PortType.get(new QName(GENII_RNS_NS,
		"EnhancedRNSPortType"));
	
	static final public int PREFERRED_BATCH_SIZE = 100;
}
