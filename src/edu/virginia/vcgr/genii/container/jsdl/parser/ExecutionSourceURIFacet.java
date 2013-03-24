package edu.virginia.vcgr.genii.container.jsdl.parser;

import java.net.URI;

import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultSourceURIFacet;

public class ExecutionSourceURIFacet extends DefaultSourceURIFacet
{
	@Override
	public void consumeURI(Object currentUnderstanding, URI uri)
	{
		ExecutionDataStagingFacet ds = (ExecutionDataStagingFacet) currentUnderstanding;

		ds.setSourceURI(uri.toString());
	}
}