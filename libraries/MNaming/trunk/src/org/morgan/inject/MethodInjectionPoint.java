package org.morgan.inject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class MethodInjectionPoint extends InjectionPoint {
	private Method _method;
	private Class<?> _targetType;

	MethodInjectionPoint(MInject injectionInformation, Method method)
			throws InjectionException {
		super(injectionInformation);

		if (method == null)
			throw new IllegalArgumentException("Method cannot be null.");

		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes.length != 1)
			throw new InjectionException(
					String.format(
							"Method %s isn't a valid injection target -- must take exactly one parameter.",
							method));

		_targetType = paramTypes[0];
		_method = method;
	}

	@Override
	final void inject(Object target, MInjectResolver resolver)
			throws InjectionException {
		Object value;

		if (_injectionInformation.lazy()) {
			Class<?>[] types = _injectionInformation.lazyTypes();
			if (types == null || types.length == 0)
				types = new Class<?>[] { _targetType };

			value = Proxy.newProxyInstance(FieldInjectionPoint.class
					.getClassLoader(), types, new LazyInjectionPointHandler(
					getInjectFactory(resolver), _injectionInformation,
					_targetType));
		} else
			value = doResolve(resolver, _targetType);

		synchronized (_method) {
			try {
				_method.setAccessible(true);
				_method.invoke(target, value);
			} catch (IllegalAccessException e) {
				throw new InjectionException(String.format(
						"Unable to inject value into method %s.", _method), e);
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause();
				if (cause instanceof RuntimeException)
					throw (RuntimeException) cause;
				else if (cause instanceof InjectionException)
					throw (InjectionException) cause;
				else
					throw new InjectionException(String.format(
							"Unable to inject value into method %s.", _method),
							cause);
			} finally {
				_method.setAccessible(false);
			}
		}
	}
}