package org.morgan.inject;

public interface MInjectFactory {
	public <Type> Type resolve(MInject injectionInformation,
			Class<Type> injectionTarget) throws InjectionException;
}
