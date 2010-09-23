package edu.virginia.vcgr.genii.container.configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.container.resolver.IResolverFactoryProxy;
import edu.virginia.vcgr.genii.container.resource.IResourceProvider;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;

@XmlAccessorType(XmlAccessType.NONE)
public class JAXBGenesisIIServiceConfiguration
	implements GenesisIIServiceConfiguration
{
	private GenesisIIServiceConfiguration _parent = null;
	
	private IResourceProvider _resourceProvider = null;
	private IAuthZProvider _defaultAuthZProvider = null;
	private IResolverFactoryProxy _defaultResolverFactoryProxy = null;
	
	@XmlAttribute(name = "default-service-certificate-lifetime", 
		required = false)
	private Long _defaultServiceCertificateLifetime = null;
	
	@XmlAttribute(name = "default-resource-certificate-lifetime",
		required = false)
	private Long _defaultResourceCertificateLifetime = null;
	
	static private <Type> Type instantiate(
		Class<Type> typeCast, String className)
			throws Throwable
	{
		if (className == null)
			return null;
		
		try
		{
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Class<?> cl = loader.loadClass(className);
			Constructor<?> cons = cl.getConstructor();
			return typeCast.cast(cons.newInstance());
		}
		catch (InvocationTargetException ite)
		{
			throw ite.getCause();
		}
	}
	
	@SuppressWarnings("unused")
	@XmlAttribute(name = "resource-provider", required = false)
	private void setResourceProviderClass(String className) throws Throwable
	{
		_resourceProvider = instantiate(IResourceProvider.class, className);
	}
	
	@SuppressWarnings("unused")
	private String getResourceProviderClass()
	{
		return (_resourceProvider == null) ? 
			null : _resourceProvider.getClass().getName();
	}
	
	@SuppressWarnings("unused")
	@XmlAttribute(name = "default-authz-provider", required = false)
	private void setDefaultAuthZProviderClass(String className) 
		throws Throwable
	{
		_defaultAuthZProvider = instantiate(IAuthZProvider.class, className);
	}
	
	@SuppressWarnings("unused")
	private String getDefaultAuthZProviderClass()
	{
		return (_defaultAuthZProvider == null) ?
			null : _defaultAuthZProvider.getClass().getName();
	}
	
	@SuppressWarnings("unused")
	@XmlAttribute(name = "default-resolver-factory-proxy", required = false)
	private void setDefaultResolverFactoryProxyClass(String className)
		throws Throwable
	{
		_defaultResolverFactoryProxy = instantiate(
			IResolverFactoryProxy.class, className);
	}
	
	@SuppressWarnings("unused")
	private String getDefaultResolverFactoryProxyClass()
	{
		return (_defaultResolverFactoryProxy == null) ?
			null : _defaultResolverFactoryProxy.getClass().getName();
	}
	
	protected void setParent(GenesisIIServiceConfiguration configuration)
	{
		_parent = configuration;
	}
	
	@Override
	final public IResourceProvider resourceProvider()
	{
		return (_resourceProvider == null) ?
			_parent.resourceProvider() : _resourceProvider;
	}

	@Override
	final public IAuthZProvider defaultAuthZProvider()
	{
		return (_defaultAuthZProvider == null) ?
			_parent.defaultAuthZProvider() : _defaultAuthZProvider;
	}

	@Override
	final public IResolverFactoryProxy defaultResolverFactoryProxy()
	{
		return (_defaultResolverFactoryProxy == null) ?
			_parent.defaultResolverFactoryProxy() : _defaultResolverFactoryProxy;
	}

	@Override
	final public Long defaultServiceCertificateLifetime()
	{
		return (_defaultServiceCertificateLifetime == null) ?
			_parent.defaultServiceCertificateLifetime() :
			_defaultServiceCertificateLifetime;
	}

	@Override
	final public Long defaultResourceCertificateLifetime()
	{
		return (_defaultResourceCertificateLifetime == null) ?
			_parent.defaultResourceCertificateLifetime() :
			_defaultResourceCertificateLifetime;
	}

	@Override
	final public Class<? extends JAXBGenesisIIServiceConfiguration> jaxbServiceConfigurationClass()
	{
		return _parent.jaxbServiceConfigurationClass();
	}
}