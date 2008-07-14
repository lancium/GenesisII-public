package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.Serializable;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;

public abstract class AbstractExecutionPhase 
	implements ExecutionPhase, Serializable
{
	static final long serialVersionUID = 0L;
	
	private ActivityState _phaseState;
	
	protected AbstractExecutionPhase(ActivityState phaseState)
	{
		_phaseState = phaseState;
	}
	
	@Override
	public ActivityState getPhaseState()
	{
		return _phaseState;
	}
}