package edu.virginia.vcgr.genii.client.jsdl.personality;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface JobIdentificationFacet extends PersonalityFacet
{
	public void consumeJobName(Object currentUnderstanding, String jobName) throws JSDLException;

	public void consumeDescription(Object currentUnderstanding, String description) throws JSDLException;

	public void consumeJobAnnotation(Object currentUnderstanding, String annotation) throws JSDLException;

	public void consumeJobProject(Object currentUnderstanding, String jobProject) throws JSDLException;
}
