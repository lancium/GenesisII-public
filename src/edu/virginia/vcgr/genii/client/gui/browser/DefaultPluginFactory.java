package edu.virginia.vcgr.genii.client.gui.browser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DefaultPluginFactory implements IMainMenuPluginFactory,
		IContextMenuPluginFactory, ITabPluginFactory
{
	static public final String MAIN_MENU_CLASS_CONF_ELEMENT = "main-menu-class";
	static public final String CONTEXT_MENU_CLASS_CONF_ELEMENT = "context-menu-class";
	static public final String TAB_CLASS_CONF_ELEMENT = "tab-class";
	
	static private final int _MAIN_MENU_INDEX = 0;
	static private final int _CONTEXT_MENU_INDEX = 1;
	static private final int _TAB_INDEX = 2;
	
	private Constructor<? extends IMainMenuPlugin> _mainMenuCons;
	private Constructor<? extends IContextMenuPlugin> _contextMenuCons;
	private Constructor<? extends ITabPlugin> _tabCons;
	
	static private <Type> Constructor<Type> findEmptyConstructor(Class<Type> cl)
		throws PluginException
	{
		if (cl == null)
			return null;
		
		try
		{
			return cl.getConstructor(new Class<?>[0]);
		}
		catch (NoSuchMethodException nsme)
		{
			throw new PluginException("Unable to find empty constructor for class \"" +
				cl.getName() + "\".", nsme);
		}
	}
	
	@SuppressWarnings("unchecked")
	static private <Type> Class<Type> findClass(Class<Type> interfaceClass, String className)
		throws PluginException
	{
		if (className == null)
			return null;
		
		try
		{
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			return (Class<Type>)loader.loadClass(className);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new PluginException("Unable to load class \"" + className + "\".", cnfe);
		}
	}
	
	static private String[] parseElement(Element configurationElement)
		throws PluginException
	{
		String []ret = new String[] { null, null, null };
		
		NodeList children = configurationElement.getChildNodes();
		for (int lcv = 0; lcv < children.getLength(); lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				String name = child.getNodeName();
				if (name.equals(MAIN_MENU_CLASS_CONF_ELEMENT))
					ret[_MAIN_MENU_INDEX] = child.getTextContent();
				else if (name.equals(CONTEXT_MENU_CLASS_CONF_ELEMENT))
					ret[_CONTEXT_MENU_INDEX] = child.getTextContent();
				else if (name.equals(TAB_CLASS_CONF_ELEMENT))
					ret[_TAB_INDEX] = child.getTextContent();
				else
					throw new PluginException("Don't know how to parse element \"" + name + "\".");
			}
		}
		
		return ret;
	}
	
	static private <Type> Type createFromConstructor(Constructor<Type> cons)
		throws PluginException
	{
		if (cons == null)
			return null;
		
		try
		{
			return cons.newInstance(new Object[0]);
		}
		catch (InstantiationException ie)
		{
			throw new PluginException("Unable to call default constructor on class \"" +
				cons.getDeclaringClass().getName() + "\".", ie);
		}
		catch (IllegalAccessException iae)
		{
			throw new PluginException("Unable to call default constructor on class \"" +
				cons.getDeclaringClass().getName() + "\".", iae);
		}
		catch (InvocationTargetException ite)
		{
			Throwable cause = ite.getCause();
			if (cause instanceof PluginException)
				throw (PluginException)cause;
			else if (cause instanceof RuntimeException)
				throw (RuntimeException)cause;
			else
				throw new PluginException("Exception thrown while calling default constructor on class \"" +
					cons.getDeclaringClass().getName() + "\".", cause);
		}
	}
	
	public DefaultPluginFactory(Class<? extends IMainMenuPlugin> mainMenuClass,
		Class<? extends IContextMenuPlugin> contextMenuClass,
		Class<? extends ITabPlugin> tabClass)
			throws PluginException
	{
		_mainMenuCons = findEmptyConstructor(mainMenuClass);
		_contextMenuCons = findEmptyConstructor(contextMenuClass);
		_tabCons = findEmptyConstructor(tabClass);
	}
	
	private DefaultPluginFactory(String []classes)
		throws PluginException
	{
		this(findClass(IMainMenuPlugin.class, classes[_MAIN_MENU_INDEX]),
			findClass(IContextMenuPlugin.class, classes[_CONTEXT_MENU_INDEX]),
			findClass(ITabPlugin.class, classes[_TAB_INDEX]));
	}
	
	public DefaultPluginFactory(Element configurationElement)
		throws PluginException
	{
		this(parseElement(configurationElement));
	}
	
	@Override
	public IMainMenuPlugin createMainMenuPlugin() throws PluginException
	{
		return createFromConstructor(_mainMenuCons);
	}

	@Override
	public IContextMenuPlugin createContextMenuPlugin() throws PluginException
	{
		return createFromConstructor(_contextMenuCons);
	}

	@Override
	public ITabPlugin createTabPlugin() throws PluginException
	{
		return createFromConstructor(_tabCons);
	}
}