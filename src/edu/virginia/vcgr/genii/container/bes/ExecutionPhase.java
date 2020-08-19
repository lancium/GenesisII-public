package edu.virginia.vcgr.genii.container.bes;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;

public interface ExecutionPhase
{	
	public ActivityState getPhaseState();

	// 2020 August 04 by CCH: Each phase gets a reference to the BESActivity that is executing that phase
	public void execute(ExecutionContext context, BESActivity activity) throws Throwable;
}
