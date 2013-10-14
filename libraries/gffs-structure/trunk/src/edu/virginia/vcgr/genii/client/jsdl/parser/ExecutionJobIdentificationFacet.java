package edu.virginia.vcgr.genii.client.jsdl.parser;

import edu.virginia.vcgr.genii.client.jsdl.JobRequest;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultJobIdentificationFacet;

public class ExecutionJobIdentificationFacet extends DefaultJobIdentificationFacet
{
	@Override
	public void consumeJobName(Object currentUnderstanding, String name)
	{
		JobRequest jr = (JobRequest) currentUnderstanding;
		jr.setJobName(name);
	}

	@Override
	public void consumeJobAnnotation(Object currentUnderstanding, String annotation)
	{
		JobRequest jr = (JobRequest) currentUnderstanding;
		jr.setJobAnnotation(annotation);
	}
}