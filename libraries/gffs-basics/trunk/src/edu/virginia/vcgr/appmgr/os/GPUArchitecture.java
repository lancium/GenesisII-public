package edu.virginia.vcgr.appmgr.os;

import java.io.InputStream;
import java.util.Properties;

import edu.virginia.vcgr.appmgr.io.IOUtils;

public enum GPUArchitecture {
	nvidia(),
	amd(),
	other();

	static final private String _GPU_ARCH_PREFIX = "gpu.arch.";
	static private Properties _propertyMap;

	static {
		InputStream in = null;
		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream("edu/virginia/vcgr/appmgr/os/gpu-arch-map.properties");
			_propertyMap = new Properties();
			_propertyMap.load(in);
		} catch (Exception e) {
			throw new RuntimeException("Unable to initialize system information.", e);
		} finally {
			IOUtils.close(in);
		}
	}

	static public GPUArchitecture getCurrent()
	{
		try {
			String gpuarch = System.getProperty("gpu.arch");

			String archName = _propertyMap.getProperty(_GPU_ARCH_PREFIX + gpuarch);
			return GPUArchitecture.valueOf(archName);
		} catch (Throwable cause) {
			throw new RuntimeException("Unable to determine current platform's architecture.", cause);
		}
	}
}
