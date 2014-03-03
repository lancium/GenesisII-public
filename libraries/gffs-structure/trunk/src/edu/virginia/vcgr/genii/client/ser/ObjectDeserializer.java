/*
 * Portions of this file Copyright 1999-2005 University of Chicago Portions of this file Copyright
 * 1999-2005 The University of Southern California.
 * 
 * This file or a portion of this file is licensed under the terms of the Globus Toolkit Public
 * License, found at http://www.globus.org/toolkit/download/license.html. If you redistribute this
 * file, with or without modifications, you must include this notice in the file.
 */
package edu.virginia.vcgr.genii.client.ser;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.AnyContentType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.w3c.dom.Element;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.Driver;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

/**
 * Converts Java DOM Elements and SOAP Elements to Java objects. The objects
 * must be compliant with the Axis Bean model, i.e. generated using the
 * WSDL2Java tool from an XML Schema definition or must be of simple type.
 */
public class ObjectDeserializer {
	static private Log _logger = LogFactory.getLog(ObjectDeserializer.class);

	/**
	 * Converts a DOM Element object into a Java object. The type of the Java
	 * object will be determined from the <i>xsi:type</i> attribute of the
	 * specified element. <br>
	 * <b>Note:</b> This operation is slow as it converts the DOM Element into a
	 * string which then is deserialized into a Java object.
	 */
	public static Object toObject(Element element) throws ResourceException {
		return toObject(element, null);
	}

	/**
	 * Converts a DOM Element object into a Java object. <br>
	 * <b>Note:</b> This operation is slow as it converts the DOM Element into a
	 * string which then is deserialized into a Java object.
	 */
	public static Object toObject(Element element, Class<?> javaClass)
			throws ResourceException {
		ObjectDeserializationContext deserializer = new ObjectDeserializationContext(
				element, javaClass);
		try {
			deserializer.parse();
		} catch (Exception e) {
			throw new ResourceException("Generic Deserialization Error.", e);
		}
		return deserializer.getValue();
	}

	/**
	 * Converts a SOAPElement object into a Java object. The type of the Java
	 * object will be determined from the <i>xsi:type</i> attribute of the
	 * specified element.
	 * 
	 * @see #toObject(SOAPElement, Class)
	 */
	public static Object toObject(SOAPElement element) throws ResourceException {
		return toObject(element, null);
	}

	/**
	 * Converts a SOAPElement object into a Java object of specified type. The
	 * class of the object must have been generated from an XML Schema, and thus
	 * contain the appropriate meta data to make deserialization possible.
	 * 
	 * @param element
	 *            SOAPElement to be converted
	 * @param javaClass
	 *            Class containing meta data about how to deserialize the object
	 *            or can be of a simple type such as Integer.
	 * @return Java object that maps to the XML representation of the input
	 * @throws ResourceException
	 *             if the input could not be deserialized into a Java type.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static <Type> Type toObject(SOAPElement element,
			Class<Type> javaClass) throws ResourceException {
		if (!(element instanceof org.apache.axis.message.MessageElement)) {
			throw new ResourceException("Unsupported type.");
		}

		org.apache.axis.message.MessageElement elem = (org.apache.axis.message.MessageElement) element;
		if (elem.getDeserializationContext() != null && !elem.isDirty()) {
			try {
				return (Type) elem.getValueAsType(elem.getType(), javaClass);
			} catch (Exception e) {
				throw new ResourceException("Generic Deserialization Error.", e);
			}
		} else {
			ObjectDeserializationContext deserializer = new ObjectDeserializationContext(
					elem, javaClass);
			try {
				deserializer.parse();
			} catch (Exception e) {
				throw new ResourceException("Generic Deserialization Error.", e);
			}

			return (Type) deserializer.getValue();
		}
	}

	/**
	 * Converts an array of SOAPElement objects into a set of Java objects. The
	 * type of the Java object will be determined from the <i>xsi:type</i>
	 * attribute of the specified element.
	 * 
	 * @see #toObject(SOAPElement, Class)
	 */
	public static Object[] toObject(SOAPElement[] elements)
			throws ResourceException {
		return toObject(elements, null);
	}

