package edu.virginia.vcgr.genii.container.bes.execution;

public interface SuspendableExecutionPhase extends ExecutionPhase
{
	public void suspend() throws ExecutionException;

	public void resume() throws ExecutionException;
}