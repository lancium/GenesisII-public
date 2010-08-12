package edu.virginia.vcgr.genii.client.nativeq.pbs;

import java.io.File;
import java.io.FileNotFoundException;

import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.client.nativeq.AbstractNativeQueue;
import edu.virginia.vcgr.genii.client.nativeq.JobStateCache;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConfiguration;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;

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
		File workingDirectory,
		NativeQueueConfiguration nativeQueueConfiguration,
		Object providerConfiguration) throws NativeQueueException
	{
		PBSQueueConfiguration pbsConfig = (PBSQueueConfiguration)providerConfiguration;
		
		if (nativeQueueConfiguration == null)
			nativeQueueConfiguration = new NativeQueueConfiguration();
		
		if (pbsConfig == null)
			pbsConfig = new PBSQueueConfiguration();
		
		String qname = pbsConfig.queueName();
		
		try
		{
			return new PBSQueueConnection(resourceOverrides,
				workingDirectory,
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
}