package edu.virginia.vcgr.genii.client.comm.axis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.encoding.SerializationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.virginia.vcgr.genii.client.ser.AnyHelper;

/**
 * a wrapper for the MessageElement class used by Apache Axis. this is a boundary between the
 * low-level axis layers and the genesis II code.
 * 
 * @author Chris Koeritz
 * @copyright Copyright (c) 2014-$now By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
public class MessageElement
{
	static private Log _logger = LogFactory.getLog(MessageElement.class);

	/* the axis message element that we're hoping will do all the work. */
	private org.apache.axis.message.MessageElement _realElem = null;

	/*
	 * records whether the object was created within our own application. if this is false, then the
	 * object can be assumed to be created elsewhere.
	 */
	private boolean _createdLocally;

	private enum ElementalBehaviors {
		/*
		 * this clones the axis MessageElement in order to lose any references to soap messages.
		 */
		CLONE_NODE,

		/*
		 * causes the message element to be accepted as is, without cleaning it up. this is
		 * necessary if the element must retain its original structure, but it can also lead to
		 * leaks if used incautiously.
		 */
		DONT_CLONE
	}

	public MessageElement()
	{
		_realElem = new org.apache.axis.message.MessageElement();
		_createdLocally = true;
	}

	public MessageElement(org.apache.axis.message.MessageElement elem)
	{
		consumeElement(ElementalBehaviors.CLONE_NODE, elem);
	}

	public MessageElement(Element elem)
	{
		if (elem instanceof org.apache.axis.message.MessageElement) {
			consumeElement(ElementalBehaviors.CLONE_NODE, (org.apache.axis.message.MessageElement) elem);
		} else {
			consumeElement(ElementalBehaviors.CLONE_NODE, new org.apache.axis.message.MessageElement(elem));
		}
	}

	public MessageElement(String namespace, String localPart)
	{
		_realElem = new org.apache.axis.message.MessageElement(namespace, localPart);
		_createdLocally = true;
	}

	public MessageElement(String localPart, String prefix, String namespace)
	{
		_realElem = new org.apache.axis.message.MessageElement(localPart, prefix, namespace);
		_createdLocally = true;
	}

	public MessageElement(Name eltName)
	{
		_realElem = new org.apache.axis.message.MessageElement(eltName);
		_createdLocally = true;
	}

	public MessageElement(String namespace, String localPart, Object value)
	{
		_realElem = new org.apache.axis.message.MessageElement(namespace, localPart, value);
		_createdLocally = true;
	}

	public MessageElement(QName name)
	{
		_realElem = new org.apache.axis.message.MessageElement(name);
		_createdLocally = true;
	}

	public MessageElement(QName name, Object value)
	{
		_realElem = new org.apache.axis.message.MessageElement(name, value);
		_createdLocally = true;
	}

	public MessageElement(CharacterData text)
	{
		_realElem = new org.apache.axis.message.MessageElement(text);
		_createdLocally = true;
	}

	public org.apache.axis.message.MessageElement getReal()
	{
		return _realElem;
	}

	/**
	 * handles our special flag for deciding on the axis element we'll hold onto.
	 */
	private void consumeElement(ElementalBehaviors mode, org.apache.axis.message.MessageElement toManage)
	{
		if (toManage == null) {
			String msg = "cannot create a message element wrapper with a null axis object";
			_logger.error(msg);
			throw new RuntimeException(msg);
		}

		_realElem = toManage;
		if (mode == ElementalBehaviors.DONT_CLONE) {
			// do nothing.
			_createdLocally = false;
		} else if (mode == ElementalBehaviors.CLONE_NODE) {
			cloneElement();
			// we cloned it, so now we consider it to be created locally. it does not need to be
			// cloned again.
			_createdLocally = true;
		} else {
			String msg = "unknown special constructor mode for MessageElement.";
			_logger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	/**
	 * attempts to free the message element of any lingering connections to other large instantiated
	 * xml structures.
	 */
	public void cloneElement()
	{
		if (getReal() == null)
			return; // should never see this, but we can't clone a null anyhow.

		// hmmm: not working yet; clone seems to be causing null pointers. trying without it to
		// validate
		// clean replacement first.
		// hmmm: works as long as the cloning is turned off. so, still not working right in the
		// current
		// set of callers. need to figure out the usage cases where we can and cannot clone.

		// hmmm: off right now.
		// _realElem = (org.apache.axis.message.MessageElement) _realElem.cloneNode(true);

		// hmmm: trying false for deep, since we see bugs related to some things not being deep
		// copied. will that really free us from memory corpulence as desired? will it even work at
		// all? after checking, even a shallow clone causes problems still. ARGH!

		// // trying a different approach; serialize it and then deserialize it.
		// // this also is probably wrong plan.
		// // seems to work great, except element cannot be used after this, gets:
		// org.apache.axis.message.Text cannot be cast to org.apache.axis.message.MessageElement
		// // which is due to java.lang.Exception: No deserialization context to use in
		// MessageElement.getValueAsType()! at
		// org.apache.axis.message.MessageElement.getValueAsType(MessageElement.java:900)
		// try {
		// org.apache.axis.message.MessageElement[] convert = new
		// org.apache.axis.message.MessageElement[] { _realElem };
		// byte[] bytes = ObjectSerializer.anyToBytes(convert);
		// _realElem = null; // drop old pointer on floor just to be definitive.
		// org.apache.axis.message.MessageElement[] cleanly =
		// ObjectDeserializer.anyFromBytes(bytes);
		// _realElem = cleanly[0];
		// } catch (Throwable t) {
		// String msg = "failed to transmogrify MessageElement using serialization.";
		// _logger.error(msg, t);
		// throw new RuntimeException(msg, t);
		// }

		// // this tries serializing the message element out to xml text and back. this really
		// // should work.
		// // it does not; it gets: failure to convert message element for serialization,
		// org.apache.axis.message.Text cannot be cast to org.apache.axis.message.MessageElement
		// // exception occurred in makeEPR, java.lang.NullPointerException, at
		// edu.virginia.vcgr.genii.client.rp.DefaultSingleResourcePropertyTranslator.deserialize(DefaultSingleResourcePropertyTranslator.java:23)
		// //at
		// edu.virginia.vcgr.genii.client.rp.RPInvoker$SingleGetterHandler.handle(RPInvoker.java:107)
		// try {
		// Node nodeToConvert = _realElem.getAsDOM();
		// org.apache.axis.message.MessageElement placeHolder = new
		// org.apache.axis.message.MessageElement();
		// Document doc = placeHolder.getOwnerDocument();
		// NodeImpl converted = (NodeImpl) doc.importNode(nodeToConvert.getLastChild(), true);
		// _realElem = (org.apache.axis.message.MessageElement) converted;
		// } catch (Throwable t) {
		// String msg = "failure to convert message element for serialization";
		// _logger.error(msg, t);
		// throw new RuntimeException(msg, t);
		// }
	}

	/**
	 * a frequently used pattern, where we want to pass axis a list of message elements with zero
	 * length.
	 */
	public static org.apache.axis.message.MessageElement[] getEmptyArray()
	{
		return new org.apache.axis.message.MessageElement[0];
	}

	/**
	 * returns a list with exactly one axis version of the MessageElement in it. the message element
	 * comes from conversion of the object passed in.
	 */
//	public static MessageElement[] objectToArray(Object toElement)
//	{
//		MessageElement[] result = new MessageElement[1];
//		result[0] = AnyHelper.toAny(toElement);
//		return result;
//	}

	/**
	 * a simple helper method that spits out an array containing the one element that is passed in.
	 */
	public static MessageElement[] unitaryArray(MessageElement toEncapsulate)
	{
		return new MessageElement[] { toEncapsulate };
	}

	/**
	 * converts an array of our message elements into the axis equivalent.
	 */
	static public org.apache.axis.message.MessageElement[] toAxisArray(MessageElement[] toConvert)
	{
		if (toConvert == null)
			return null;
		if (toConvert.length == 0)
			return new org.apache.axis.message.MessageElement[0];
		org.apache.axis.message.MessageElement[] toReturn = new org.apache.axis.message.MessageElement[toConvert.length];
		int indy = 0;
		for (MessageElement e : toConvert) {
			toReturn[indy++] = e.getReal();
		}
		return toReturn;
	}

	/**
	 * converts a collection of our message elements into an array of them.
	 */
	static public MessageElement[] toArray(Collection<MessageElement> toConvert)
	{
		if (toConvert == null)
			return null;
		if (toConvert.size() == 0)
			return new MessageElement[0];
		MessageElement[] toReturn = new MessageElement[toConvert.size()];
		int indy = 0;
		for (MessageElement e : toConvert) {
			toReturn[indy++] = e;
		}
		return toReturn;
	}

	/**
	 * converts a collection of clean message elements into the axis equivalent.
	 */
	static public org.apache.axis.message.MessageElement[] toAxisArray(Collection<MessageElement> toConvert)
	{
		if (toConvert == null)
			return null;
		if (toConvert.size() == 0)
			return new org.apache.axis.message.MessageElement[0];
		org.apache.axis.message.MessageElement[] toReturn = new org.apache.axis.message.MessageElement[toConvert.size()];
		int indy = 0;
		for (MessageElement e : toConvert) {
			toReturn[indy++] = e.getReal();
		}
		return toReturn;
	}

	/**
	 * converts an array of axis message elements into an array of our wrapper objects.
	 */
	static public MessageElement[] fromAxisArray(org.apache.axis.message.MessageElement[] toConvert)
	{
		if (toConvert == null)
			return null;
		if (toConvert.length == 0)
			return new MessageElement[0];

		MessageElement[] toReturn = new MessageElement[toConvert.length];
		int indy = 0;
		for (org.apache.axis.message.MessageElement e : toConvert) {
			toReturn[indy++] = new MessageElement(e);
		}
		return toReturn;
	}

	/* below are boiler-plate functions that make our class look closer to the real one. */

	public void addChild(MessageElement el) throws SOAPException
	{
		getReal().addChild(el.getReal());
		// our children can affect our state.
		if (!el._createdLocally)
			_createdLocally = false;
	}

	public void addChild(Element el) throws SOAPException
	{
		if (el instanceof org.apache.axis.message.MessageElement) {
			getReal().addChild((org.apache.axis.message.MessageElement) el);
		} else {
			String msg = "cannot add child element other than base axis MessageElement";
			_logger.error(msg);
			throw new RuntimeException(msg);
		}
		_createdLocally = false;
	}

	public SOAPElement addTextNode(String s) throws SOAPException
	{
		return getReal().addTextNode(s);
	}

	public Node appendChild(Node nodeRepresentation)
	{
		Node toReturn = getReal().appendChild(nodeRepresentation);
		_createdLocally = false;
		return toReturn;
	}

	public QName getQName()
	{
		return getReal().getQName();
	}

	@SuppressWarnings("rawtypes")
	public List getChildren()
	{
		return getReal().getChildren();
	}

	/**
	 * slightly different from original which returns a List (without generic arguments); we switch
	 * to an array automatically since we mainly use that.
	 */
	public org.apache.axis.message.MessageElement[] getOurChildren()
	{
		if (getReal().getChildren() == null)
			return null;
		return (org.apache.axis.message.MessageElement[]) (getReal().getChildren().toArray());
	}

	public void setAttributeNS(String namespaceURI, String qualifiedName, String value)
	{
		getReal().setAttributeNS(namespaceURI, qualifiedName, value);
	}

	public void setType(QName xmlType)
	{
		getReal().setType(xmlType);
	}

	public String getNamespaceURI(String prefix)
	{
		return getReal().getNamespaceURI(prefix);
	}

	public DeserializationContext getDeserializationContext()
	{
		return getReal().getDeserializationContext();
	}

	public Node getFirstChild()
	{
		return getReal().getFirstChild();
	}

	public String getName()
	{
		return getReal().getName();
	}

	public Object getObjectValue()
	{
		return getReal().getObjectValue();
	}

	@SuppressWarnings("rawtypes")
	public Object getObjectValue(Class class1) throws Exception
	{
		return getReal().getObjectValue(class1);
	}

	public Document getOwnerDocument()
	{
		return getReal().getOwnerDocument();
	}

	public QName getType()
	{
		return getReal().getType();
	}

	public String getValue()
	{
		return getReal().getValue();
	}

	public Object getValueAsType(QName qName) throws Exception
	{
		return getReal().getValueAsType(qName);
	}

	@SuppressWarnings("rawtypes")
	public Object getValueAsType(QName type, Class class1) throws Exception
	{
		return getReal().getValueAsType(type, class1);
	}

	public void output(SerializationContext context) throws Exception
	{
		getReal().output(context);
	}

	public String getPrefix(String ns)
	{
		return getReal().getPrefix(ns);
	}

	public boolean isDirty()
	{
		return getReal().isDirty();
	}

	public MessageElement getChildElement(QName qName)
	{
		org.apache.axis.message.MessageElement kid = getReal().getChildElement(qName);
		if (kid == null)
			return null;
		return new MessageElement(kid);
	}

	/**
	 * note that this is an iterator over a list of _our_ MessageElement objects, not across the
	 * base class objects.
	 */
	public Iterator<MessageElement> getChildElements()
	{
		ArrayList<MessageElement> strayCats = new ArrayList<MessageElement>();
		@SuppressWarnings("unchecked")
		Iterator<org.apache.axis.message.MessageElement> kids = getReal().getChildElements();
		while (kids.hasNext()) {
			strayCats.add(new MessageElement(kids.next()));
		}
		return strayCats.iterator();
	}

	public String getAttribute(String propertyNameAttribute)
	{
		return getReal().getAttribute(propertyNameAttribute);
	}

	public Element getAsDOM() throws Exception
	{
		return getReal().getAsDOM();
	}

	public NodeList getChildNodes()
	{
		return getReal().getChildNodes();
	}

	public void addChild(org.apache.axis.message.MessageElement toAdd) throws SOAPException
	{
		getReal().addChild(toAdd);
	}

	public void setQName(QName name)
	{
		getReal().setQName(name);
	}

	public void setAttribute(String wsuNs, String string, String wsuId)
	{
		getReal().setAttribute(wsuNs, string, wsuId);
	}

	public void setObjectValue(Object obj) throws SOAPException
	{
		getReal().setObjectValue(obj);
	}

	public void setAttribute(String creationPropertyNameAttr, String key)
	{
		getReal().setAttribute(creationPropertyNameAttr, key);
	}

	public void setValue(String string)
	{
		getReal().setValue(string);
	}

	public void addAttribute(String nsPrefixSchemaXsi, String uriDefaultSchemaXsi, String string, String string2)
	{
		getReal().addAttribute(nsPrefixSchemaXsi, uriDefaultSchemaXsi, string, string2);
	}

	public SOAPElement addNamespaceDeclaration(String prefix, String ns) throws SOAPException
	{
		return getReal().addNamespaceDeclaration(prefix, ns);
	}

	public String getLocalName()
	{
		return getReal().getLocalName();
	}

	public String getNamespaceURI()
	{
		return getReal().getNamespaceURI();
	}

	public String getAttributeValue(String string)
	{
		return getReal().getAttributeValue(string);
	}

	public String getAsString() throws Exception
	{
		return getReal().getAsString();
	}
}
