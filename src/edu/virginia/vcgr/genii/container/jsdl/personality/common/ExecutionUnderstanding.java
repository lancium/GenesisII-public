package edu.virginia.vcgr.genii.container.jsdl.personality.common;

import java.util.Vector;

import org.ggf.jsdl.JobDefinition_Type;

import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.ExecutionPhase;

public interface ExecutionUnderstanding
{
	public Vector<ExecutionPhase> createExecutionPlan(BESConstructionParameters creationProperties, JobDefinition_Type jsdl, String BESipaddr, String activityid) throws JSDLException;

	public String getJobName();
	public String getJobAnnotation();
}
