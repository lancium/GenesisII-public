package edu.virginia.vcgr.genii.client.rp;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

public interface SingleResourcePropertyTranslator
	extends ResourcePropertyTranslator
{
	public MessageElement serialize(QName name, Object obj)
		throws ResourcePropertyException;
	public <Type> Type deserialize(Class<Type> clazz, MessageElement element)
		throws ResourcePropertyException;
}
