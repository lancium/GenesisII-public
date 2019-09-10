package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.GPUArchitectureEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLConstants;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.GPUArchitectureFacet;

public class DefaultGPUArchitectureFacet extends DefaultPersonalityFacet implements GPUArchitectureFacet
{
	static private Log _logger = LogFactory.getLog(DefaultGPUArchitectureFacet.class);
	@Override
	public void consumeGPUArchitectureName(Object currentUnderstanding, GPUArchitectureEnumeration gpuArchitectureName)
		throws JSDLException
	{
		_logger.info("---JSDL: ----- in DefaultGPUArchitectureFacet--------" );
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "GPUArchitectureName"));
	}
}
