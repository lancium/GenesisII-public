package edu.virginia.vcgr.genii.client.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.morgan.util.io.StreamUtils;

public class FileResource
{
	private String _resourcePath;
	
	public FileResource(String resourcePath)
	{
		_resourcePath = resourcePath;
	}
	
	public InputStream open()
	{
		return open(Thread.currentThread().getContextClassLoader());
	}
	
	public InputStream open(ClassLoader loader)
	{
		return loader.getResourceAsStream(_resourcePath);
	}
	
	public String toString()
	{
		StringWriter writer = new StringWriter();
		InputStream in = null;
		char []buffer = new char[1024 * 4];
		
		try
		{
			in = open();
			if (in != null)
			{
				InputStreamReader reader = new InputStreamReader(in);
				int read;
				
				while ( (read = reader.read(buffer)) > 0 )
					writer.write(buffer, 0, read);
				
				writer.close();
			}
		}
		catch (IOException ioe)
		{
			throw new RuntimeException("Unable to read resource.", ioe);
		}
		finally
		{
			StreamUtils.close(in);
		}
		
		return writer.toString();
	}
}