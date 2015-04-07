/*
 * This code was developed by Mark Morgan (mmm2a@virginia.edu) at the University of Virginia and is an implementation of JSDL, JSDL
 * ParameterSweep and other JSDL related specifications from the OGF.
 * 
 * Copyright 2010 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.virginia.vcgr.jsdl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.jsdl.mapping.Mappings;

/**
 * This class is intended to represent a requested operating system in a JSDL file. It should not care about the current machine's operating
 * system, unless this machine is a BES that was just handed the OperatingSystemName for comparison. In that case, the mapFromOperatingSystem
 * function can determine the enum value corresponding to the machine's current OS. Most other queries about the machine's current OS should
 * be using the OperatingSystemType class insteda.
 * 
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

	static private Log _logger = LogFactory.getLog(OperatingSystemNames.class);

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
		/*
		 * catch-all for windows OS. currently every enum value that starts with Windows is MS windows.
		 */
		return (_label != null) && _label.startsWith("Windows");
	}

	public boolean isMacOSX()
	{
		return OperatingSystemNames.MACOS.equals(this);
	}

	public boolean isLinux()
	{
		return OperatingSystemNames.LINUX.equals(this);
	}

	static public OperatingSystemNames mapFromCurrentOperatingSystem()
	{
		String javaValue = OperatingSystemType.getOpsysName();
		if (javaValue == null) {
			_logger.debug("unexpected null value when querying operating system name");
			return Unknown;
		}
		return Mappings.osMap().get(javaValue);
	}
}
