package edu.virginia.vcgr.genii.container.bes.jsdl.personality.simpleexec;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultJobIdentificationFacet;

public class SEJobIdentificationFacet extends DefaultJobIdentificationFacet
{
	@Override
	public void consumeJobName(Object currentUnderstanding, String jobName)
			throws JSDLException
	{
		((SimpleExecutionUnderstanding)currentUnderstanding).setJobName(
			jobName);
	}
}