package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLMatchException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLUtils;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultOperatingSystemFacet;

public class CommonOperatingSystemFacet extends DefaultOperatingSystemFacet
{
	@Override
	public void consumeOperatingSystemVersion(Object currentUnderstanding, String operatingSystemVersion) throws JSDLException
	{
		if (operatingSystemVersion != null
			&& !operatingSystemVersion.equals(JSDLUtils.getLocalOperatingSystem().getOperatingSystemVersion()))
			throw new JSDLMatchException("Operating system versions don't match.");
	}
}