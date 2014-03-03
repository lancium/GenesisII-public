package org.morgan.inject;

public interface MInjectResolver extends MInjectFactory
{
	public boolean handles(MInject injectionInformation, Class<?> injectionTarget);
}