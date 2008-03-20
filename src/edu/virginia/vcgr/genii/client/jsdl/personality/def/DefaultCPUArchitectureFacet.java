package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import javax.xml.namespace.QName;

import org.ggf.jsdl.ProcessorArchitectureEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLConstants;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.CPUArchitectureFacet;

public class DefaultCPUArchitectureFacet extends DefaultPersonalityFacet
		implements CPUArchitectureFacet
{
	@Override
	public void consumeCPUArchitectureName(Object currentUnderstanding,
			ProcessorArchitectureEnumeration cpuArchitectureName)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(
			JSDLConstants.JSDL_NS, "CPUArchitectureName"));
	}
}
