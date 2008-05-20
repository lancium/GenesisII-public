package edu.virginia.vcgr.genii.container.stats;

public class DatabaseStatisticsReport
{
	private long _numOpened;
	private long _numClosed;
	private long _totalDuration;
	
	public DatabaseStatisticsReport()
	{
		_numOpened = 0L;
		_numClosed = 0L;
		_totalDuration = 0L;
	}
	
	void add(DBConnectionDataPoint dataPoint)
	{
		_numOpened++;
		if (dataPoint.isClosed())
		{
			_numClosed++;
			_totalDuration += dataPoint.duration();
		}
	}
	
	public long numOpened()
	{
		return _numOpened;
	}
	
	public long numClosed()
	{
		return _numClosed;
	}
	
	public long averageDuration()
	{
		return _totalDuration / _numClosed;
	}
}