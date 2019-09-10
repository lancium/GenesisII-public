package edu.virginia.vcgr.genii.client.jsdl.parser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.GPUArchitectureEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.Restrictions;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultGPUArchitectureFacet;

public class ExecutionGPUArchitectureFacet extends DefaultGPUArchitectureFacet
{
	static private Log _logger = LogFactory.getLog(ExecutionGPUArchitectureFacet.class);
	@Override
	public void consumeGPUArchitectureName(Object currentUnderstanding, GPUArchitectureEnumeration gpuArchitectureName)
		throws JSDLException
	{
		_logger.info("-------------------In the ExecutionGPUArchitectureFacet----------------");
		Restrictions restrictions = (Restrictions) currentUnderstanding;
		restrictions.setGPUArchitectureRestriction(gpuArchitectureName);
	}
}
