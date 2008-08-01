package org.morgan.util.ini;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.morgan.util.io.StreamUtils;

public class INIFile
{
	static private final Pattern SECTION_PATTERN =
		Pattern.compile("^\\s*\\[([^\\]]+)\\]\\s*$");
	static private final Pattern PROPERTY_PATTERN =
		Pattern.compile("^\\s*([^=\\s]+)\\s*=\\s*(.*)$");
	
	private Properties _default;
	private Map<String, Properties> _sections = 
		new HashMap<String, Properties>();	

	private void initialize(BufferedReader reader) throws IOException
	{
		int lineNo = 0;
		_default = new Properties();
		Properties currentSection = _default;
		String line;
		Matcher matcher;
		
		while ( (line = reader.readLine()) != null)
		{
			lineNo++;
			int index = line.indexOf('#');
			if (index >= 0)
				line = line.substring(0, index);
			
			matcher = SECTION_PATTERN.matcher(line);
			if (matcher.matches())
			{
				String sectionName = matcher.group(1);
				currentSection = _sections.get(sectionName);
				if (currentSection == null)
					_sections.put(sectionName, 
						currentSection = new Properties(_default));
			} else
			{
				matcher = PROPERTY_PATTERN.matcher(line);
				if (matcher.matches())
				{
					currentSection.put(matcher.group(1).trim(), 
						matcher.group(2).trim());
				} else if (!line.trim().isEmpty())
					throw new IOException("Unable to parse line " + lineNo);
			}
		}
	}
	
	public INIFile(BufferedReader reader) throws IOException
	{
		initialize(reader);
	}
	
	public INIFile(Reader reader) throws IOException
	{
		this(new BufferedReader(reader));
	}
	
	public INIFile(InputStream in) throws IOException
	{
		this(new InputStreamReader(in));
	}
	
	public INIFile(File iniFile) throws IOException
	{
		Reader in = null;
		
		try
		{
			in = new FileReader(iniFile);
			initialize(new BufferedReader(in));
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	public INIFile(String iniFileName) throws IOException
	{
		this(new File(iniFileName));
	}
	
	static private void append(String tabs, Properties properties, 
		StringBuilder builder)
	{
		for (Object key : properties.keySet())
		{
			builder.append(String.format("%s%s=%s\n", tabs, key, 
				properties.get(key)));
		}
	}
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
	
		append("", _default, builder);
		
		for (String section : _sections.keySet())
		{
			builder.append(String.format("\n[%s]\n", section));
			append("\t", _sections.get(section), builder);
		}
		
		return builder.toString();
	}
	
	public Collection<String> sections()
	{
		return _sections.keySet();
	}
	
	public Properties section(String sectionName)
	{
		return _sections.get(sectionName);
	}
	
	public String property(String sectionName, String propertyName)
	{
		Properties props = section(sectionName);
		if (props != null)
			return props.getProperty(propertyName);
		
		return null;
	}
	
	static public void main(String []args) throws Throwable
	{
		InputStream in = null;
		
		try
		{
			in = INIFile.class.getResourceAsStream("test.ini");
			INIFile ini = new INIFile(in);
			System.err.println(ini);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
}