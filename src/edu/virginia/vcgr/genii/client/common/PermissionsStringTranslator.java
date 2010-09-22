package edu.virginia.vcgr.genii.client.common;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.fsii.security.Permissions;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.SingleResourcePropertyTranslator;

public class PermissionsStringTranslator implements
		SingleResourcePropertyTranslator
{
	@Override
	public <Type> Type deserialize(Class<Type> clazz, MessageElement element)
			throws ResourcePropertyException
	{
		if (!clazz.equals(Permissions.class))
			throw new ResourcePropertyException(
				"The PermissionsStringTranslator can ONLY be used to translate " +
				"Permission string resource properties.");
		
		if (element == null)
			return null;
		
		try
		{
			String pString = element.getValue();
			return clazz.cast(new Permissions(pString));
		}
		catch (Exception e)
		{
			throw new ResourcePropertyException(
				"Unable to extract permissions from property.", e);
		}
	}

	@SuppressWarnings("unused")
	@Override
	public MessageElement serialize(QName name, Object obj)
			throws ResourcePropertyException
	{
		if (!(obj instanceof Permissions))
			throw new ResourcePropertyException(
				"The PermissionsStringTranslator can ONLY be used to translate " +
				"Permissions resource properties.");
		
		Permissions perms = (Permissions)obj;
		if (perms == null)
			return null;
		
		return new MessageElement(name, perms.toString());
	}
}