package edu.virginia.vcgr.genii.client.machine;

public class CommonMachineActor implements MachineActor
{
	@Override
	public void killProcess(Process proc)
	{
		proc.destroy();
	}

	@Override
	public boolean canSuspendResume()
	{
		return false;
	}

	@Override
	public void suspendProcess(Process proc)
	{
		throw new IllegalStateException("Not capable of suspending a process on this platform.");
	}

	@Override
	public void resumeProcess(Process proc)
	{
		throw new IllegalStateException("Not capable of resuming a process on this platform.");
	}
}