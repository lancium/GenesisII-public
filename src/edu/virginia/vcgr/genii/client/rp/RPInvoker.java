package edu.virginia.vcgr.genii.client.rp;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.common.GeniiCommon;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rp_2.UpdateResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.UpdateType;

public class RPInvoker implements InvocationHandler
{
	static private Log _logger = LogFactory.getLog(RPInvoker.class);

	static private interface Handler
	{
		public Object handle(Method method, Object []args)
			throws Throwable;
	}

	private class SingleGetterHandler implements Handler
	{
		private SingleResourcePropertyTranslator _translator;
		private QName _propName;
		
		public SingleGetterHandler(QName propName,
			SingleResourcePropertyTranslator translator)
		{
			_propName = propName;
			_translator = translator;
		}
		
		public Object handle(Method method, Object []args)
			throws Throwable
		{
			MessageElement m = getProperty(_propName);
			return _translator.deserialize(method.getReturnType(), m);
		}
	}
	
	private class MultiGetterHandler implements Handler
	{
		private MultiResourcePropertyTranslator _translator;
		private QName _propName;
	
		public MultiGetterHandler(QName propName,
			MultiResourcePropertyTranslator translator)
		{
			_propName = propName;
			_translator = translator;
		}
		
		public Object handle(Method method, Object []args)
			throws Throwable
		{
			return _translator.deserialize(method.getReturnType(), 
				getProperties(_propName));
		}
	}
	
	private class SingleSetterHandler implements Handler
	{
		private SingleResourcePropertyTranslator _translator;
		private QName _propName;
		
		public SingleSetterHandler(QName propName, SingleResourcePropertyTranslator translator)
		{
			_propName = propName;
			_translator = translator;
		}
		
		public Object handle(Method m, Object []args) throws Throwable
		{
			MessageElement me = _translator.serialize(_propName, args[0]);
			setProperty(_propName, me);
			return null;
		}
	}
	
	private class MultiSetterHandler implements Handler
	{
		private MultiResourcePropertyTranslator _translator;
		private QName _propName;
		
		public MultiSetterHandler(QName propName, MultiResourcePropertyTranslator translator)
		{
			_propName = propName;
			_translator = translator;
		}
		
		@SuppressWarnings("unchecked")
		public Object handle(Method m, Object []args) throws Throwable
		{
			Collection<MessageElement> me = _translator.serialize(
				_propName, (Collection<Object>)args[0]);
			setProperties(_propName, me);
			return null;
		}
	}
	
	static private Method _refreshMethod;
	
	static
	{
		try
		{
			_refreshMethod = ResourcePropertyRefresher.class.getMethod(
				"refreshResourceProperties", new Class<?>[0]);
		}
		catch (Throwable t)
		{
			// This shouldn't happen
			_logger.fatal("Unexpected exception looking for known method.", t);
			throw new RuntimeException(
				"Unexpected exception looking for known method.", t);
		}
	}
	
	private HashMap<Method, Handler> _handlers =
		new HashMap<Method, Handler>();
	private GeniiCommon _stub;
	private Collection<QName> _likelyRPs;
	private HashMap<QName, Collection<MessageElement>> _propertiesCache =
		new HashMap<QName, Collection<MessageElement>>();
	
	synchronized private void setProperties(QName properties, 
		Collection<MessageElement> values)
			throws ResourcePropertyException
	{
		try
		{
			_stub.updateResourceProperties(new UpdateResourceProperties(new UpdateType(
				values.toArray(new MessageElement[0]))));
			_propertiesCache.put(properties, values);
		}
		catch (ResourceUnknownFaultType ruft)
		{
			throw new ResourcePropertyException("Resource is unknown.", ruft);
		}
		catch (RemoteException re)
		{
			throw new ResourcePropertyException(
				"Unknown remote exception occurred.", re);
		}
	}
	
