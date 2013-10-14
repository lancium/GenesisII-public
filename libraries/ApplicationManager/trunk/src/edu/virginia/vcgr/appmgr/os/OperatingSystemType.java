package edu.virginia.vcgr.appmgr.os;

import java.io.InputStream;
import java.util.Properties;

import edu.virginia.vcgr.appmgr.io.IOUtils;

public enum OperatingSystemType {
	Unknown(),
	WINNT(),
	LINUX(),
	HP_MPE(),
	Other(),
	WINCE(),
	Lynx(),
	NextStep(),
	MACOS(),
	NCR3000(),
	XENIX(),
	PalmPilot(),
	ATTUNIX(),
	NetWare(),
	VM(),
	Rhapsody(),
	DGUX(),
	OSF(),
	Interactive_UNIX(),
	Windows_2000(),
	DECNT(),
	DC_OS(),
	BSDUNIX(),
	Dedicated(),
	Tru64_UNIX(),
	Reliant_UNIX(),
	FreeBSD(),
	OS_390(),
	OpenVMS(),
	SCO_UnixWare(),
	NetBSD(),
	VSE(),
	HPUX(),
	SCO_OpenServer(),
	GNU_Hurd(),
	TPF(),
	AIX(),
	Sequent(),
	OS9(),
	Windows_R_Me(),
	MVS(),
	IRIX(),
	MACH_Kernel(),
	Caldera_Open_UNIX(),
	OS400(),
	Solaris(),
	Inferno(),
	OpenBSD(),
	OS_2(),
	SunOS(),
	QNX(),
	Not_Applicable(),
	JavaVM(),
	U6000(),
	EPOC(),
	Windows_XP(),
	Windows_VISTA(),
	Windows_7(),
	Windows_8(),
	MSDOS(),
	ASERIES(),
	IxWorks(),
	z_OS(),
	WIN3x(),
	TandemNSK(),
	VxWorks(),
	WIN95(),
	TandemNT(),
	MiNT(),
	WIN98(),
	BS2000(),
	BeOS();

	static final private String _OS_NAME_PREFIX = "os.name.";
	static private Properties _propertyMap;

	static {
		InputStream in = null;
		try {
			in =
				Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("edu/virginia/vcgr/appmgr/os/os-map.properties");
			_propertyMap = new Properties();
			_propertyMap.load(in);
		} catch (Exception e) {
			throw new RuntimeException("Unable to initialize system information.", e);
		} finally {
			IOUtils.close(in);
		}
	}

	static public String getCurrentVersion()
	{
		return System.getProperty("os.version");
	}

	static public OperatingSystemType getCurrent()
	{
		try {
			String osname = System.getProperty("os.name");
			String ostypename = _propertyMap.getProperty(_OS_NAME_PREFIX + osname);

			return OperatingSystemType.valueOf(ostypename);
		} catch (Throwable cause) {
			throw new RuntimeException("Unable to determine current Operating System type.", cause);
		}
	}

	public boolean isWindows()
	{
		OperatingSystemType me = getCurrent();

		return (me == OperatingSystemType.Windows_XP) || (me == OperatingSystemType.WINNT)
			|| (me == OperatingSystemType.Windows_2000) || (me == OperatingSystemType.Windows_R_Me)
			|| (me == OperatingSystemType.WINCE) || (me == OperatingSystemType.WIN98)
			|| (me == OperatingSystemType.Windows_VISTA) || (me == OperatingSystemType.Windows_7)
			|| (me == OperatingSystemType.Windows_8) || (me == OperatingSystemType.WIN95) || (me == OperatingSystemType.WIN3x);
	}

	static public void main(String[] args) throws Throwable
	{
		System.out.println("OS Type is:");
		OperatingSystemType opsys = getCurrent();
		System.out.println("  => " + opsys.toString());
		System.out.println("version is:");
		System.out.println("  => " + getCurrentVersion());

	}

}
