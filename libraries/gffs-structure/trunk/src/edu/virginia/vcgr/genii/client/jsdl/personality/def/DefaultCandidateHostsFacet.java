package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.JSDLConstants;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.CandidateHostsFacet;

public class DefaultCandidateHostsFacet extends DefaultPersonalityFacet implements CandidateHostsFacet
{
	@Override
	public void consumeHostName(Object currentUnderstanding, String hostname) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "HostName"));
	}
}
