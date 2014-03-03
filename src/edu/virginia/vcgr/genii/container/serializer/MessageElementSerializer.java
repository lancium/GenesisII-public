package edu.virginia.vcgr.genii.container.serializer;

import javax.xml.namespace.QName;

public class MessageElementSerializer {
	public static org.apache.axis.message.MessageElement serialize(QName name,
			Object item) {
		return new org.apache.axis.message.MessageElement(name, item);
	}
}
