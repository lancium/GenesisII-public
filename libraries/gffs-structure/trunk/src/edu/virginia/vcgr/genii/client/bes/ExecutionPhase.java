package edu.virginia.vcgr.genii.client.bes;

import edu.virginia.vcgr.genii.client.bes.ExecutionContext;

public interface ExecutionPhase
{
	public ActivityState getPhaseState();

	// 2020 August 04 by CCH: Ugly hack so each phase has a reference to the BESActivity that is executing that phase
	// The problem: The BESActivity type is not defined in the edu.virginia.vcgr.genii.client.bes package, so I cannot import/use it here
	// The hack: Pass the BESActivity as generic object, then in each of the implemented execute methods cast them as BESActivity objects
	public void execute(ExecutionContext context, Object activity) throws Throwable;
}