package edu.virginia.vcgr.genii.container.configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.container.resolver.IResolverFactoryProxy;
import edu.virginia.vcgr.genii.container.resource.IResourceProvider;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;

class AnnotationSuppliedGenesisIIServiceConfiguration
	implements GenesisIIServiceConfiguration
{
	private IResourceProvider _resourceProvider = null;
	private IAuthZProvider _defaultAuthZProvider = null;
	private IResolverFactoryProxy _defaultResolverFactoryProxy = null;
	private Long _defaultServiceCertificateLifetime = null;
	private Long _defaultResourceCertificateLifetime = null;
	private Class<? extends JAXBGenesisIIServiceConfiguration>
		_jaxbServiceConfigurationClass = null;
	
	static private <Type> Class<Type> getNonInterfaceClass(Class<Type> cl)
	{
		if (cl == null || cl.isInterface())
			return null;
		
		return cl;
	}
	
	static private <Type> Type instantiate(Class<Type> cl)
	{
		Constructor<Type> cons;
		
		cl = getNonInterfaceClass(cl);
		if (cl == null)
			return null;
		
		try
		{
			cons = cl.getConstructor();
			return cons.newInstance();
		}
		catch (SecurityException e)
		{
			throw new ConfigurationException(String.format(
				"Unable to instantiate %s for service configuration.",
				cl), e);
		}
		catch (NoSuchMethodException e)
		{
			throw new ConfigurationException(String.format(
				"Unable to instantiate %s for service configuration.",
				cl), e);
		}
		catch (IllegalArgumentException e)
		{
			throw new ConfigurationException(String.format(
				"Unable to instantiate %s for service configuration.",
				cl), e);
		}
		catch (InstantiationException e)
		{
			throw new ConfigurationException(String.format(
				"Unable to instantiate %s for service configuration.",
				cl), e);
		}
		catch (IllegalAccessException e)
		{
			throw new ConfigurationException(String.format(
				"Unable to instantiate %s for service configuration.",
				cl), e);
		}
		catch (InvocationTargetException e)
		{
			throw new ConfigurationException(String.format(
				"Unable to instantiate %s for service configuration.",
				cl), e.getCause());
		}
	}
	
	static private Long getPositiveLong(long value)
	{
		if (value > 0)
			return new Long(value);
		
		return null;
	}
	
	AnnotationSuppliedGenesisIIServiceConfiguration(
		GeniiServiceConfiguration serviceConfiguration)
	{
		if (serviceConfiguration != null)
		{
			_resourceProvider = instantiate(
				serviceConfiguration.resourceProvider());
			_defaultAuthZProvider = instantiate(
				serviceConfiguration.defaultAuthZProvider());
			_defaultResolverFactoryProxy = instantiate(
				serviceConfiguration.defaultResolverFactoryProxy());
			
			_defaultServiceCertificateLifetime = getPositiveLong(
				serviceConfiguration.defaultServiceCertificateLifetime());
			_defaultResourceCertificateLifetime = getPositiveLong(
				serviceConfiguration.defaultResourceCertificateLifetime());
			
			_jaxbServiceConfigurationClass = getNonInterfaceClass( 
				serviceConfiguration.jaxbServiceConfigurationClass());
		}
	}
	
	AnnotationSuppliedGenesisIIServiceConfiguration(Class<?> cl)
	{
		this(cl.getAnnotation(GeniiServiceConfiguration.class));
	}

	@Override
	final public IResourceProvider resourceProvider()
	{
		return _resourceProvider;
	}

	@Override
	final public IAuthZProvider defaultAuthZProvider()
	{
		return _defaultAuthZProvider;
	}

	@Override
	final public IResolverFactoryProxy defaultResolverFactoryProxy()
	{
		return _defaultResolverFactoryProxy;
	}

	@Override
	final public Long defaultServiceCertificateLifetime()
	{
		return _defaultServiceCertificateLifetime;
	}

	@Override
	final public Long defaultResourceCertificateLifetime()
	{
		return _defaultResourceCertificateLifetime;
	}

	@Override
	final public Class<? extends JAXBGenesisIIServiceConfiguration>
		jaxbServiceConfigurationClass()
	{
		return _jaxbServiceConfigurationClass;
	}
}