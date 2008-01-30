package edu.virginia.vcgr.genii.client.gui.browser.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

/**
 * The PluginManager is the central point from which plugins are obtained.  It
 * is responsible for parsing configuration files and organizing plugins into
 * convenient structures for use by the GUI browser.
 * 
 * @author mmm2a
 */
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
	
	/**
	 * Construct a new plugin manager with already parsed data.
	 * 
	 * @param mainMenuPlugins The Main Menu (or Top Menu) plugins.
	 * @param contextMenuPlugins The context menu or right-click pop-up 
	 * plugins.
	 * @param tabs The Tab plugins.
	 */
	private PluginManager(
		HashMap<String, HashMap<String, Collection<MainMenuDescriptor>>> mainMenuPlugins,
		HashMap<String, Collection<ContextMenuDescriptor>> contextMenuPlugins,
		TreeSet<TabPluginDescriptor> tabs)
	{
		_mainMenuPlugins = mainMenuPlugins;
		_contextMenuPlugins = contextMenuPlugins;
		_tabs = tabs;
	}
	
	/**
	 * Retrieve the main menu plugins registered with this plugin manager.
	 * 
	 * @return The main menu plugins registered with this plugin.  This is
	 * a HashMap which maps the menu name to another HashMap which maps
	 * the group name to the menu item plugin description.
	 */
	public HashMap<String, HashMap<String, Collection<MainMenuDescriptor>>>
		getMainMenuPlugins()
	{
		return _mainMenuPlugins;
	}
	
	/**
	 * Retrieve the context menu plugins registered with this plugin manager.
	 * 
	 * @return The context menu plugins registered with this plugin.  This is
	 * a HashMap which maps the group names to the context menu plugin 
	 * descriptions.
	 */
	public HashMap<String, Collection<ContextMenuDescriptor>>
		getContextMenuPlugins()
	{
		return _contextMenuPlugins;
	}
	
	/**
	 * Retrieve the set of registered tab plugins.
	 * 
	 * @return The set of registered tab plugins, already
	 * sorted by priority.
	 */
	public TreeSet<TabPluginDescriptor> getTabs()
	{
		return _tabs;
	}
	
	/**
	 * An internal method for parsing a plugin descriptor XML element into
	 * the correct data structures.
	 * 
	 * @param pluginElement The XML element describing the plugin.
	 * @param mainMenuPlugins The target map of main menu plugins.
	 * @param contextMenuPlugins The target map of context menu plugins.
	 * @param tabs The target collection of tab plugins.
	 * 
	 * @throws PluginException
	 * @throws SAXException
	 */
	static private void parsePlugin(Element pluginElement,
		HashMap<String, HashMap<String, Collection<MainMenuDescriptor>>> mainMenuPlugins,
		HashMap<String, Collection<ContextMenuDescriptor>> contextMenuPlugins,
		TreeSet<TabPluginDescriptor> tabs) throws PluginException, SAXException
	{
		/* First, get attributes from the element that describe the plugin */
		String name = XMLUtils.getRequiredAttribute(
			pluginElement, PLUGIN_NAME_ATTR, null);
		String implementingClass = XMLUtils.getRequiredAttribute(
			pluginElement, IMPLEMENTING_CLASS_ATTR, null);
		IPlugin plugin = null;
		
		/* Iterate through the child XML elements of this one, looking
		 * for configuration information about the plugin.
		 */
		for (Element child : new NodeIterable(pluginElement))
		{
			String nodeName = child.getNodeName();
			
			/* If it is the constructor params element, then we are
			 * parsing out an XML element that will be passed into the
			 * target plugin classes constructor.
			 */
			if (nodeName.equals(CONSTRUCTOR_PARAMS_ELEMENT))
			{
				/* If the plugin has already been instantiated, then it's too
				 * late to give constructor params.
				 */
				if (plugin != null)
					throw new PluginException(
						"Saw a \"" + nodeName 
						+ "\" element after the plugin was configured.");
				
				/* Otherwise, we go ahead and use the construction params to
				 * instantiate the new plugin instance.
				 */
				plugin = Instantiater.instantiate(implementingClass, child);
			} else
			{
				/* We don't have a plugin, and it's not a constructor params
				 * element which means that the configuration describes a
				 * plugin that takes not construction parameters.  Go ahead
				 * and instantiate the plugin with no construction params.
				 */
				if (plugin == null)
					plugin = Instantiater.instantiate(implementingClass, null);
				
				/* Now, if the element identifies a main menu plugin, then set that
				 * up.
				 */
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
					
					/* Let's see if the menu already exists (i.e., another
					 * plugin is also in the same menu).
					 */
					HashMap<String, Collection<MainMenuDescriptor>> menu
						= mainMenuPlugins.get(descriptor.getMenuName());
					if (menu == null)
					{
						/* If it doesn't exist yet, create it */
						menu = new LinkedHashMap<String, Collection<MainMenuDescriptor>>();
						mainMenuPlugins.put(descriptor.getMenuName(), menu);
					}
					
					/* Now, let's see if the group exists yet.  Again, if it doesn't, we'll
					 * create it.
					 */
					Collection<MainMenuDescriptor> group = menu.get(descriptor.getMenuGroup());
					if (group == null)
					{
						group = new ArrayList<MainMenuDescriptor>();
						menu.put(descriptor.getMenuGroup(), group);
					}
					
					/* Finally, add the descriptor to the group */
					group.add(descriptor);
				} else if (nodeName.equals(CONTEXT_MENU_ELEMENT))
				{
					/* Now we'll take care of a context menu element. */
					if (!(plugin instanceof IMenuPlugin))
						throw new PluginException("Plugin \"" + name 
							+ "\" does not appear to implement the IMenuPlugin interface.");

					ContextMenuDescriptor descriptor = new ContextMenuDescriptor(
						name, (IMenuPlugin)plugin,
						XMLUtils.getRequiredAttribute(child, MENU_LABEL_ATTR, null),
						XMLUtils.getRequiredAttribute(child, MENU_GROUP_ATTR, null));
					
					/* See if another plugin already registered the group */
					Collection<ContextMenuDescriptor> group = 
						contextMenuPlugins.get(descriptor.getMenuGroup());
					if (group == null)
					{
						/* If not, create the group */
						group = new ArrayList<ContextMenuDescriptor>();
						contextMenuPlugins.put(descriptor.getMenuGroup(), group);
					}
					
					/* Add the context menu descriptor to the group */
					group.add(descriptor);
				} else if (nodeName.equals(TAB_ELEMENT))
				{
					/* Finally, if it's a tab plugin...*/
					if (!(plugin instanceof ITabPlugin))
						throw new PluginException("Plugin \"" + name 
							+ "\" does not appear to implement the ITabPlugin interface.");

					/* Just go ahead and add the tab plugin descriptor to the collection */
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
	
	/**
	 * This is an internal method which searches through the configuration 
	 * document looking for plugin descriptions to configure.
	 * 
	 * @param configuration The root XML document to look for plugins inside
	 * of.
	 * @return A newly created PluginManager with the configured plugins
	 * included.
	 * 
	 * @throws PluginException
	 */
	static private PluginManager loadPlugins(Element configuration)
		throws PluginException
	{
		HashMap<String, HashMap<String, Collection<MainMenuDescriptor>>> mainMenuPlugins
			= new LinkedHashMap<String, HashMap<String,Collection<MainMenuDescriptor>>>();
		HashMap<String, Collection<ContextMenuDescriptor>> contextMenuPlugins
			= new LinkedHashMap<String, Collection<ContextMenuDescriptor>>();
		TreeSet<TabPluginDescriptor> tabs = new TreeSet<TabPluginDescriptor>(
			TabPluginDescriptor.getPriorityComparator());
	
		/* Iterate through the child elements of this document */
		for (Element child : new NodeIterable(configuration))
		{
			String name = child.getNodeName();
			if (name.equals(PLUGIN_ELEMENT))
			{
				/* If it's a plugin element, configure that plugin */
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
				/* Otherwise, it's an error -- plugins is all the document 
				 * should have. 
				 */
				throw new PluginException("Unexpected element \""
					+ name + "\" in configuration while looking for \""
					+ PLUGIN_ELEMENT + "\".");
		}
		
		return new PluginManager(
			mainMenuPlugins, contextMenuPlugins, tabs);
	}
	
	/**
	 * Configure a new plugin manager based off of a input stream (assumed to 
	 * have a valid XML configuration file embedded.
	 * 
	 * @param configuration The configuration stream.
	 * @return A newly create plugin manager configured from the stream.
	 * 
	 * @throws PluginException
	 */
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
	
	/**
	 * Configure a new plugin manager based off of an input file (assumed to 
	 * have a valid XML configuration embedded).
	 * 
	 * @param configurationFile The configuration file.
	 * @return A newly create plugin manager configured from the file.
	 * 
	 * @throws PluginException
	 */
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
	
	/**
	 * Configure a new plugin manager based off of an input resource (assumed to 
	 * be a valid XML configuration resource).
	 * 
	 * @param configurationResource The configuration resource path.
	 * @return A newly create plugin manager configured from the resource.
	 * 
	 * @throws PluginException
	 */
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