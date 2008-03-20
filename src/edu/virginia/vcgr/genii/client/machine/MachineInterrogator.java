package edu.virginia.vcgr.genii.client.machine;

public interface MachineInterrogator
{
	public boolean canDetermineUserLoggedIn();
	public boolean isUserLoggedIn();
	
	public boolean canDetermineScreenSaverActive();
	public boolean isScreenSaverActive();
}