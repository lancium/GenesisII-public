package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.ApplicationFacet;

public class DefaultApplicationFacet extends DefaultPersonalityFacet implements ApplicationFacet
{
	@Override
	public void consumeApplicationName(Object currentUnderstanding, String applicationName) throws JSDLException
	{
	}

	@Override
	public void consumeApplicationVersion(Object currentUnderstanding, String applicationVersion) throws JSDLException
	{
	}

	@Override
	public void consumeDescription(Object currentUnderstanding, String description) throws JSDLException
	{
	}
}
