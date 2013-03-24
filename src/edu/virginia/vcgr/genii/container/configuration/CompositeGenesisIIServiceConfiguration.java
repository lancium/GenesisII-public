package edu.virginia.vcgr.genii.container.configuration;

import java.util.LinkedList;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.container.resolver.IResolverFactoryProxy;
import edu.virginia.vcgr.genii.container.resource.IResourceProvider;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;

class CompositeGenesisIIServiceConfiguration implements GenesisIIServiceConfiguration
{
	private LinkedList<GenesisIIServiceConfiguration> _values = new LinkedList<GenesisIIServiceConfiguration>();

	CompositeGenesisIIServiceConfiguration(Class<?> serviceClass)
	{
		Class<?> lastJAXBClassOverride = null;
		Class<?> lastJAXBClassOverrideService = null;

		for (Class<?> newServiceClass = serviceClass; newServiceClass != Object.class; newServiceClass = newServiceClass
			.getSuperclass()) {
			GeniiServiceConfiguration serviceConf = newServiceClass.getAnnotation(GeniiServiceConfiguration.class);

			if (serviceConf != null) {
				Class<?> jaxbOverride = serviceConf.jaxbServiceConfigurationClass();

				if (!jaxbOverride.isInterface()) {
					if ((lastJAXBClassOverride != null) && !jaxbOverride.isAssignableFrom(lastJAXBClassOverride)) {
						throw new ConfigurationException(String.format("JAXBServiceConfigurationClass %s on %s is "
							+ "not compatible with previous declaration %s " + "on service class %s.", jaxbOverride,
							newServiceClass, lastJAXBClassOverride, lastJAXBClassOverrideService));
					}

					lastJAXBClassOverride = jaxbOverride;
					lastJAXBClassOverrideService = newServiceClass;
				}

				_values.addLast(new AnnotationSuppliedGenesisIIServiceConfiguration(serviceConf));
			}
		}

		_values.addLast(new DefaultGenesisIIServiceConfiguration());
	}

	@Override
	final public IResourceProvider resourceProvider()
	{
		IResourceProvider provider = null;

		for (GenesisIIServiceConfiguration conf : _values) {
			provider = conf.resourceProvider();
			if (provider != null)
				break;
		}

		return provider;
	}

	@Override
	final public IAuthZProvider defaultAuthZProvider()
	{
		IAuthZProvider provider = null;

		for (GenesisIIServiceConfiguration conf : _values) {
			provider = conf.defaultAuthZProvider();
			if (provider != null)
				break;
		}

		return provider;
	}

	@Override
	final public IResolverFactoryProxy defaultResolverFactoryProxy()
	{
		IResolverFactoryProxy provider = null;

		for (GenesisIIServiceConfiguration conf : _values) {
			provider = conf.defaultResolverFactoryProxy();
			if (provider != null)
				break;
		}

		return provider;
	}

	@Override
	final public Long defaultServiceCertificateLifetime()
	{
		Long provider = null;

		for (GenesisIIServiceConfiguration conf : _values) {
			provider = conf.defaultServiceCertificateLifetime();
			if (provider != null)
				break;
		}

		return provider;
	}

	@Override
	final public Long defaultResourceCertificateLifetime()
	{
		Long provider = null;

		for (GenesisIIServiceConfiguration conf : _values) {
			provider = conf.defaultResourceCertificateLifetime();
			if (provider != null)
				break;
		}

		return provider;
	}

	@Override
	final public Class<? extends JAXBGenesisIIServiceConfiguration> jaxbServiceConfigurationClass()
	{
		Class<? extends JAXBGenesisIIServiceConfiguration> ret = null;

		for (GenesisIIServiceConfiguration conf : _values) {
			ret = conf.jaxbServiceConfigurationClass();
			if (ret != null)
				break;
		}

		if (ret == null)
			ret = JAXBGenesisIIServiceConfiguration.class;

		return ret;
	}
}