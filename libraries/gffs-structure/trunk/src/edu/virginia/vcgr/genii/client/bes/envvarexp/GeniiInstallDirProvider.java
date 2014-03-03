package edu.virginia.vcgr.genii.client.bes.envvarexp;

import edu.virginia.vcgr.genii.client.configuration.Installation;

public class GeniiInstallDirProvider implements EnvironmentVariableExportValueProvider
{
	private String _value;

	public GeniiInstallDirProvider()
	{
		_value = Installation.getInstallDirectory().getAbsolutePath();
	}

	@Override
	final public String value()
	{
		return _value;
	}
}