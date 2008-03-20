package edu.virginia.vcgr.genii.container.bes.execution;

import edu.virginia.vcgr.genii.client.bes.ActivityState;

public interface ExecutionPhase
{
	public ActivityState getPhaseState();
	
	public void execute(ExecutionContext context) throws Throwable;
}