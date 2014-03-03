package edu.virginia.vcgr.genii.client.nativeq;

import java.util.Properties;

public abstract class AbstractNativeQueue<ConfigType> implements NativeQueue
{
	private String _providerName;
	private Class<ConfigType> _providerConfigurationType;

	protected AbstractNativeQueue(String providerName, Class<ConfigType> providerConfigurationType)
	{
		_providerName = providerName;
		_providerConfigurationType = providerConfigurationType;
	}

	@Override
	public String getProviderName()
	{
		return _providerName;
	}

	@Override
	public Class<ConfigType> providerConfigurationType()
	{
		return _providerConfigurationType;
	}

	protected String getRequiredProperty(String propertyName, Properties connectionProperties) throws NativeQueueException
	{
		String value = connectionProperties.getProperty(propertyName);
		if (value == null)
			throw new NativeQueueException("Unable to find required queue connection property \"" + propertyName + "\".");

		return value;
	}
}