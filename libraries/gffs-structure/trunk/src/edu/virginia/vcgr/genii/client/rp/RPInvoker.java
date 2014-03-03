package edu.virginia.vcgr.genii.client.rp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.common.GeniiCommon;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rp_2.UpdateResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.UpdateType;

/**
 * This is the invoker handler that handles ALL resource property interactions. It is used as part
 * of the {@link java.lang.reflect.Proxy} proxying mechanism.
 * 
 * @author mmm2a
 */
public class RPInvoker implements InvocationHandler
{
	static private Log _logger = LogFactory.getLog(RPInvoker.class);

	/**
	 * The RP invoker is essentially a collection of "handlers" that handle RP interactions for both
	 * single and multi-value properties. Further, these handlers can be getters or setters. THis
	 * interface represents that common ability.
	 * 
	 * @author mmm2a
	 */
	static private interface Handler
	{
		/**
		 * Handle a resource property request (get/set, single/multi).
		 * 
		 * @param method
		 *            The method that as called on an RP interface
		 * @param args
		 *            The arguments passed to that method (should be empty for a getter, and a
		 *            single argument (which could be a collection for a multi-valued RP, or not for
		 *            a single-value RP) for a setter).
		 * 
		 * @return The results of the RP get/set.
		 * 
		 * @throws Throwable
		 */
		public Object handle(Method method, Object[] args) throws Throwable;
	}

	/**
	 * This is the implementation of handlers that can get single-valued RPs.
	 * 
	 * @author mmm2a
	 */
	private class SingleGetterHandler implements Handler
	{
		/**
		 * The translator to use for RP translation.
		 */
		private SingleResourcePropertyTranslator _translator;

		/**
		 * The QName of the source XML element.
		 */
		private QName _propName;

		/**
		 * Create a new RP SingleGetterHandler.
		 * 
		 * @param propName
		 *            The resource property name.
		 * @param translator
		 *            The translator to use for translation.
		 */
		public SingleGetterHandler(QName propName, SingleResourcePropertyTranslator translator)
		{
			_propName = propName;
			_translator = translator;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object handle(Method method, Object[] args) throws Throwable
		{
			MessageElement m = getProperty(_propName);
			return _translator.deserialize(method.getReturnType(), m);
		}
	}

	/**
	 * This is the implementation of handlers that can get multi-valued RPs.
	 * 
	 * @author mmm2a
	 */
	private class MultiGetterHandler implements Handler
	{
		/**
		 * The translator to use for RP translation.
		 */
		private MultiResourcePropertyTranslator _translator;

		/**
		 * The QName of the source XML element.
		 */
		private QName _propName;

		/**
		 * Create a new RP MultiGetterHandler.
		 * 
		 * @param propName
		 *            The resource property name.
		 * @param translator
		 *            The translator to use for translation.
		 */
		public MultiGetterHandler(QName propName, MultiResourcePropertyTranslator translator)
		{
			_propName = propName;
			_translator = translator;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object handle(Method method, Object[] args) throws Throwable
		{
			Class<?> retType = method.getReturnType();
			if (!Collection.class.isAssignableFrom(retType))
				throw new IllegalArgumentException(String.format("The return type for property \"%s\" is not a collection.",
					_propName));

			Type gRetType = method.getGenericReturnType();
			if (!(gRetType instanceof ParameterizedType))
				throw new IllegalArgumentException(String.format("Unable to determine actual type for property \"%s\".",
					_propName));
			Type actualType = ((ParameterizedType) gRetType).getActualTypeArguments()[0];
			if (!(actualType instanceof Class<?>))
				throw new IllegalArgumentException(String.format("Unable to determine actual type for property \"%s\".",
					_propName));

			return _translator.deserialize((Class<?>) actualType, getProperties(_propName));
		}
	}

	/**
	 * This is the implementation of handlers that can set single-valued RPs.
	 * 
	 * @author mmm2a
	 */
	private class SingleSetterHandler implements Handler
	{
		/**
		 * The translator to use for RP translation.
		 */
		private SingleResourcePropertyTranslator _translator;

		/**
		 * The QName of the target XML element.
		 */
		private QName _propName;

		/**
		 * Creates a new SingleSetterHandler
		 * 
		 * @param propName
		 *            The Resource Property name for this setter.
		 * @param translator
		 *            The translator to use for resource property translations.
		 */
		public SingleSetterHandler(QName propName, SingleResourcePropertyTranslator translator)
		{
			_propName = propName;
			_translator = translator;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object handle(Method m, Object[] args) throws Throwable
		{
			MessageElement me = _translator.serialize(_propName, args[0]);
			setProperty(_propName, me);
			return null;
		}
	}

