package edu.virginia.vcgr.genii.client.jsdl.personality;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface ApplicationFacet extends PersonalityFacet
{
	public void consumeApplicationName(Object currentUnderstanding, String applicationName) throws JSDLException;

	public void consumeApplicationVersion(Object currentUnderstanding, String applicationVersion) throws JSDLException;

	public void consumeDescription(Object currentUnderstanding, String description) throws JSDLException;
}
