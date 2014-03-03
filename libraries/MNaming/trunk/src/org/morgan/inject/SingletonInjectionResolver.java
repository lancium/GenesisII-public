package org.morgan.inject;

final public class SingletonInjectionResolver implements MInjectResolver
{
	private Object _instance;

	public SingletonInjectionResolver(Object instance)
	{
		if (instance == null)
			throw new IllegalArgumentException("Instance for singleton cannot be null.");

		_instance = instance;
	}

	@Override
	final public boolean handles(MInject injectionInformation, Class<?> injectionTarget)
	{
		return (injectionInformation.name().isEmpty() && injectionTarget.isAssignableFrom(_instance.getClass()));
	}

	@Override
	final public <Type> Type resolve(MInject injectionInformation, Class<Type> injectionTarget) throws InjectionException
	{
		return injectionTarget.cast(_instance);
	}
}