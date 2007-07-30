/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.morgan.util.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses a configuration file which has the format:
 * &lt;mconf:node
 * 		xmlns:mconf="http://www.mark-morgan.net/org/morgan/util/configuration>"&gt;
 * 		&lt;mconf:config-sections&gt;
 * 			&lt;mconf:config-section name={qname} 
 * 				class=IXMLConfigurationSectionHandler/&gt;*
 * 		&lt;mconf:config-sections&gt;
 * 
 * 		{xsd:any}
 * &lt;/mconf:node&gt;
 *
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class XMLConfiguration
{
	static public final String NAMESPACE =
		"http://www.mark-morgan.net/org/morgan/util/configuration";
	static public final String CONFIG_SECTIONS = "config-sections";
	static public final String CONFIG_SECTION = "config-section";
	static public final String CONFIG_SECTION_NAME = "name";
	static public final String CONFIG_SECTION_CLASS = "class";
	
	static public QName CONFIG_SECTIONS_QNAME =
		new QName(NAMESPACE, CONFIG_SECTIONS);
	static public QName CONFIG_SECTION_QNAME =
		new QName(NAMESPACE, CONFIG_SECTION);
	
	private HashMap<QName, ArrayList<Object> > _configurations =
		new HashMap<QName, ArrayList<Object> >();
	private HashMap<QName, ArrayList<Node> > _unparsedXML =
		new HashMap<QName, ArrayList<Node> >();
	private HashMap<QName, IXMLConfigurationSectionHandler> _handlers =
		new HashMap<QName, IXMLConfigurationSectionHandler>();
	
	static public QName getQName(Node n)
	{
		String namespace = n.getNamespaceURI();
		String name = n.getLocalName();
		if (name == null)
			return new QName(n.getNodeName());
		return new QName(namespace, name);
	}
	
	private IXMLConfigurationSectionHandler getNamedHandler(String clazz)
		throws ConfigurationException
	{
		try
		{
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Class cl = loader.loadClass(clazz);
			if (!IXMLConfigurationSectionHandler.class.isAssignableFrom(cl))
				throw new ConfigurationException("Class \"" + clazz + 
					"\" does not implement IXMLConfigurationSectionHandler.");
			Constructor cons = cl.getConstructor(new Class[0]);
			return (IXMLConfigurationSectionHandler)cons.newInstance(
				new Object[0]);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ConfigurationException("Couldn't find class \"" + clazz +
				"\".");
		}
		catch (NoSuchMethodException nsme)
		{
			throw new ConfigurationException(
				"Couldn't find a default constructor for class \"" + clazz + 
				"\".", nsme);
		}
		catch (InvocationTargetException ite)
		{
			Throwable cause = ite.getCause();
			if (cause == null)
				cause = ite;
			
			throw new ConfigurationException(cause);
		}
		catch (InstantiationException ia)
		{
			throw new ConfigurationException("Couldn't create instance of \"" +
				clazz + "\".", ia);
		}
		catch (IllegalAccessException iae)
		{
			throw new ConfigurationException("Couldn't create instance of \"" +
				clazz + "\".", iae);
		}
	}

	private void addConfigHandler(Node responsibleNode, String name, String clazz)
		throws ConfigurationException
	{
		QName section;
		name = name.trim();
		clazz = clazz.trim();
	
		int index = name.indexOf(':');
		if (index < 0)
			section = new QName(name);
		else
		{
			String ns = responsibleNode.lookupNamespaceURI(
				name.substring(0, index));
			if (ns == null)
				throw new ConfigurationException(
					"Couldn't look-up a namespace for \"" +
					name.substring(0, index) + "\".");
			section = new QName(ns, name.substring(index + 1));
		}
		
		IXMLConfigurationSectionHandler handler = getNamedHandler(clazz);
		_handlers.put(section, handler);
	}
	
	private void handleConfigSections(Node configSections)
		throws ConfigurationException
	{
		NodeList children = configSections.getChildNodes();
		int length = children.getLength();
		
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node configSection = children.item(lcv);
			if (configSection.getNodeType() == Node.ELEMENT_NODE)
			{
				QName name = getQName(configSection);
				if (!name.equals(CONFIG_SECTION_QNAME))
					throw new ConfigurationException(
						"Found an invalid node in a config-sections element.");
				
				NamedNodeMap attrs = configSection.getAttributes();
				Node nameNode = attrs.getNamedItem(CONFIG_SECTION_NAME);
				Node classNode = attrs.getNamedItem(CONFIG_SECTION_CLASS);
				
				if (nameNode == null)
					throw new ConfigurationException(
						"Found a config-section element with no name.");
				if (classNode == null)
					throw new ConfigurationException(
						"Founda  config-section element with no class.");
				
				addConfigHandler(configSection,
					nameNode.getTextContent(), classNode.getTextContent());
			}
		}
	}

	private void handleChild(Node child) throws ConfigurationException
	{
		QName nodeQName = XMLConfiguration.getQName(child);
		
		if (nodeQName.equals(CONFIG_SECTIONS_QNAME))
			handleConfigSections(child);
		else
		{
			ArrayList<Node> list = _unparsedXML.get(nodeQName);
			if (list == null)
			{
				_unparsedXML.put(nodeQName, list = new ArrayList<Node>());
			}
			list.add(child);
		}
	}
	
	private void initialize(Node node) throws ConfigurationException
	{
		NodeList children = node.getChildNodes();
		int length = children.getLength();
		
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node n = children.item(lcv);
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				handleChild(n);
			}
		}
	}
	
	private void initialize(InputStream in)
		throws ParserConfigurationException, IOException, SAXException,
			ConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(in);
		initialize(doc.getDocumentElement());
	}
	
	public XMLConfiguration(String filepath)
		throws FileNotFoundException, IOException, ConfigurationException
	{
		this(new File(filepath));
	}
	
	public XMLConfiguration(File file)
		throws FileNotFoundException, IOException, ConfigurationException
	{
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(file);
			initialize(fin);
		}
		catch (SAXException se)
		{
			throw new ConfigurationException(se);
		}
		catch (ParserConfigurationException pce)
		{
			throw new ConfigurationException(pce);
		}
		finally
		{
			if (fin != null)
			{
				try { fin.close(); } catch (IOException ioe) {}
			}
		}
	}
	
	public XMLConfiguration(InputStream in)
		throws IOException, ConfigurationException
	{
		try
		{
			initialize(in);
		}
		catch (ParserConfigurationException pce)
		{
			throw new ConfigurationException(pce);
		}
		catch (SAXException se)
		{
			throw new ConfigurationException(se);
		}
	}
	
	public XMLConfiguration(Node node)
		throws ConfigurationException
	{
		initialize(node);
	}
	
	/**
	 * Retrieve a section from a configuration.  If more than one section is
	 * registered with this name, or if none are, this will throw an exception.
	 * 
	 * @param sectionName The name of the section to retrieve.
	 * @return The object, parsed by the registered handler.
	 * @throws ConfigurationException if no sections with this name exist, or if
	 * more than one exists.
	 */
	synchronized public Object retrieveSection(QName sectionName)
		throws ConfigurationException
	{
		ArrayList<Node> unparsed = _unparsedXML.get(sectionName);
		if (unparsed != null)
		{
			if (unparsed.size() > 1)
				throw new ConfigurationException(
					"Too many sections with name \"" + sectionName + 
					"\" to retrieve to retrieve a singleton.");
				
			IXMLConfigurationSectionHandler handler = _handlers.get(sectionName);
			if (handler == null)
				throw new ConfigurationException("No handlers defined for \"" +
					sectionName + "\".");
			
			Object obj = handler.parse(unparsed.get(0));
			ArrayList<Object> list = new ArrayList<Object>(1);
			list.add(obj);
			_configurations.put(sectionName, list);
			_unparsedXML.remove(sectionName);
			return obj;
		} else
		{
			ArrayList<Object> list = _configurations.get(sectionName);
			if (list == null)
				throw new ConfigurationException("No section found with name \""
					+ sectionName + "\".");
			if (list.size() > 1)
				throw new ConfigurationException(
					"Too many sections with name \"" + sectionName + 
					"\" to retrieve to retrieve a singleton.");
			
			return list.get(0);
		}
	}
	
	synchronized public ArrayList<Object> retrieveSections(QName sectionName)
		throws ConfigurationException
	{
		ArrayList<Node> unparsed = _unparsedXML.get(sectionName);
		if (unparsed != null)
		{
			IXMLConfigurationSectionHandler handler = _handlers.get(sectionName);
			if (handler == null)
				throw new ConfigurationException("No handlers defined for \"" +
					sectionName + "\".");
			
			ArrayList<Object> list = new ArrayList<Object>(unparsed.size());
			_configurations.put(sectionName, list);
			_unparsedXML.remove(sectionName);
			for (Node node : unparsed)
			{
				list.add(handler.parse(node));
			}
			
			return list;
		} else
		{
			ArrayList<Object> ret = _configurations.get(sectionName);
			if (ret == null)
				throw new ConfigurationException("No section found with name \""
					+ sectionName + "\".");
			return ret;
		}
	}
}
