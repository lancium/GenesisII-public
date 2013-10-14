package edu.virginia.vcgr.genii.client.rp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

/**
 * This is the default resource property translator used for multi-valued resource properties.
 * 
 * @author mmm2a
 */
public class DefaultMultiResourcePropertyTranslator implements MultiResourcePropertyTranslator
{
	/**
	 * The single valued RP translator to use internally.
	 */
	static private DefaultSingleResourcePropertyTranslator _single = new DefaultSingleResourcePropertyTranslator();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <Type> Collection<Type> deserialize(Class<Type> clazz, Collection<MessageElement> elements)
		throws ResourcePropertyException
	{
		if (elements == null)
			elements = new Vector<MessageElement>();

		Collection<Type> ret = new ArrayList<Type>(elements.size());
		for (MessageElement element : elements) {
			ret.add(_single.deserialize(clazz, element));
		}

		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<MessageElement> serialize(QName name, Collection<Object> obj) throws ResourcePropertyException
	{
		Collection<MessageElement> ret = new ArrayList<MessageElement>(obj.size());
		for (Object o : obj) {
			ret.add(_single.serialize(name, o));
		}

		return ret;
	}
}