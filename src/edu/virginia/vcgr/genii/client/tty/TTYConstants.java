package edu.virginia.vcgr.genii.client.tty;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.PortType;

public interface TTYConstants
{
	static final public String TTY_NS =
		"http://vcgr.cs.virginia.edu/tty/2008/03/tty";
	static final public String TTY_PORT_TYPE_NAME =
		"TTYPortType";
	
	static final public PortType TTY_PORT_TYPE =
		PortType.get(new QName(TTY_NS, TTY_PORT_TYPE_NAME));
	
	static public final String TTY_CALLING_CONTEXT_PROPERTY =
		"edu.virginia.vcgr.genii.tty.calling-context";
}