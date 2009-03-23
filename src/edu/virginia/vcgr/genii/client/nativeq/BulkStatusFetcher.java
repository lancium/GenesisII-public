package edu.virginia.vcgr.genii.client.nativeq;

import java.util.Map;

public interface BulkStatusFetcher
{
	public Map<JobToken, NativeQueueState> getStateMap()
		throws NativeQueueException;
}