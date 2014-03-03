/*
 * This code was developed by Mark Morgan (mmm2a@virginia.edu) at the University of Virginia and is
 * an implementation of JSDL, JSDL ParameterSweep and other JSDL related specifications from the
 * OGF.
 * 
 * Copyright 2010 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.virginia.vcgr.jsdl;

import edu.virginia.vcgr.jsdl.mapping.Mappings;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public enum OperatingSystemNames {
	Unknown,
	MACOS("Mac OS X"),
	ATTUNIX("AT & T Unix"),
	DGUX,
	DECNT("DEC NT"),
	Tru64_UNIX("Tru 64 Unix"),
	OpenVMS("Open VMS"),
	HPUX,
	AIX,
	MVS,
	OS400,
	OS_2("OS 2"),
	JavaVM("Java VM"),
	MSDOS("MS-DOS"),
	WIN3x("Windows 3x"),
	WIN95("Windows 95"),
	WIN98("Windows 98"),
	WINNT("Windows NT"),
	WINCE("Windows CE"),
	NCR3000,
	NetWare,
	OSF,
	DC_OS("DC OS"),
	Reliant_UNIX("Reliant UNIX"),
	SCO_UnixWare("SCO UnixWare"),
	SCO_OpenServer("SCO OpenServer"),
	Sequent,
	IRIX,
	Solaris,
	SunOS,
	U6000,
	ASERIES("A-SERIES"),
	TandemNSK("Tandem NSK"),
	TandemNT("Tandem NT"),
	BS2000,
	LINUX("Linux"),
	Lynx,
	XENIX,
	VM,
	Interactive_UNIX("Interactive UNIX"),
	BSDUNIX("BSD UNIX"),
	FreeBSD("Free BSD"),
	NetBSD("Net BSD"),
	GNU_Hurd("GNU Hurd"),
	OS9,
	MACH_Kernel("MACH Kernel"),
	Inferno,
	QNX,
	EPOC,
	IxWorks,
	VxWorks,
	MiNT,
	BeOS,
	HP_MPE,
	NextStep,
	PalmPilot("Palm Pilot"),
	Rhapsody,
	Windows_2000("Windows 2000"),
	Dedicated,
	OS_390("OS 390"),
	VSE,
	TPF,
	Windows_R_Me("Windows R Me"),
	Caldera_Open_UNIX("Caldera Open UNIX"),
	OpenBSD,
	Not_Applicable("Not Applicable"),
	Windows_XP("Windows XP"),
	Windows_7("Windows 7"),
	Windows_8("Windows 8"),
	Windows_VISTA("Windows_VISTA"),
	z_OS("z OS"),
	other;

	private String _label;

	private OperatingSystemNames()
	{
		this(null);
	}

	private OperatingSystemNames(String label)
	{
		_label = label;

		if (_label == null)
			_label = name();
	}

	@Override
	public String toString()
	{
		return _label;
	}

	public boolean isWindows()
	{
		return (this == OperatingSystemNames.Windows_XP) || (this == OperatingSystemNames.Windows_7)
			|| (this == OperatingSystemNames.Windows_8) || (this == OperatingSystemNames.Windows_VISTA)
			|| (this == OperatingSystemNames.WINNT) || (this == OperatingSystemNames.Windows_2000)
			|| (this == OperatingSystemNames.Windows_R_Me) || (this == OperatingSystemNames.WINCE)
			|| (this == OperatingSystemNames.WIN98) || (this == OperatingSystemNames.WIN95)
			|| (this == OperatingSystemNames.WIN3x);
	}

	public boolean isMacOSX()
	{
		return (this == OperatingSystemNames.MACOS);
	}

	public boolean isLinux()
	{
		return (this == OperatingSystemNames.LINUX);
	}

	static public OperatingSystemNames getCurrentOperatingSystem()
	{
		String javaValue = System.getProperty("os.name");
		if (javaValue == null)
			return null;

		return Mappings.osMap().get(javaValue);
	}

	static public String getCurrentVersion()
	{
		return System.getProperty("os.version");
	}
}
