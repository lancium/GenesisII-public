package edu.virginia.vcgr.genii.client.stats;

import java.io.Serializable;

public class MethodStatisticsReportPoint implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private long _totalCallsStarted;
	private long _totalSucceeded;
	private long _totalFailed;
	
	private long _totalSuccessfullCallDuration;
	private long _totalFailedCallDuration;
	
	public String toString()
	{
		return String.format("Calls Started = %d, Calls Succeeded = %d, Calls Failed = %d, Failure Rate = %.2f%%, Average Duration = %d ms",
			totalCallsStarted(), totalSucceeded(), totalFailed(), failureRate() * 100,
			averageDuration());
	}
	
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
		long completed = totalCompleted();
		if (completed == 0)
			return Double.NaN;
		
		return (double)_totalFailed / totalCompleted();
	}
	
	public long averageSuccessfullDuration()
	{
		if (_totalSucceeded == 0)
			return -1L;
		
		return _totalSuccessfullCallDuration / _totalSucceeded;
	}
	
	public long averageFailedDuration()
	{
		if (_totalFailed == 0)
			return -1L;
		
		return _totalFailedCallDuration / _totalFailed;
	}
	
	public long averageDuration()
	{
		long div = totalCompleted();
		if (div == 0)
			return -1L;
		
		return (_totalSuccessfullCallDuration + _totalFailedCallDuration) / (div);
	}
}