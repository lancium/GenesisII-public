package edu.virginia.vcgr.genii.client.nativeq;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.mortbay.log.LogFactory;

public class JobStateCache
{
	static private Log _logger = LogFactory.getLog(JobStateCache.class);
	
	private Map<JobToken, NativeQueueState> _cache =
		new HashMap<JobToken, NativeQueueState>();
	private long _lastUpdated = -1L;
	
	public NativeQueueState get(JobToken token,
		BulkStatusFetcher fetcher, long cacheWindow) 
			throws NativeQueueException
	{
		NativeQueueState state;
		
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
			
			state = _cache.get(token);
		}
		
		_logger.debug(String.format("Status of \"%s\" is %s.\n",
			token, state));
		return state;
	}
}