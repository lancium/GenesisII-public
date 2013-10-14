package edu.virginia.vcgr.genii.client.jsdl.personality;

import org.ggf.jsdl.OperatingSystemTypeEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface OperatingSystemTypeFacet extends PersonalityFacet
{
	public void consumeOperatingSystemName(Object currentUnderstanding, OperatingSystemTypeEnumeration operatingSystemType)
		throws JSDLException;
}
