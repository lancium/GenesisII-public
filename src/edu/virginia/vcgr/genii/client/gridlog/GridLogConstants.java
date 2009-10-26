package edu.virginia.vcgr.genii.client.gridlog;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.PortType;

public interface GridLogConstants
{
	static public final String GRIDLOG_NS = 
		"http://vcgr.cs.virginia.edu/genii/gridlog";
	static public final String GRIDLOG_PORT_TYPE_NAME = "GridLogPortType";
	
	static public final PortType GRIDLOG_PORT_TYPE =
		PortType.get(new QName(GRIDLOG_NS, GRIDLOG_PORT_TYPE_NAME));
	
	static public final String CONTEXT_PARAMTER_NAME =
		"Grid Log Context";
}