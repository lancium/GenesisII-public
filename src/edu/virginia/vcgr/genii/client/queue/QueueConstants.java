package edu.virginia.vcgr.genii.client.queue;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.PortType;

public interface QueueConstants
{
	static final public String QUEUE_NS = "http://vcgr.cs.virginia.edu/genii/queue";
	static final public String QUEUE_PORT_TYPE_NAME = "QueuePortType";
	static final public String CURRENT_RESOURCE_INFORMATION_NAME = "current-resource-information";
	
	static final public QName CURRENT_RESOURCE_INFORMATION_QNAME = new QName(
		QUEUE_NS, CURRENT_RESOURCE_INFORMATION_NAME);
	
	static final public PortType QUEUE_PORT_TYPE =
		PortType.get(new QName(QUEUE_NS, QUEUE_PORT_TYPE_NAME));
	
	static public QName RESOURCE_SLOTS_QNAME =
		new QName(QUEUE_NS, "total-slots");
	
	static final public String ATTEMPT_NUMBER_HISTORY_PROPERTY =
		"Attempt Number";
	static final public String QUEUE_STARTED_HISTORY_PROPERTY =
		"Queue Started";
	
	static final public int PREFERRED_BATCH_SIZE = 100;
}