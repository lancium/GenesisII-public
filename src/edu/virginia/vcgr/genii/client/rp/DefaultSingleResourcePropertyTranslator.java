package edu.virginia.vcgr.genii.client.rp;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

public class DefaultSingleResourcePropertyTranslator implements
		SingleResourcePropertyTranslator
{
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

	@Override
	public MessageElement serialize(QName name, Object obj)
			throws ResourcePropertyException
	{
		return new MessageElement(name, obj);
	}
}