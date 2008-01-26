package edu.virginia.vcgr.genii.container.processmanager;

public class ProcessManagerUtils {
	static private IProcessManagerProvider _provider;
	static
	{
		String osName = System.getProperty("os.name");
		
		if (osName.equals("Linux"))
			// Currently undefined Linux behavior for process management
			_provider = null;
		else if ((osName.equals("Windows XP")|| osName.equals("Windows 2003")))
			_provider = new WindowsProvider();
		else
			throw new RuntimeException(
				"Don't know an ISystemInfoProvider for OS type \"" +
				osName + "\".");
	}

	static private IProcessManagerProvider getProvider()
	{
		return _provider;
	}
	
	static public boolean kill(double thePid) {
		return getProvider().kill(thePid);
	}
	
	static public boolean suspend(double thePid) {
		return getProvider().suspend(thePid);
	}
	
	static public boolean resume(double thePid) {
		return getProvider().resume(thePid);
	}
}
