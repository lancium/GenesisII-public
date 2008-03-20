package edu.virginia.vcgr.genii.client.machine;

public interface MachineActor
{
	public boolean canSuspendResume();
	
	public void suspendProcess(Process proc);
	public void resumeProcess(Process proc);
	
	public void killProcess(Process proc);
}