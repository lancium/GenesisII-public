package edu.virginia.vcgr.genii.container.jsdl.personality.common;

import java.util.Vector;

import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemRelativePath;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.JobUnderstandingContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;

public interface ApplicationUnderstanding
{
	public FilesystemManager getFilesystemManager();

	public BESWorkingDirectory getWorkingDirectory();
	
	public FilesystemRelativePath getExecutable();


	public void addExecutionPhases(BESConstructionParameters creationProperties, Vector<ExecutionPhase> executionPlan,
		Vector<ExecutionPhase> cleanupPhases, JobUnderstandingContext jobContext) throws JSDLException;
}