package edu.virginia.vcgr.appmgr.os;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.io.IOUtils;

/**
 * This class provides a number of helper methods for checking the operating system that is currently running (as seen by the java application
 * and jvm) and deciding what type it is.
 */
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

	static private Log _logger = LogFactory.getLog(OperatingSystemType.class);

	static final private String _OS_NAME_PREFIX = "os.name.";
	static private Properties _propertyMap;
	static private final String OS_MAP_PROPERTIES_FILE = "edu/virginia/vcgr/appmgr/os/os-map.properties";

	static {
		InputStream in = null;
		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(OS_MAP_PROPERTIES_FILE);
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

	static private boolean showedOS = false;

	static public OperatingSystemTypes getCurrent()
	{
		try {
			String ostypename = _propertyMap.getProperty(_OS_NAME_PREFIX + getOpsysName());
			OperatingSystemTypes toReturn = OperatingSystemTypes.valueOf(ostypename);
			if (!showedOS) {
				_logger.info("Decided that operating system is: '" + toReturn + "'");
				showedOS = true;
			}
			return toReturn;
		} catch (Throwable cause) {
			throw new RuntimeException("Unable to determine current Operating System type.", cause);
		}
	}

	static public boolean isWindows()
	{
		OperatingSystemTypes current = getCurrent();
		/* catch-all for all types of MS windows so far... */
		return current.toString().startsWith("Windows");
	}

	static public boolean isLinux()
	{
		return OperatingSystemTypes.LINUX.equals(getCurrent());
	}

	static public boolean isMacOSX()
	{
		return OperatingSystemTypes.MACOS.equals(getCurrent());
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
