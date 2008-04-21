package edu.virginia.vcgr.genii.client.rp;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

/**
 * This is a resource property translator interface that is used for
 * any translator that can translate single XML elements to and from
 * resource property objects.
 * 
 * @author mmm2a
 */
public interface SingleResourcePropertyTranslator
	extends ResourcePropertyTranslator
{
	/**
	 * Given an XML QName and a resource property value, serialize that
	 * value into an XML element.
	 * 
	 * @param name The name of the resource property (and element to create).
	 * @param obj The java object representing the resource property value.
	 * 
	 * @return The newly serialized XML element.
	 * 
	 * @throws ResourcePropertyException
	 */
	public MessageElement serialize(QName name, Object obj)
		throws ResourcePropertyException;

	/**
	 * Given an XML element, deserialize that element into its Java object
	 * representation.
	 * 
	 * @param <Type> The type of java object that the resource property
	 * represents.
	 * @param clazz The class of the resource property (used by Apache Axis
	 * for deserialization).
	 * @param element The XML element to deserialize.
	 * 
	 * @return The newly deserialized resource property value.
	 * 
	 * @throws ResourcePropertyException
	 */
	public <Type> Type deserialize(Class<Type> clazz, MessageElement element)
		throws ResourcePropertyException;
}
