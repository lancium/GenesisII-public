package edu.virginia.vcgr.genii.container.processmanager;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.appmgr.os.OperatingSystemType.OperatingSystemTypes;

public class ProcessManagerUtils
{
	static private IProcessManagerProvider _provider;

	static {
		OperatingSystemTypes os = OperatingSystemType.getCurrent();

		if (os == OperatingSystemTypes.LINUX) {
			// Currently undefined Linux behavior for process management
			_provider = new LinuxProvider();
		} else if (OperatingSystemType.isWindows()) {
			_provider = new WindowsProvider();
		} else {
			throw new RuntimeException("Don't know an IProcessManagerProvider for OS type \"" + os + "\".");
		}
	}

	static private IProcessManagerProvider getProvider()
	{
		return _provider;
	}

	static public boolean kill(double thePid)
	{
		return getProvider().kill(thePid);
	}

	static public boolean suspend(double thePid)
	{
		return getProvider().suspend(thePid);
	}

	static public boolean resume(double thePid)
	{
		return getProvider().resume(thePid);
	}
}
