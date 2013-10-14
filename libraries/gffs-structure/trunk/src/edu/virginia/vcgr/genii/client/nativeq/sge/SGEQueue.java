package edu.virginia.vcgr.genii.client.nativeq.sge;

import java.io.File;
import java.io.FileNotFoundException;

import edu.virginia.vcgr.genii.client.bes.ResourceManagerType;
import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.client.nativeq.AbstractNativeQueue;
import edu.virginia.vcgr.genii.client.nativeq.JobStateCache;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConfiguration;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;

public class SGEQueue extends AbstractNativeQueue<SGEQueueConfiguration>
{
	static final public String PROVIDER_NAME = "sge";

	private JobStateCache _statusCache = new JobStateCache();

	public SGEQueue()
	{
		super(PROVIDER_NAME, SGEQueueConfiguration.class);
	}

	@Override
	public NativeQueueConnection connect(ResourceOverrides resourceOverrides,
		CmdLineManipulatorConfiguration cmdLineManipulatorConf, File workingDirectory,
		NativeQueueConfiguration nativeQueueConfiguration, Object providerConfiguration) throws NativeQueueException
	{
		SGEQueueConfiguration sgeConfig = (SGEQueueConfiguration) providerConfiguration;

		if (nativeQueueConfiguration == null)
			nativeQueueConfiguration = new NativeQueueConfiguration();

		if (cmdLineManipulatorConf == null)
			cmdLineManipulatorConf = new CmdLineManipulatorConfiguration();

		if (sgeConfig == null)
			sgeConfig = new SGEQueueConfiguration();

		String qname = sgeConfig.queueName();

		try {
			return new SGEQueueConnection(resourceOverrides, cmdLineManipulatorConf, workingDirectory,
				nativeQueueConfiguration, sgeConfig, qname, sgeConfig.startQSub(), sgeConfig.startQStat(),
				sgeConfig.startQDel(), _statusCache);
		} catch (FileNotFoundException fnfe) {
			throw new NativeQueueException("Unable to find queue binaries.", fnfe);
		}
	}

	@Override
	final public ResourceManagerType resourceManagerType()
	{
		return ResourceManagerType.SGE;
	}
}