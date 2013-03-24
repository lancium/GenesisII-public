package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import org.ggf.jsdl.OperatingSystemTypeEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultOperatingSystemTypeFacet;

public class CommonOperatingSystemTypeFacet extends DefaultOperatingSystemTypeFacet
{
	@Override
	public void consumeOperatingSystemName(Object currentUnderstanding, OperatingSystemTypeEnumeration operatingSystemType)
		throws JSDLException
	{
		/*
		 * This has been causing us problems with BES containers that front end other machines.
		 * Instead, we're just going to ignore it.
		 */
		/*
		 * if (operatingSystemType != null && !operatingSystemType.equals(
		 * JSDLUtils.getLocalOperatingSystemType().getOperatingSystemName())) throw new
		 * JSDLMatchException( "Requested operating system not supported.");
		 */
	}
}