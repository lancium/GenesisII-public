package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.util.Properties;
import java.util.Vector;

import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;

public interface ApplicationUnderstanding
{
	public FilesystemManager getFilesystemManager();
	public BESWorkingDirectory getWorkingDirectory();
	
	public void addExecutionPhases(
		Properties creationProperties,
		Vector<ExecutionPhase> executionPlan, 
		Vector<ExecutionPhase> cleanupPhases,
		JobUnderstandingContext jobContext)
			throws JSDLException;
}