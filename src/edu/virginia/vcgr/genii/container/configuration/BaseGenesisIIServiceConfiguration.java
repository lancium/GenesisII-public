package edu.virginia.vcgr.genii.container.configuration;

import edu.virginia.vcgr.genii.container.resolver.IResolverFactoryProxy;
import edu.virginia.vcgr.genii.container.resource.IResourceProvider;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;

class BaseGenesisIIServiceConfiguration 
	implements GenesisIIServiceConfiguration
{
	@Override
	public IResourceProvider resourceProvider()
	{
		return null;
	}

	@Override
	public IAuthZProvider defaultAuthZProvider()
	{
		return null;
	}

	@Override
	public IResolverFactoryProxy defaultResolverFactoryProxy()
	{
		return null;
	}

	@Override
	public Long defaultServiceCertificateLifetime()
	{
		return null;
	}

	@Override
	public Long defaultResourceCertificateLifetime()
	{
		return null;
	}

	@Override
	final public Class<? extends JAXBGenesisIIServiceConfiguration> jaxbServiceConfigurationClass()
	{
		return JAXBGenesisIIServiceConfiguration.class;
	}
}