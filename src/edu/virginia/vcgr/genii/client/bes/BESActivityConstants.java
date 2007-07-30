package edu.virginia.vcgr.genii.client.bes;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;

public class BESActivityConstants
{
	static public QName STATUS_ATTR = new QName(
		WellKnownPortTypes.VCGR_BES_ACTIVITY_SERVICE_PORT_TYPE.getNamespaceURI(), 
		"Status");
}