	/**
	 * This is the implementation of handlers that can set multi-valued RPs.
	 * 
	 * @author mmm2a
	 */
	private class MultiSetterHandler implements Handler
	{
		/**
		 * The translator to use for RP translation.
		 */
		private MultiResourcePropertyTranslator _translator;

		/**
		 * The QName of the target XML element.
		 */
		private QName _propName;

		/**
		 * Creates a new MultiSetterHandler
		 * 
		 * @param propName
		 *            The Resource Property name for this setter.
		 * @param translator
		 *            The translator to use for resource property translations.
		 */
		public MultiSetterHandler(QName propName, MultiResourcePropertyTranslator translator)
		{
			_propName = propName;
			_translator = translator;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Object handle(Method m, Object[] args) throws Throwable
		{
			Collection<MessageElement> me = _translator.serialize(_propName, (Collection<Object>) args[0]);
			setProperties(_propName, me);
			return null;
		}
	}

	/**
	 * This is the method that represents the request to refresh the cache. We store it as a
	 * performance optimization.
	 */
	static private Method _refreshMethod;

	static {
		try {
			_refreshMethod = ResourcePropertyRefresher.class.getMethod("refreshResourceProperties", new Class<?>[0]);
		} catch (Throwable t) {
			// This shouldn't happen
			_logger.fatal("Unexpected exception looking for known method.", t);
			throw new RuntimeException("Unexpected exception looking for known method.", t);
		}
	}

	private HashMap<Method, Handler> _handlers = new HashMap<Method, Handler>();
	private GeniiCommon _stub;
	private Collection<QName> _likelyRPs;
	private HashMap<QName, Collection<MessageElement>> _propertiesCache = new HashMap<QName, Collection<MessageElement>>();

	/**
	 * Update a list of resource properties (by making the appropriate outcalls and updating the
	 * cache.
	 * 
	 * @param properties
	 *            The resource properties to set.
	 * @param values
	 *            The values of those resource properties.
	 * 
	 * @throws ResourcePropertyException
	 */
	synchronized private void setProperties(QName properties, Collection<MessageElement> values)
		throws ResourcePropertyException
	{
		try {
			_stub.updateResourceProperties(new UpdateResourceProperties(new UpdateType(values.toArray(new MessageElement[0]))));
			_propertiesCache.put(properties, values);
		} catch (ResourceUnknownFaultType ruft) {
			throw new ResourcePropertyException("Resource is unknown.", ruft);
		} catch (RemoteException re) {
			throw new ResourcePropertyException("Unknown remote exception occurred.", re);
		}
	}

	/**
	 * Fill in the cache with a bunch of resource properties from a remote resource.
	 * 
	 * @param properties
	 *            The resource properties to get from the remote resource.
	 * 
	 * @throws ResourcePropertyException
	 */
	synchronized private void fillInCache(Collection<QName> properties) throws ResourcePropertyException
	{
		boolean addLikelys = false;

		ArrayList<QName> toFill = new ArrayList<QName>(properties);
		for (QName name : properties) {
			if (_likelyRPs.contains(name)) {
				addLikelys = true;
				break;
			}
		}

		if (addLikelys) {
			for (QName likely : _likelyRPs) {
				if (!toFill.contains(likely))
					toFill.add(likely);
			}
		}

		try {
			MessageElement[] ret = _stub.getMultipleResourceProperties(toFill.toArray(new QName[0])).get_any();
			HashMap<QName, Collection<MessageElement>> table = new HashMap<QName, Collection<MessageElement>>();
			for (MessageElement m : ret) {
				Collection<MessageElement> prop = table.get(m.getQName());
				if (prop == null) {
					table.put(m.getQName(), prop = new ArrayList<MessageElement>());
				}
				prop.add(m);
			}

			for (QName name : table.keySet()) {
				_propertiesCache.put(name, table.get(name));
			}
		} catch (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType ruft) {
			throw new ResourcePropertyException("Resource is unknown.", ruft);
		} catch (ResourceUnavailableFaultType ruft) {
			throw new ResourcePropertyException("Resource is unavailable.", ruft);
		} catch (RemoteException re) {
			throw new ResourcePropertyException("Unknown remote exception occurred.", re);
		}
	}

