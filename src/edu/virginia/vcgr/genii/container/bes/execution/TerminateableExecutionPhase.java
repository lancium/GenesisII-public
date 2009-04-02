package edu.virginia.vcgr.genii.container.bes.execution;

public interface TerminateableExecutionPhase extends ExecutionPhase
{
	public void terminate(boolean countAsFailedAttempt) 
		throws ExecutionException;
}