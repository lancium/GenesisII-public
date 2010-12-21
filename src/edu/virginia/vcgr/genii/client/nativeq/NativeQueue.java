package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;

import edu.virginia.vcgr.genii.client.bes.ResourceManagerType;
import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;

public interface NativeQueue
{
	public String getProviderName();
	public Class<?> providerConfigurationType();
	public ResourceManagerType resourceManagerType();
	
	public NativeQueueConnection connect(
		ResourceOverrides resourceOverrides,
		File workingDirectory,
		NativeQueueConfiguration nativeQueueConfiguration,
		Object providerConfiguration) throws NativeQueueException;
}