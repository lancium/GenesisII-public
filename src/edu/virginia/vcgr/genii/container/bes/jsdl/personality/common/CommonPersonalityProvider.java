package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.DataStagingFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.JobIdentificationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.ResourcesFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.SourceURIFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.TargetURIFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultPersonalityProvider;

public class CommonPersonalityProvider extends DefaultPersonalityProvider
{
	@Override
	public Object createNewUnderstanding() throws JSDLException
	{
		return new CommonExecutionUnderstanding();
	}
	
	@Override
	public ResourcesFacet getResourcesFacet(Object currentUnderstanding)
		throws JSDLException
	{
		return new CommonResourcesFacet();
	}
	
	@Override
	public DataStagingFacet getDataStagingFacet(Object currentUnderstanding)
		throws JSDLException
	{
		return new CommonDataStagingFacet();
	}
	
	@Override
	public JobIdentificationFacet getJobIdentificationFacet(
		Object currentUnderstanding) throws JSDLException
	{
		return new CommonJobIdentificationFacet();
	}
	
	@Override
	public SourceURIFacet getSourceURIFacet(Object currentUnderstanding)
		throws JSDLException
	{
		return new CommonSourceURIFacet();
	}

	@Override
	public TargetURIFacet getTargetURIFacet(Object currentUnderstanding)
		throws JSDLException
	{
		return new CommonTargetURIFacet();
	}
}