	/**
	 * Converts an array of SOAPElement objects into a set of Java objects.
	 * 
	 * @see #toObject(SOAPElement, Class)
	 */
	public static Object[] toObject(SOAPElement[] elements, Class<?> javaClass)
			throws ResourceException {
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
	 * Converts a AnyContentType object into a set of Java objects. The type of
	 * the Java object will be determined from the <i>xsi:type</i> attribute of
	 * the specified element.
	 * 
	 * @see #toObject(SOAPElement, Class)
	 */
	public static Object[] toObject(AnyContentType any)
			throws ResourceException {
		return toObject(any, null);
	}

	/**
	 * Converts a AnyContentType object into a set of Java objects.
	 * 
	 * @see #toObject(SOAPElement, Class)
	 */
	public static Object[] toObject(AnyContentType any, Class<?> javaClass)
			throws ResourceException {
		if (any == null) {
			return null;
		}
		return toObject(any.get_any(), javaClass);
	}

	/**
	 * Converts the first element of AnyContentType object into a Java object.
	 * The type of the Java object will be determined from the <i>xsi:type</i>
	 * attribute of the specified element.
	 * 
	 * @see #toObject(SOAPElement, Class)
	 */
	public static Object getFirstAsObject(AnyContentType any)
			throws ResourceException {
		return getFirstAsObject(any, null);
	}

	/**
	 * Converts the first element of AnyContentType object into a Java object.
	 * 
	 * @see #toObject(SOAPElement, Class)
	 */
	public static <Type> Type getFirstAsObject(AnyContentType any,
			Class<Type> javaClass) throws ResourceException {
		if (any == null || any.get_any() == null || any.get_any().length == 0) {
			return null;
		}
		return toObject(any.get_any()[0], javaClass);
	}

	/**
	 * Checks to see whether the any element is of type javaClass. The javaClass
	 * must be generated from an XML Schema representation. This operation could
	 * be seen as the instanceof equivalent for xsd:any types
	 */
	public static boolean contains(SOAPElement element, Class<?> javaClass) {
		if (element == null || javaClass == null) {
			throw new IllegalArgumentException();
		}
		if (!(element instanceof org.apache.axis.message.MessageElement)) {
			throw new IllegalArgumentException();
		}
		TypeDesc desc = TypeDesc.getTypeDescForClass(javaClass);
		if (desc == null) {
			return false;
		}
		QName qname = desc.getXmlType();
		QName type = ((org.apache.axis.message.MessageElement) element)
				.getType();
		if (type == null) {
			type = ((org.apache.axis.message.MessageElement) element)
					.getQName();
		}

		return (type != null && qname != null && type.equals(qname));
	}

	/**
	 * Deserializes input with XML into a Java object of the given type.
	 */
	public static Object deserialize(InputSource input, Class<?> javaClass)
			throws ResourceException {
		ObjectDeserializationContext deserializer = new ObjectDeserializationContext(
				input, javaClass);
		try {
			deserializer.parse();
		} catch (Exception e) {
			throw new ResourceException("Generic Deserialization Error.", e);
		}
		return deserializer.getValue();
	}

	@SuppressWarnings("unchecked")
	static public <Type> Type fromBytes(Class<Type> cl, byte[] data)
			throws ResourceException {
		ByteArrayInputStream bais = null;

		try {
			bais = new ByteArrayInputStream(data);
			return (Type) ObjectDeserializer.deserialize(new InputSource(bais),
					cl);
		} finally {
			StreamUtils.close(bais);
		}
	}

	static public org.apache.axis.message.MessageElement[] anyFromBytes(
			byte[] data) throws ResourceException {
		if (data == null)
			return null;

		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;

		try {
			bais = new ByteArrayInputStream(data);
			ois = new ObjectInputStream(bais);

			int numElements = ois.readInt();
			org.apache.axis.message.MessageElement[] ret = new org.apache.axis.message.MessageElement[numElements];
			for (int lcv = 0; lcv < ret.length; lcv++) {
				ret[lcv] = new org.apache.axis.message.MessageElement(
						(Element) ois.readObject());
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

	/**
	 * this tests to see if we leak any byte[] or char[]. intended to run under
	 * profiler.
	 */
	static public void main(String[] args) throws Throwable {
		Driver.loadClientState();

		RNSPath rooty = null;
		RNSPath currentPath = null;
		try {
			currentPath = ContextManager.getExistingContext().getCurrentPath();
			rooty = currentPath.getRoot();
		} catch (Exception e) {
			_logger.error("failed to get root EPR.");
			return;
		}

		EndpointReferenceType epr = rooty.getCachedEPR();
		if (epr == null) {
			_logger.error("failed to load root EPR; is there a grid configured?");
			return;
		}

		System.out
				.println("about to start the test; pausing 20 seconds to allow profiler to be engaged...");
		Thread.sleep(1000 * 20);
		System.out.println("now running the test...");

		for (int iter = 0; iter < 100000; iter++) {
			// the code below was snagged from the Sanitizer class, which the
			// profiler led us to
			// believe was leaking.
			try {
				byte[] eprBytes = ObjectSerializer.toBytes(epr, new QName(
						GenesisIIConstants.GENESISII_NS, "endpoint"));
				EndpointReferenceType toReturn = ObjectDeserializer.fromBytes(
						EndpointReferenceType.class, eprBytes);
				if (toReturn == null) {
					_logger.error("got a null endpoint reference.");
					return;
				}
			} catch (ResourceException e) {
				throw new RuntimeException(
						"failed to sanitize EndpointReferenceType", e);
			}
		}
		System.out
				.println("Sleeping now for 20 minutes to allow analysis...  feel free to break this.");
		Thread.sleep(1000 * 1200);

		/*
		 * results:
		 * 
		 * 2013-12-20: no memory leaks seen. memory periodically returned to
		 * near baseline, and nothing persistent was seen.
		 */
	}
}
