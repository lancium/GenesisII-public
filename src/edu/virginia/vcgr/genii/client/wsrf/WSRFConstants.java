package edu.virginia.vcgr.genii.client.wsrf;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.PortType;

public interface WSRFConstants
{
	static public final String WSRF_RPW_NS = "http://docs.oasis-open.org/wsrf/rpw-2";
	static public final String WSRF_RLW_NS = "http://docs.oasis-open.org/wsrf/rlw-2";
	
	static public final String WSRF_RPW_GET_RP_PORT_NAME = "GetResourceProperty";
	static public final String WSRF_RPW_GET_MULTIPLE_RP_PORT_NAME = "GetMultipleResourceProperties";
	static public final String WSRF_IMMEDIATE_TERMINATE_PORT_NAME = "ImmediateResourceTermination";
	static public final String WSRF_SCHEDULED_TERMINATE_PORT_NAME = "ScheduledResourceTermination";
	
	static public final PortType WSRF_RPW_GET_RP_PORT = PortType.get(new QName(
		WSRF_RPW_NS, WSRF_RPW_GET_RP_PORT_NAME));
	static public final PortType WSRF_RPW_GET_MULTIPLE_RP_PORT = 
		PortType.get(new QName(
			WSRF_RPW_NS, WSRF_RPW_GET_MULTIPLE_RP_PORT_NAME));
	static public final PortType WSRF_RLW_IMMEDIATE_TERMINATE_PORT = 
		PortType.get(new QName(
			WSRF_RLW_NS, WSRF_IMMEDIATE_TERMINATE_PORT_NAME));
	static public final PortType WSRF_RLW_SCHEDULED_TERMINATE_PORT = 
		PortType.get(new QName(
			WSRF_RLW_NS, WSRF_SCHEDULED_TERMINATE_PORT_NAME));
	
	static public final String XPATH_QUERY_EXPRESSION_DIALECT_STRING =
		"http://www.w3.org/TR/1999/REC-xpath-19991116";
}