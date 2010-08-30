package edu.virginia.vcgr.genii.container.invoker.inject;

import java.io.IOException;

import org.morgan.inject.InjectionException;
import org.morgan.inject.MInject;
import org.morgan.inject.MInjectResolver;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;

public class CallingContextInjectionResolver
	implements MInjectResolver
{
	@Override
	public boolean handles(MInject arg, Class<?> targetType)
	{
		return (arg.name().isEmpty() && 
			ICallingContext.class.isAssignableFrom(targetType));
	}

	@Override
	public <Type> Type resolve(MInject arg, Class<Type> targetType)
		throws InjectionException
	{
		try
		{
			return targetType.cast(
				ContextManager.getCurrentContext());
		}
		catch (IOException e)
		{
			throw new InjectionException(
				"Unable to resolve injection value.", e);
		}
	}
}