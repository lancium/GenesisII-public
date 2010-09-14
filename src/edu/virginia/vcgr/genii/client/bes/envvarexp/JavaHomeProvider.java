package edu.virginia.vcgr.genii.client.bes.envvarexp;

import org.morgan.util.configuration.ConfigurationException;

public class JavaHomeProvider implements EnvironmentVariableExportValueProvider
{
	private String _value;
	
	public JavaHomeProvider()
	{
		_value = System.getProperty("java.home");
		if (_value == null)
		{
			_value = System.getenv("JAVA_HOME");
			if (_value == null)
				throw new ConfigurationException(
					"Unable to determine JAVA_HOME value.");
		}
	}
	
	@Override
	final public String value()
	{
		return _value;
	}
}