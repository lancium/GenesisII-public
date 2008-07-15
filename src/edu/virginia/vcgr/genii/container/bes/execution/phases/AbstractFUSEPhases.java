package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionException;

abstract class AbstractFUSEPhases extends AbstractExecutionPhase
{
	static final long serialVersionUID = 0L;
	
	private String _mountPoint;
	
	protected AbstractFUSEPhases(String mountPoint,
		ActivityState aState)
	{
		super(aState);
		
		_mountPoint = mountPoint;
	}
	
	protected File getMountPoint(ExecutionContext context)
		throws ExecutionException
	{
		if (_mountPoint.startsWith("/"))
			return new File(_mountPoint);
		
		return new File(context.getCurrentWorkingDirectory(), _mountPoint);
	}
}