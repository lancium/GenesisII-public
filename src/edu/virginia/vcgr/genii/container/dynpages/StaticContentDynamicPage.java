package edu.virginia.vcgr.genii.container.dynpages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.morgan.util.io.StreamUtils;

class StaticContentDynamicPage implements DynamicPage
{
	private ClassLoader _loader;
	private String _resourcePath;
	
	public StaticContentDynamicPage(ClassLoader loader,
		String resourcePath)
	{
		_loader = loader;
		_resourcePath = resourcePath;
	}
	
	@Override
	public void generate(PrintStream ps) throws IOException
	{
		ClassLoader loader = _loader;
		if (loader == null)
			loader = Thread.currentThread().getContextClassLoader();
		
		InputStream in = null;
		
		try
		{
			in = loader.getResourceAsStream(_resourcePath);
			if (in == null)
				throw new FileNotFoundException(String.format(
					"Unable to find resource \"%s\".", _resourcePath));
			
			StreamUtils.copyStream(in, ps);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
}