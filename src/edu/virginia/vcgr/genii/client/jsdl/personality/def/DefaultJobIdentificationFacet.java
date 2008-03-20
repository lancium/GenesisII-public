package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.JobIdentificationFacet;

public class DefaultJobIdentificationFacet extends DefaultPersonalityFacet
		implements JobIdentificationFacet
{
	@Override
	public void consumeDescription(Object currentUnderstanding,
			String description) throws JSDLException
	{
	}

	@Override
	public void consumeJobAnnotation(Object currentUnderstanding,
			String annotation) throws JSDLException
	{
	}

	@Override
	public void consumeJobName(Object currentUnderstanding,
			String jobName) throws JSDLException
	{
	}

	@Override
	public void consumeJobProject(Object currentUnderstanding,
			String jobProject) throws JSDLException
	{
	}
}
