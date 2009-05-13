package edu.virginia.vcgr.genii.client.utils.units;

import java.io.Serializable;
import java.text.ParseException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple class to store a size.  Sizes are nothing more then
 * longs indicating the number of bytes in the size.  What makes
 * this class special is it's ability to parse size descriptions from
 * strings.
 * 
 * @author mmm2a
 */
public class Size implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static private final long BYTES_PER_BYTE = 1L;
	static private final long BYTES_PER_KILOBYTE = 1024L * BYTES_PER_BYTE;
	static private final long BYTES_PER_MEGABYTE = 1024L * BYTES_PER_KILOBYTE;
	static private final long BYTES_PER_GIGABYTE = 1024L * BYTES_PER_MEGABYTE;
	static private final long BYTES_PER_TERABYTE = 1024L * BYTES_PER_GIGABYTE;
	
	static private HashMap<String, Long> _byteMap;
	
	static
	{
		/* We initialize a byte map so that we can
		 * easily convert from a size unit (like KB)
		 * into bytes.
		 */
		_byteMap = new HashMap<String, Long>();
		Long value;
		
		value = new Long(BYTES_PER_TERABYTE);
		_byteMap.put("terabyte", value);
		_byteMap.put("terabytes", value);
		_byteMap.put("tb", value);
		_byteMap.put("t", value);
		
		value = new Long(BYTES_PER_GIGABYTE);
		_byteMap.put("gigabyte", value);
		_byteMap.put("gigabytes", value);
		_byteMap.put("gig", value);
		_byteMap.put("gb", value);
		_byteMap.put("g", value);
		
		value = new Long(BYTES_PER_MEGABYTE);
		_byteMap.put("megabyte", value);
		_byteMap.put("megabytes", value);
		_byteMap.put("meg", value);
		_byteMap.put("mb", value);
		_byteMap.put("m", value);
		
		value = new Long(BYTES_PER_KILOBYTE);
		_byteMap.put("kilobyte", value);
		_byteMap.put("kilobytes", value);
		_byteMap.put("kb", value);
		_byteMap.put("k", value);
		
		value = new Long(BYTES_PER_BYTE);
		_byteMap.put("byte", value);
		_byteMap.put("bytes", value);
		_byteMap.put("b", value);
	}
	
	private long _bytes;
	
	/**
	 * Create a new size with the given number of
	 * bytes.
	 * 
	 * @param bytes The number of bytes in this
	 * size.
	 */
	public Size(long bytes)
	{
		_bytes = bytes;
	}
	
	/**
	 * Acquire the number of bytes in this size.
	 * 
	 * @return The number of bytes in this size.
	 */
	public long getBytes()
	{
		return _bytes;
	}
	
	@Override
	public String toString()
	{
		int tera;
		int giga;
		int mega;
		int kilo;
		
		long b = _bytes;
		tera = (int)(b / BYTES_PER_TERABYTE);
		b %= BYTES_PER_TERABYTE;
		
		giga = (int)(b / BYTES_PER_GIGABYTE);
		b %= BYTES_PER_GIGABYTE;
		
		mega = (int)(b / BYTES_PER_MEGABYTE);
		b %= BYTES_PER_MEGABYTE;
		
		kilo = (int)(b / BYTES_PER_KILOBYTE);
		b %= BYTES_PER_KILOBYTE;
		
		return String.format("%d TB, %d GB, %d MB, %d KB, %d B", 
			tera, giga, mega, kilo, b);
	}
	
	static private Pattern _NUMBER_ONLY = Pattern.compile("^\\d+$");
	static private Pattern _TOKENS = Pattern.compile("\\W*(\\d+)\\s*([a-zA-Z]+)");
	
	static public Size parse(String durationString)
		throws ParseException
	{
		Matcher matcher;
		
		/* First, let's see if the string was just a simple number in which
		 * case we assume that that number represents bytes.
		 */
		matcher = _NUMBER_ONLY.matcher(durationString);
		if (matcher.matches())
			return new Size(Long.parseLong(durationString));
		
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
			
			Long multiplier = _byteMap.get(unit.toLowerCase());
			if (multiplier == null)
				throw new ParseException("Unknown size unit encountered (" +
					unit + ").", matcher.regionStart());
			
			total += value * multiplier.longValue();
		}
		
		return new Size(total);
	}
}