package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import java.net.URI;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.JSDLConstants;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.SourceURIFacet;

public class DefaultSourceURIFacet extends DefaultPersonalityFacet implements
		SourceURIFacet
{
	@Override
	public void consumeURI(Object currentUnderstanding, URI uri)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLConstants.JSDL_NS, "Source"));
	}
}
