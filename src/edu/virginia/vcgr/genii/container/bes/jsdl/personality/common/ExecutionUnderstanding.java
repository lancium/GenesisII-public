package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.util.Vector;

import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;

public interface ExecutionUnderstanding 
{
	public Vector<ExecutionPhase> createExecutionPlan(BESConstructionParameters creationProperties) throws JSDLException;
	public String getJobName();
}
