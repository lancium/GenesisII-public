package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import java.net.URI;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.JSDLConstants;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.TargetURIFacet;

public class DefaultTargetURIFacet extends DefaultPersonalityFacet implements
		TargetURIFacet
{
	@Override
	public void consumeURI(Object currentUnderstanding, URI uri)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLConstants.JSDL_NS, "Target"));
	}
}
