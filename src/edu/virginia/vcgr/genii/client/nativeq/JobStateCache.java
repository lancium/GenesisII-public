package edu.virginia.vcgr.genii.client.nativeq;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
		boolean usedCache = true;
		NativeQueueState state;
		
		synchronized(_cache)
		{
			if ( (_lastUpdated < 0) ||
				(cacheWindow >= 0 && 
					(System.currentTimeMillis() - _lastUpdated) >= cacheWindow))
			{
				usedCache = false;
				_cache.clear();
				_cache.putAll(fetcher.getStateMap());
				_lastUpdated = System.currentTimeMillis();
			}
			
			state = _cache.get(token);
			if (usedCache && state == null)
			{
				// It could be that the job is done, or it could simply be
				// that it's so new that we haven't cached it yet, we'll
				// give it a chance to show up by making the out call
				return this.get(token, fetcher, 0);
			}
		}
		
		_logger.debug(String.format("Status of \"%s\" is %s.\n",
			token, state));
		return state;
	}
}