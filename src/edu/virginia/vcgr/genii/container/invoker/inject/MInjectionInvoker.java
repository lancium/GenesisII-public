package edu.virginia.vcgr.genii.container.invoker.inject;

import org.morgan.inject.InjectionException;
import org.morgan.inject.MInjector;
import org.morgan.inject.MNamingInjectionResolver;

import edu.virginia.vcgr.genii.container.invoker.IAroundInvoker;
import edu.virginia.vcgr.genii.container.invoker.InvocationContext;

public class MInjectionInvoker implements IAroundInvoker
{
	static private MInjector _injector;

	static {
		_injector = new MInjector(new ResourceMInjectResolver(), new CallingContextInjectionResolver(),
			new MNamingInjectionResolver());
	}

	static public void inject(Object target) throws InjectionException
	{
		_injector.inject(target);
	}

	@Override
	public Object invoke(InvocationContext invocationContext) throws Exception
	{
		inject(invocationContext.getTarget());
		return invocationContext.proceed();
	}
}