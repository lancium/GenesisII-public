package edu.virginia.vcgr.genii.client.gui.browser.plugins;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Element;

public class Instantiater
{
	static public IPlugin instantiate(Class<IPlugin> cl,
		Element constructionParams)
			throws PluginException
	{
		try
		{
			try
			{
				Constructor<IPlugin> cons = cl.getConstructor(
					new Class<?>[] { Element.class });
				return cons.newInstance(new Object[] { constructionParams } );
			}
			catch (NoSuchMethodException nsme)
			{
				if (constructionParams != null && 
					constructionParams.hasChildNodes())
					throw nsme;
				
				Constructor<IPlugin> cons = cl.getConstructor(
					new Class<?>[0]);
				return cons.newInstance(new Object[0]);
			}
		}
		catch (InstantiationException ia)
		{
			throw new PluginException(
				"Unable to instantiate plugin class \"" + cl.getName()
				+ "\".", ia);
		}
		catch (IllegalAccessException iae)
		{
			throw new PluginException(
				"Constructor for plugin class \"" + cl.getName()
				+ "\" is not public.", iae);
		}
		catch (NoSuchMethodException nsme)
		{
			throw new PluginException(
				"Unable to find suitable constructor for plugin class \"" 
				+ cl.getName() + "\".", nsme);
		}
		catch (InvocationTargetException ite)
		{
			throw new PluginException(
				"Constructor for plugin class \"" + cl.getName()
				+ "\" threw an exception.", ite.getCause());
		}
	}
	
	@SuppressWarnings("unchecked")
	static public IPlugin instantiate(String className,
		Element constructionParams) throws PluginException
	{
		try
		{
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Class<IPlugin> cl = (Class<IPlugin>)loader.loadClass(className);
		
			return instantiate(cl, constructionParams);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new PluginException("Unable to find plugin class \"" 
				+ className + "\".", cnfe);
		}
	}
}