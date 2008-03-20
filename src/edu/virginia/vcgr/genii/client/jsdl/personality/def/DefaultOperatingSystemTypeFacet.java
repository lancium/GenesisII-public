package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import javax.xml.namespace.QName;

import org.ggf.jsdl.OperatingSystemTypeEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLConstants;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.OperatingSystemTypeFacet;

public class DefaultOperatingSystemTypeFacet extends DefaultPersonalityFacet
		implements OperatingSystemTypeFacet
{
	@Override
	public void consumeOperatingSystemName(Object currentUnderstanding,
			OperatingSystemTypeEnumeration operatingSystemType)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLConstants.JSDL_NS, "OperatingSystemName"));
	}
}
