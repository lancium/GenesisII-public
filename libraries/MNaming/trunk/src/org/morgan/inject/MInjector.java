package org.morgan.inject;

final public class MInjector
{
	private MInjectResolver _resolver;

	public MInjector(MInjectResolver... resolvers)
	{
		if (resolvers.length == 0)
			throw new IllegalArgumentException("Must have at least one resolver.");

		if (resolvers.length == 1)
			_resolver = resolvers[0];
		else
			_resolver = new CompositeInjectionResolver(resolvers);
	}

	final public void inject(Object target) throws InjectionException
	{
		if (target == null)
			return;

		for (InjectionPoint point : InjectionPointDatabase.injectionPoints(target.getClass())) {
			point.inject(target, _resolver);
		}
	}
}