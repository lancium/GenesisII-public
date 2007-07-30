package edu.virginia.vcgr.genii.client.queue;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;

public class QueueConstants
{
	static public QName RESOURCE_SLOTS_QNAME =
		new QName(WellKnownPortTypes.QUEUE_PORT_TYPE.getNamespaceURI(),
			"total-slots");
}