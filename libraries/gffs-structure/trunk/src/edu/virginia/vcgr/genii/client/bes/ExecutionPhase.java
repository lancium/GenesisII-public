package edu.virginia.vcgr.genii.client.bes;

import edu.virginia.vcgr.genii.client.bes.ExecutionContext;

public interface ExecutionPhase
{
	public ActivityState getPhaseState();

	public void execute(ExecutionContext context) throws Throwable;
}