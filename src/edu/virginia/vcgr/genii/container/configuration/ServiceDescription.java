/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.container.configuration;

import java.util.Properties;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.resolver.IResolverFactoryProxy;
import edu.virginia.vcgr.genii.container.resource.IResourceProvider;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;

public class ServiceDescription
{
	static final private String SECURITY_SETTING_SERVICE_CERTIFICATE_LIFETIME_NAME =
		"edu.virginia.vcgr.genii.container.security.service-certificate-lifetime";
	static final private String SECURITY_SETTING_RESOURCE_CERTIFICATE_LIFETIME_NAME =
		"edu.virginia.vcgr.genii.container.security.resource-certificate-lifetime";
	static final private String DEFAULT_RESOLVER_FACTORY_PROXY_CLASS =
		"edu.virginia.vcgr.genii.container.resolver.default-resolver-factory-proxy-class";
	
	private IResourceProvider _resourceProvider;
	private IAuthZProvider _authZProvider;
	private Long _serviceCertificateLifetime = null;
	private Long _resourceCertificateLifetime = null;
	private Properties _defaultResolverFactoryProps = null;
	private Class<? extends IResolverFactoryProxy> _defaultResolverFactoryProxyClass = null;
	
	@SuppressWarnings("unchecked")
	public ServiceDescription(
			String resourceProviderName, 
			String authzProviderName, 
			Properties securityProperties, 
			Properties defaultResolverFactoryProps)
	{
		if (securityProperties != null)
		{
			String tmp = securityProperties.getProperty(
				SECURITY_SETTING_SERVICE_CERTIFICATE_LIFETIME_NAME);
			if (tmp != null)
				_serviceCertificateLifetime = Long.valueOf(tmp);
			tmp = securityProperties.getProperty(
				SECURITY_SETTING_RESOURCE_CERTIFICATE_LIFETIME_NAME);
			if (tmp != null)
				_resourceCertificateLifetime = Long.valueOf(tmp);
		}
		
		if (defaultResolverFactoryProps != null)
		{
			String tmp = defaultResolverFactoryProps.getProperty(
					DEFAULT_RESOLVER_FACTORY_PROXY_CLASS);
			if (tmp != null)
			{
				try
				{
					/* TODO: check if this is the right way to create proxy class from name */
					_defaultResolverFactoryProxyClass = 
						(Class<? extends IResolverFactoryProxy>)Thread.currentThread().getContextClassLoader(
							).loadClass(tmp);
				}
				catch(ClassNotFoundException cnfe)
				{
					throw new ConfigurationException("Could not find class \"" + tmp, cnfe);
				}
				defaultResolverFactoryProps.remove(DEFAULT_RESOLVER_FACTORY_PROXY_CLASS);
				_defaultResolverFactoryProps = defaultResolverFactoryProps;
			}
		}
		
		Object obj = NamedInstances.getServerInstances().lookup(resourceProviderName);
		if (obj == null) {
			throw new ConfigurationException("Couldn't locate instance \"" +
					resourceProviderName + "\".");
		}
		_resourceProvider = (IResourceProvider)obj;

		obj = NamedInstances.getServerInstances().lookup(authzProviderName);
		if (obj == null) {
			throw new ConfigurationException("Couldn't locate instance \"" +
					authzProviderName + "\".");
		}
		_authZProvider = (IAuthZProvider)obj;
	}
	
	public IResourceProvider retrieveResourceProvider()
	{
		return _resourceProvider;
	}
	
	public IAuthZProvider retrieveAuthZProvider()
	{
		return _authZProvider;
	}

	public long getServiceCertificateLifetime()
	{
		if (_serviceCertificateLifetime != null)
			return _serviceCertificateLifetime.longValue();
		return Container.getDefaultCertificateLifetime();
	}
	
	public long getResourceCertificateLifetime()
	{
		if (_resourceCertificateLifetime != null)
			return _resourceCertificateLifetime.longValue();
		return Container.getDefaultCertificateLifetime();
	}
	
	public Properties getDefaultResolverFactoryProperties()
	{
		return _defaultResolverFactoryProps;
	}

	public Class<? extends IResolverFactoryProxy> getDefaultResolverFactoryProxyClass()
	{
		return _defaultResolverFactoryProxyClass;
	}
}
