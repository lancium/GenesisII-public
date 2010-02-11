package org.morgan.data;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class TimeRange
{
	private long _start;
	private long _stop;
	
	public TimeRange(long start, long stop)
	{
		if (start >= stop)
			throw new IllegalArgumentException(
				"Start time must be before stop time.");
		
		_start = start;
		_stop = stop;
	}
	
	public TimeRange(Calendar start, Calendar stop)
	{
		this(start.getTimeInMillis(), stop.getTimeInMillis());
	}
	
	public TimeRange(Calendar start, long duration, TimeUnit durationUnits)
	{
		this(start.getTimeInMillis(), start.getTimeInMillis() +
				durationUnits.toMillis(duration));
	}
	
	final public Calendar beginning()
	{
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(_start);
		return ret;
	}
	
	final public Calendar end()
	{
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(_stop);
		return ret;
	}
	
	final public boolean during(Calendar time)
	{
		return during(time.getTimeInMillis());
	}
	
	final public boolean during(long time)
	{
		return (time >= _start) && (time < _stop);
	}
	
	final public TimeRange previous()
	{
		long diff = _stop - _start;
		return new TimeRange(
			_start - diff, _stop - diff);
	}
	
	final public TimeRange next()
	{
		long diff = _stop - _start;
		return new TimeRange(
			_start + diff, _stop + diff);
	}
	
	@Override
	public String toString()
	{
		return String.format(
			"[%1$tD %1$tT, %2$tD %2$tT)",
			_start, _stop);
	}
}