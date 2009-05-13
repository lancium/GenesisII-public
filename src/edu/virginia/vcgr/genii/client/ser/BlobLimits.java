package edu.virginia.vcgr.genii.client.ser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.utils.units.Size;

public class BlobLimits
{
	static private Log _logger = LogFactory.getLog(BlobLimits.class);
	
	static private final Pattern SECTION_PATTERN = Pattern.compile(
		"^\\[([^\\]]+)\\]$");
	static private final Pattern VARIABLE_PATTERN = Pattern.compile(
		"^\\$\\{([^\\}]+)\\}$");
	
	static private final long TWO_GIG = 1024L * 1024L * 1024L * 2;
	
	static private final String CONSTANTS_SECTION = "constants";
	
	static private BlobLimits _blobLimits;
	
	static
	{
		InputStream in = null;
		
		try
		{
			in = BlobLimits.class.getResourceAsStream("blob-limits.cfg");
			if (in == null)
				throw new ConfigurationException(
					"Unable to load blob limits resource.");
			
			_blobLimits = new BlobLimits(in);
		}
		catch (IOException ioe)
		{
			throw new ConfigurationException(
				"Unable to read from blob limits resource.", ioe);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	static private Long parseSize(String str)
	{
		try
		{
			Size s = Size.parse(str);
			return new Long(s.getBytes());
		}
		catch (ParseException e)
		{
			throw new ConfigurationException(String.format(
				"Unable to parse size \"%s\".", str), e);
		}
	}
	
	static public BlobLimits limits()
	{
		return _blobLimits;
	}
	
	private Map<String, Map<String, Long>> _limits =
		new HashMap<String, Map<String,Long>>();
	
	private String parseLine(String currentSection, 
		Map<String, Long> constants, String line)
	{
		Long lValue;
		
		Matcher matcher = SECTION_PATTERN.matcher(line);
		if (matcher.matches())
			currentSection = matcher.group(1);
		else
		{
			if (currentSection == null)
				throw new ConfigurationException(String.format(
					"Blob limit line \"%s\" found outside of sections.\n",
					line));
				
			int index = line.indexOf('=');
			if (index < 0)
				throw new ConfigurationException(String.format(
					"Cannot parse blob limit line \"%s\".\n",
					line));
			
			String name = line.substring(0, index).trim();
			String value = line.substring(index + 1).trim();
			
			matcher = VARIABLE_PATTERN.matcher(value);
			if (matcher.matches())
			{
				String variable = matcher.group(1);
				lValue = constants.get(variable);
				if (lValue == null)
					throw new ConfigurationException(String.format(
						"Variable \"%s\" is undefined.", variable));
			} else
			{
				lValue = parseSize(value);
			}
			
			if (currentSection.equals(CONSTANTS_SECTION))
				constants.put(name, lValue);
			else
			{
				Map<String, Long> section = _limits.get(currentSection);
				if (section == null)
					_limits.put(currentSection, section = 
						new HashMap<String, Long>());
				section.put(name, lValue);
			}
		}
		
		return currentSection;
	}
	
	private BlobLimits(InputStream in) 
		throws IOException
	{
		String currentSection = null;
		String line = null;
		
		Map<String, Long> constants = new HashMap<String, Long>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		while ( (line = reader.readLine()) != null )
		{
			int index = line.indexOf('#');
			if (index >= 0)
				line = line.substring(0, index);
			line = line.trim();
			
			if (line.length() == 0)
				continue;
			
			currentSection = parseLine(currentSection, constants, line);
		}
	}

	public long getLimit(String tableName, String columnName)
	{		
		Map<String, Long> tableLimits = _limits.get(tableName);
		if (tableLimits == null)
		{
			_logger.warn(String.format(
				"Warning:  No BLOB size limits set for table %s.", tableName));
			
			return TWO_GIG;
		}
		
		Long value = tableLimits.get(columnName);
		if (value == null)
		{
			_logger.warn(String.format(
				"Warning:  No BLOB size limits set for column %s in table %s.", 
				tableName, columnName));
			return TWO_GIG;
		}
		
		return value.longValue();
	}
	
	public void checkLimit(long blobSize, String tableName, String columnName)
	{
		long value = getLimit(tableName, columnName);
		if (blobSize >= value)
			_logger.warn(String.format(
				"Warning:  Recommended BLOB limit (%d) for table %s and column %s was exceeded (%d).\n",
				value, tableName, columnName, blobSize));
	}
}