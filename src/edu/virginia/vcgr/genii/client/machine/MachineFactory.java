package edu.virginia.vcgr.genii.client.machine;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;

public class MachineFactory
{
	static public MachineActor getActorInstance()
	{
		OperatingSystemType os = OperatingSystemType.getCurrent();
		
		if (os.isWindows())
		{
			return new WindowsMachineActor();
		} else if (os == OperatingSystemType.LINUX)
		{
			return new LinuxMachineActor();
		} else
			throw new RuntimeException("Operating system " +
				os + " is unsupported.");
	}
	
	static public MachineInterrogator getInterrogatorInstance()
	{
		OperatingSystemType os = OperatingSystemType.getCurrent();
		
		
		if (os.isWindows())
		{
			return new WindowsMachineInterrogator();
		} else if (os == OperatingSystemType.LINUX)
		{
			return new LinuxMachineInterrogator();
		} else if (os == OperatingSystemType.MACOS)
		{
			return new MacOSXMachineInterrogator();
		} else
			throw new RuntimeException("Operating system " +
				os + " is unsupported.");
	}
}