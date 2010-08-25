package edu.virginia.vcgr.genii.client.utils.units;

import java.util.Calendar;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A simple class to store a duration.  Durations are nothing more then
 * longs indicating the number of milliseconds in the duration.  What makes
 * this class special is it's ability to parse duration descriptions from
 * strings.
 * 
 * @author mmm2a
 */
@XmlJavaTypeAdapter(DurationXmlAdapter.class)
public class Duration extends UnitableValue<DurationUnits>
{
	static final long serialVersionUID = 0L;
	
	static private final long MILLISECONDS_PER_YEAR = DurationUnits.Years.multiplier();
	static private final long MILLISECONDS_PER_MONTH = DurationUnits.Months.multiplier();
	@SuppressWarnings("unused")
	static private final long MILLISECONDS_PER_WEEK = DurationUnits.Weeks.multiplier();
	static private final long MILLISECONDS_PER_DAY = DurationUnits.Days.multiplier();
	static private final long MILLISECONDS_PER_HOUR = DurationUnits.Hours.multiplier();
	static private final long MILLISECONDS_PER_MINUTE = DurationUnits.Minutes.multiplier();
	static private final long MILLISECONDS_PER_SECOND = DurationUnits.Seconds.multiplier();

	@Override
	protected DurationUnits defaultUnits()
	{
		return DurationUnits.Milliseconds;
	}

	@Override
	protected DurationUnits parseUnits(String textRepresentation)
	{
		return DurationUnits.parse(textRepresentation);
	}
	
	@Override
	public double as(DurationUnits targetUnits)
	{
		return targetUnits.convert(value(), units());
	}
	
	public Duration()
	{
		super();
	}

	public Duration(double value, DurationUnits units)
	{
		super(value, units);
	}

	public Duration(double value)
	{
		super(value);
	}

	public Duration(String textRepresentation)
	{
		super(textRepresentation);
	}

	final public Calendar getTime()
	{
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(ret.getTimeInMillis() + (long)as(DurationUnits.Milliseconds));
		return ret;
	}
	
	public org.apache.axis.types.Duration toApacheDuration()
	{
		int years;
		int days;
		int hours;
		int minutes;
		int seconds;
		
		long millis = (long)units().toMilliseconds(value());
		years = (int)(millis / MILLISECONDS_PER_YEAR);
		millis %= MILLISECONDS_PER_YEAR;
		
		days = (int)(millis / MILLISECONDS_PER_DAY);
		millis %= MILLISECONDS_PER_DAY;
		
		hours = (int)(millis/ MILLISECONDS_PER_HOUR);
		millis %= MILLISECONDS_PER_HOUR;
		
		minutes = (int)(millis / MILLISECONDS_PER_MINUTE);
		millis %= MILLISECONDS_PER_MINUTE;
		
		seconds = (int)(millis / MILLISECONDS_PER_SECOND);
		
		return new org.apache.axis.types.Duration(
			false, years, 0, days, hours, minutes, seconds);
	}
	
	static public Duration fromApacheDuration(
		org.apache.axis.types.Duration aDur)
	{
		if (aDur == null)
			return null;
		
		long millis = aDur.getYears() * MILLISECONDS_PER_YEAR;
		millis += aDur.getMonths() * MILLISECONDS_PER_MONTH;
		millis += aDur.getDays() * MILLISECONDS_PER_DAY;
		millis += aDur.getHours() * MILLISECONDS_PER_HOUR;
		millis += aDur.getMinutes() * MILLISECONDS_PER_MINUTE;
		millis += aDur.getSeconds() * MILLISECONDS_PER_SECOND;
		
		return new Duration(millis);
	}
}