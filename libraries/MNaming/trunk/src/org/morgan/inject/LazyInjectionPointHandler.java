package org.morgan.inject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class LazyInjectionPointHandler implements InvocationHandler
{
	private MInjectFactory _resolver;
	private MInject _injectInformation;
	private Class<?> _injectionTarget;

	private Object _resolvedValue = null;

	public LazyInjectionPointHandler(MInjectFactory resolver, MInject injectInformation, Class<?> injectionTarget)
	{
		if (resolver == null)
			throw new IllegalArgumentException("Resolver cannot be null.");

		if (injectionTarget == null)
			throw new IllegalArgumentException("Injection target cannot be null.");

		if (injectInformation == null)
			throw new IllegalArgumentException("Injection Information cannot be null.");

		_resolver = resolver;
		_injectInformation = injectInformation;
		_injectionTarget = injectionTarget;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		synchronized (this) {
			if (_resolvedValue == null)
				_resolvedValue = _resolver.resolve(_injectInformation, _injectionTarget);
		}

		return method.invoke(_resolvedValue, args);
	}
}