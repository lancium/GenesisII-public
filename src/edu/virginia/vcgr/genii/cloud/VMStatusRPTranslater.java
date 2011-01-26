package edu.virginia.vcgr.genii.cloud;

import javax.xml.namespace.QName;
import org.apache.axis.message.MessageElement;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.SingleResourcePropertyTranslator;

public class VMStatusRPTranslater 
implements SingleResourcePropertyTranslator{

	

	
	@Override
	public MessageElement serialize(QName name, Object obj)
			throws ResourcePropertyException {
		if (!(obj instanceof VMStats))
			throw new ResourcePropertyException(
					"Unable to translate from type \"" +
					obj.getClass().getName() + "\" to VMStats.");

		return ((VMStats)obj).toMessageElement(name);
	}

	@Override
	public <Type> Type deserialize(Class<Type> clazz, MessageElement element)
			throws ResourcePropertyException {
		if (!VMStats.class.isAssignableFrom(clazz))
			throw new ResourcePropertyException(
					"Unable to translate from type \"" +
					clazz.getName() + "\" to VMStats.");

		return clazz.cast(VMStats.fromMessageElement(element));
	}


	
	
}
