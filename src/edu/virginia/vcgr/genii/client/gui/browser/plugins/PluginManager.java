package edu.virginia.vcgr.genii.client.gui.browser.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.morgan.util.io.StreamUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.utils.xml.NodeIterable;
import edu.virginia.vcgr.genii.client.utils.xml.XMLUtils;

public class PluginManager
{
	static public final String PLUGIN_ELEMENT = "plugin";
	static public final String PLUGIN_NAME_ATTR = "name";
	static public final String IMPLEMENTING_CLASS_ATTR = "implementing-class";
	static public final String CONSTRUCTOR_PARAMS_ELEMENT = "constructor-params";
	static public final String MAIN_MENU_ELEMENT = "main-menu";
	static public final String CONTEXT_MENU_ELEMENT = "context-menu";
	static public final String TAB_ELEMENT = "tab";
	
	static public final String MENU_NAME_ATTR = "menu-name";
	static public final String MENU_LABEL_ATTR = "menu-label";
	static public final String MENU_GROUP_ATTR = "menu-group";
	static public final String TAB_NAME_ATTR = "tab-name";
	static public final String TAB_PRIORITY_ATTR = "tab-priority";
	
	private HashMap<String, HashMap<String, Collection<MainMenuDescriptor>>> _mainMenuPlugins;
	private HashMap<String, Collection<ContextMenuDescriptor>> _contextMenuPlugins;
	private TreeSet<TabPluginDescriptor> _tabs;
	
	private PluginManager(
		HashMap<String, HashMap<String, Collection<MainMenuDescriptor>>> mainMenuPlugins,
		HashMap<String, Collection<ContextMenuDescriptor>> contextMenuPlugins,
		TreeSet<TabPluginDescriptor> tabs)
	{
		_mainMenuPlugins = mainMenuPlugins;
		_contextMenuPlugins = contextMenuPlugins;
		_tabs = tabs;
	}
	
	public HashMap<String, HashMap<String, Collection<MainMenuDescriptor>>>
		getMainMenuPlugins()
	{
		return _mainMenuPlugins;
	}
	
	public HashMap<String, Collection<ContextMenuDescriptor>>
		getContextMenuPlugins()
	{
		return _contextMenuPlugins;
	}
	
	public TreeSet<TabPluginDescriptor> getTabs()
	{
		return _tabs;
	}
	
	static private void parsePlugin(Element pluginElement,
		HashMap<String, HashMap<String, Collection<MainMenuDescriptor>>> mainMenuPlugins,
		HashMap<String, Collection<ContextMenuDescriptor>> contextMenuPlugins,
		TreeSet<TabPluginDescriptor> tabs) throws PluginException, SAXException
	{
		String name = XMLUtils.getRequiredAttribute(
			pluginElement, PLUGIN_NAME_ATTR, null);
		String implementingClass = XMLUtils.getRequiredAttribute(
			pluginElement, IMPLEMENTING_CLASS_ATTR, null);
		IPlugin plugin = null;
		
		for (Element child : new NodeIterable(pluginElement))
		{
			String nodeName = child.getNodeName();
			if (nodeName.equals(CONSTRUCTOR_PARAMS_ELEMENT))
			{
				if (plugin != null)
					throw new PluginException(
						"Saw a \"" + nodeName 
						+ "\" element after the plugin was configured.");
				
				plugin = Instantiater.instantiate(implementingClass, child);
			} else
			{
				if (plugin == null)
					plugin = Instantiater.instantiate(implementingClass, null);
				
				if (nodeName.equals(MAIN_MENU_ELEMENT))
				{
					if (!(plugin instanceof IMenuPlugin))
						throw new PluginException("Plugin \"" + name 
							+ "\" does not appear to implement the IMenuPlugin interface.");

					MainMenuDescriptor descriptor = new MainMenuDescriptor(
						name, (IMenuPlugin)plugin,
						XMLUtils.getRequiredAttribute(child, MENU_NAME_ATTR, null),
						XMLUtils.getRequiredAttribute(child, MENU_LABEL_ATTR, null),
						XMLUtils.getRequiredAttribute(child, MENU_GROUP_ATTR, null));
					HashMap<String, Collection<MainMenuDescriptor>> menu
						= mainMenuPlugins.get(descriptor.getMenuName());
					if (menu == null)
					{
						menu = new HashMap<String, Collection<MainMenuDescriptor>>();
						mainMenuPlugins.put(descriptor.getMenuName(), menu);
					}
					
					Collection<MainMenuDescriptor> group = menu.get(descriptor.getMenuGroup());
					if (group == null)
					{
						group = new ArrayList<MainMenuDescriptor>();
						menu.put(descriptor.getMenuGroup(), group);
					}
					
					group.add(descriptor);
				} else if (nodeName.equals(CONTEXT_MENU_ELEMENT))
				{
					if (!(plugin instanceof IMenuPlugin))
						throw new PluginException("Plugin \"" + name 
							+ "\" does not appear to implement the IMenuPlugin interface.");

					ContextMenuDescriptor descriptor = new ContextMenuDescriptor(
						name, (IMenuPlugin)plugin,
						XMLUtils.getRequiredAttribute(child, MENU_LABEL_ATTR, null),
						XMLUtils.getRequiredAttribute(child, MENU_GROUP_ATTR, null));
					Collection<ContextMenuDescriptor> group = 
						contextMenuPlugins.get(descriptor.getMenuGroup());
					if (group == null)
					{
						group = new ArrayList<ContextMenuDescriptor>();
						contextMenuPlugins.put(descriptor.getMenuGroup(), group);
					}
					
					group.add(descriptor);
				} else if (nodeName.equals(TAB_ELEMENT))
				{
					if (!(plugin instanceof ITabPlugin))
						throw new PluginException("Plugin \"" + name 
							+ "\" does not appear to implement the ITabPlugin interface.");

					tabs.add(new TabPluginDescriptor(
						name, (ITabPlugin)plugin,
						XMLUtils.getRequiredAttribute(child, TAB_NAME_ATTR, null),
						Integer.parseInt(XMLUtils.getRequiredAttribute(child, TAB_PRIORITY_ATTR, "0"))));
				} else
					throw new PluginException("Unrecognized element \""
						+ nodeName + " in plugin configuration.");
			}
		}
	}
	
