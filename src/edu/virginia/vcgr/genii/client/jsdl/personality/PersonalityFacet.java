package edu.virginia.vcgr.genii.client.jsdl.personality;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface PersonalityFacet
{
	public Object createFacetUnderstanding(Object parentUnderstanding)
		throws JSDLException;
	
	public void completeFacet(Object parentUnderstanding,
		Object facetUnderstanding) throws JSDLException;
	
	public void consumeAny(Object currentUnderstanding,
		MessageElement any) throws JSDLException;
}
