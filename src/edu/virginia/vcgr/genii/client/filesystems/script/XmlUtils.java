package edu.virginia.vcgr.genii.client.filesystems.script;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class XmlUtils
{
	static Element[] getChildren(Element parent)
	{
		NodeList list = parent.getChildNodes();
		ArrayList<Element> ret = new ArrayList<Element>(list.getLength());
		
		for (int lcv = 0; lcv < list.getLength(); lcv++)
		{
			Node child = list.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
				ret.add((Element)child);
		}
		
		Element []array = new Element[ret.size()];
		ret.toArray(array);
		return array;
	}
	
	static QName qname(Node node)
	{
		return new QName(node.getNamespaceURI(), node.getLocalName());
	}
	
	static String requiredAttribute(Element node, String attributeName)
		throws FilterScriptException
	{
		String value = node.getAttribute(attributeName);
		if (value == null || value.length() == 0)
			throw new FilterScriptException(String.format(
				"Element %s is missing required attribute %s.",
				qname(node), attributeName));
		
		return value;
	}
}