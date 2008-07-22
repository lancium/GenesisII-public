package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultJobIdentificationFacet;

public class CommonJobIdentificationFacet extends DefaultJobIdentificationFacet
{
	@Override
	public void consumeJobName(Object currentUnderstanding, String jobName)
			throws JSDLException
	{
		((CommonExecutionUnderstanding)currentUnderstanding).setJobName(
			jobName);
	}
}