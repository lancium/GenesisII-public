package edu.virginia.vcgr.genii.client.utils.units;

public enum DurationUnits
{
	Milliseconds(1L, "ms", "milli", "millis", "millisecond", "milliseconds"),
	Seconds(1000L, "s", "sec", "secs", "second", "seconds"),
	Minutes(1000L * 60, "m", "min", "mins", "minute", "minutes"),
	Hours(1000L * 60 * 60, "h", "hr", "hrs", "hour", "hours"),
	Days(1000L * 60 * 60 * 24, "d", "dy", "dys", "day", "days"),
	Weeks(1000L * 60 * 60 * 24 * 7, "w", "wks", "week", "weeks"),
	Months(1000L * 60 * 60 * 24 * 30, "mth", "mths", "mon", "mons", "month", "months"),
	Years(1000L * 60 * 60 * 24 * 365, "y", "yr", "yrs", "year", "years");
	
	private long _multiplier;
	private String []_alternateNames;
	
	private DurationUnits(long multiplier, String...alternateNames)
	{
		_multiplier = multiplier;
		_alternateNames = alternateNames;
	}

	public long multiplier()
	{
		return _multiplier;
	}
	
	public double convert(double sourceValue, DurationUnits sourceUnits)
	{
		return sourceValue * (sourceUnits._multiplier / _multiplier);
	}
	
	public double toMilliseconds(double sourceValue)
	{
		return convert(sourceValue, DurationUnits.Milliseconds);
	}
	
	public double toSeconds(double sourceValue)
	{
		return convert(sourceValue, DurationUnits.Seconds);
	}
	
	public double toMinutes(double sourceValue)
	{
		return convert(sourceValue, DurationUnits.Minutes);
	}
	
	public double toHours(double sourceValue)
	{
		return convert(sourceValue, DurationUnits.Hours);
	}
	
	public double toDays(double sourceValue)
	{
		return convert(sourceValue, DurationUnits.Days);
	}
	
	public double toMonths(double sourceValue)
	{
		return convert(sourceValue, DurationUnits.Months);
	}
	
	public double toYears(double sourceValue)
	{
		return convert(sourceValue, DurationUnits.Years);
	}
	
	static public DurationUnits parse(String text)
	{
		for (DurationUnits units : DurationUnits.values())
		{
			if (text.compareToIgnoreCase(units.name()) == 0)
				return units;
			
			for (String name : units._alternateNames)
				if (text.compareToIgnoreCase(name) == 0)
					return units;
		}
		
		throw new IllegalArgumentException(String.format(
			"Can't match %s to a Duration enumeration value.",
			text));
	}
}