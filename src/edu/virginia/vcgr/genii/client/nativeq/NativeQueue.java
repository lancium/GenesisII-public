package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;

import edu.virginia.vcgr.genii.client.bes.ResourceManagerType;
import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;

public interface NativeQueue
{
	public String getProviderName();
	public Class<?> providerConfigurationType();
	public ResourceManagerType resourceManagerType();
	
	public NativeQueueConnection connect(
		ResourceOverrides resourceOverrides,
		CmdLineManipulatorConfiguration cmdLineManipulatorCon,
		File workingDirectory,
		NativeQueueConfiguration nativeQueueConfiguration,
		Object providerConfiguration) throws NativeQueueException;
}