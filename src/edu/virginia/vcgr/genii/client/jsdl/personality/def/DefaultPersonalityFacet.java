package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.PersonalityFacet;

public class DefaultPersonalityFacet implements PersonalityFacet
{
	@Override
	public void completeFacet(
		Object parentUnderstanding, Object currentUnderstanding)
			throws JSDLException
	{
	}

	@Override
	public void consumeAny(Object currentUnderstanding,
			MessageElement any) throws JSDLException
	{
		throw new UnsupportedJSDLElement(any.getQName());
	}

	@Override
	public Object createFacetUnderstanding(Object parentUnderstanding)
			throws JSDLException
	{
		return parentUnderstanding;
	}
}
