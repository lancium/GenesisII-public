package edu.virginia.vcgr.genii.container.invoker.inject;

import org.morgan.inject.InjectionException;
import org.morgan.inject.MInject;
import org.morgan.inject.MInjectResolver;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceLock;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.resource.StringResourceIdentifier;

public class ResourceMInjectResolver implements MInjectResolver
{
	@Override
	public boolean handles(MInject arg, Class<?> targetType)
	{
		return (arg.name().isEmpty() && (IResource.class.isAssignableFrom(targetType)
			|| ResourceLock.class.isAssignableFrom(targetType) || StringResourceIdentifier.class.isAssignableFrom(targetType)));
	}

	@Override
	public <Type> Type resolve(MInject arg, Class<Type> targetType) throws InjectionException
	{
		try {
			if (IResource.class.isAssignableFrom(targetType))
				return targetType.cast(ResourceManager.getCurrentResource().dereference());
			else if (ResourceLock.class.isAssignableFrom(targetType))
				return targetType.cast(ResourceManager.getCurrentResource().getResourceLock());
			else if (StringResourceIdentifier.class.isAssignableFrom(targetType))
				return targetType.cast(new StringResourceIdentifier(ResourceManager.getCurrentResource().getResourceKey()));
		} catch (ResourceException e) {
			throw new InjectionException("Unable to resolve injection value.", e);
		} catch (ResourceUnknownFaultType e) {
			throw new InjectionException("Unable to resolve injection value.", e);
		}

		throw new InjectionException(String.format("Don't know how to inject into type %s.", targetType));
	}
}