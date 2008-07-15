package edu.virginia.vcgr.genii.container.bes.execution.phases;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.fuse.server.GeniiFuse;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class TeardownFUSEPhase extends AbstractFUSEPhases
{
	static final long serialVersionUID = 0L;
	
	public TeardownFUSEPhase(String mountPoint)
	{
		super(mountPoint, new ActivityState(
			ActivityStateEnumeration.Running, "fuse-teardown", false));
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		GeniiFuse.unmountGenesisII(getMountPoint(context));
	}
}