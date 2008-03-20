package edu.virginia.vcgr.genii.client.jsdl.personality;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface CandidateHostsFacet extends PersonalityFacet
{
	public void consumeHostName(Object currentUnderstanding,
		String hostname) throws JSDLException;
}
