package edu.virginia.vcgr.genii.client.jsdl.personality;

import org.ggf.jsdl.GPUArchitectureEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface GPUArchitectureFacet extends PersonalityFacet
{
	public void consumeGPUArchitectureName(Object currentUnderstanding, GPUArchitectureEnumeration gpuArchitectureName)
		throws JSDLException;
}
