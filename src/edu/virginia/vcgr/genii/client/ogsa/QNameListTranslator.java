package edu.virginia.vcgr.genii.client.ogsa;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.SingleResourcePropertyTranslator;

public class QNameListTranslator implements SingleResourcePropertyTranslator
{
	@Override
	public <Type> Type deserialize(Class<Type> clazz, MessageElement element)
			throws ResourcePropertyException
	{
		if (!clazz.equals(OGSAQNameList.class))
			throw new ResourcePropertyException(
				"The QNameListTranslator can ONLY be used to translate " +
				"QNameList resource properties.");
		
		OGSAQNameList list = new OGSAQNameList(element);
		return clazz.cast(list);
	}

	@Override
	public MessageElement serialize(QName name, Object obj)
			throws ResourcePropertyException
	{
		if (!(obj instanceof OGSAQNameList))
			throw new ResourcePropertyException(
				"The QNameListTranslator can ONLY be used to translate " +
				"QNameList resource properties.");
		
		try
		{
			return ((OGSAQNameList)obj).toMessageElement(name);
		}
		catch (SOAPException se)
		{
			throw new ResourcePropertyException(
				"Unable to create resource property " + name, se);
		}
	}
}