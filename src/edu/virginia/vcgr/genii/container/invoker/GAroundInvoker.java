package edu.virginia.vcgr.genii.container.invoker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.axis.MessageContext;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GAroundInvoker extends RPCProvider {
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(GAroundInvoker.class);

	static private HashMap<Method, IAroundInvoker[]> _cachedHandlers = new HashMap<Method, IAroundInvoker[]>();

	protected Object invokeMethod(MessageContext msgContext, Method method,
			Object obj, Object[] argValues) throws Exception {
		Method realMethod = obj.getClass().getMethod(method.getName(),
				method.getParameterTypes());
		_logger.debug("invoking: " + realMethod.toString());
		IAroundInvoker[] handlers;
		synchronized (_cachedHandlers) {
			handlers = _cachedHandlers.get(realMethod);
		}

		if (handlers == null) {
			Collection<IAroundInvoker> invokers = new ArrayList<IAroundInvoker>();

			fillInInvokers(invokers, obj.getClass(), method);
			handlers = new IAroundInvoker[invokers.size()];
			invokers.toArray(handlers);

			synchronized (_cachedHandlers) {
				_cachedHandlers.put(realMethod, handlers);
			}
		}

		return (new InvocationContext(msgContext, obj, method, argValues,
				handlers)).proceed();
	}

	static private void fillInInvokers(Collection<IAroundInvoker> invokers,
			Class<?> cl, Method m) throws NoSuchMethodException,
			IllegalAccessException, InstantiationException,
			InvocationTargetException {
		if (cl == null || cl.equals(Object.class))
			return;

		Class<?> sup = cl.getSuperclass();
		if (sup != null)
			fillInInvokers(invokers, sup, m);

		Class<?>[] interfaces = cl.getInterfaces();
		for (Class<?> face : interfaces) {
			fillInInvokers(invokers, face, m);
		}

		GAroundInvoke annotation = cl.getAnnotation(GAroundInvoke.class);
		if (annotation != null) {
			Class<? extends IAroundInvoker>[] handlers = annotation.value();
			for (Class<? extends IAroundInvoker> handler : handlers) {
				if (_logger.isDebugEnabled())
					_logger.debug("adding case1 handler: " + handler.toString()
							+ " for method: " + m.toString());
				invokers.add(getInvoker(handler));
			}
		}

		Method realM = null;
		try {
			realM = cl.getMethod(m.getName(), m.getParameterTypes());
		} catch (Throwable t) {
		}

		if (realM != null) {
			annotation = realM.getAnnotation(GAroundInvoke.class);
			if (annotation != null) {
				Class<? extends IAroundInvoker>[] handlers = annotation.value();
				for (Class<? extends IAroundInvoker> handler : handlers) {
					if (_logger.isDebugEnabled())
						_logger.debug("adding case2 handler: "
								+ handler.toString() + " for method: "
								+ m.toString());
					invokers.add(getInvoker(handler));
				}
			}
		}
	}

	static private IAroundInvoker getInvoker(Class<? extends IAroundInvoker> cl)
			throws NoSuchMethodException, IllegalAccessException,
			InstantiationException, InvocationTargetException {
		Constructor<? extends IAroundInvoker> cons = cl
				.getConstructor(new Class[0]);
		if (_logger.isDebugEnabled())
			_logger.debug("creating invoker for: " + cl.toString());
		return cons.newInstance(new Object[0]);
	}
}