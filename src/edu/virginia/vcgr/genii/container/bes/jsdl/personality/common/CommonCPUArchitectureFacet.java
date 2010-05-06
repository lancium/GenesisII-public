package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import org.ggf.jsdl.ProcessorArchitectureEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultCPUArchitectureFacet;

public class CommonCPUArchitectureFacet extends DefaultCPUArchitectureFacet
{
	@Override
	public void consumeCPUArchitectureName(Object currentUnderstanding,
			ProcessorArchitectureEnumeration cpuArchitectureName)
			throws JSDLException
	{
		/* This has been causing us problems with BES containers that front
		 * end other machines.  Instead, we're just going to ignore it.
		 */
		/*
		if (!JSDLUtils.getLocalCPUArchitecture().getCPUArchitectureName().equals(cpuArchitectureName))
			throw new JSDLMatchException(String.format(
				"Can't match requested arch (%s) against local arch (%s).",
				cpuArchitectureName, JSDLUtils.getLocalCPUArchitecture().getCPUArchitectureName()));
		*/
	}
}