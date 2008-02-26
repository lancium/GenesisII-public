package edu.virginia.vcgr.genii.client.rp;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

public interface MultiResourcePropertyTranslator
	extends ResourcePropertyTranslator
{
	public Collection<MessageElement> serialize(
		QName name, Collection<Object> obj)
			throws ResourcePropertyException;
	public <Type> Collection<Type> deserialize(
		Class<Type> clazz, Collection<MessageElement> element)
			throws ResourcePropertyException;
}
