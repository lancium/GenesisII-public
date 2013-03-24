/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package edu.virginia.vcgr.genii.client.ser;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.apache.axis.encoding.AnyContentType;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.message.MessageElement;

import org.morgan.util.io.StreamUtils;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

/**
 * Converts Java DOM Elements and SOAP Elements to Java objects. The objects must be compliant with
 * the Axis Bean model, i.e. generated using the WSDL2Java tool from an XML Schema definition or
 * must be of simple type.
 */
public class ObjectDeserializer
{

	/**
	 * Converts a DOM Element object into a Java object. The type of the Java object will be
	 * determined from the <i>xsi:type</i> attribute of the specified element. <br>
	 * <b>Note:</b> This operation is slow as it converts the DOM Element into a string which then
	 * is deserialized into a Java object.
	 */
	public static Object toObject(Element element) throws ResourceException
	{
		return toObject(element, null);
	}

	/**
	 * Converts a DOM Element object into a Java object. <br>
	 * <b>Note:</b> This operation is slow as it converts the DOM Element into a string which then
	 * is deserialized into a Java object.
	 */
	public static Object toObject(Element element, Class<?> javaClass) throws ResourceException
	{
		ObjectDeserializationContext deserializer = new ObjectDeserializationContext(element, javaClass);
		try {
			deserializer.parse();
		} catch (Exception e) {
			throw new ResourceException("Generic Deserialization Error.", e);
		}
		return deserializer.getValue();
	}

	/**
	 * Converts a SOAPElement object into a Java object. The type of the Java object will be
	 * determined from the <i>xsi:type</i> attribute of the specified element.
	 * 
	 * @see #toObject(SOAPElement, Class)
	 */
	public static Object toObject(SOAPElement element) throws ResourceException
	{
		return toObject(element, null);
	}

	/**
	 * Converts a SOAPElement object into a Java object of specified type. The class of the object
	 * must have been generated from an XML Schema, and thus contain the appropriate meta data to
	 * make deserialization possible.
	 * 
	 * @param element
	 *            SOAPElement to be converted
	 * @param javaClass
	 *            Class containing meta data about how to deserialize the object or can be of a
	 *            simple type such as Integer.
	 * @return Java object that maps to the XML representation of the input
	 * @throws ResourceException
	 *             if the input could not be deserialized into a Java type.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static <Type> Type toObject(SOAPElement element, Class<Type> javaClass) throws ResourceException
	{
		if (!(element instanceof MessageElement)) {
			throw new ResourceException("Unsupported type.");
		}

		MessageElement elem = (MessageElement) element;
		if (elem.getDeserializationContext() != null && !elem.isDirty()) {
			try {
				return (Type) elem.getValueAsType(elem.getType(), javaClass);
			} catch (Exception e) {
				throw new ResourceException("Generic Deserialization Error.", e);
			}
		} else {
			ObjectDeserializationContext deserializer = new ObjectDeserializationContext(elem, javaClass);
			try {
				deserializer.parse();
			} catch (Exception e) {
				throw new ResourceException("Generic Deserialization Error.", e);
			}

			return (Type) deserializer.getValue();
		}
	}

	/**
	 * Converts an array of SOAPElement objects into a set of Java objects. The type of the Java
	 * object will be determined from the <i>xsi:type</i> attribute of the specified element.
	 * 
	 * @see #toObject(SOAPElement, Class)
	 */
	public static Object[] toObject(SOAPElement[] elements) throws ResourceException
	{
		return toObject(elements, null);
	}

	/**
	 * Converts an array of SOAPElement objects into a set of Java objects.
	 * 
	 * @see #toObject(SOAPElement, Class)
	 */
	public static Object[] toObject(SOAPElement[] elements, Class<?> javaClass) throws ResourceException
	{
		if (elements == null) {
			return null;
		}
		Object[] objects = new Object[elements.length];
		for (int i = 0; i < elements.length; i++) {
			objects[i] = toObject(elements[i], javaClass);
		}
		return objects;
	}

