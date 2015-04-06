package edu.virginia.vcgr.genii.client.nativeq.slurm;

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

public class SLURMQueue extends AbstractNativeQueue<SLURMQueueConfiguration>
{
	static final public String PROVIDER_NAME = "slurm";

	private JobStateCache _statusCache = new JobStateCache();

	public SLURMQueue()
	{
		super(PROVIDER_NAME, SLURMQueueConfiguration.class);
	}

	@Override
	public NativeQueueConnection connect(ResourceOverrides resourceOverrides, CmdLineManipulatorConfiguration cmdLineManipulatorConf,
		File workingDirectory, NativeQueueConfiguration nativeQueueConfiguration, Object providerConfiguration) throws NativeQueueException
	{
		SLURMQueueConfiguration slurmConfig = (SLURMQueueConfiguration) providerConfiguration;

		if (nativeQueueConfiguration == null)
			nativeQueueConfiguration = new NativeQueueConfiguration();

		if (slurmConfig == null)
			slurmConfig = new SLURMQueueConfiguration();

		if (cmdLineManipulatorConf == null)
			cmdLineManipulatorConf = new CmdLineManipulatorConfiguration();

		String qname = slurmConfig.queueName();

		try {
			return new SLURMQueueConnection(resourceOverrides, cmdLineManipulatorConf, workingDirectory, nativeQueueConfiguration,
				slurmConfig, qname, slurmConfig.startSBatch(), slurmConfig.startSQueue(), slurmConfig.startSCancel(), _statusCache);
		} catch (FileNotFoundException fnfe) {
			throw new NativeQueueException("Unable to find queue binaries.", fnfe);
		}
	}

	@Override
	final public ResourceManagerType resourceManagerType()
	{
		return ResourceManagerType.SLURM;
	}
}
