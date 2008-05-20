package edu.virginia.vcgr.genii.container.stats;

public class DBConnectionDataPoint implements DataPoint
{
	private long _openTime;
	private long _closeTime;
	
	public DBConnectionDataPoint()
	{
		_openTime = System.currentTimeMillis();
		_closeTime = -1L;
	}
	
	public void markClosed()
	{
		_closeTime = System.currentTimeMillis();
	}
	
	public boolean withinWindow(long currentTime, long windowSize)
	{
		return _openTime >= (currentTime - windowSize);
	}
	
	public boolean isClosed()
	{
		return _closeTime >= 0L;
	}
	
	public long duration()
	{
		return _closeTime - _openTime;
	}
}