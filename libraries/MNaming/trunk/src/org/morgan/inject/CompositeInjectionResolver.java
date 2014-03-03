package org.morgan.inject;

public class CompositeInjectionResolver implements MInjectResolver {
	private MInjectResolver[] _resolvers;

	public CompositeInjectionResolver(MInjectResolver... resolvers) {
		_resolvers = resolvers;
	}

	@Override
	final public boolean handles(MInject injectionInformation,
			Class<?> injectionTarget) {
		for (MInjectResolver resolver : _resolvers) {
			if (resolver.handles(injectionInformation, injectionTarget))
				return true;
		}

		return false;
	}

	@Override
	public <Type> Type resolve(MInject injectionInformation,
			Class<Type> injectionTarget) throws InjectionException {
		for (MInjectResolver resolver : _resolvers) {
			if (resolver.handles(injectionInformation, injectionTarget))
				return resolver.resolve(injectionInformation, injectionTarget);
		}

		throw new InjectionException("Resolver doesn't handle injection type.");
	}
}