package edu.virginia.vcgr.genii.client.rp;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

/**
 * This is the default resource property translator used for
 * single-valued resource properties.
 * 
 * @author mmm2a
 */
public class DefaultSingleResourcePropertyTranslator implements
		SingleResourcePropertyTranslator
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <Type> Type deserialize(Class<Type> clazz, MessageElement element)
			throws ResourcePropertyException
	{
		try
		{
			return clazz.cast(ObjectDeserializer.toObject(element, clazz));
		}
		catch (ResourceException re)
		{
			throw new ResourcePropertyException(
				"Unable to deserialize resource property " +
				element.getQName(), re);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageElement serialize(QName name, Object obj)
			throws ResourcePropertyException
	{
		return new MessageElement(name, obj);
	}
}