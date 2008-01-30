package edu.virginia.vcgr.genii.client.gui.browser.plugins;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Element;

/**
 * The Instantiater is a helper class that the RNS Browser uses to instantiate
 * classes identified by class name.
 * 
 * @author mmm2a
 */
public class Instantiater
{
	/**
	 * Given a class and possible construction parameters, try and 
	 * instantiate it into a plugin.  In order to instantiate a
	 * plugin, the class must have a public constructor that takes an Element as
	 * a parameter (which will be the <construction-params/> element given with
	 * the plugin in the configuration files, or it must have a default empty
	 * constructor and NO construction-params given in the config file.
	 * 
	 * @param cl The plugin class to instantiate.
	 * @param constructionParams Any construction parameters given in the config
	 * file.
	 * @return The instantiated plugin.
	 * 
	 * @throws PluginException
	 */
	static public IPlugin instantiate(Class<IPlugin> cl,
		Element constructionParams)
			throws PluginException
	{
		try
		{
			try
			{
				/* First, see if there is a constructor that takes an Element
				 * instance as it's only constructor parameter.  This is the
				 * preferred way to instantiate plug-ins.
				 */
				Constructor<IPlugin> cons = cl.getConstructor(
					new Class<?>[] { Element.class });
				
				/* If such a constructor exists, go ahead and use it to create
				 * this plugin.
				 */
				return cons.newInstance(new Object[] { constructionParams } );
			}
			catch (NoSuchMethodException nsme)
			{
				/*
				 * If we couldn't find the init(Element) constructor, see if it
				 * is permissible to use a default constructor (it's permissible
				 * if the config file didn't indicate any construction parameters).
				 */
				if (constructionParams != null && 
					constructionParams.hasChildNodes())
					throw nsme;
				
				/*
				 * It seems to be permissible to create the plugin with a 
				 * default constructor, so let's see if one exists and go
				 * ahead and use it if it does.
				 */
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
	
	/**
	 * Given a classname and possible construction parameters, try and find the
	 * class and instantiate it into a plugin.  In order to instantiate a
	 * plugin, the class must have a public constructor that takes an Element as
	 * a parameter (which will be the <construction-params/> element given with
	 * the plugin in the configuration files, or it must have a default empty
	 * constructor and NO construction-params given in the config file.
	 * 
	 * @param className The full class name to instantiate.
	 * @param constructionParams Any construction parameters given in the config
	 * file.
	 * @return The instantiated plugin.
	 * 
	 * @throws PluginException
	 */
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