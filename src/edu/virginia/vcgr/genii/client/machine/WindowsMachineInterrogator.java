package edu.virginia.vcgr.genii.client.machine;

import edu.virginia.vcgr.genii.container.sysinfo.SystemInfoUtils;

class WindowsMachineInterrogator extends CommonMachineInterrogator
{
	@Override
	public boolean canDetermineScreenSaverActive()
	{
		return true;
	}

	@Override
	public boolean canDetermineUserLoggedIn()
	{
		return true;
	}

	@Override
	public boolean isScreenSaverActive()
	{
		return SystemInfoUtils.getScreenSaverActive();
	}

	@Override
	public boolean isUserLoggedIn()
	{
		return SystemInfoUtils.getUserLoggedIn();
	}
}