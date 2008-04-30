package edu.virginia.vcgr.genii.container.bes.jsdl.personality.simpleexec;

import java.util.Properties;
import java.util.Vector;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;

public interface Application
{
	public void addExecutionPhases(
		Properties creationProperties,
		Vector<ExecutionPhase> executionPlan, 
		Vector<ExecutionPhase> cleanupPhases,
		String ogrshVersion)
			throws JSDLException;
}