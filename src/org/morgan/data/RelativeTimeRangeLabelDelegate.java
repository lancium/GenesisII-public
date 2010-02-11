package org.morgan.data;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class RelativeTimeRangeLabelDelegate 
	implements TimeAwareDataRangeLabelDelegate<TimeRange>
{
	static private String toWordCase(String string)
	{
		return string.substring(0, 1).toUpperCase() +
			string.substring(1).toLowerCase();
	}
	
	private long _mark = 0;
	private TimeUnit _units;
	private String _format;
	
	public RelativeTimeRangeLabelDelegate(String format, TimeUnit units)
	{
		_units = units;
		_format = format;
	}
	
	public RelativeTimeRangeLabelDelegate(TimeUnit units)
	{
		this(String.format("-%%d %s",
			toWordCase(units.toString())), units);
	}
	
	@Override
	final public void markTime(Calendar timestamp)
	{
		_mark = timestamp.getTimeInMillis();
	}

	@Override
	public String toString(TimeRange dataRange)
	{
		return String.format(_format,
			(_mark - dataRange.beginning().getTimeInMillis()) / 
			_units.toMillis(1));
	}
}