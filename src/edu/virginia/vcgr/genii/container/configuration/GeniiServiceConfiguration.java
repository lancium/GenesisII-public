package edu.virginia.vcgr.genii.container.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.virginia.vcgr.genii.container.resolver.IResolverFactoryProxy;
import edu.virginia.vcgr.genii.container.resource.IResourceProvider;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GeniiServiceConfiguration {
	Class<? extends IResourceProvider> resourceProvider() default IResourceProvider.class;

	Class<? extends IAuthZProvider> defaultAuthZProvider() default IAuthZProvider.class;

	Class<? extends IResolverFactoryProxy> defaultResolverFactoryProxy() default IResolverFactoryProxy.class;

	long defaultServiceCertificateLifetime() default -1L;

	long defaultResourceCertificateLifetime() default -1L;

	Class<? extends JAXBGenesisIIServiceConfiguration> jaxbServiceConfigurationClass() default JAXBGenesisIIServiceConfiguration.class;
}