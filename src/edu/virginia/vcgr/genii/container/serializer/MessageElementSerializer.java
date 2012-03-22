package edu.virginia.vcgr.genii.container.serializer;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

public class MessageElementSerializer 
{
	public static MessageElement serialize(QName name, Object item) 
	{
		return new MessageElement(name, item);
	}
}
