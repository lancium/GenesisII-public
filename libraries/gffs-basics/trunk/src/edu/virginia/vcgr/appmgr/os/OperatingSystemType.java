package edu.virginia.vcgr.appmgr.os;

import java.io.InputStream;
import java.util.Properties;

import edu.virginia.vcgr.appmgr.io.IOUtils;

public class OperatingSystemType 
{
	public enum OperatingSystemTypes {
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

	}

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
	
	/**
	 * returns the java os.name property, but stashes the value since this doesn't change at run-time.
	 */
	static private String _osName = null;
	static public String getOpsysName()
	{
		synchronized (OperatingSystemType.class) {
			if (_osName == null) {
				_osName = System.getProperty("os.name");
			}
		}
		return _osName;
	}

	static public OperatingSystemTypes getCurrent()
	{
		try {
			String ostypename = _propertyMap.getProperty(_OS_NAME_PREFIX + getOpsysName());
			return OperatingSystemTypes.valueOf(ostypename);
		} catch (Throwable cause) {
			throw new RuntimeException("Unable to determine current Operating System type.", cause);
		}
	}

	static public boolean isWindows()
	{
		OperatingSystemTypes current = getCurrent();
		return OperatingSystemTypes.Windows_XP.equals(current) || OperatingSystemTypes.WINNT.equals(current)
			|| OperatingSystemTypes.Windows_2000.equals(current) || OperatingSystemTypes.Windows_R_Me.equals(current)
			|| OperatingSystemTypes.WINCE.equals(current) || OperatingSystemTypes.WIN98.equals(current)
			|| OperatingSystemTypes.Windows_VISTA.equals(current) || OperatingSystemTypes.Windows_7.equals(current)
			|| OperatingSystemTypes.Windows_8.equals(current) || OperatingSystemTypes.WIN95.equals(current)
			|| OperatingSystemTypes.WIN3x.equals(current)
			/* catch-all for windows OS with newer names. */
			|| current.toString().startsWith("Windows");
	}

	static public void main(String[] args) throws Throwable
	{
		System.out.println("OS Type is:");
		OperatingSystemTypes opsys = getCurrent();
		System.out.println("  => " + opsys.toString());
		System.out.println("version is:");
		System.out.println("  => " + getCurrentVersion());
	}

}
