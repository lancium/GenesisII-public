package edu.virginia.vcgr.genii.client.nativeq;

import java.util.HashMap;
import java.util.Map;

public class JobStateCache
{
	private Map<JobToken, NativeQueueState> _cache =
		new HashMap<JobToken, NativeQueueState>();
	private long _lastUpdated = -1L;
	
	public NativeQueueState get(JobToken token,
		BulkStatusFetcher fetcher, long cacheWindow) 
			throws NativeQueueException
	{
		synchronized(_cache)
		{
			if ( (_lastUpdated < 0) ||
				(cacheWindow >= 0 && 
					(System.currentTimeMillis() - _lastUpdated) > cacheWindow))
			{
				_cache.clear();
				_cache.putAll(fetcher.getStateMap());
				_lastUpdated = System.currentTimeMillis();
			}
			
			return _cache.get(token);
		}
	}
}