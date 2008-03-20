package edu.virginia.vcgr.genii.client.bes;

import javax.xml.namespace.QName;

public interface BESActivityConstants
{
	static public final String GENII_BES_ACTIVITY_NS =
		"http://vcgr.cs.virginia.edu/bes/2006/06/bes-activity";
	static public final String GENII_BES_ACTIVITY_PORT_TYPE =
		"BESActivityPortType";
	static public final QName GENII_BES_ACTIVITY_PORT_TYPE_QNAME =
		new QName(GENII_BES_ACTIVITY_NS, GENII_BES_ACTIVITY_PORT_TYPE);
	
	static public final QName STATUS_ATTR = new QName(
		GENII_BES_ACTIVITY_NS, "Status");
}