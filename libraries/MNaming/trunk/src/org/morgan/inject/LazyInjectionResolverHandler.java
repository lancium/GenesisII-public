package org.morgan.inject;

public interface LazyInjectionResolverHandler
{
	public Class<?> valueType();

	public Object value();
}