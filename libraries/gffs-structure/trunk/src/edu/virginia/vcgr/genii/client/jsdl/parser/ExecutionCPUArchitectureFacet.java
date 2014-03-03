package edu.virginia.vcgr.genii.client.jsdl.parser;

import org.ggf.jsdl.ProcessorArchitectureEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.Restrictions;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultCPUArchitectureFacet;

public class ExecutionCPUArchitectureFacet extends DefaultCPUArchitectureFacet {
	@Override
	public void consumeCPUArchitectureName(Object currentUnderstanding,
			ProcessorArchitectureEnumeration cpuArchitectureName)
			throws JSDLException {
		Restrictions restrictions = (Restrictions) currentUnderstanding;
		restrictions.setProcessorArchitectureRestriction(cpuArchitectureName);
	}
}