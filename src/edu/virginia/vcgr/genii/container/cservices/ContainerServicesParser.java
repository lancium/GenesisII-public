package edu.virginia.vcgr.genii.container.cservices;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.MacroUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.virginia.vcgr.genii.client.utils.xml.XMLUtils;

public class ContainerServicesParser
{
	static private Log _logger = LogFactory.getLog(ContainerServicesParser.class);
	
	static private final String VARIABLE_ELEMENT_NAME = "variable";
	static private final String VARIABLE_NAME_ATTR = "name";
	static private final String VARIABLE_VALUE_ATTR = "value";
	
	static private final String CONTAINER_SERVICE_ELEMENT_NAME = "container-service";
	static private final String CONTAINER_SERVICE_CLASS_ATTR = "class";
	
	static private final String PROPERTY_ELEMENT_NAME = "property";
	static private final String PROPERTY_NAME_ATTR = "name";
	static private final String PROPERTY_VALUE_ATTR = "value";
	
	static private void parseVariable(Element node, 
		Properties variables) throws SAXException, IOException
	{
		String varName = XMLUtils.getRequiredAttribute(
			node, VARIABLE_NAME_ATTR, null);
		String varValue = XMLUtils.getRequiredAttribute(
			node, VARIABLE_VALUE_ATTR, null);
		
		if (varName == null)
			throw new IOException(String.format(
				"Couldn't find %s attribute in %s element.", 
				VARIABLE_NAME_ATTR, VARIABLE_ELEMENT_NAME));
		if (varValue == null)
			throw new IOException(String.format(
				"Couldn't find %s attribute in %s element.", 
				VARIABLE_VALUE_ATTR, VARIABLE_ELEMENT_NAME));
		
		varValue = MacroUtils.replaceMacros(variables, varValue);
		variables.put(varName, varValue);
	}
	
	static private Properties parseConstructionProperties(
		NodeList children, Properties variables)
		throws SAXException, IOException
	{
		Properties ret = new Properties();
		for (int lcv = 0; lcv < children.getLength(); lcv++)
		{
			Node childNode = children.item(lcv);
			if (childNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Element childElement = (Element)childNode;
				String nodeName = childElement.getNodeName();
				
				if (nodeName.equals(PROPERTY_ELEMENT_NAME))
				{
					String varName = XMLUtils.getRequiredAttribute(
						childElement, PROPERTY_NAME_ATTR, null);
					String varValue = XMLUtils.getRequiredAttribute(
						childElement, PROPERTY_VALUE_ATTR, null);
					
					ret.setProperty(varName, MacroUtils.replaceMacros(
						variables, varValue));
				} else
				{
					throw new IOException(String.format(
						"Unknown element \"%s\" found.", nodeName));
				}
			}
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	static private Class<? extends ContainerService> findClass(
		String className) throws IOException
	{
		try
		{
			return (Class<? extends ContainerService>)
				ContainerServicesParser.class.getClassLoader().loadClass(
					className);
		}
		catch (Throwable cause)
		{
			throw new IOException("Unable to find class \"" + 
				className + "\".", cause);
		}
	}
	
	static private ContainerService constructClass(
		Class<? extends ContainerService> serviceClass,
		Properties constructionProperties) throws IOException
	{
		try
		{
			try
			{
				Constructor<? extends ContainerService> constructor =
					serviceClass.getConstructor(Properties.class);
				return constructor.newInstance(constructionProperties);
			}
			catch (NoSuchMethodException nsme)
			{
				if (constructionProperties.size() > 0)
					throw new IOException(String.format(
						"Unable to find appropriate constructor for class \"%s\".",
						serviceClass.getName()));
				Constructor<? extends ContainerService> constructor =
					serviceClass.getConstructor();
				return constructor.newInstance();
			}
		}
		catch (Throwable cause)
		{
			throw new IOException(String.format(
				"Unable to construct container service class \"%s\".", 
				serviceClass.getName()));
		}
	}
	
	static private ContainerService parseContainerService(
		Element node, Properties variables) 
			throws SAXException, IOException
	{
		String className = XMLUtils.getRequiredAttribute(node, 
			CONTAINER_SERVICE_CLASS_ATTR, null);
		className = MacroUtils.replaceMacros(variables, className);
		
		Properties constProperties = parseConstructionProperties(
			node.getChildNodes(), variables);
		
		Class<? extends ContainerService> serviceClass = findClass(className);
		return constructClass(serviceClass, constProperties);
	}
	
	static private void parseNode(Element node, Properties variables,
		Collection<ContainerService> services) 
			throws SAXException, IOException
	{
		String nodeName = node.getNodeName();
		
		if (nodeName.equals(VARIABLE_ELEMENT_NAME))
		{
			parseVariable(node, variables);
		} else if (nodeName.equals(CONTAINER_SERVICE_ELEMENT_NAME))
		{
			try
			{
				services.add(parseContainerService(node, variables));
			}
			catch (Throwable cause)
			{
				_logger.error("Unable to load service.", cause);
			}
		} else
		{
			throw new IOException(String.format(
				"Unrecognized configuration element \"%s\".", nodeName));
		}
	}
	
	static private Collection<ContainerService> parseConfigDocument(Document doc)
		throws SAXException, IOException
	{
		Collection<ContainerService> ret = new LinkedList<ContainerService>();
		Properties variables = new Properties();
		
		Element element = doc.getDocumentElement();
		NodeList children = element.getChildNodes();
		
		for (int lcv = 0; lcv < children.getLength(); lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				parseNode((Element)child, variables, ret);
			}
		}
		
		return ret;
	}
	
	static public Collection<ContainerService> parseConfigFile(File configFile)
		throws IOException
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			factory.setNamespaceAware(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(configFile);
			return parseConfigDocument(doc);
		}
		catch (ParserConfigurationException ce)
		{
			throw new IOException("Unable to create XML parser.", ce);
		}
		catch (SAXException se)
		{
			throw new IOException("Unable to parse config file \"" + 
				configFile + "\".", se);
		}
	}
}