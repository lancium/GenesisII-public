package edu.virginia.vcgr.genii.container.jsdl.parser;

import org.ggf.jsdl.ProcessorArchitectureEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultCPUArchitectureFacet;
import edu.virginia.vcgr.genii.container.jsdl.Restrictions;

public class ExecutionCPUArchitectureFacet extends DefaultCPUArchitectureFacet
{
	@Override
	public void consumeCPUArchitectureName(Object currentUnderstanding,
		ProcessorArchitectureEnumeration cpuArchitectureName)
			throws JSDLException
	{
		Restrictions restrictions = (Restrictions)currentUnderstanding;
		restrictions.setProcessorArchitectureRestriction(cpuArchitectureName);
	}
}