	static private PluginManager loadPlugins(Element configuration)
		throws PluginException
	{
		HashMap<String, HashMap<String, Collection<MainMenuDescriptor>>> mainMenuPlugins
			= new HashMap<String, HashMap<String,Collection<MainMenuDescriptor>>>();
		HashMap<String, Collection<ContextMenuDescriptor>> contextMenuPlugins
			= new HashMap<String, Collection<ContextMenuDescriptor>>();
		TreeSet<TabPluginDescriptor> tabs = new TreeSet<TabPluginDescriptor>(
			TabPluginDescriptor.getPriorityComparator());
		
		for (Element child : new NodeIterable(configuration))
		{
			String name = child.getNodeName();
			if (name.equals(PLUGIN_ELEMENT))
			{
				try
				{
					parsePlugin(child,
						mainMenuPlugins, contextMenuPlugins, tabs);
				}
				catch (SAXException se)
				{
					throw new PluginException("Unable to parse plugin.", se);
				}
			} else
				throw new PluginException("Unexpected element \""
					+ name + "\" in configuration while looking for \""
					+ PLUGIN_ELEMENT + "\".");
		}
		
		return new PluginManager(
			mainMenuPlugins, contextMenuPlugins, tabs);
	}
	
	static public PluginManager loadPlugins(InputStream configuration)
		throws PluginException
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			factory.setNamespaceAware(false);
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(configuration);
			return loadPlugins(doc.getDocumentElement());
		}
		catch (ParserConfigurationException pce)
		{
			throw new PluginException("Unable to parse configuration.", pce);
		}
		catch (SAXException se)
		{
			throw new PluginException("Unable to parse configuration.", se);
		}
		catch (IOException ioe)
		{
			throw new PluginException("Unable to read configuration.", ioe);
		}
	}
	
	static public PluginManager loadPlugins(File configurationFile)
		throws PluginException
	{
		InputStream in = null;
		
		try
		{
			in = new FileInputStream(configurationFile);
			return loadPlugins(in);
		}
		catch (FileNotFoundException fnfe)
		{
			throw new PluginException("Unable to locate configuration file \""
				+ configurationFile.getAbsolutePath() + "\".", fnfe);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	static public PluginManager loadPlugins(FileResource configurationResource)
		throws PluginException
	{
		InputStream in = null;
		
		try
		{
			in = configurationResource.open();
			return loadPlugins(in);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
}