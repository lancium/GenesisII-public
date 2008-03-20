package edu.virginia.vcgr.genii.client.jsdl.personality;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface OperatingSystemFacet extends PersonalityFacet
{
	public void consumeOperatingSystemVersion(
		Object currentUnderstanding, String operatingSystemVersion) 
			throws JSDLException;
	public void consumeDescription(Object currentUnderstanding,
		String description) throws JSDLException;
}
