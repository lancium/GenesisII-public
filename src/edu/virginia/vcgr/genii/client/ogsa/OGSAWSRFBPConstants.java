package edu.virginia.vcgr.genii.client.ogsa;

import javax.xml.namespace.QName;

public interface OGSAWSRFBPConstants
{
	static public final String WSRF_RL_NS =
		"http://docs.oasis-open.org/wsrf/rl-2";
	
	static public final String OGSA_WSRF_BP_NS =
		"http://schemas.ggf.org/ogsa/2006/05/wsrf-bp";
	
	static public final String CURRENT_TIME_ATTR = "CurrentTime";
	static public final String RESOURCE_ENDPOINT_REFERENCE_ATTR =
		"ResourceEndpointReference";
	static public final String TERMINATION_TIME_ATTR = "TerminationTime";
	static public final String WS_RESOURCE_INTERFACES_ATTR = "WSResourceInterfaces";
	static public final String RESOURCE_PROPERTY_NAMES_ATTR = "ResourcePropertyNames";
	
	static public final QName CURRENT_TIME_ATTR_QNAME = new QName(
		WSRF_RL_NS, CURRENT_TIME_ATTR);
	static public final QName RESOURCE_ENDPOINT_REFERENCE_ATTR_QNAME =
		new QName(OGSA_WSRF_BP_NS, RESOURCE_ENDPOINT_REFERENCE_ATTR);
	static public final QName TERMINATION_TIME_ATTR_QNAME = new QName(
		WSRF_RL_NS, TERMINATION_TIME_ATTR);
	static public final QName WS_RESOURCE_INTERFACES_ATTR_QNAME = new QName(
		OGSA_WSRF_BP_NS, WS_RESOURCE_INTERFACES_ATTR);
	static public final QName RESOURCE_PROPERTY_NAMES_ATTR_QNAME = new QName(
		OGSA_WSRF_BP_NS, RESOURCE_PROPERTY_NAMES_ATTR);
}