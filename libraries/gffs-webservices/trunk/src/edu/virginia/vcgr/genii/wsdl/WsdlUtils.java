package edu.virginia.vcgr.genii.wsdl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class WsdlUtils
{
	static protected QName getQName(Node n)
	{
		if (n.getNodeType() != Node.ELEMENT_NODE)
			return null;

		return new QName(n.getNamespaceURI(), n.getLocalName());
	}

	static protected String getAttribute(NamedNodeMap attrs, String attrName, boolean required) throws WsdlException
	{
		Node attrNode = attrs.getNamedItem(attrName);
		if (attrNode == null)
			if (required)
				throw new WsdlException("Unable to find \"" + attrName + "\" attribute in wsdl:import node.");
			else
				return null;

		return attrNode.getTextContent();
	}

	static private Pattern _QNAME_PATTERN = Pattern.compile("^\\{([^\\}]+)\\}(.+)$");

	static public QName getQNameFromString(String str) throws WsdlException
	{
		Matcher m = _QNAME_PATTERN.matcher(str);
		if (!m.matches())
			throw new WsdlException("\"" + str + "\" does not appear to be a qname.");
		return new QName(m.group(1), m.group(2));
	}

	static QName getQNameFromString(Node someNode, String str) throws WsdlException
	{
		int index = str.indexOf(':');
		if (index <= 0)
			throw new WsdlException("Couldn't find namespace for \"" + str + "\".");

		String namespace = someNode.lookupNamespaceURI(str.substring(0, index));
		if (namespace == null) {
			Document alternativeResolver = AlternativeNamespaceResolution.getAlternativeResolver();
			if (alternativeResolver != null) {
				namespace = alternativeResolver.lookupNamespaceURI(str.substring(0, index));
			}

			if (namespace == null)
				throw new WsdlException("Couldn't find namespace for " + str.substring(0, index) + " prefix.");
		}

		return new QName(namespace, str.substring(index + 1));
	}

	static public String findHierarchicalAttribute(Node myNode, String attrName)
	{
		try {
			while (myNode != null) {
				String value = getAttribute(myNode.getAttributes(), attrName, false);
				if (value != null)
					return value;
				myNode = myNode.getParentNode();
			}
		} catch (WsdlException we) {
			// Can't happen.
		}

		return null;
	}
}