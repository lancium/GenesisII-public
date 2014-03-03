package edu.virginia.vcgr.genii.gjt.data.xpath;

import javax.xml.XMLConstants;

public class DefaultXPathAttributeNode extends DefaultXPathNode implements
		XPathAttributeNode {
	public DefaultXPathAttributeNode(String namespaceURI, String prefix) {
		super(namespaceURI, prefix);
	}

	@Override
	public String toString(NamespacePrefixMap prefixMap) {
		if (_namespaceURI == null
				|| _namespaceURI.equals(XMLConstants.NULL_NS_URI))
			return _name;

		return super.toString();
	}
}