	/**
	 * A singleton version of getting a resource property (singleton in the sense of the number of
	 * RPs retrieve, not their value cardinalities).
	 * 
	 * @param properties
	 *            The resource property to get.
	 * 
	 * @return The retrieved RP values.
	 * 
	 * @throws ResourcePropertyException
	 */
	synchronized private Collection<MessageElement> getProperties(QName properties) throws ResourcePropertyException
	{
		if (!_propertiesCache.containsKey(properties)) {
			ArrayList<QName> toFill = new ArrayList<QName>();
			toFill.add(properties);
			fillInCache(toFill);
		}

		return _propertiesCache.get(properties);
	}

	/**
	 * Set a resource property.
	 * 
	 * @param propertyName
	 *            The name of the property to set.
	 * @param me
	 *            The XML representation of the resource property.
	 * 
	 * @throws ResourcePropertyException
	 */
	private void setProperty(QName propertyName, MessageElement me) throws ResourcePropertyException
	{
		try {
			_stub.updateResourceProperties(new UpdateResourceProperties(new UpdateType(new MessageElement[] { me })));
			ArrayList<MessageElement> values = new ArrayList<MessageElement>();
			values.add(me);
			_propertiesCache.put(propertyName, values);
		} catch (ResourceUnknownFaultType ruft) {
			throw new ResourcePropertyException("Resource is unknown.", ruft);
		} catch (RemoteException re) {
			throw new ResourcePropertyException("Unknown remote exception occurred.", re);
		}
	}

	/**
	 * Get a single value resource property.
	 * 
	 * @param propertyName
	 *            The name of the resource property to retrieve.
	 * 
	 * @return The value of the resource property.
	 * 
	 * @throws ResourcePropertyException
	 */
	private MessageElement getProperty(QName propertyName) throws ResourcePropertyException
	{
		if (!_propertiesCache.containsKey(propertyName)) {
			ArrayList<QName> toFill = new ArrayList<QName>();
			toFill.add(propertyName);
			fillInCache(toFill);
		}

		Collection<MessageElement> ret = _propertiesCache.get(propertyName);
		if (ret == null || ret.size() == 0)
			return null;
		if (ret.size() == 1)
			return ret.iterator().next();

		throw new ResourcePropertyException("Attempt to retrieve multi-valued resource " + "property as a singleton.");
	}

	/**
	 * A convenience method to create a translator from it's class.
	 * 
	 * @param transClass
	 *            The class of the translator to create.
	 * 
	 * @return The newly created resource property translator.
	 * 
	 * @throws ResourcePropertyException
	 */
	static private ResourcePropertyTranslator createTanslator(Class<? extends ResourcePropertyTranslator> transClass)
		throws ResourcePropertyException
	{
		try {
			Constructor<? extends ResourcePropertyTranslator> cons = transClass.getConstructor(new Class<?>[0]);
			return cons.newInstance(new Object[0]);
		} catch (NoSuchMethodException nsme) {
			throw new ResourcePropertyException("Unable to find default constructor for RP translator.", nsme);
		} catch (IllegalAccessException iae) {
			throw new ResourcePropertyException("Unable to find public default constructor for RP translator.", iae);
		} catch (InvocationTargetException ite) {
			Throwable cause = ite.getCause();
			if (cause instanceof ResourcePropertyException)
				throw (ResourcePropertyException) cause;

			throw new ResourcePropertyException("RP Translator constructor threw exception.", ite);
		} catch (InstantiationException ie) {
			throw new ResourcePropertyException("Unable to create new RP translator.", ie);
		}
	}

	/**
	 * Determine from return type whether or not the given method is a setter or a getter method.
	 * 
	 * @param method
	 *            The method to analyze.
	 * 
	 * @return true if the method given represents a set operation, false otherwise.
	 * 
	 * @throws ResourcePropertyException
	 */
	static private boolean isSetter(Method method) throws ResourcePropertyException
	{
		Class<?> returnType = method.getReturnType();
		Class<?>[] paramTypes = method.getParameterTypes();

		if (returnType.equals(Void.class) || returnType.equals(void.class)) {
			// possible setter.
			if (paramTypes.length == 1)
				return true;
		} else {
			// possible getter.
			if (paramTypes.length == 0)
				return false;
		}

		throw new ResourcePropertyException("The method \"" + method.toGenericString()
			+ "\" does not match either the RP getter or RP setter pattern.");
	}

