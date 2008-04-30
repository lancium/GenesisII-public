package edu.virginia.vcgr.genii.client.nativeq;

import java.util.Properties;

public abstract class AbstractNativeQueue implements NativeQueue
{
	private String _providerName;
	
	protected AbstractNativeQueue(String providerName)
	{
		_providerName = providerName;
	}
	
	@Override
	public String getProviderName()
	{
		return _providerName;
	}
	
	protected String getRequiredProperty(String propertyName,
		Properties connectionProperties) throws NativeQueueException
	{
		String value = connectionProperties.getProperty(propertyName);
		if (value == null)
			throw new NativeQueueException(
				"Unable to find required queue connection property \"" +
				propertyName + "\".");
		
		return value;
	}
}