package edu.virginia.vcgr.genii.container.processmanager;

public class LinuxProvider implements IProcessManagerProvider
{
	@Override
	public boolean kill(double thePid)
	{
		throw new RuntimeException(
			"Linux Process Manager functions not implemented.");
	}

	@Override
	public boolean resume(double thePid)
	{
		throw new RuntimeException(
			"Linux Process Manager functions not implemented.");
	}

	@Override
	public boolean suspend(double thePid)
	{
		throw new RuntimeException(
			"Linux Process Manager functions not implemented.");
	}
}