package edu.virginia.vcgr.genii.client.bes;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.PortType;

public class BESActivityConstants
{
	public final String GENII_BES_ACTIVITY_NS = "http://vcgr.cs.virginia.edu/bes/2006/06/bes-activity";
	public final String GENII_BES_ACTIVITY_PORT_TYPE_NAME = "BESActivityPortType";

	public final PortType GENII_BES_ACTIVITY_PORT_TYPE()
	{
		return PortType.portTypeFactory().get(new QName(GENII_BES_ACTIVITY_NS, GENII_BES_ACTIVITY_PORT_TYPE_NAME));
	}

	public final QName STATUS_ATTR = new QName(GENII_BES_ACTIVITY_NS, "Status");
}