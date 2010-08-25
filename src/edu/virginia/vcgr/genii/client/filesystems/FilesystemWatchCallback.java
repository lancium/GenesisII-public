package edu.virginia.vcgr.genii.client.filesystems;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

class FilesystemWatchCallback
{
	static private Log _logger = LogFactory.getLog(
		FilesystemWatchCallback.class);
	
	private boolean _firstTime = true;
	private boolean _lastMatched = false;
	private Integer _callLimit;
	private boolean _registerAntiCallback;
	private int _callsMade = 0;
	private FilesystemWatchHandler _handler;
	
	FilesystemWatchCallback(Integer callLimit,
		FilesystemWatchHandler handler)
	{
		_callLimit = callLimit;
		_handler = handler;
	}
	
	FilesystemWatchCallback(Integer callLimit, boolean registerAntiCallback, 
		Class<? extends FilesystemWatchHandler> handlerClass, 
		Collection<Element> configurationData) throws IOException
	{
		_registerAntiCallback = registerAntiCallback;
		_callLimit = callLimit;
		Constructor<? extends FilesystemWatchHandler> cons;
		
		if (configurationData == null)
			configurationData = new ArrayList<Element>(0);
		
		try
		{
			try
			{
				cons = handlerClass.getConstructor(Element[].class);
				Element []configArray = new Element[configurationData.size()];
				configurationData.toArray(configArray);
				_handler = cons.newInstance(new Object[] {configArray});
			}
			catch (NoSuchMethodException nsme1)
			{
				try
				{
					cons = handlerClass.getConstructor(Element.class);
					if (configurationData.size() == 0)
						_handler = cons.newInstance(new Object[]{null});
					else
						_handler = cons.newInstance(
							configurationData.iterator().next());
				}
				catch (NoSuchMethodException nsme2)
				{
					try
					{
						cons = handlerClass.getConstructor();
						_handler = cons.newInstance();
					}
					catch (NoSuchMethodException e)
					{
						throw new IOException(String.format(
							"Couldn't find suitable constructor" +
							" for handler class \"%s\".", handlerClass));
					}
				}
			}
		}
		catch (InvocationTargetException ite)
		{
			throw new IOException(String.format(
				"Unable to instantiate handler for class \"%s\".",
				handlerClass), ite.getCause());
		}
		catch (IllegalAccessException iae)
		{
			throw new IOException(String.format(
				"Unable to instantiate handler for class \"%s\".",
				handlerClass), iae);
		}
		catch (InstantiationException ie)
		{
			throw new IOException(String.format(
				"Unable to instantiate handler for class \"%s\".",
				handlerClass), ie);
		}
	}
	
	final void resetCallCount()
	{
		_callsMade = 0;
	}
	
	final void performCallback(FilesystemManager manager,
		String filesystemName, Filesystem filesystem,
		FilesystemUsageInformation usageInformation,
		boolean matched)
	{
		if (_lastMatched != matched)
			_callsMade = 0;
		
		_lastMatched = matched;
		
		if ((matched || (_registerAntiCallback && _callsMade < 1)) &&
			(_callLimit == null || _callsMade < _callLimit))
		{
			try
			{
				if (!_firstTime || matched)
					_handler.notifyFilesystemEvent(
						manager, filesystemName, filesystem, usageInformation,
						matched);
			}
			catch (Throwable throwable)
			{
				_logger.error(String.format(
					"Error thrown by file system watcher on filesystem %s.",
					filesystemName), throwable);
			}
			finally
			{
				_firstTime = false;
			}
		}
		
		_callsMade++;
	}
}