	synchronized private void fillInCache(Collection<QName> properties)
		throws ResourcePropertyException
	{
		boolean addLikelys = false;
		
		ArrayList<QName> toFill = new ArrayList<QName>(properties);
		for (QName name : properties)
		{
			if (_likelyRPs.contains(name))
			{
				addLikelys = true;
				break;
			}
		}
		
		if (addLikelys)
		{
			for (QName likely : _likelyRPs)
			{
				if (!toFill.contains(likely))
					toFill.add(likely);
			}
		}
		
		try
		{
			MessageElement []ret = 
				_stub.getMultipleResourceProperties(toFill.toArray(
					new QName[0])).get_any();
			HashMap<QName, Collection<MessageElement>> table = 
				new HashMap<QName, Collection<MessageElement>>();
			for (MessageElement m : ret)
			{
				Collection<MessageElement> prop = table.get(m.getQName());
				if (prop == null)
				{
					table.put(m.getQName(), prop = new ArrayList<MessageElement>());
				}
				prop.add(m);
			}
			
			for (QName name : table.keySet())
			{
				_propertiesCache.put(name, table.get(name));
			}
		}
		catch (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType ruft)
		{
			throw new ResourcePropertyException("Resource is unknown.", ruft);
		}
		catch (ResourceUnavailableFaultType ruft)
		{
			throw new ResourcePropertyException("Resource is unavailable.", ruft);
		}
		catch (RemoteException re)
		{
			throw new ResourcePropertyException(
				"Unknown remote exception occurred.", re);
		}
	}
	
	synchronized private Collection<MessageElement> getProperties(
		QName properties) throws ResourcePropertyException
	{
		if (!_propertiesCache.containsKey(properties))
		{
			ArrayList<QName> toFill = new ArrayList<QName>();
			toFill.add(properties);
			fillInCache(toFill);
		}
		
		return _propertiesCache.get(properties);
	}
	
	private void setProperty(QName propertyName, MessageElement me)
		throws ResourcePropertyException
	{
		try
		{
			_stub.updateResourceProperties(new UpdateResourceProperties(
				new UpdateType(new MessageElement[] { me })));
			ArrayList<MessageElement> values = new ArrayList<MessageElement>();
			values.add(me);
			_propertiesCache.put(propertyName, values);
		}
		catch (ResourceUnknownFaultType ruft)
		{
			throw new ResourcePropertyException("Resource is unknown.", ruft);
		}
		catch (RemoteException re)
		{
			throw new ResourcePropertyException(
				"Unknown remote exception occurred.", re);
		}
	}
	
	private MessageElement getProperty(QName propertyName)
		throws ResourcePropertyException
	{
		if (!_propertiesCache.containsKey(propertyName))
		{
			ArrayList<QName> toFill = new ArrayList<QName>();
			toFill.add(propertyName);
			fillInCache(toFill);
		}
		
		Collection<MessageElement> ret = _propertiesCache.get(propertyName);
		if (ret == null || ret.size() == 0)
			return null;
		if (ret.size() == 1)
			return ret.iterator().next();
		
		throw new ResourcePropertyException(
			"Attempt to retrieve multi-valued resource property as a singleton.");
	}
	
	static private ResourcePropertyTranslator createTanslator(
		Class<? extends ResourcePropertyTranslator> transClass)
			throws ResourcePropertyException
	{
		try
		{
			Constructor<? extends ResourcePropertyTranslator> cons =
				transClass.getConstructor(new Class<?>[0]);
			return cons.newInstance(new Object[0]);
		}
		catch (NoSuchMethodException nsme)
		{
			throw new ResourcePropertyException(
				"Unable to find default constructor for RP translator.", nsme);
		}
		catch (IllegalAccessException iae)
		{
			throw new ResourcePropertyException(
				"Unable to find public default constructor for RP translator.",
				iae);
		}
		catch (InvocationTargetException ite)
		{
			Throwable cause = ite.getCause();
			if (cause instanceof ResourcePropertyException)
				throw (ResourcePropertyException)cause;
			
			throw new ResourcePropertyException(
				"RP Translator constructor threw exception.", ite);
		}
		catch (InstantiationException ie)
		{
			throw new ResourcePropertyException(
				"Unable to create new RP translator.", ie);
		}
	}
	
