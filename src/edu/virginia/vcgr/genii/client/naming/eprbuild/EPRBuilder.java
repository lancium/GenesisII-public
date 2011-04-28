package edu.virginia.vcgr.genii.client.naming.eprbuild;

import java.net.URI;
import java.util.Collection;

import org.w3c.dom.Element;
import org.ws.addressing.EndpointReferenceType;

public interface EPRBuilder
{
	public URI address();
	public void address(URI address);
	
	public void addReferenceParameters(Element...referenceParameters);
	public Collection<Element> referenceParameters();
	
	public void addMetadata(Element...metadata);
	public Collection<Element> metadata();
	
	public EndpointReferenceType mint();
}