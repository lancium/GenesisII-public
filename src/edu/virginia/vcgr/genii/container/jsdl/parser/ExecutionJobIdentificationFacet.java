package edu.virginia.vcgr.genii.container.jsdl.parser;

import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultJobIdentificationFacet;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;

public class ExecutionJobIdentificationFacet extends
		DefaultJobIdentificationFacet
{
	@Override
	public void consumeJobName(Object currentUnderstanding,
		String name)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.setJobName(name);
	}
	
	@Override
	public void consumeJobAnnotation(Object currentUnderstanding,
		String annotation)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.setJobAnnotation(annotation);
	}
}