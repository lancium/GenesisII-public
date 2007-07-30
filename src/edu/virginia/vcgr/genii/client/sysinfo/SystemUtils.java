package edu.virginia.vcgr.genii.client.sysinfo;

import java.io.InputStream;
import java.util.Properties;

import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.morgan.util.io.StreamUtils;

public class SystemUtils
{	
	static final private String _OS_ARCH_PREFIX = "os.arch.";
	static final private String _OS_NAME_PREFIX = "os.name.";
	static private Properties _propertyMap;
	
	static
	{
		InputStream in = null;
		try
		{
			in = Thread.currentThread().getContextClassLoader(
				).getResourceAsStream(
				"edu/virginia/vcgr/genii/client/sysinfo/property-map.properties");
			_propertyMap = new Properties();
			_propertyMap.load(in);
		}
		catch (Exception e)
		{
			throw new RuntimeException(
				"Unable to initialize system information.", e);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	static public CPUArchitecture_Type[] getSupportedArchitectures()
	{
		ProcessorArchitectureEnumeration archName;
		String osarch = System.getProperty("os.arch");
		
		archName = ProcessorArchitectureEnumeration.fromString(
			_propertyMap.getProperty(_OS_ARCH_PREFIX + osarch));
		
		return new CPUArchitecture_Type[] {
			new CPUArchitecture_Type(archName, null)
		};
	}
	
	static public OperatingSystem_Type[] getSupportedOperatingSystems()
	{
		OperatingSystemTypeEnumeration operatingSystemName;
		String operatingSystemVersion;
		String description = null;
		
		String osname = System.getProperty("os.name");
		operatingSystemName = OperatingSystemTypeEnumeration.fromString(
			_propertyMap.getProperty(_OS_NAME_PREFIX + osname));
		operatingSystemVersion = System.getProperty("os.version");
		
		return new OperatingSystem_Type[] {
			new OperatingSystem_Type(
				new OperatingSystemType_Type(operatingSystemName, null),
				operatingSystemVersion, description, null)
		};
	}
	
	static public void main(String []args) throws Throwable
	{
		System.err.println(getSupportedArchitectures());
		System.err.println(getSupportedOperatingSystems());
	}
}
