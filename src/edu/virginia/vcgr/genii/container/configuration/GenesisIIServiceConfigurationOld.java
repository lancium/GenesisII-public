package edu.virginia.vcgr.genii.container.configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.container.resolver.IResolverFactoryProxy;
import edu.virginia.vcgr.genii.container.resource.IResourceProvider;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;

public class GenesisIIServiceConfigurationOld
{
	private IResourceProvider _resourceProvider;
	private IAuthZProvider _defaultAuthZProvider;
	private IResolverFactoryProxy _defaultResolverFactoryProxy;
	
	static private <Type> Type instantiate(Class<Type> cl)
	{
		if (cl == null)
			return null;
		
		try
		{
			Constructor<Type> cons = cl.getConstructor();
			return cons.newInstance();
		}
		catch (SecurityException e)
		{
			throw new ConfigurationException(String.format(
				"Unable to create instance for configuration item %s.",
				cl), e);
		}
		catch (NoSuchMethodException e)
		{
			throw new ConfigurationException(String.format(
				"Unable to create instance for configuration item %s.",
				cl), e);
		}
		catch (IllegalArgumentException e)
		{
			throw new ConfigurationException(String.format(
				"Unable to create instance for configuration item %s.",
				cl), e);
		}
		catch (InstantiationException e)
		{
			throw new ConfigurationException(String.format(
				"Unable to create instance for configuration item %s.",
				cl), e);
		}
		catch (IllegalAccessException e)
		{
			throw new ConfigurationException(String.format(
				"Unable to create instance for configuration item %s.",
				cl), e);
		}
		catch (InvocationTargetException e)
		{
			throw new ConfigurationException(String.format(
				"Unable to create instance for configuration item %s.",
				cl), e.getCause());
		}
	}
	
	private GenesisIIServiceConfigurationOld(
		Class<? extends IResourceProvider> resourceProviderClass,
		Class<? extends IAuthZProvider> defaultAuthZProviderClass,
		Class<? extends IResolverFactoryProxy> defaultResolverFactoryProxyClass)
	{
		if (resourceProviderClass == null)
			throw new IllegalArgumentException(
				"Resource provider cannot be null.");
		
		if (defaultAuthZProviderClass == null)
			throw new IllegalArgumentException(
				"Default AuthZ provider cannot be null.");
		
		_resourceProvider = instantiate(resourceProviderClass);
		_defaultAuthZProvider = instantiate(defaultAuthZProviderClass);
		_defaultResolverFactoryProxy = instantiate(
			defaultResolverFactoryProxyClass);
	}
	
	final public IResourceProvider resourceProvider()
	{
		return _resourceProvider;
	}
	
	final public IAuthZProvider defaultAuthZProvider()
	{
		return _defaultAuthZProvider;
	}
	
	final public IResolverFactoryProxy defaultResolverFactoryProxy()
	{
		return _defaultResolverFactoryProxy;
	}
	
	static private Map<Class<?>, GenesisIIServiceConfigurationOld> _confMap =
		new HashMap<Class<?>, GenesisIIServiceConfigurationOld>();
	
	static private GenesisIIServiceConfigurationOld discoverServiceConfiguration(
		Class<?> serviceClass)
	{
		Class<? extends IResourceProvider> resourceProviderClass = null;
		Class<? extends IAuthZProvider> authzProviderClass = null;
		Class<? extends IResolverFactoryProxy> resolverFactoryProxyClass 
			= null;
		
		while (serviceClass != Object.class)
		{
			GeniiServiceConfiguration conf = serviceClass.getAnnotation(
				GeniiServiceConfiguration.class);
			if (conf != null)
			{
				if (resourceProviderClass == null)
					resourceProviderClass = conf.resourceProvider();
				if (authzProviderClass == null)
					authzProviderClass = conf.defaultAuthZProvider();
				if (resolverFactoryProxyClass == null)
					resolverFactoryProxyClass = 
						conf.defaultResolverFactoryProxy();
			}
			
			serviceClass = serviceClass.getSuperclass();
		}
		
		return new GenesisIIServiceConfigurationOld(
			resourceProviderClass, authzProviderClass,
			resolverFactoryProxyClass);
	}
	
	static public GenesisIIServiceConfigurationOld configurationFor(
		Class<?> serviceClass)
	{
		GenesisIIServiceConfigurationOld serviceConf;
		
		synchronized(_confMap)
		{
			serviceConf = _confMap.get(serviceClass);
			if (serviceConf == null)
				_confMap.put(serviceClass,
					serviceConf = discoverServiceConfiguration(serviceClass));
		}
		
		return serviceConf;
	}
}