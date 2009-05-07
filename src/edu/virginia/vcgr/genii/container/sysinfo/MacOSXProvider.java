package edu.virginia.vcgr.genii.container.sysinfo;

public class MacOSXProvider implements ISystemInfoProvider
{
	@Override
	public long getIndividualCPUSpeed()
	{
		System.err.println("I'm lying about my capabilities.");
		return 100L;
	}

	@Override
	public long getPhysicalMemory()
	{
		System.err.println("I'm lying about my capabilities.");
		return 100L;
	}

	@Override
	public long getPhysicalMemoryAvailable()
	{
		System.err.println("I'm lying about my capabilities.");
		return 100L;
	}

	@Override
	public boolean getScreenSaverActive()
	{
		System.err.println("I'm lying about my capabilities.");
		return false;
	}

	@Override
	public boolean getUserLoggedIn()
	{
		System.err.println("I'm lying about my capabilities.");
		return false;
	}

	@Override
	public long getVirtualMemory()
	{
		System.err.println("I'm lying about my capabilities.");
		return 100L;
	}

	@Override
	public long getVirtualMemoryAvailable()
	{
		System.err.println("I'm lying about my capabilities.");
		return 100L;
	}
}