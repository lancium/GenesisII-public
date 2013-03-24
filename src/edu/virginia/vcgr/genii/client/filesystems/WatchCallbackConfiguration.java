package edu.virginia.vcgr.genii.client.filesystems;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;

import org.w3c.dom.Element;

class WatchCallbackConfiguration
{
	@XmlAttribute(name = "call-limit", required = true)
	private String _callLimit = null;

	@XmlAttribute(name = "register-anti-callback", required = false)
	private boolean _registerAntiCallback = false;

	@XmlAttribute(name = "class", required = true)
	private String _handlerClassName = null;

	@XmlAnyElement
	private Collection<Element> _any = null;

	/**
	 * Returns the number of calls that can be made before the filter becomes false again.
	 * 
	 * @return The number of calls (or null if it's infinite)
	 */
	final Integer callLimit()
	{
		if (_callLimit.equals("unbounded"))
			return null;

		return Integer.valueOf(_callLimit);
	}

	final boolean registerAntiCallback()
	{
		return _registerAntiCallback;
	}

	@SuppressWarnings("unchecked")
	final Class<? extends FilesystemWatchHandler> handlerClass() throws ClassNotFoundException
	{
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Class<?> cl = loader.loadClass(_handlerClassName);
		return (Class<? extends FilesystemWatchHandler>) cl;
	}

	final Collection<Element> configurationContent()
	{
		return _any;
	}
}