	/**
	 * Create a new handler appropriate for handling singleton values.
	 * 
	 * @param propertyName
	 *            The name of the resource property this handler will handle.
	 * @param translator
	 *            The translator to use for translating the resource properties.
	 * @param method
	 *            The method that this handler is being used for.
	 * 
	 * @return A newly created handler appropriate for the given method.
	 * 
	 * @throws ResourcePropertyException
	 */
	private Handler createSingleHandler(QName propertyName, SingleResourcePropertyTranslator translator, Method method)
		throws ResourcePropertyException
	{
		if (isSetter(method))
			return new SingleSetterHandler(propertyName, translator);
		else
			return new SingleGetterHandler(propertyName, translator);
	}

	/**
	 * Create a new handler appropriate for handling multi-values.
	 * 
	 * @param propertyName
	 *            The name of the resource property this handler will handle.
	 * @param translator
	 *            The translator to use for translating the resource properties.
	 * @param method
	 *            The method that this handler is being used for.
	 * 
	 * @return A newly created handler appropriate for the given method.
	 * 
	 * @throws ResourcePropertyException
	 */
	private Handler createMultiHandler(QName propertyName, MultiResourcePropertyTranslator translator, Method method)
		throws ResourcePropertyException
	{
		if (isSetter(method)) {
			Class<?> type = method.getParameterTypes()[0];
			if (Collection.class.isAssignableFrom(type))
				return new MultiSetterHandler(propertyName, translator);
		} else {
			Class<?> type = method.getReturnType();
			if (Collection.class.isAssignableFrom(type))
				return new MultiGetterHandler(propertyName, translator);
		}

		throw new ResourcePropertyException("Multi-value resource property setter/getter (" + method.toGenericString()
			+ ") does not take/return a collection.");
	}

	/**
	 * Create a new handler for the given method. This method is responsible for reflecting on the
	 * method and it's annotations and determining which kind of handler is appropriate, what the
	 * name of the RP is, what translators to use, etc.
	 * 
	 * @param method
	 *            The method to create a handler for.
	 * 
	 * @return The newly created handler.
	 * 
	 * @throws ResourcePropertyException
	 */
	private Handler createHandler(Method method) throws ResourcePropertyException
	{
		ResourceProperty rp = method.getAnnotation(ResourceProperty.class);
		if (rp == null)
			throw new ResourcePropertyException("Unable to find a ResourceProperty annotation on the method "
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

		if (max > 1) {
			// multi
			if (translator == null)
				translator = new DefaultMultiResourcePropertyTranslator();

			if (translator instanceof SingleResourcePropertyTranslator)
				throw new ResourcePropertyException("Attempt to use a singleton RP translator on a "
					+ "multi-valued resource property.");

			return createMultiHandler(propertyName, (MultiResourcePropertyTranslator) translator, method);
		} else {
			// single
			if (translator == null)
				translator = new DefaultSingleResourcePropertyTranslator();

			if (translator instanceof MultiResourcePropertyTranslator)
				throw new ResourcePropertyException("Attempt to use a multi-value RP translator on a "
					+ "singleton resource property.");

			return createSingleHandler(propertyName, (SingleResourcePropertyTranslator) translator, method);
		}
	}

	/**
	 * Get the handler registered for a given method. THis is called at run time to handle an RP
	 * request.
	 * 
	 * @param method
	 *            The method that we are getting the handler for.
	 * 
	 * @return The registered handler.
	 * 
	 * @throws ResourcePropertyException
	 */
	private Handler getHandler(Method method) throws ResourcePropertyException
	{
		synchronized (_handlers) {
			Handler h = _handlers.get(method);
			if (h == null) {
				h = createHandler(method);
				_handlers.put(method, h);
			}

			return h;
		}
	}

	/**
	 * Create a new RPInvoker.
	 * 
	 * @param likelyRPs
	 *            The list of resource properties that we are "likely" going to have to handle.
	 * @param target
	 *            The target resource to get/set RPs for.
	 * @throws IOException
	 * @throws FileNotFoundException
	 * 
	 * @throws ConfigurationException
	 */
	public RPInvoker(Collection<QName> likelyRPs, EndpointReferenceType target, ICallingContext callingContext)
		throws FileNotFoundException, IOException
	{
		_likelyRPs = likelyRPs;
		if (callingContext == null)
			callingContext = ContextManager.getExistingContext();

		_stub = ClientUtils.createProxy(GeniiCommon.class, target, callingContext);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		if (method.equals(_refreshMethod)) {
			_propertiesCache.clear();
			return null;
		}

		return getHandler(method).handle(method, args);
	}
}