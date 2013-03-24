package edu.virginia.vcgr.genii.client.utils.creation;

import java.util.Iterator;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

/**
 * This class is a collection of utility functions that makes it easier to add Java Properties to a
 * creation message (the any element).
 * 
 * @author mmm2a
 */
public class CreationProperties
{
	static public final String CREATION_PROPERTIES_NS = GenesisIIConstants.GENESISII_NS;
	static public final String CREATION_PROPERTIES_NAME = "creation-properties";
	static public final QName CREATION_PROPERTIES_QNAME = new QName(CREATION_PROPERTIES_NS, CREATION_PROPERTIES_NAME);

	static public final String CREATION_PROPERTY_NS = GenesisIIConstants.GENESISII_NS;
	static public final String CREATION_PROPERTY_NAME = "creation-property";
	static public final QName CREATION_PROPERTY_QNAME = new QName(CREATION_PROPERTY_NS, CREATION_PROPERTY_NAME);

	static public final String CREATION_PROPERTY_NAME_ATTR = "name";
	static public final String CREATION_PROPERTY_VALUE_ATTR = "value";

	static public MessageElement translate(Properties properties)
	{
		try {
			MessageElement ret = new MessageElement(CREATION_PROPERTIES_QNAME);

			for (Object key : properties.keySet()) {
				String sKey = key.toString();

				ret.addChild(translate(sKey, properties.getProperty(sKey)));
			}

			return ret;
		} catch (SOAPException se) {
			throw new RuntimeException("Unable to create message properties " + "for creation properties.", se);
		}
	}

	static public Properties translate(MessageElement element) throws SOAPException
	{
		Properties properties = new Properties();

		Iterator<?> iter = element.getChildElements();
		while (iter.hasNext()) {
			MessageElement child = (MessageElement) iter.next();
			if (!child.getQName().equals(CREATION_PROPERTY_QNAME))
				throw new RuntimeException("Error parsing creation property.");

			add(properties, child);
		}

		return properties;
	}

	static private MessageElement translate(String key, String value)
	{
		MessageElement ret = new MessageElement(CREATION_PROPERTY_QNAME);
		ret.setAttribute(CREATION_PROPERTY_NAME_ATTR, key);
		ret.setAttribute(CREATION_PROPERTY_VALUE_ATTR, value);

		return ret;
	}

	static private void add(Properties props, MessageElement child)
	{
		String key = child.getAttribute(CREATION_PROPERTY_NAME_ATTR);
		String value = child.getAttribute(CREATION_PROPERTY_VALUE_ATTR);

		if (key == null || value == null)
			throw new RuntimeException("Unable to parse creation property element.");

		props.setProperty(key, value);
	}
}