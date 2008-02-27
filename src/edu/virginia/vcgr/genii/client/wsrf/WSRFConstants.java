package edu.virginia.vcgr.genii.client.wsrf;

import javax.xml.namespace.QName;

public interface WSRFConstants
{
	static public final String WSRF_RPW_NS = "http://docs.oasis-open.org/wsrf/rpw-2";
	static public final String WSRF_RLW_NS = "http://docs.oasis-open.org/wsrf/rlw-2";
	
	static public final String WSRF_RPW_GET_RP_PORT = "GetResourceProperty";
	static public final String WSRF_RPW_GET_MULTIPLE_RP_PORT = "GetMultipleResourceProperties";
	static public final String WSRF_IMMEDIATE_TERMINATE_PORT = "ImmediateResourceTermination";
	static public final String WSRF_SCHEDULED_TERMINATE_PORT = "ScheduledResourceTermination";
	
	static public final QName WSRF_RPW_GET_RP_PORT_QNAME = new QName(
		WSRF_RPW_NS, WSRF_RPW_GET_RP_PORT);
	static public final QName WSRF_RPW_GET_MULTIPLE_RP_PORT_QNAME = new QName(
		WSRF_RPW_NS, WSRF_RPW_GET_MULTIPLE_RP_PORT);
	static public final QName WSRF_RLW_IMMEDIATE_TERMINATE_PORT_QNAME = new QName(
		WSRF_RLW_NS, WSRF_IMMEDIATE_TERMINATE_PORT);
	static public final QName WSRF_RLW_SCHEDULED_TERMINATE_PORT_QNAME = new QName(
		WSRF_RLW_NS, WSRF_SCHEDULED_TERMINATE_PORT);
	
	static public final String XPATH_QUERY_EXPRESSION_DIALECT_STRING =
		"http://www.w3.org/TR/1999/REC-xpath-19991116";
}