package edu.virginia.vcgr.genii.client.pipe;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.PortType;

public interface PipeConstants
{
	static final public String PIPE_NS = "http://vcgr.cs.virginia.edu/pipe/2011/03/pipe";
	static final public String PIPE_PORT_TYPE_NAME = "PipePortType";

	static final public PortType PIPE_PORT_TYPE = PortType.get(new QName(PIPE_NS, PIPE_PORT_TYPE_NAME));
}
