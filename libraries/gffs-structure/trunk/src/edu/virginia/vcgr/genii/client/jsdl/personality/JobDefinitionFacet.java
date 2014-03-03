package edu.virginia.vcgr.genii.client.jsdl.personality;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface JobDefinitionFacet extends PersonalityFacet
{
	public void consumeID(Object currentUnderstanding, String id) throws JSDLException;
}
