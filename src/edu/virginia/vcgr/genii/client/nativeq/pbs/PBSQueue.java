package edu.virginia.vcgr.genii.client.nativeq.pbs;

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

public class PBSQueue extends AbstractNativeQueue<PBSQueueConfiguration>
{
	static final public String PROVIDER_NAME = "pbs";
	
	private JobStateCache _statusCache = new JobStateCache();
	
	public PBSQueue()
	{
		super(PROVIDER_NAME, PBSQueueConfiguration.class);
	}
	
	@Override
	public NativeQueueConnection connect(
		ResourceOverrides resourceOverrides,
		CmdLineManipulatorConfiguration cmdLineManipulatorConf,
		File workingDirectory,
		NativeQueueConfiguration nativeQueueConfiguration,
		Object providerConfiguration) throws NativeQueueException
	{
		PBSQueueConfiguration pbsConfig = (PBSQueueConfiguration)providerConfiguration;
		
		if (nativeQueueConfiguration == null)
			nativeQueueConfiguration = new NativeQueueConfiguration();
		
		if (pbsConfig == null)
			pbsConfig = new PBSQueueConfiguration();
		
		if (cmdLineManipulatorConf == null)
			cmdLineManipulatorConf = new CmdLineManipulatorConfiguration();
		
		String qname = pbsConfig.queueName();
		
		try
		{
			return new PBSQueueConnection(resourceOverrides,
				cmdLineManipulatorConf, workingDirectory,
				nativeQueueConfiguration, pbsConfig, qname, 
				pbsConfig.startQSub(), pbsConfig.startQStat(),
				pbsConfig.startQDel(), _statusCache);
		}
		catch (FileNotFoundException fnfe)
		{
			throw new NativeQueueException(
				"Unable to find queue binaries.", fnfe);
		}
	}

	@Override
	final public ResourceManagerType resourceManagerType()
	{
		return ResourceManagerType.PBS;
	}
}