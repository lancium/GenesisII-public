package org.morgan.inject;

public class LazyInjectionResolver implements MInjectResolver
{
	private LazyInjectionResolverHandler _handler;

	public LazyInjectionResolver(LazyInjectionResolverHandler handler)
	{
		if (handler == null)
			throw new IllegalArgumentException("Handler cannot be null.");

		_handler = handler;
	}

	@Override
	final public boolean handles(MInject injectionInformation, Class<?> injectionTarget)
	{
		return (injectionInformation.name().isEmpty() && injectionTarget.isAssignableFrom(_handler.valueType()));
	}

	@Override
	final public <Type> Type resolve(MInject injectionInformation, Class<Type> injectionTarget) throws InjectionException
	{
		return injectionTarget.cast(_handler.value());
	}
}