package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.GeniiPropertyFacet;

public class DefaultGeniiPropertyFacet extends DefaultPersonalityFacet implements GeniiPropertyFacet
{
	@Override
	public void consumeProperty(Object currentUnderstanding, String propertyName, String propertyValue) throws JSDLException
	{
		// By default we just ignore these for now.
	}
}