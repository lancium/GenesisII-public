package edu.virginia.vcgr.genii.client.sysinfo;

import java.io.InputStream;
import java.util.Properties;

import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.morgan.util.io.StreamUtils;

public class SystemUtils
{
	/*
	 * static private Log _logger = LogFactory.getLog(SystemUtils.class);
	 * 
	 * static final private String _OS_ARCH_PREFIX = "os.arch."; static final private String
	 * _OS_NAME_PREFIX = "os.name.";
	 */
	static private Properties _propertyMap;

	static {
		InputStream in = null;
		try {
			in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("edu/virginia/vcgr/genii/client/sysinfo/property-map.properties");
			_propertyMap = new Properties();
			_propertyMap.load(in);
		} catch (Exception e) {
			throw new RuntimeException("Unable to initialize system information.", e);
		} finally {
			StreamUtils.close(in);
		}
	}

	static public CPUArchitecture_Type[] getSupportedArchitectures()
	{
		throw new RuntimeException("This type of cpu arch query is no longer supported.");

		/*
		 * ProcessorArchitectureEnumeration primaryArchName = null; ProcessorArchitectureEnumeration
		 * secondaryArchName = null;
		 * 
		 * String osarch = System.getProperty("os.arch");
		 * 
		 * primaryArchName = ProcessorArchitectureEnumeration.fromString(
		 * _propertyMap.getProperty(_OS_ARCH_PREFIX + osarch));
		 * 
		 * if (primaryArchName == ProcessorArchitectureEnumeration.x86_64) secondaryArchName =
		 * ProcessorArchitectureEnumeration.x86; else { try { String osName =
		 * System.getProperty("os.name"); if (osName.equals("Linux")) secondaryArchName =
		 * ProcessorArchitectureEnumeration.fromString( ExecutionEngine.simpleExecute("uname",
		 * "-m")); } catch (IOException ioe) {
		 * _logger.warn("Unable to determine whether or not this JVM is running on a 64 bit machine."
		 * , ioe); } }
		 * 
		 * if (secondaryArchName != null) return new CPUArchitecture_Type[] { new
		 * CPUArchitecture_Type(primaryArchName, null), new CPUArchitecture_Type(secondaryArchName,
		 * null) };
		 * 
		 * return new CPUArchitecture_Type[] { new CPUArchitecture_Type(primaryArchName, null) };
		 */
	}

	static public OperatingSystem_Type[] getSupportedOperatingSystems()
	{
		throw new RuntimeException("This type of os query is no longer supported.");

		/*
		 * OperatingSystemTypeEnumeration operatingSystemName; String operatingSystemVersion; String
		 * description = null;
		 * 
		 * String osname = System.getProperty("os.name"); operatingSystemName =
		 * OperatingSystemTypeEnumeration.fromString( _propertyMap.getProperty(_OS_NAME_PREFIX +
		 * osname)); operatingSystemVersion = System.getProperty("os.version");
		 * 
		 * return new OperatingSystem_Type[] { new OperatingSystem_Type( new
		 * OperatingSystemType_Type(operatingSystemName, null), operatingSystemVersion, description,
		 * null) };
		 */
	}
}