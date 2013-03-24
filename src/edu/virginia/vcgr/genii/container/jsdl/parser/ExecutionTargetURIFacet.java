package edu.virginia.vcgr.genii.container.jsdl.parser;

import java.net.URI;

import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultTargetURIFacet;

public class ExecutionTargetURIFacet extends DefaultTargetURIFacet
{
	@Override
	public void consumeURI(Object currentUnderstanding, URI uri)
	{
		ExecutionDataStagingFacet ds = (ExecutionDataStagingFacet) currentUnderstanding;

		ds.setTargetURI(uri.toString());
	}
}