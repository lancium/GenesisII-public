package edu.virginia.vcgr.genii.cloud;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.SingleResourcePropertyTranslator;

public class CloudStatusRPTranslater 
implements SingleResourcePropertyTranslator {

	@Override
	public <Type> Type deserialize(Class<Type> clazz, MessageElement element)
	throws ResourcePropertyException
	{
		if (!CloudStat.class.isAssignableFrom(clazz))
			throw new ResourcePropertyException(
					"Unable to translate from type \"" +
					clazz.getName() + "\" to CloudStat.");

		return clazz.cast(CloudStat.fromMessageElement(element));
	}

	@Override
	public MessageElement serialize(QName name, Object obj)
	throws ResourcePropertyException
	{
		if (!(obj instanceof CloudStat))
			throw new ResourcePropertyException(
					"Unable to translate from type \"" +
					obj.getClass().getName() + "\" to CloudStat.");

		return ((CloudStat)obj).toMessageElement(name);
	}

}
