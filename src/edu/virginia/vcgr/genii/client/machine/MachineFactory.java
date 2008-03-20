package edu.virginia.vcgr.genii.client.machine;

import edu.virginia.vcgr.genii.container.sysinfo.SupportedOperatingSystems;

public class MachineFactory
{
	static public MachineActor getActorInstance()
	{
		SupportedOperatingSystems os = SupportedOperatingSystems.current();
		
		if (os.equals(SupportedOperatingSystems.WINDOWS))
		{
			return new WindowsMachineActor();
		} else if (os.equals(SupportedOperatingSystems.LINUX))
		{
			return new LinuxMachineActor();
		} else
			throw new RuntimeException("Operating system " +
				os + " is unsupported.");
	}
	
	static public MachineInterrogator getInterrogatorInstance()
	{
		SupportedOperatingSystems os = SupportedOperatingSystems.current();
		
		if (os.equals(SupportedOperatingSystems.WINDOWS))
		{
			return new WindowsMachineInterrogator();
		} else if (os.equals(SupportedOperatingSystems.LINUX))
		{
			return new LinuxMachineInterrogator();
		} else
			throw new RuntimeException("Operating system " +
				os + " is unsupported.");
	}
}