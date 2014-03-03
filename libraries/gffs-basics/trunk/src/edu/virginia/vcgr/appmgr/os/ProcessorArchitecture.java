package edu.virginia.vcgr.appmgr.os;

import java.io.InputStream;
import java.util.Properties;

import edu.virginia.vcgr.appmgr.io.IOUtils;

public enum ProcessorArchitecture {
	sparc(), powerpc(), x86(), x86_32(), x86_64(), parisc(), mips(), ia64(), arm(), other();

	static final private String _OS_ARCH_PREFIX = "os.arch.";
	static private Properties _propertyMap;

	static {
		InputStream in = null;
		try {
			in = Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(
							"edu/virginia/vcgr/appmgr/os/arch-map.properties");
			_propertyMap = new Properties();
			_propertyMap.load(in);
		} catch (Exception e) {
			throw new RuntimeException(
					"Unable to initialize system information.", e);
		} finally {
			IOUtils.close(in);
		}
	}

	static public ProcessorArchitecture getCurrent() {
		try {
			String osarch = System.getProperty("os.arch");

			String archName = _propertyMap
					.getProperty(_OS_ARCH_PREFIX + osarch);
			return ProcessorArchitecture.valueOf(archName);
		} catch (Throwable cause) {
			throw new RuntimeException(
					"Unable to determine current platform's architecture.",
					cause);
		}
	}
}
