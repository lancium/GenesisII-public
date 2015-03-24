package edu.virginia.vcgr.genii.client.machine;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.appmgr.os.OperatingSystemType.OperatingSystemTypes;

public class MachineFactory
{
	static public MachineActor getActorInstance()
	{
		OperatingSystemTypes os = OperatingSystemType.getCurrent();

		if (OperatingSystemType.isWindows()) {
			return new WindowsMachineActor();
		} else if (os == OperatingSystemTypes.LINUX) {
			return new LinuxMachineActor();
		} else
			throw new RuntimeException("Operating system " + os + " is unsupported.");
	}

	static public MachineInterrogator getInterrogatorInstance()
	{
		OperatingSystemTypes os = OperatingSystemType.getCurrent();

		if (OperatingSystemType.isWindows()) {
			return new WindowsMachineInterrogator();
		} else if (os == OperatingSystemTypes.LINUX) {
			return new LinuxMachineInterrogator();
		} else if (os == OperatingSystemTypes.MACOS) {
			return new MacOSXMachineInterrogator();
		} else
			throw new RuntimeException("Operating system " + os + " is unsupported.");
	}
}