package edu.virginia.vcgr.genii.ui.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.morgan.util.io.StreamUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UIPluginConfigParser
{
	static final private String DEFAULT_RESOURCE_PATH = "edu/virginia/vcgr/genii/ui/plugins/config.xml";

	static final private String PLUGINS_ELEMENT = "plugins";
	static final private String PLUGIN_ELEMENT = "plugin";
	static final private String CLASS_ATTR = "class";
	static final private String TOP_MENU_ELEMENT = "top-menu";
	static final private String MENU_NAME_ATTR = "menu-name";
	static final private String GROUP_ATTR = "group";
	static final private String ITEM_NAME_ATTR = "item-name";
	static final private String PROPERTY_ELEMENT = "property";
	static final private String NAME_ATTR = "name";
	static final private String VALUE_ATTR = "value";
	static final private String TAB_ELEMENT = "tab";
	static final private String POPUP_MENU_ELEMENT = "popup-menu";
	static final private String PLUGIN_PROPERTIES_ELEMENT = "plugin-properties";

	static private String requiredAttribute(Element element, String attributeName) throws SAXException
	{
		String ret = element.getAttribute(attributeName);
		if (ret == null)
			throw new SAXException(String.format("Unable to get required attribute \"%s\" from element \"%s\".", attributeName,
				element.getNodeName()));

		return ret;
	}

	static private Properties readProperties(Element element) throws SAXException
	{
		Properties props = new Properties();

		for (Element child : new ElementIterable(element)) {
			if (!child.getNodeName().equals(PROPERTY_ELEMENT))
				throw new SAXException(String.format("Unexpected element \"%s\".", child.getNodeName()));

			props.setProperty(requiredAttribute(child, NAME_ATTR), requiredAttribute(child, VALUE_ATTR));
		}

		return props;
	}

	@SuppressWarnings("unchecked")
	static private UIPlugin createPlugin(String className) throws SAXException
	{
		try {
			Class<? extends UIPlugin> pluginClass = (Class<? extends UIPlugin>) Class.forName(className);
			return pluginClass.newInstance();
		} catch (ClassNotFoundException cnfe) {
			throw new SAXException(String.format("Unable to find plugin class \"%s\".", className), cnfe);
		} catch (InstantiationException e) {
			throw new SAXException(String.format("Unable to instantiate plugin class \"%s\".", className), e);
		} catch (IllegalAccessException e) {
			throw new SAXException(String.format("Unable to instantiate plugin class \"%s\".", className), e);
		}
	}

	static private UIPluginDescription parsePlugin(Element pluginElement) throws SAXException, UIPluginException
	{
		UITabFacetDescription tabDescription = null;
		UIPopupMenuFacetDescription popupDescription = null;
		UITopMenuFacetDescription topMenuDescription = null;
		String className = requiredAttribute(pluginElement, CLASS_ATTR);
		UIPlugin plugin = createPlugin(className);

		for (Element child : new ElementIterable(pluginElement)) {
			String name = child.getNodeName();
			if (name.equals(TOP_MENU_ELEMENT)) {
				UITopMenuPlugin topPlugin = (UITopMenuPlugin) plugin;

				topMenuDescription = new UITopMenuFacetDescription(requiredAttribute(child, MENU_NAME_ATTR), requiredAttribute(
					child, GROUP_ATTR), requiredAttribute(child, ITEM_NAME_ATTR), topPlugin);
				topPlugin.configureTopMenu(readProperties(child));
			} else if (name.equals(POPUP_MENU_ELEMENT)) {
				UIPopupMenuPlugin popupPlugin = (UIPopupMenuPlugin) plugin;

				popupDescription = new UIPopupMenuFacetDescription(requiredAttribute(child, GROUP_ATTR), requiredAttribute(
					child, ITEM_NAME_ATTR), popupPlugin);
				popupPlugin.configurePopupMenu(readProperties(child));
			} else if (name.equals(TAB_ELEMENT)) {
				UITabPlugin tabPlugin = (UITabPlugin) plugin;

				tabDescription = new UITabFacetDescription(requiredAttribute(child, NAME_ATTR), tabPlugin);
				tabPlugin.configureTabPlugin(readProperties(child));
			} else if (name.equals(PLUGIN_PROPERTIES_ELEMENT)) {
				plugin.configurePlugin(readProperties(child));
			} else {
				throw new SAXException(String.format("Unexpected element found:  \"%s\".", name));
			}
		}

		return new UIPluginDescription(topMenuDescription, popupDescription, tabDescription);
	}

	static private Collection<UIPluginDescription> parse(Document document) throws SAXException, UIPluginException
	{
		Collection<UIPluginDescription> ret = new LinkedList<UIPluginDescription>();

		Element pluginsElement = document.getDocumentElement();
		if (pluginsElement.getNodeType() != Element.ELEMENT_NODE)
			throw new SAXException(String.format("Expected \"%s\" element.", PLUGINS_ELEMENT));
		String name = pluginsElement.getNodeName();
		if (!name.equals(PLUGINS_ELEMENT))
			throw new SAXException(String.format("Expected \"%s\" element.", PLUGINS_ELEMENT));

		for (Element child : new ElementIterable(pluginsElement)) {
			name = child.getNodeName();
			if (name.equals(PLUGIN_ELEMENT)) {
				ret.add(parsePlugin(child));
			} else
				throw new SAXException(String.format("Unable to parse plugins.  Unexpected element \"%s\".", name));
		}

		return ret;
	}

	static public Collection<UIPluginDescription> parse(InputStream configStream) throws ParserConfigurationException,
		SAXException, IOException, UIPluginException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true);
		factory.setNamespaceAware(false);
		factory.setValidating(false);

		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(configStream);
		return parse(document);
	}

	static public Collection<UIPluginDescription> parse(File configFile) throws ParserConfigurationException, SAXException,
		IOException, UIPluginException
	{
		FileInputStream fin = null;

		try {
			fin = new FileInputStream(configFile);
			return parse(fin);
		} finally {
			StreamUtils.close(fin);
		}
	}

	static public Collection<UIPluginDescription> parse(String resourcePath) throws ParserConfigurationException, SAXException,
		IOException, UIPluginException
	{
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream in = null;

		try {
			in = loader.getResourceAsStream(resourcePath);
			if (in == null)
				throw new FileNotFoundException(String.format("Unable to load resource \"%s\".", resourcePath));
			return parse(in);
		} finally {
			StreamUtils.close(in);
		}
	}

	static public Collection<UIPluginDescription> parse() throws ParserConfigurationException, SAXException, IOException,
		UIPluginException
	{
		return parse(DEFAULT_RESOURCE_PATH);
	}

	static private class ElementIterable implements Iterable<Element>
	{
		private Element _parent;

		private ElementIterable(Element parent)
		{
			_parent = parent;
		}

		@Override
		public Iterator<Element> iterator()
		{
			return new ElementIterator(_parent.getChildNodes());
		}
	}

	static private class ElementIterator implements Iterator<Element>
	{
		private NodeList _list;
		private int _next;

		private ElementIterator(NodeList list)
		{
			_list = list;
			_next = 0;
		}

		@Override
		public boolean hasNext()
		{
			while (_next < _list.getLength()) {
				if (_list.item(_next).getNodeType() == Element.ELEMENT_NODE)
					return true;
				_next++;
			}

			return false;
		}

		@Override
		public Element next()
		{
			while (_next < _list.getLength()) {
				Node node = _list.item(_next++);
				if (node.getNodeType() == Node.ELEMENT_NODE)
					return (Element) node;
			}

			return null;
		}

		@Override
		public void remove()
		{
			throw new RuntimeException("Remove operation not supported.");
		}
	}
}