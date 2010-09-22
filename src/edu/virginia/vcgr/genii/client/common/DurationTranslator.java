package edu.virginia.vcgr.genii.client.common;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.Duration;

import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.SingleResourcePropertyTranslator;

public class DurationTranslator 
	implements SingleResourcePropertyTranslator
{
	@Override
	public <Type> Type deserialize(Class<Type> clazz, MessageElement element)
			throws ResourcePropertyException
	{
		if (!clazz.equals(edu.virginia.vcgr.genii.client.utils.units.Duration.class))
			throw new ResourcePropertyException(
				"The DurationTranslator can ONLY be used to translate " +
				"Duration resource properties.");
		
		if (element == null)
			return null;
		
		try
		{
			Duration aDur = (Duration)element.getObjectValue(Duration.class);
			edu.virginia.vcgr.genii.client.utils.units.Duration gDur =
				edu.virginia.vcgr.genii.client.utils.units.Duration.fromApacheDuration(aDur);
			return clazz.cast(gDur);
		}
		catch (Exception e)
		{
			throw new ResourcePropertyException(
				"Unable to extract duration from property.", e);
		}
	}

	@SuppressWarnings("unused")
	@Override
	public MessageElement serialize(QName name, Object obj)
			throws ResourcePropertyException
	{
		if (!(obj instanceof edu.virginia.vcgr.genii.client.utils.units.Duration))
			throw new ResourcePropertyException(
				"The DurationTranslator can ONLY be used to translate " +
				"Duration resource properties.");
		
		edu.virginia.vcgr.genii.client.utils.units.Duration gDur =
			(edu.virginia.vcgr.genii.client.utils.units.Duration)obj;
		if (gDur == null)
			return null;
		
		return new MessageElement(name, gDur.toApacheDuration());
	}
}