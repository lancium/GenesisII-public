package edu.virginia.vcgr.genii.container.bes.execution;

import edu.virginia.vcgr.genii.client.bes.ExecutionException;
import edu.virginia.vcgr.genii.client.bes.ExecutionPhase;

public interface SuspendableExecutionPhase extends ExecutionPhase {
	public void suspend() throws ExecutionException;

	public void resume() throws ExecutionException;
}