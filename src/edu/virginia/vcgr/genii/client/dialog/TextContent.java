package edu.virginia.vcgr.genii.client.dialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.morgan.util.io.StreamUtils;

public class TextContent
{
	private Collection<String> _content;
	
	public TextContent(Collection<String> content)
	{
		_content = new ArrayList<String>(content);
	}
	
	public TextContent(String...content)
	{
		_content = new ArrayList<String>(content.length);
		for (String s : content)
		{
			_content.add(s);
		}
	}
	
	public TextContent(edu.virginia.vcgr.genii.client.io.FileResource resource)
	{
		String line;
		InputStream in = null;
		
		_content = new LinkedList<String>();
		
		try
		{
			in = resource.open();
			if (in == null)
				throw new IllegalArgumentException(
					"Unable to open indicated file resource \"" + 
					resource + "\".");
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(in));
			while ( (line = reader.readLine()) != null)
			{
				_content.add(line);
			}
		}
		catch (IOException ioe)
		{
			throw new IllegalArgumentException(
				"Can't read file resource.", ioe);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	public Collection<String> getContent()
	{
		return _content;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		
		for (String s : _content)
		{
			if (first)
				first = false;
			else
				builder.append('\n');
			
			builder.append(s);
		}
		
		return builder.toString();
	}
}