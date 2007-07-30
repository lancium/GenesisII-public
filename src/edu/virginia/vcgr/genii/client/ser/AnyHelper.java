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
 * The <code>AnyHelper</code> is a utility that provides common functions
 * for working with <code>MessageElement</code> and beans with
 * <code>AnyContentType</code> class.
 * <b>Do not used this class for serialization or deserialization of
 * objects.</b>
 * Use {@link edu.virginia.vcgr.genii.client.ser.ObjectSerializer ObjectSerializer} and
 * {@link edu.virginia.vcgr.genii.client.ser.ObjectDeserializer ObjectDeserializer} for
 * that purposes instead.
 */
public class AnyHelper {
    static Log logger = LogFactory.getLog(AnyHelper.class.getName());

    /**
     * Populates a SOAP MessageElement array with a single object.
     * @param obj object to be serialized as a text node
     * @return content of any element as a SOAP MessageElement array
     */
    public static MessageElement[] toText(Object obj) {
        MessageElement[] result = new MessageElement[1];
        result[0] = new MessageElement(new Text(obj.toString()));
        return result;
    }

    // -------------------------------------------

    /**
     * Populates a SOAP MessageElement array with an array of arbitrary
     * objects.
     * @param obj array of objects to be serialized in the any element
     * @return content of any element as a SOAP MessageElement array
     */
    public static MessageElement[] toAnyArray(Object[] obj) {
        MessageElement[] result = new MessageElement[obj.length];
        for (int i = 0; i < obj.length; i++) {
            result[i] = toAny(obj[i]);
        }
        return result;
    }

    /**
     * Populates a SOAP MessageElement array with a single object.
     * @param obj object to be serialized in the any element
     * @return content of any element as a SOAP MessageElement array
     */
    public static MessageElement[] toAnyArray(Object obj) {
        MessageElement[] result = new MessageElement[1];
        result[0] = toAny(obj);
        return result;
    }

    /**
     * Populates a SOAP MessageElement array with a single DOM element.
     * @param element element to be inserted in the any element
     * @return content of any element as a SOAP MessageElement array
     */
    public static MessageElement[] toAnyArray(Element element) {
        MessageElement[] result = {new MessageElement(element)};
        return result;
    }

    /**
     * Populates a SOAP MessageElement with an arbitrary
     * object, and wraps it inside of a value element with an xsi:type
     * attribute. This is similar to using the xsd:any in the same way you
     * would use xsd:anyType objects.
     * @param obj object to be serialized in the any element
     * @return content of any element as a SOAP MessageElement
     */
    public static MessageElement toAnyTypeElement(Object obj) {
        MessageElement messageElement =
            new MessageElement(GenesisIIConstants.GENESISII_NS, "value", obj);
        messageElement.setType(org.apache.axis.Constants.XSD_ANYTYPE);
        return messageElement;
    }

    /**
     * Populates a SOAP MessageElement with an arbitrary
     * object.
     * @see #toAnyTypeElement(Object)
     * @param obj object to be serialized in the any element.
     * @return content of any element as a SOAP MessageElement
     */
    public static MessageElement toAny(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof MessageElement) {
            return (MessageElement)obj;
        } else if (obj instanceof Element) {
            return new MessageElement((Element) obj);
        }

        return toAnyTypeElement(obj);
    }

    // ------------------------

    public static MessageElement getParent(MessageElement element) {
        return (element == null) ?
            null :
            (MessageElement)element.getParentElement();
    }

    public static MessageElement getParent(MessageElement [] elements) {
        return (elements != null && elements.length > 0) ?
            getParent(elements[0]) :
            null;
    }

    public static MessageElement getParent(AnyContentType any) {
        return (any != null) ? getParent(any.get_any()) : null;
    }

    public static void setAny(AnyContentType object, SOAPElement value) {
        if (value == null || object == null) {
            return;
        }
        if (!(value instanceof MessageElement)) {
            throw new IllegalArgumentException();
        }
        object.set_any( new MessageElement[]{(MessageElement)value} );
    }

    public static void setAny(AnyContentType object, SOAPElement [] values) {
        if (values == null || object == null) {
            return;
        }
        MessageElement [] me = null;
        if (values instanceof MessageElement[]) {
            me = (MessageElement[])values;
        } else {
            me = new MessageElement[values.length];
            for (int i=0;i<values.length;i++) {
                if (values[i] instanceof MessageElement) {
                    me[i] = (MessageElement)values[i];
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }
        object.set_any(me);
    }

    public static void setAny(AnyContentType object, List values) {
        if (values == null) {
            return;
        }
        Object obj;
        MessageElement [] v = new MessageElement[values.size()];
        for (int i=0;i<values.size();i++) {
            obj = values.get(i);
            if (obj instanceof MessageElement) {
                v[i] = (MessageElement)obj;
            } else {
                throw new IllegalArgumentException();
            }
        }
        object.set_any( v );
    }

    public static void write(Writer writer, MessageElement element)
        throws Exception {
        MessageContext messageContext = Config.mooch();
        SerializationContext context =
            new SerializationContext(writer, messageContext);
        context.setPretty(true);
        element.output(context);
        writer.write('\n');
    }

    // ********* toString **********

    /**
     * Converts a SOAP MessageElement to an XML String representation
     * @param element SOAP MessageElement to be converted
     * @return String in XML format representing the input
     */
    public static String toString(MessageElement element)
        throws Exception {
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
    public static String[] toString(MessageElement[] elements)
        throws Exception {
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
     *
     */
    public static String toSingleString(MessageElement[] elements)
        throws Exception {
        if (elements == null) {
            return null;
        }
        MessageContext messageContext = Config.mooch();
        StringWriter writer = new StringWriter();
        SerializationContext context =
            new SerializationContext(writer, messageContext);
        context.setPretty(true);
        for (int i = 0; i < elements.length; i++) {
            elements[i].output(context);
        }
        writer.flush();
        return writer.toString();
    }

    /**
     *
     */
    public static String toSingleString(AnyContentType any)
        throws Exception {
        return (any == null) ? null : toSingleString(any.get_any());
    }

    /**
     * Converts type containing any element to a String,
     * representing the parent MessageElement.
     * @see #toString(MessageElement element)
     */
    public static String getFirstParentAsString(AnyContentType any)
        throws Exception {
        return toString(getParent(any));
    }

    // ****** toElement ********

    /**
     * Converts a SOAP MessageElement to a DOM Element representation
     * @param element SOAP MessageElement to be converted
     * @return DOM Element representing the input
     * @throws Exception if the DOM Element could not be created
     */
    public static Element toElement(MessageElement element)
        throws Exception {
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
    public static Element[] toElement(MessageElement[] elements)
        throws Exception {
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
     * @see #toElement(MessageElement element)
     */
    public static Element[] toElement(AnyContentType any)
        throws Exception {
        if (any == null) {
            return null;
        }
        return toElement(any.get_any());
    }

    /**
     * Converts type containing any element to a single DOM Element,
     * representing the parent MessageElement.
     * @see #toElement(MessageElement element)
     */
    public static Element getFirstParentAsElement(AnyContentType any)
        throws Exception {
        return toElement(getParent(any));
    }

    /**
     * Converts type containing any element to a single DOM Element.
     * @see #toElement(MessageElement element)
     */
    public static Element getFirstAsElement(AnyContentType any)
        throws Exception {
        Element[] values = toElement(any);
        return (values != null && values.length > 0) ? values[0] : null;
    }

}
