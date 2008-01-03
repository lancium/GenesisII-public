package edu.virginia.vcgr.genii.client.utils.units;

import java.text.ParseException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple class to store a duration.  Durations are nothing more then
 * longs indicating the number of milliseconds in the duration.  What makes
 * this class special is it's ability to parse duration descriptions from
 * strings.
 * 
 * @author mmm2a
 */
public class Duration
{
	static private HashMap<String, Long> _milliSecondMap;
	
	static
	{
		/* We initialize a milli-second map so that we can
		 * easily convert from a duration unit (like days)
		 * into milliseconds.
		 */
		_milliSecondMap = new HashMap<String, Long>();
		Long value;
		
		value = new Long(365 * 24 * 60 * 60 * 1000L);
		_milliSecondMap.put("year", value);
		_milliSecondMap.put("years", value);
		_milliSecondMap.put("yr", value);
		_milliSecondMap.put("yrs", value);
		_milliSecondMap.put("y", value);
		
		value = new Long(30 * 24 * 60 * 60 * 1000L);
		_milliSecondMap.put("month", value);
		_milliSecondMap.put("months", value);
		
		value = new Long(7 * 24 * 60 * 60 * 1000L);
		_milliSecondMap.put("week", value);
		_milliSecondMap.put("weeks", value);
		_milliSecondMap.put("wks", value);
		_milliSecondMap.put("w", value);
		
		value = new Long(24 * 60 * 60 * 1000L);
		_milliSecondMap.put("day", value);
		_milliSecondMap.put("days", value);
		_milliSecondMap.put("d", value);
		
		value = new Long(60 * 60 * 1000L);
		_milliSecondMap.put("hour", value);
		_milliSecondMap.put("hours", value);
		_milliSecondMap.put("hrs", value);
		_milliSecondMap.put("hr", value);
		_milliSecondMap.put("h", value);
		
		value = new Long(60 * 1000L);
		_milliSecondMap.put("minute", value);
		_milliSecondMap.put("minutes", value);
		_milliSecondMap.put("min", value);
		_milliSecondMap.put("mins", value);
		_milliSecondMap.put("m", value);
		
		value = new Long(1000L);
		_milliSecondMap.put("second", value);
		_milliSecondMap.put("seconds", value);
		_milliSecondMap.put("sec", value);
		_milliSecondMap.put("secs", value);
		_milliSecondMap.put("s", value);
		
		value = new Long(1L);
		_milliSecondMap.put("millisecond", value);
		_milliSecondMap.put("milliseconds", value);
		_milliSecondMap.put("milli", value);
		_milliSecondMap.put("millis", value);
		_milliSecondMap.put("ms", value);
	}
	
	private long _milliseconds;
	
	/**
	 * Create a new duration with the given number of
	 * milliseconds.
	 * 
	 * @param milliseconds The number of milliseconds in this
	 * duration.
	 */
	public Duration(long milliseconds)
	{
		_milliseconds = milliseconds;
	}
	
	/**
	 * Acquire the number of milliseconds in this duration.
	 * 
	 * @return The number of milliseconds in this duration.
	 */
	public long getMilliseconds()
	{
		return _milliseconds;
	}
	
	static private Pattern _NUMBER_ONLY = Pattern.compile("^\\d+$");
	static private Pattern _TOKENS = Pattern.compile("\\W*(\\d+)\\s*([a-zA-Z]+)");
	/**
	 * This method creates a duration by parsing a string which represents 
	 * the duration with units included.  There are two possible ways to
	 * parse it.  If the string contains ONLY digits, then it is assumed to
	 * be milliseconds.  Otherwise, the string is one or more unit tokens
	 * where a unit token is an integer followed by one of<BR>
	 * <UL>
	 * <LI>year, years, or y (assumed to be 365 days)</LI>
	 * <LI>month or months (assumed to be 30 days)</LI>
	 * <LI>week, weeks, or w</LI>
	 * <LI>day, days, or d</LI>
	 * <LI>hour, hours, or h<LI>
	 * <LI>minute, minutes, min, mins, or m</LI>
	 * <LI>second, seconds, sec, secs, or s</LI>
	 * <LI>millisecond, milliseconds, or ms</LI>
	 * </UL>
	 * All non-alphanumerics are ignored between tokens and spaces are irrelevant.
	 * So, for example, it would parse both the string 
	 * "1 day, 5 minutes, 3 seconds" and the string
	 * "1d5m3s" as being 1 day, 5 minutes, and 3 seconds, or
	 * (3 + (5 + (1 * 24 * 60)) * 60) * 1000 milliseconds.
	 * 
	 * @param durationString The duration string to parse.
	 * @return A duration with the correct number of milliseconds represented.
	 */
	static public Duration parse(String durationString)
		throws ParseException
	{
		Matcher matcher;
		
		/* First, let's see if the string was just a simple number in which
		 * case we assume that that number represents milliseconds.
		 */
		matcher = _NUMBER_ONLY.matcher(durationString);
		if (matcher.matches())
			return new Duration(Long.parseLong(durationString));
		
		/*
		 * If it wasn't a simple number, then we parse a fancy duration
		 * string.
		 */
		long total = 0L;
		
		matcher = _TOKENS.matcher(durationString);
		
		/*
		 * While there are more tokens that match...
		 */
		while (matcher.find())
		{
			long value = Long.parseLong(matcher.group(1));
			String unit = matcher.group(2);
			
			Long multiplier = _milliSecondMap.get(unit);
			if (multiplier == null)
				throw new ParseException("Unknown duration unit encountered (" +
					unit + ").", matcher.regionStart());
			
			total += value * multiplier.longValue();
		}
		
		return new Duration(total);
	}
	
	static public void main(String []args) throws Throwable
	{
		parse("123");
		parse("1 day, 5hours, 3    seconds");
	}
}