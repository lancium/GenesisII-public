package edu.virginia.vcgr.genii.container.sysinfo;

import edu.virginia.vcgr.genii.container.sysinfo.macosx.MacOSXSysInfoAccumulator;

public class MacOSXProvider implements ISystemInfoProvider
{
	@Override
	public long getIndividualCPUSpeed()
	{
		return MacOSXSysInfoAccumulator.individualCPUSpeed();
	}

	@Override
	public long getPhysicalMemory()
	{
		return MacOSXSysInfoAccumulator.physicalMemory();
	}

	@Override
	public long getPhysicalMemoryAvailable()
	{
		return MacOSXSysInfoAccumulator.physicalMemoryAvailable();
	}

	@Override
	public boolean getScreenSaverActive()
	{
		return MacOSXSysInfoAccumulator.screenSaverActivte();
	}

	@Override
	public boolean getUserLoggedIn()
	{
		return MacOSXSysInfoAccumulator.userLoggedIn();
	}

	@Override
	public long getVirtualMemory()
	{
		return MacOSXSysInfoAccumulator.virtualMemory();
	}

	@Override
	public long getVirtualMemoryAvailable()
	{
		return MacOSXSysInfoAccumulator.virtualMemoryAvailable();
	}
}