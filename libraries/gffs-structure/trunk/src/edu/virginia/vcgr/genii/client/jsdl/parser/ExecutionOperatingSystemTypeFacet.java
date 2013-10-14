package edu.virginia.vcgr.genii.client.jsdl.parser;

import org.ggf.jsdl.OperatingSystemTypeEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.Restrictions;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultOperatingSystemTypeFacet;

public class ExecutionOperatingSystemTypeFacet extends DefaultOperatingSystemTypeFacet
{
	@Override
	public void consumeOperatingSystemName(Object currentUnderstanding, OperatingSystemTypeEnumeration operatingSystemType)
		throws JSDLException
	{
		Restrictions restrictions = (Restrictions) currentUnderstanding;
		restrictions.setOperatingSystemTypeRestriction(operatingSystemType);
	}
}