package edu.virginia.vcgr.genii.client.bes;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.SingleResourcePropertyTranslator;
import edu.virginia.vcgr.genii.container.bes.BESPolicy;

public class BESPolicyRPTranslater implements SingleResourcePropertyTranslator
{
	@Override
	public <Type> Type deserialize(Class<Type> clazz, MessageElement element) throws ResourcePropertyException
	{
		if (!BESPolicy.class.isAssignableFrom(clazz))
			throw new ResourcePropertyException("Unable to translate from type \"" + clazz.getName() + "\" to BESPolicy.");

		return clazz.cast(BESPolicy.fromMessageElement(element));
	}

	@Override
	public MessageElement serialize(QName name, Object obj) throws ResourcePropertyException
	{
		if (!(obj instanceof BESPolicy))
			throw new ResourcePropertyException("Unable to translate from type \"" + obj.getClass().getName()
				+ "\" to BESPolicy.");

		return ((BESPolicy) obj).toMessageElement(name);
	}
}