package edu.virginia.vcgr.genii.container.dynpages;

import java.io.IOException;

public class StaticContentPageDescription implements PageDescription
{
	private ClassLoader _loader;
	private String _resourcePath;
	
	public StaticContentPageDescription(ClassLoader loader, 
		String resourcePath)
	{
		_loader = loader;
		_resourcePath = resourcePath;
	}
	
	@Override
	public DynamicPage loadPage() throws IOException
	{
		return new StaticContentDynamicPage(_loader, _resourcePath);
	}
}
