package edu.virginia.vcgr.genii.client.rp;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

/**
 * This interface represents the methods that a multi-valued
 * resource property translator MUST implement in order to
 * be used as an RP translator.
 * 
 * @author mmm2a
 */
public interface MultiResourcePropertyTranslator
	extends ResourcePropertyTranslator
{
	/**
	 * Serialize a collection of elements into XML using the provided
	 * XML QName.
	 * 
	 * @param name The name of the resource property to create an XML
	 * representation for.
	 * @param obj THe collection of objects to "serialize".
	 * 
	 * @return THe collection of XML elements that represent the given
	 * objects.
	 * 
	 * @throws ResourcePropertyException
	 */
	public Collection<MessageElement> serialize(
		QName name, Collection<Object> obj)
			throws ResourcePropertyException;
	
	/**
	 * Deserialize a collection of XML elements into their Java object
	 * components.
	 * 
	 * @param <Type> The Java type created from this deserialization process.
	 * @param clazz The class of the type to use for deserialization (this is
	 * used by Apache Axis to determine the structure of the corresponding
	 * elements).
	 * @param element THe collectionof XML elements to deserialize.
	 * 
	 * @return The collection of java objects represented by the given XML.
	 * 
	 * @throws ResourcePropertyException
	 */
	public <Type> Collection<Type> deserialize(
		Class<Type> clazz, Collection<MessageElement> element)
			throws ResourcePropertyException;
}