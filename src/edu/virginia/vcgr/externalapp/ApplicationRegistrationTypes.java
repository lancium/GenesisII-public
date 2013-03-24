package edu.virginia.vcgr.externalapp;

import edu.virginia.vcgr.jsdl.OperatingSystemNames;

public enum ApplicationRegistrationTypes {
	Windows, Linux, MacOSX, Common;

	public boolean matches(OperatingSystemNames osName)
	{
		switch (this) {
			case Windows:
				return osName.isWindows();
			case Linux:
				return osName.isLinux();
			case MacOSX:
				return osName.isMacOSX();
			default:
				return true;
		}
	}

	public boolean matches()
	{
		return matches(OperatingSystemNames.getCurrentOperatingSystem());
	}
}