	/**
	 * Converts a AnyContentType object into a set of Java objects. The type of the Java object will
	 * be determined from the <i>xsi:type</i> attribute of the specified element.
	 * 
	 * @see #toObject(SOAPElement, Class)
	 */
	public static Object[] toObject(AnyContentType any) throws ResourceException
	{
		return toObject(any, null);
	}

	/**
	 * Converts a AnyContentType object into a set of Java objects.
	 * 
	 * @see #toObject(SOAPElement, Class)
	 */
	public static Object[] toObject(AnyContentType any, Class<?> javaClass) throws ResourceException
	{
		if (any == null) {
			return null;
		}
		return toObject(any.get_any(), javaClass);
	}

	/**
	 * Converts the first element of AnyContentType object into a Java object. The type of the Java
	 * object will be determined from the <i>xsi:type</i> attribute of the specified element.
	 * 
	 * @see #toObject(SOAPElement, Class)
	 */
	public static Object getFirstAsObject(AnyContentType any) throws ResourceException
	{
		return getFirstAsObject(any, null);
	}

	/**
	 * Converts the first element of AnyContentType object into a Java object.
	 * 
	 * @see #toObject(SOAPElement, Class)
	 */
	public static <Type> Type getFirstAsObject(AnyContentType any, Class<Type> javaClass) throws ResourceException
	{
		if (any == null || any.get_any() == null || any.get_any().length == 0) {
			return null;
		}
		return toObject(any.get_any()[0], javaClass);
	}

	/**
	 * Checks to see whether the any element is of type javaClass. The javaClass must be generated
	 * from an XML Schema representation. This operation could be seen as the instanceof equivalent
	 * for xsd:any types
	 */
	public static boolean contains(SOAPElement element, Class<?> javaClass)
	{
		if (element == null || javaClass == null) {
			throw new IllegalArgumentException();
		}
		if (!(element instanceof MessageElement)) {
			throw new IllegalArgumentException();
		}
		TypeDesc desc = TypeDesc.getTypeDescForClass(javaClass);
		if (desc == null) {
			return false;
		}
		QName qname = desc.getXmlType();
		QName type = ((MessageElement) element).getType();
		if (type == null) {
			type = ((MessageElement) element).getQName();
		}

		return (type != null && qname != null && type.equals(qname));
	}

	/**
	 * Deserializes input with XML into a Java object of the given type.
	 */
	public static Object deserialize(InputSource input, Class<?> javaClass) throws ResourceException
	{
		ObjectDeserializationContext deserializer = new ObjectDeserializationContext(input, javaClass);
		try {
			deserializer.parse();
		} catch (Exception e) {
			throw new ResourceException("Generic Deserialization Error.", e);
		}
		return deserializer.getValue();
	}

	@SuppressWarnings("unchecked")
	static public <Type> Type fromBytes(Class<Type> cl, byte[] data) throws ResourceException
	{
		ByteArrayInputStream bais = null;

		try {
			bais = new ByteArrayInputStream(data);
			return (Type) ObjectDeserializer.deserialize(new InputSource(bais), cl);
		} finally {
			StreamUtils.close(bais);
		}
	}

	static public MessageElement[] anyFromBytes(byte[] data) throws ResourceException
	{
		if (data == null)
			return null;

		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;

		try {
			bais = new ByteArrayInputStream(data);
			ois = new ObjectInputStream(bais);

			int numElements = ois.readInt();
			MessageElement[] ret = new MessageElement[numElements];
			for (int lcv = 0; lcv < ret.length; lcv++) {
				ret[lcv] = new MessageElement((Element) ois.readObject());
			}

			return ret;
		} catch (ResourceException re) {
			throw re;
		} catch (Exception e) {
			throw new ResourceException(e.getLocalizedMessage(), e);
		} finally {
			StreamUtils.close(ois);
		}
	}
}