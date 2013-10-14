package edu.virginia.vcgr.genii.client.utils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Duration implements Serializable, Comparable<Duration>
{
	static final long serialVersionUID = 0L;

	private long _milliseconds;

	public Duration(long milliseconds)
	{
		if (milliseconds < 0)
			throw new IllegalArgumentException("Duration value must be non-negative.");

		_milliseconds = milliseconds;
	}

	public Duration(long timeoutValue, TimeUnit timeoutUnits)
	{
		this(TimeUnit.MILLISECONDS.convert(timeoutValue, timeoutUnits));
	}

	final public long getTimeoutInMilliseconds()
	{
		return _milliseconds;
	}

	final public long getTimeout(TimeUnit units)
	{
		return units.convert(_milliseconds, TimeUnit.MILLISECONDS);
	}

	final public Calendar getExpiration(Calendar startTime)
	{
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(startTime.getTimeInMillis() + _milliseconds);
		return ret;
	}

	final public Calendar getExpiration()
	{
		return getExpiration(Calendar.getInstance());
	}

	public boolean equals(Duration other)
	{
		return _milliseconds == other._milliseconds;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Duration)
			return equals((Duration) other);

		return false;
	}

	@Override
	public int hashCode()
	{
		return (int) _milliseconds;
	}

	@Override
	public String toString()
	{
		return Long.toString(_milliseconds);
	}

	public String toString(TimeUnit units)
	{
		return Long.toString(getTimeout(units));
	}

	@Override
	public int compareTo(Duration other)
	{
		long value = _milliseconds - other._milliseconds;
		if (value < 0)
			return -1;
		else if (value > 0)
			return 1;
		else
			return 0;
	}

	final static public Duration ZeroDuration = new Duration(0L);
	final static public Duration InfiniteDuration = new Duration(Long.MAX_VALUE);
}