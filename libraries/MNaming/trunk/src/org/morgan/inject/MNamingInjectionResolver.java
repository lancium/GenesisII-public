package org.morgan.inject;

import org.morgan.mnaming.InitialMNamingContext;
import org.morgan.mnaming.MNamingException;

final public class MNamingInjectionResolver implements MInjectResolver
{
	static private InitialMNamingContext _context = new InitialMNamingContext();

	@Override
	final public boolean handles(MInject injectionInformation, Class<?> injectionTarget)
	{
		return !injectionInformation.name().isEmpty();
	}

	@Override
	final public <Type> Type resolve(MInject injectionInformation, Class<Type> injectionTarget) throws InjectionException
	{
		String name = injectionInformation.name();
		if (name.isEmpty())
			throw new InjectionException("Name cannot be empty for this type of injection resolver.");

		try {
			return _context.get(injectionTarget, name);
		} catch (MNamingException e) {
			throw new InjectionException(String.format("Unable to find name %s.", name), e);
		}
	}
}