	static private boolean isSetter(Method method)
		throws ResourcePropertyException
	{
		Class<?> returnType = method.getReturnType();
		Class<?> []paramTypes = method.getParameterTypes();
		
		if (returnType.equals(Void.class) || returnType.equals(void.class))
		{
			// possible setter.
			if (paramTypes.length == 1)
				return true;
		} else
		{
			// possible getter.
			if (paramTypes.length == 0)
				return false;
		}
		
		throw new ResourcePropertyException("The method \"" 
			+ method.toGenericString() + 
			"\" does not match either the RP getter or RP setter pattern.");
	}
	
	private Handler createSingleHandler(QName propertyName, 
		SingleResourcePropertyTranslator translator, Method method)
		throws ResourcePropertyException
	{
		if (isSetter(method))
			return new SingleSetterHandler(propertyName, translator);
		else
			return new SingleGetterHandler(propertyName, translator);
	}
	
	private Handler createMultiHandler(QName propertyName, 
			MultiResourcePropertyTranslator translator, Method method)
			throws ResourcePropertyException
	{
		if (isSetter(method))
		{
			Class<?> type = method.getParameterTypes()[0];
			if (Collection.class.isAssignableFrom(type))
				return new MultiSetterHandler(propertyName, translator);
		} else
		{
			Class<?> type = method.getReturnType();
			if (Collection.class.isAssignableFrom(type))
				return new MultiGetterHandler(propertyName, translator);
		}
		
		throw new ResourcePropertyException(
			"Multi-value resource property setter/getter (" + 
			method.toGenericString() + ") does not take/return a collection.");
	}
		
	private Handler createHandler(Method method) throws ResourcePropertyException
	{
		ResourceProperty rp = method.getAnnotation(ResourceProperty.class);
		if (rp == null)
			throw new ResourcePropertyException(
				"Unable to find a ResourceProperty annotation on the method "
				+ method.toGenericString());
		
		QName propertyName = new QName(rp.namespace(), rp.localname());
		Class<? extends ResourcePropertyTranslator> transClass = rp.translator();
		ResourcePropertyTranslator translator = null;
		
		if (!transClass.equals(ResourcePropertyTranslator.class))
			translator = createTanslator(transClass);
		
		String sMin = rp.min();
		String sMax = rp.max();
		int min = sMin.equals("unbounded") ? Integer.MAX_VALUE : Integer.parseInt(sMin);
		int max = sMax.equals("unbounded") ? Integer.MAX_VALUE : Integer.parseInt(sMax);
		
		if (min > max)
			throw new ResourcePropertyException("min value cannot be greater than max value.");
		
		if (max > 1)
		{
			// multi
			if (translator == null)
				translator = new DefaultMultiResourcePropertyTranslator();
			
			if (translator instanceof SingleResourcePropertyTranslator)
				throw new ResourcePropertyException(
					"Attempt to use a singleton RP translator on a " +
					"multi-valued resource property.");
			
			return createMultiHandler(propertyName,
				(MultiResourcePropertyTranslator)translator, method);
		} else
		{
			// single
			if (translator == null)
				translator = new DefaultSingleResourcePropertyTranslator();
			
			if (translator instanceof MultiResourcePropertyTranslator)
				throw new ResourcePropertyException(
					"Attempt to use a multi-value RP translator on a " +
					"singleton resource property.");
			
			return createSingleHandler(propertyName, 
				(SingleResourcePropertyTranslator)translator, method);
		}
	}
	
	private Handler getHandler(Method method) throws ResourcePropertyException
	{
		synchronized(_handlers)
		{
			Handler h = _handlers.get(method);
			if (h == null)
			{
				h = createHandler(method);
				_handlers.put(method, h);
			}
			
			return h;
		}
	}
	
	public RPInvoker(Collection<QName> likelyRPs, EndpointReferenceType target)
		throws GenesisIISecurityException, ConfigurationException, 
			ResourceException
	{
		_likelyRPs = likelyRPs;
		_stub = ClientUtils.createProxy(GeniiCommon.class, target);
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
		throws Throwable
	{
		if (method.equals(_refreshMethod))
		{
			_propertiesCache.clear();
			return null;
		}
		
		return getHandler(method).handle(method, args);
	}
}