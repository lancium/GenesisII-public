package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.JSDLConstants;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.OperatingSystemFacet;

public class DefaultOperatingSystemFacet extends DefaultPersonalityFacet implements OperatingSystemFacet
{
	@Override
	public void consumeDescription(Object currentUnderstanding, String description) throws JSDLException
	{
	}

	@Override
	public void consumeOperatingSystemVersion(Object currentUnderstanding, String operatingSystemVersion) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "OperatingSystemVersion"));
	}
}
