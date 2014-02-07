/*
 * Portions of this file Copyright 1999-2005 University of Chicago Portions of this file Copyright
 * 1999-2005 The University of Southern California.
 * 
 * This file or a portion of this file is licensed under the terms of the Globus Toolkit Public
 * License, found at http://www.globus.org/toolkit/download/license.html. If you redistribute this
 * file, with or without modifications, you must include this notice in the file.
 */
package edu.virginia.vcgr.genii.client.ser;

import java.io.Writer;
import java.io.StringWriter;
import java.io.StringReader;
import java.util.List;

import javax.xml.soap.SOAPElement;

import org.apache.axis.MessageContext;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.Text;
import org.apache.axis.encoding.AnyContentType;
import org.apache.axis.encoding.SerializationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.InputSource;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

/**
 * The <code>AnyHelper</code> is a utility that provides common functions for working with
 * <code>MessageElement</code> and beans with <code>AnyContentType</code> class. <b>Do not used this
 * class for serialization or deserialization of objects.</b> Use
 * {@link edu.virginia.vcgr.genii.client.ser.ObjectSerializer ObjectSerializer} and
 * {@link edu.virginia.vcgr.genii.client.ser.ObjectDeserializer ObjectDeserializer} for that
 * purposes instead.
 */
public class AnyHelper
{
	static Log _logger = LogFactory.getLog(AnyHelper.class.getName());

	/**
	 * Populates a SOAP MessageElement with an arbitrary object, and wraps it inside of a value
	 * element with an xsi:type attribute. This is similar to using the xsd:any in the same way you
	 * would use xsd:anyType objects.
	 * 
	 * @param obj
	 *            object to be serialized in the any element
	 * @return content of any element as a SOAP MessageElement
	 */
	public static MessageElement toAnyTypeElement(Object obj)
	{
		MessageElement messageElement = new MessageElement(GenesisIIConstants.GENESISII_NS, "value", obj);
		messageElement.setType(org.apache.axis.Constants.XSD_ANYTYPE);
		return messageElement;
	}

	/**
	 * Populates a SOAP MessageElement with an arbitrary object.
	 * 
	 * @see #toAnyTypeElement(Object)
	 * @param obj
	 *            object to be serialized in the any element.
	 * @return content of any element as a SOAP MessageElement
	 */
	public static MessageElement toAny(Object obj)
	{
		if (obj == null) {
			return null;
		}

		if (obj instanceof MessageElement) {
			return (MessageElement) obj;
		} else if (obj instanceof Element) {
			return new MessageElement((Element) obj);
		}

		return toAnyTypeElement(obj);
	}

	public static void write(Writer writer, org.apache.axis.message.MessageElement element) throws Exception
	{
		MessageContext messageContext = Config.getContext();
		SerializationContext context = new SerializationContext(writer, messageContext);
		context.setPretty(true);
		element.output(context);
		writer.write('\n');
	}

	// ********* toString **********

	/**
	 * Converts a SOAP MessageElement to an XML String representation
	 * 
	 * @param element
	 *            SOAP MessageElement to be converted
	 * @return String in XML format representing the input
	 */
	public static String toString(MessageElement element) throws Exception
	{
		if (element == null) {
			return null;
		}
		StringWriter writer = new StringWriter();
		write(writer, element);
		writer.flush();
		return writer.toString();
	}

	/**
	 * Array version of {@link #toString(MessageElement element) toString}
	 */
	public static String[] toString(MessageElement[] elements) throws Exception
	{
		if (elements == null) {
			return null;
		}
		String[] result = new String[elements.length];
		for (int i = 0; i < elements.length; i++) {
			result[i] = toString(elements[i]);
		}
		return result;
	}

	/**
	 * Converts a SOAP MessageElement to a DOM Element representation
	 * 
	 * @param element
	 *            SOAP MessageElement to be converted
	 * @return DOM Element representing the input
	 * @throws Exception
	 *             if the DOM Element could not be created
	 */
	public static Element toElement(MessageElement element) throws Exception
	{
		String str = toString(element);
		if (str == null) {
			return null;
		}
		StringReader reader = new StringReader(str);
		Document doc = XmlUtils.newDocument(new InputSource(reader));
		return (doc == null) ? null : doc.getDocumentElement();
	}

	/**
	 * Array version of {@link #toElement(MessageElement element) toElement}
	 */
	public static Element[] toElement(MessageElement[] elements) throws Exception
	{
		if (elements == null) {
			return null;
		}
		Element[] result = new Element[elements.length];
		for (int i = 0; i < elements.length; i++) {
			result[i] = toElement(elements[i]);
		}
		return result;
	}

	/**
	 * Converts type containing any element to an array of DOM Elements.
	 * 
	 * @see #toElement(MessageElement element)
	 */
	public static Element[] toElement(AnyContentType any) throws Exception
	{
		if (any == null) {
			return null;
		}
		return toElement(any.get_any());
	}
}
