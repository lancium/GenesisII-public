package edu.virginia.vcgr.genii.client.jsdl.parser;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.Restrictions;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultOperatingSystemFacet;

public class ExecutionOperatingSystemFacet extends DefaultOperatingSystemFacet
{
	@Override
	public void consumeOperatingSystemVersion(Object currentUnderstanding, String operatingSystemVersion) throws JSDLException
	{
		Restrictions restrictions = (Restrictions) currentUnderstanding;
		restrictions.setOperatingSystemVersionRestriction(operatingSystemVersion);
	}
}