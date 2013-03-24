package edu.virginia.vcgr.genii.client.machine;

public class CommonMachineInterrogator implements MachineInterrogator
{
	@Override
	public boolean canDetermineScreenSaverActive()
	{
		return false;
	}

	@Override
	public boolean canDetermineUserLoggedIn()
	{
		return false;
	}

	@Override
	public boolean isScreenSaverActive()
	{
		throw new IllegalStateException("Cannot determine screen saver state on this plaform.");
	}

	@Override
	public boolean isUserLoggedIn()
	{
		throw new IllegalStateException("Cannot determine user log in status on this platform.");
	}
}