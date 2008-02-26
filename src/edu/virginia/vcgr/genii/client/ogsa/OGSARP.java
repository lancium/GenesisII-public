package edu.virginia.vcgr.genii.client.ogsa;

import java.util.Calendar;

import javax.xml.namespace.QName;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.rp.ResourceProperty;

public interface OGSARP extends OGSAWSRFBPConstants
{
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
	
	@ResourceProperty(namespace = WSRF_RL_NS, localname = CURRENT_TIME_ATTR)
	public Calendar getCurrentTime();
	
	@ResourceProperty(namespace = OGSA_WSRF_BP_NS, localname = RESOURCE_ENDPOINT_REFERENCE_ATTR)
	public EndpointReferenceType getResourceEndpoint();
	
	@ResourceProperty(namespace = WSRF_RL_NS, localname = TERMINATION_TIME_ATTR)
	public Calendar getTerminationTime();
	
	@ResourceProperty(namespace = OGSA_WSRF_BP_NS,
		localname = WS_RESOURCE_INTERFACES_ATTR,
		translator = QNameListTranslator.class)
	public OGSAQNameList getWSResourceInterfaces();
	
	@ResourceProperty(namespace = OGSA_WSRF_BP_NS,
		localname = RESOURCE_PROPERTY_NAMES_ATTR,
		translator = QNameListTranslator.class)
	public OGSAQNameList getResourcePropertyNames();
}