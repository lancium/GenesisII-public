package edu.virginia.vcgr.genii.container.wsrf.wsn.topic.xml;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ElementBuilderUtils
{
	static Element createElement(
		Document documentOwner, QName elementName)
	{
		String namespaceURI = elementName.getNamespaceURI();
		String local = elementName.getLocalPart();
		String prefix = elementName.getPrefix();
		
		if (prefix != null && prefix.length() > 0)
			return documentOwner.createElementNS(namespaceURI,
				String.format("%s:%s", prefix, local));
		else
			return documentOwner.createElementNS(namespaceURI, local);
	}
}