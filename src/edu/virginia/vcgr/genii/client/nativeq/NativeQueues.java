package edu.virginia.vcgr.genii.client.nativeq;

import java.util.ServiceLoader;

public class NativeQueues
{
	static private ServiceLoader<NativeQueue> _queues =
		ServiceLoader.load(NativeQueue.class);
	
	static public NativeQueue getNativeQueue(String providerName)
		throws NativeQueueException
	{
		for (NativeQueue queue : _queues)
		{
			if (queue.getProviderName().equals(providerName))
				return queue;
		}
		
		throw new NativeQueueException("Unable to find queue provider \"" 
			+ providerName + "\".");
	}
}