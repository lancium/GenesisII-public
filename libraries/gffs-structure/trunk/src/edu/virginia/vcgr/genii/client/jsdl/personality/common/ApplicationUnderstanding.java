package edu.virginia.vcgr.genii.client.jsdl.personality.common;

import java.util.Vector;

import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.bes.ExecutionPhase;

public interface ApplicationUnderstanding {
	public FilesystemManager getFilesystemManager();

	public BESWorkingDirectory getWorkingDirectory();

	public void addExecutionPhases(
			BESConstructionParameters creationProperties,
			Vector<ExecutionPhase> executionPlan,
			Vector<ExecutionPhase> cleanupPhases,
			JobUnderstandingContext jobContext) throws JSDLException;
}