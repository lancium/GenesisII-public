package edu.virginia.vcgr.genii.client.jsdl.personality;

import org.ggf.jsdl.ProcessorArchitectureEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface CPUArchitectureFacet extends PersonalityFacet
{
	public void consumeCPUArchitectureName(Object currentUnderstanding, ProcessorArchitectureEnumeration cpuArchitectureName)
		throws JSDLException;
}
