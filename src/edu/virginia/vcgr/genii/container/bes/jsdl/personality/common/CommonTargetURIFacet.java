package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.net.URI;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultTargetURIFacet;

public class CommonTargetURIFacet extends DefaultTargetURIFacet
{
	@Override
	public void consumeURI(Object currentUnderstanding, URI uri) throws JSDLException
	{
		((DataStagingUnderstanding) currentUnderstanding).setTargetURI(uri);
	}
}