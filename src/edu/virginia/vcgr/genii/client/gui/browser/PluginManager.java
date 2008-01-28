package edu.virginia.vcgr.genii.client.gui.browser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.morgan.util.io.StreamUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.utils.xml.NodeIterable;

public class PluginManager
{
	static public final String PLUGIN_FACTORIES_ELEMENT = "plugin-factories";
	static public final String PLUGIN_FACTORY_ELEMENT = "plugin-factory";
	static public final String PLUGIN_ELEMENT = "plugin";
	
	
	
	private HashMap<String, PluginMenu> _mainMenuBar;
	private PluginMenu _contextMenu;
	private TreeSet<ITabPlugin> _tabs;
	
	private PluginManager(
		HashMap<String, PluginMenu> mainMenuBar,
		PluginMenu contextMenu,
		TreeSet<ITabPlugin> tabs)
	{
		_mainMenuBar = mainMenuBar;
		_contextMenu = contextMenu;
		_tabs = tabs;
	}
	
	public HashMap<String, PluginMenu> getMainMenuBar()
	{
		return _mainMenuBar;
	}
	
	public PluginMenu getContextMenu()
	{
		return _contextMenu;
	}
	
	public TreeSet<ITabPlugin> getTabs()
	{
		return _tabs;
	}
	
	static private HashMap<String, Object> parseFactories(Element factories)
		throws SAXException
	{
		for (Element factory : new NodeIterable(factories))
		{
			if (!factory.getNodeName().equals(PLUGIN_FACTORY_ELEMENT))
				throw new SAXException("Element \"" + factory.getNodeName()
					+ "\" encountered while looking for \"" 
					+ PLUGIN_FACTORY_ELEMENT + "\".");
			
			String 
		}
	}
	
	static private void parsePlugin(HashMap<String, Object> factories,
		Element plugin, HashMap<String, PluginMenu> mainMenu,
		PluginMenu contextMenu, TreeSet<ITabPlugin> tabs)
	{
		// TODO
	}
	
	static private PluginManager createPluginManager(Document doc)
		throws SAXException
	{
		HashMap<String, PluginMenu> mainMenuBar = new HashMap<String, PluginMenu>();
		PluginMenu contextMenu = new PluginMenu();
		TreeSet<ITabPlugin> tabs = new TreeSet<ITabPlugin>(new TabComparator());
		HashMap<String, Object> pluginFactories = null;
		
		doc.getDocumentElement();
		for (Element child : new NodeIterable(doc))
		{
			if (child.getNodeName().equals(PLUGIN_FACTORIES_ELEMENT))
			{
				pluginFactories = parseFactories((Element)child);
			} else if (child.getNodeName().equals(PLUGIN_ELEMENT))
			{
				if (pluginFactories == null)
					throw new SAXException("Cannot parse a \""
						+ PLUGIN_ELEMENT + "\" element until the \""
						+ PLUGIN_FACTORIES_ELEMENT 
						+ "\" element has been read.");
				
				parsePlugin(pluginFactories, (Element)child,
					mainMenuBar, contextMenu, tabs);
			} else
			{
				throw new SAXException("Element \"" + child.getNodeName() 
					+ "\" is not recognized.");
			}
		}
		
		return new PluginManager(mainMenuBar, contextMenu, tabs);
	}
	
	static public PluginManager createPluginManager(
		InputStream configuration) 
			throws ParserConfigurationException, IOException,
				SAXException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true);
		factory.setNamespaceAware(false);
		factory.setValidating(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(configuration);
		return createPluginManager(doc);
	}
	
	static public PluginManager createPluginManager(
		File configurationFile)
			throws ParserConfigurationException, IOException,
				SAXException
	{
		InputStream in = null;
		
		try
		{
			in = new FileInputStream(configurationFile);
			return createPluginManager(in);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	static public PluginManager createPluginManager(
		FileResource configurationResource)
			throws IOException, ParserConfigurationException,
				SAXException
	{
		InputStream in = null;
		
		try
		{
			in = configurationResource.open();
			return createPluginManager(in);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	static private class TabComparator implements Comparator<ITabPlugin>
	{
		@Override
		public int compare(ITabPlugin o1, ITabPlugin o2)
		{
			return o1.getPriority() - o2.getPriority();
		}
	}
}