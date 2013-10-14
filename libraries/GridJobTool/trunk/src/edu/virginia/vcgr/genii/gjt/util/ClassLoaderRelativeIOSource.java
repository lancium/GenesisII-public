package edu.virginia.vcgr.genii.gjt.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ClassLoaderRelativeIOSource implements IOSource
{
	private ClassLoader _loader;
	private String _resourceName;

	public ClassLoaderRelativeIOSource(ClassLoader loader, String resourceName)
	{
		_loader = loader;
		_resourceName = resourceName;

		if (_loader == null)
			_loader = Thread.currentThread().getContextClassLoader();
	}

	public ClassLoaderRelativeIOSource(String resourceName)
	{
		this(null, resourceName);
	}

	@Override
	public InputStream open() throws IOException
	{
		InputStream in = _loader.getResourceAsStream(_resourceName);
		if (in == null)
			throw new FileNotFoundException(String.format("Unable to load resource \"%s\" from class loader.", _resourceName));

		return in;
	}
}