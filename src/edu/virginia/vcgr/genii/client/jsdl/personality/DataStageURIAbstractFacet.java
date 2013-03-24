package edu.virginia.vcgr.genii.client.jsdl.personality;

import java.net.URI;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface DataStageURIAbstractFacet extends PersonalityFacet
{
	public void consumeURI(Object currentUnderstanding, URI uri) throws JSDLException;
}
