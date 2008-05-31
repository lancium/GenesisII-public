package edu.virginia.vcgr.genii.configurer.conf;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.io.FileResource;

public class SettingDescription
{
	static private Log _logger = LogFactory.getLog(SettingDescription.class);
	
	private String _shortDescription;
	private String _longDescription;
	
	public SettingDescription(String shortDescription, String longDescription)
	{
		if (shortDescription == null)
			throw new IllegalArgumentException("Short description cannot be null.");
		if (longDescription == null)
			throw new IllegalArgumentException("Long description cannot be null.");
		
		_shortDescription = shortDescription;
		_longDescription = longDescription;
	}
	
	public SettingDescription(String shortDescription, FileResource longDescription)
	{
		this(shortDescription, readResource(longDescription));
	}
	
	public SettingDescription(FileResource shortDescription, String longDescription)
	{
		this(readResource(shortDescription), longDescription);
	}
	
	public SettingDescription(FileResource shortDescription, 
		FileResource longDescription)
	{
		this(readResource(shortDescription), readResource(longDescription));
	}
	
	public String shortDescription()
	{
		return _shortDescription;
	}
	
	public String longDescString()
	{
		return _longDescription;
	}
	
	static private String readResource(FileResource resource)
	{
		if (resource == null)
			throw new IllegalArgumentException(
				"Resource parameter cannot be null.");
		
		InputStream in = null;
		
		try
		{
			in = resource.open();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder builder = new StringBuilder();
			String line;
			while ( (line = reader.readLine()) != null)
			{
				builder.append(line);
				builder.append('\n');
			}
			
			return builder.toString();
		}
		catch (Throwable cause)
		{
			_logger.error(
				"Unable to read file resource for setting description.", 
				cause);
			return "Unable to read resource:  " + cause.getLocalizedMessage();  
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
}