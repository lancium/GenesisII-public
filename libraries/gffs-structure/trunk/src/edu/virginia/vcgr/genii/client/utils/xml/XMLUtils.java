package edu.virginia.vcgr.genii.client.utils.xml;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XMLUtils
{
	//hmmm: consolidate this down into the gffs basics xml utils stuff.
	
	static public String getRequiredAttribute(Element element, String attrName, String defaultValue) throws SAXException
	{
		String value = element.getAttribute(attrName);
		if (value == null || value.length() == 0) {
			if (defaultValue == null)
				throw new SAXException("Unable to find required attribute \"" + attrName + "\" in element \""
					+ element.getNodeName() + "\".");

			value = defaultValue;
		}

		return value;
	}
}