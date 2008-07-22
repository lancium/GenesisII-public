package edu.virginia.vcgr.genii.client.sysinfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.utils.exec.ExecutionEngine;

public class SystemUtils
{	
	static private Log _logger = LogFactory.getLog(SystemUtils.class);
	
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
		ProcessorArchitectureEnumeration primaryArchName = null;
		ProcessorArchitectureEnumeration secondaryArchName = null;
		
		String osarch = System.getProperty("os.arch");
		
		primaryArchName = ProcessorArchitectureEnumeration.fromString(
			_propertyMap.getProperty(_OS_ARCH_PREFIX + osarch));
		
		if (primaryArchName == ProcessorArchitectureEnumeration.x86_64)
			secondaryArchName = ProcessorArchitectureEnumeration.x86;
		else
		{
			try
			{
				String osName = System.getProperty("os.name");
				if (osName.equals("Linux"))
					secondaryArchName = ProcessorArchitectureEnumeration.fromString(
						ExecutionEngine.simpleExecute("uname", "-m"));
			}
			catch (IOException ioe)
			{
				_logger.warn("Unable to determine whether or not this JVM is running on a 64 bit machine.", ioe);
			}
		}
		
		if (secondaryArchName != null)
			return new CPUArchitecture_Type[] {
				new CPUArchitecture_Type(primaryArchName, null),
				new CPUArchitecture_Type(secondaryArchName, null)
			};
		
		return new CPUArchitecture_Type[] {
			new CPUArchitecture_Type(primaryArchName, null)
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
