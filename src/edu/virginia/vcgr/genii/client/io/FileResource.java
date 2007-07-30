package edu.virginia.vcgr.genii.client.io;

import java.io.InputStream;

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
		return _resourcePath;
	}
}