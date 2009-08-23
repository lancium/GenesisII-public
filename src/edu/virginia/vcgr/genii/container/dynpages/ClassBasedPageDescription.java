package edu.virginia.vcgr.genii.container.dynpages;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClassBasedPageDescription implements PageDescription
{
	static private Log _logger = LogFactory.getLog(
		ClassBasedPageDescription.class);
	
	private Class<? extends DynamicPage> _pageClass;
	
	@SuppressWarnings("unchecked")
	public ClassBasedPageDescription(ClassLoader loader,
		String className) throws ClassNotFoundException
	{
		_pageClass = (Class<? extends DynamicPage>)loader.loadClass(
			className);
	}
	
	@Override
	public DynamicPage loadPage() throws IOException
	{
		try
		{
			return _pageClass.newInstance();
		} 
		catch (InstantiationException e)
		{
			_logger.warn("Unable to load page class.", e);
			throw new IOException("Unable to load dynamic page.", e);
		}
		catch (IllegalAccessException e)
		{
			_logger.warn("Unable to load page class.", e);
			throw new IOException("Unable to load dynamic page.", e);
		}
	}
}