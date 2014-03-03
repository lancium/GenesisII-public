package org.morgan.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

abstract class InjectionPoint
{
	protected MInject _injectionInformation;

	protected InjectionPoint(MInject injectionInformation)
	{
		if (injectionInformation == null)
			throw new IllegalArgumentException("Injection information cannot be null.");

		_injectionInformation = injectionInformation;
	}

	protected MInjectFactory getInjectFactory(MInjectResolver resolver) throws InjectionException
	{
		Class<? extends MInjectFactory> factoryOverrideType = _injectionInformation.injectionFactory();

		if (factoryOverrideType == MInjectFactory.class)
			return resolver;

		Constructor<? extends MInjectFactory> factoryOverrideConstructor = null;

		try {
			factoryOverrideConstructor = factoryOverrideType.getDeclaredConstructor();

			synchronized (factoryOverrideConstructor) {
				try {
					factoryOverrideConstructor.setAccessible(true);

					MInjectFactory factoryOverride = factoryOverrideConstructor.newInstance();
					MInjector injector = new MInjector(resolver);
					injector.inject(factoryOverride);
					return factoryOverride;
				} finally {
					factoryOverrideConstructor.setAccessible(false);
				}
			}
		} catch (IllegalAccessException e) {
			throw new InjectionException("Unable to inject target.", e);
		} catch (InstantiationException e) {
			throw new InjectionException("Unable to inject target.", e);
		} catch (SecurityException e) {
			throw new InjectionException("Unable to inject target.", e);
		} catch (NoSuchMethodException e) {
			throw new InjectionException("Unable to inject target.", e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException)
				throw (RuntimeException) cause;
			else if (cause instanceof InjectionException)
				throw (InjectionException) cause;

			throw new InjectionException("Unable to inject target.", cause);
		}
	}

	protected Object doResolve(MInjectResolver resolver, Class<?> targetType) throws InjectionException
	{
		return getInjectFactory(resolver).resolve(_injectionInformation, targetType);
	}

	abstract void inject(Object target, MInjectResolver resolver) throws InjectionException;
}