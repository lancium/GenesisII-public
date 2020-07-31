package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.Serializable;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.ExecutionPhase;

public abstract class AbstractExecutionPhase implements ExecutionPhase, Serializable
{
	static final long serialVersionUID = 0L;

	private ActivityState _phaseState;
	private boolean _needsWorkingContext = true;

	protected AbstractExecutionPhase(ActivityState phaseState)
	{
		_phaseState = phaseState;
	}

	@Override
	public ActivityState getPhaseState()
	{
		return _phaseState;
	}
	
	//LAK: This method allows an execution phase to skip creating the workingContext. This is currently used for CompleteAccountingPhase.
	@Override
	public boolean getNeedsWorkingContext()
	{
		return _needsWorkingContext;
	}
}