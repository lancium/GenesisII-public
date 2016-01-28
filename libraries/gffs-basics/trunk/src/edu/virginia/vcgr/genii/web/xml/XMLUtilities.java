package edu.virginia.vcgr.genii.web.xml;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtilities
{
	static public String getRequiredAttribute(Element element, String attrName, String defaultValue) throws SAXException
	{
		String value = element.getAttribute(attrName);
		if (value == null || value.length() == 0) {
			if (defaultValue == null)
				throw new SAXException(
					"Unable to find required attribute \"" + attrName + "\" in element \"" + element.getNodeName() + "\".");

			value = defaultValue;
		}

		return value;
	}

	static public QName getQName(Node node)
	{
		return new QName(node.getNamespaceURI(), node.getLocalName());
	}

	static public String getRequiredAttribute(NamedNodeMap attributes, String attributeName) throws SAXException
	{
		String ret = getAttribute(attributes, attributeName, null);
		if (ret == null)
			throw new SAXException(String.format("Unable to find required attribute %s.", attributeName));
		return ret;
	}

	static public String getRequiredAttribute(Node node, String attributeName) throws SAXException
	{
		return getRequiredAttribute(node.getAttributes(), attributeName);
	}

	static public String getAttribute(NamedNodeMap attributes, String attributeName, String defaultValue)
	{
		Node attr = attributes.getNamedItem(attributeName);
		if (attr == null)
			return defaultValue;

		return attr.getTextContent();
	}

	static public String getAttribute(Node node, String attributeName, String defaultValue)
	{
		return getAttribute(node.getAttributes(), attributeName, defaultValue);
	}

	static public String getTextContent(Element e) throws SAXException
	{
		Node n = e.getFirstChild();
		if (n.getNodeType() != Node.TEXT_NODE)
			throw new SAXException(String.format("XML Element %s does not contain text.", getQName(e)));
		return n.getTextContent();
	}

	/**
	 * consumes the xml text in "xmlString" and attempts to return a w3c document object that represents the contents.
	 */
	public static Document parseXML(String xmlString) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		InputSource source = new InputSource(new StringReader(xmlString));
		return builder.parse(source);
	}
}
