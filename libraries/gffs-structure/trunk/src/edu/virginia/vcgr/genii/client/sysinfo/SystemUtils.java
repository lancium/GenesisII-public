package edu.virginia.vcgr.genii.client.sysinfo;

import java.io.InputStream;
import java.util.Properties;

import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.system.classloader.GenesisClassLoader;

public class SystemUtils
{
	/*
	 * static private Log _logger = LogFactory.getLog(SystemUtils.class);
	 * 
	 * static final private String _OS_ARCH_PREFIX = "os.arch."; static final private String _OS_NAME_PREFIX = "os.name.";
	 */
	static private Properties _propertyMap;

	static {
		InputStream in = null;
		try {
			in =
				GenesisClassLoader.classLoaderFactory().getResourceAsStream("edu/virginia/vcgr/genii/client/sysinfo/property-map.properties");
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
	}

	static public OperatingSystem_Type[] getSupportedOperatingSystems()
	{
		throw new RuntimeException("This type of os query is no longer supported.");
	}
}