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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.StringWriter;

import org.apache.axis.Constants;
import org.apache.axis.message.MessageElement;

import org.morgan.util.io.StreamUtils;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

/**
 * Converts Java Objects to DOM Elements and SOAP Elements.
 * The objects must be compliant with the Axis Bean model, i.e. generated using
 * the WSDL2Java tool from an XML Schema definition or must be of simple type.
 */
public class ObjectSerializer {

    public static SOAPElement toSOAPElement(Object obj)
        throws ResourceException {
        return toSOAPElement(obj, null, false);
    }

    public static SOAPElement toSOAPElement(Object obj, QName name)
        throws ResourceException {
        return toSOAPElement(obj, name, false);
    }

    /**
     * Populates a SOAPElement with an arbitrary
     * object. The object will get wrapped inside of an element
     * named after the qname parameter.
     *
     * @param obj object to be serialized in the any element
     * @param name name of element the value should be wrapped inside
     * @return content of any element as a SOAPElement
     * @throws ResourceException if the object cannot be put
     *                  in a MessageElement
     */
    public static SOAPElement toSOAPElement(Object obj,
                                            QName name,
                                            boolean nillable)
        throws ResourceException {
        if (obj instanceof MessageElement) {
            MessageElement element = (MessageElement)obj;
            if (name == null || name.equals(element.getQName())) {
                return element;
            } else {
                throw new ResourceException("Not Implemented.");
            }
        } else if (obj instanceof Element) {
            Element element = (Element)obj;
            if (name == null ||
                (name.getLocalPart().equals(element.getLocalName()) &&
                 name.getNamespaceURI().equals(element.getNamespaceURI()))) {
                return new MessageElement((Element)obj);
            } else {
                throw new ResourceException("Not Implemented.");
            }
        }

        if (name == null) {
            throw new IllegalArgumentException(
				"Null argument for name parameter.");
        }

        MessageElement messageElement = new MessageElement();
        messageElement.setQName(name);
        try {
            messageElement.setObjectValue(obj);
        } catch (Exception e) {
            throw new ResourceException("Generic Serialization Error.", e);
        }
        if (obj == null && nillable) {
            try {
                messageElement.addAttribute(Constants.NS_PREFIX_SCHEMA_XSI,
                                            Constants.URI_DEFAULT_SCHEMA_XSI,
                                            "nil",
                                            "true");
            } catch (Exception e) {
                throw new ResourceException("Generic Serialization Error.", e);
            }
        }
        return messageElement;
    }

    public static Element toElement(Object obj)
        throws ResourceException {
        return toElement(obj, null, false);
    }

    public static Element toElement(Object obj, QName name)
        throws ResourceException {
        return toElement(obj, name, false);
    }

    public static Element toElement(Object obj,
                                    QName name,
                                    boolean nillable)
        throws ResourceException {
        if (obj instanceof MessageElement) {
            MessageElement messageElement = (MessageElement)obj;
            if (name == null || name.equals(messageElement.getQName())) {
                Element element = null;
                try {
                    element = AnyHelper.toElement(messageElement);
                } catch (Exception e) {
                    throw new ResourceException("Generic Serialization Error.",
						e);
                }
                return element;
            } else {
                throw new ResourceException("Generic Serialization Error.");
            }
        } else if (obj instanceof Element) {
            Element element = (Element)obj;
            if (name == null ||
                (name.getLocalPart().equals(element.getLocalName()) &&
                 name.getNamespaceURI().equals(element.getNamespaceURI()))) {
                return element;
            } else {
                throw new ResourceException("Not Implemented.");
            }
        }

        MessageElement messageElement =
            (MessageElement)toSOAPElement(obj, name, nillable);
        try {
            return AnyHelper.toElement(messageElement);
        } catch (Exception e) {
            throw new ResourceException("Generic Serialization Error.", e);
        }
    }

    public static String toString(Object obj)
        throws ResourceException {
        return toString(obj, null, false);
    }

    public static String toString(Object obj, QName name)
        throws ResourceException {
        return toString(obj, name, false);
    }

    public static String toString(Object obj, QName name, boolean nillable)
        throws ResourceException {
        MessageElement messageElement =
            (MessageElement)toSOAPElement(obj, name, nillable);
        try {
            return AnyHelper.toString(messageElement);
        } catch (Exception e) {
            throw new ResourceException("Generic Serialization Error.", e);
        }
    }

    public static void serialize(Writer writer, Object obj, QName name) 
        throws ResourceException {
        serialize(writer, obj, name, false);

    }

    public static void serialize(Writer writer, Object obj, 
                                 QName name, boolean nillable) 
        throws ResourceException {
        SOAPElement soapElement = 
            ObjectSerializer.toSOAPElement(obj, name, nillable);
        try {
            AnyHelper.write(writer, (MessageElement)soapElement);
        } catch (Exception e) {
            throw new ResourceException("Generic Serialization Error.", e);
        }
    }

    public static Object clone(Object obj)
        throws ResourceException {
        StringWriter writer = new StringWriter();
        ObjectSerializer.serialize(writer, obj, CLONE_QNAME);
        writer.flush();
        StringBufferReader reader = new StringBufferReader(writer.getBuffer());
        InputSource input = new InputSource(reader);
        return ObjectDeserializer.deserialize(input, obj.getClass());
    }

    private static final QName CLONE_QNAME = 
        new QName("http://globus.org", "cloneElement");

    static public <Type> byte[] toBytes(Type obj, QName name)
		throws ResourceException
	{
    	ByteArrayOutputStream baos = null;
	
    	if (obj == null)
    		return null;
    	
    	try
    	{
    		baos = new ByteArrayOutputStream();
    		OutputStreamWriter writer =  new OutputStreamWriter(baos);
    		ObjectSerializer.serialize(writer, obj, name);
    		writer.flush();
    		writer.close();
		
    		return baos.toByteArray();
    	}
    	catch (IOException ioe)
    	{
    		throw new ResourceException(ioe.toString(), ioe);
    	}
    	finally
    	{
    		StreamUtils.close(baos);
    	}
	}
    
    static public byte[] anyToBytes(MessageElement []any)
    	throws ResourceException
	{
    	if (any == null)
    		return null;
    	
    	ByteArrayOutputStream baos = null;
    	ObjectOutputStream oos = null;
    	
    	try
    	{
    		oos = new ObjectOutputStream(baos = new ByteArrayOutputStream());
    		oos.writeInt(any.length);
    		for (MessageElement elem : any)
    		{
    			oos.writeObject(elem.getAsDOM());
    		}
    		
    		oos.flush();
    		return baos.toByteArray();
    	}
    	catch (ResourceException e)
    	{
    		throw e;
    	}
    	catch (Exception e)
    	{
    		throw new ResourceException(e.getLocalizedMessage(), e);
    	}
    	finally
    	{
    		StreamUtils.close(oos);
    	}
	}
}