package edu.virginia.vcgr.genii.container.security.authz.providers;

import java.util.HashMap;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.configuration.GenesisIIServiceConfiguration;
import edu.virginia.vcgr.genii.container.configuration.GenesisIIServiceConfigurationFactory;

/**
 * This class is simply a repository for providers for given services. Each service will have
 * associated with it it's own resource provider. This allows each one to have its authorization
 * provider.
 * 
 */
public class AuthZProviders
{
	static private HashMap<String, IAuthZProvider> _providerCache = new HashMap<String, IAuthZProvider>();

	/**
	 * Retrieve the resource provider for a given service name.
	 * 
	 * @param serviceName
	 *            The name of the service to retrieve resource providers for.
	 * @return The services resource provider.
	 * @throws ResourceException
	 *             If anything goes wrong.
	 */
	static public IAuthZProvider getProvider(String serviceName) throws ResourceException
	{
		IAuthZProvider provider = null;

		synchronized (_providerCache) {
			provider = _providerCache.get(serviceName);
		}

		if (provider != null)
			return provider;

		Class<?> serviceClass = Container.classForService(serviceName);
		if (serviceClass == null) {
			throw new ResourceException(String.format("Unable to find service class for service.", serviceName));
		}

		GenesisIIServiceConfiguration conf = GenesisIIServiceConfigurationFactory.configurationFor(serviceClass);
		provider = conf.defaultAuthZProvider();

		if (provider == null)
			throw new ResourceException(String.format("Unable to find resource provider for service %s "
				+ "(implemented by class %s).", serviceName, serviceClass));

		synchronized (_providerCache) {
			_providerCache.put(serviceName, provider);
		}

		return provider;
	}
}