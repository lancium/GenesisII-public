package edu.virginia.vcgr.genii.container.bes.jsdl.personality.simpleexec;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.DataStagingFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.HPCApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.JobIdentificationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.POSIXApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.SourceURIFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.TargetURIFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultPersonalityProvider;

public class SimpleExecutionPersonalityProvider extends
		DefaultPersonalityProvider
{
	@Override
	public HPCApplicationFacet getHPCApplicationFacet(
			Object currentUnderstanding) throws JSDLException
	{
		return new SEHPCApplicationFacet();
	}

	@Override
	public Object createNewUnderstanding() throws JSDLException
	{
		return new SimpleExecutionUnderstanding();
	}

	@Override
	public DataStagingFacet getDataStagingFacet(Object currentUnderstanding)
			throws JSDLException
	{
		return new SEDataStagingFacet();
	}

	@Override
	public JobIdentificationFacet getJobIdentificationFacet(
			Object currentUnderstanding) throws JSDLException
	{
		return new SEJobIdentificationFacet();
	}

	@Override
	public POSIXApplicationFacet getPOSIXApplicationFacet(
			Object currentUnderstanding) throws JSDLException
	{
		return new SEPOSIXApplicationFacet();
	}

	@Override
	public SourceURIFacet getSourceURIFacet(Object currentUnderstanding)
			throws JSDLException
	{
		return new SESourceURIFacet();
	}

	@Override
	public TargetURIFacet getTargetURIFacet(Object currentUnderstanding)
			throws JSDLException
	{
		return new SETargetURIFacet();
	}
}