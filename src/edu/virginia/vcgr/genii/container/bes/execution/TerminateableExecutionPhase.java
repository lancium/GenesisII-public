package edu.virginia.vcgr.genii.container.bes.execution;

import edu.virginia.vcgr.genii.client.bes.ExecutionException;
import edu.virginia.vcgr.genii.client.bes.ExecutionPhase;

public interface TerminateableExecutionPhase extends ExecutionPhase {
	public void terminate(boolean countAsFailedAttempt)
			throws ExecutionException;
}