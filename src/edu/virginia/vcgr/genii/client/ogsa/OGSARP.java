package edu.virginia.vcgr.genii.client.ogsa;

import java.util.Calendar;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.rp.ResourceProperty;

/**
 * This is the RP interface for getting OGSA BP resource
 * properties.
 * 
 * @author mmm2a
 */
public interface OGSARP extends OGSAWSRFBPConstants
{
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