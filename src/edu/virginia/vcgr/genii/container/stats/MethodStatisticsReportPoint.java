package edu.virginia.vcgr.genii.container.stats;

public class MethodStatisticsReportPoint
{
	private long _totalCallsStarted;
	private long _totalSucceeded;
	private long _totalFailed;
	
	private long _totalSuccessfullCallDuration;
	private long _totalFailedCallDuration;
	
	public MethodStatisticsReportPoint()
	{
		_totalCallsStarted = 0L;
		_totalSucceeded = 0L;
		_totalFailed = 0L;
		
		_totalSuccessfullCallDuration = 0L;
		_totalFailedCallDuration = 0L;
	}
	
	void add(MethodDataPoint dp)
	{
		_totalCallsStarted++;
		
		if (dp.isCompleted())
		{
			if (dp.successfull())
			{
				_totalSucceeded++;
				_totalSuccessfullCallDuration += dp.duration();
			} else
			{
				_totalFailed++;
				_totalFailedCallDuration += dp.duration();
			}
		}
	}
	
	public long totalCallsStarted()
	{
		return _totalCallsStarted;
	}
	
	public long totalSucceeded()
	{
		return _totalSucceeded;
	}
	
	public long totalFailed()
	{
		return _totalFailed;
	}
	
	public long totalCompleted()
	{
		return _totalFailed + _totalSucceeded;
	}
	
	public double failureRate()
	{
		return (double)_totalFailed / totalCompleted();
	}
	
	public long averageSuccessfullDuration()
	{
		return _totalSuccessfullCallDuration / _totalSucceeded;
	}
	
	public long averageFailedDuration()
	{
		return _totalFailedCallDuration / _totalFailed;
	}
	
	public long averageDuration()
	{
		return (_totalSuccessfullCallDuration + _totalFailedCallDuration) / (_totalSucceeded + _totalFailed);
	}
}