package edu.virginia.vcgr.genii.container.configuration;

import edu.virginia.vcgr.genii.container.resolver.IResolverFactoryProxy;
import edu.virginia.vcgr.genii.container.resource.IResourceProvider;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;

public interface GenesisIIServiceConfiguration
{
	public IResourceProvider resourceProvider();

	public IAuthZProvider defaultAuthZProvider();

	public IResolverFactoryProxy defaultResolverFactoryProxy();

	public Long defaultServiceCertificateLifetime();

	public Long defaultResourceCertificateLifetime();

	public Class<? extends JAXBGenesisIIServiceConfiguration> jaxbServiceConfigurationClass();
}