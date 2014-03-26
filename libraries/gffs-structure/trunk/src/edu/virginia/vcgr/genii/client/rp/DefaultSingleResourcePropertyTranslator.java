package edu.virginia.vcgr.genii.client.rp;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

/**
 * This is the default resource property translator used for single-valued resource properties.
 * 
 * @author mmm2a
 */
public class DefaultSingleResourcePropertyTranslator implements SingleResourcePropertyTranslator
{
	static private Log _logger = LogFactory.getLog(DefaultSingleResourcePropertyTranslator.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <Type> Type deserialize(Class<Type> clazz, MessageElement element) throws ResourcePropertyException
	{
		if (element == null) {
			_logger.error("attempt to deserialize null.");
			return null;
		}
		if (clazz == null) {
			_logger.error("provided class type in clazz is null.");
			return null;
		}
		_logger.debug("deserializing type " + clazz.getCanonicalName() + " from elem real type "
			+ element.getClass().getCanonicalName());
		try {
			Object ob = ObjectDeserializer.toObject(element, clazz);
			if (ob == null) {
				_logger.error("deserialization problem for type " + clazz.getCanonicalName() + " from elem real type "
					+ element.getClass().getCanonicalName());
				return null;
			}
			_logger.debug("object type deserialized: " + ob.getClass().getCanonicalName());
			Type toReturn = clazz.cast(ob);
			_logger.debug("...now returning type " + toReturn.getClass().getCanonicalName());
			return toReturn;
		} catch (ResourceException re) {
			String msg =
				"Unable to deserialize resource property " + element.getQName() + " for type " + clazz.getCanonicalName()
					+ " from elem real type " + element.getClass().getCanonicalName();
			_logger.error(msg);
			throw new ResourcePropertyException(msg, re);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageElement serialize(QName name, Object obj) throws ResourcePropertyException
	{
		return new MessageElement(name, obj);
	}
}
