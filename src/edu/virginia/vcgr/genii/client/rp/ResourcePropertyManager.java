package edu.virginia.vcgr.genii.client.rp;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;

import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;

import javax.xml.namespace.QName; 

public class ResourcePropertyManager
{
	static public ResourcePropertyRefresher createRPInterface(
		ClassLoader loader,
		EndpointReferenceType target,
		Class<?> []interfaces,
		QName...propertyHints) throws ResourcePropertyException
	{
		Collection<QName> likelyRPs = new ArrayList<QName>(propertyHints.length);
		for (QName name : propertyHints)
			likelyRPs.add(name);
		
		Collection<Class<?>> classes = new ArrayList<Class<?>>(interfaces.length + 1);
		for (Class<?> iface : interfaces)
			classes.add(iface);
		classes.add(ResourcePropertyRefresher.class);
		
		try
		{
			return (ResourcePropertyRefresher)Proxy.newProxyInstance(
				loader, classes.toArray(new Class<?>[0]), new RPInvoker(likelyRPs, target));
		}
		catch (ConfigurationException ce)
		{
			throw new ResourcePropertyException(
				"Configuration exception in Genesis II.", ce);
		}
		catch (GenesisIISecurityException gse)
		{
			throw new ResourcePropertyException(
				"Security exception in Genesis II.", gse);
		}
		catch (ResourceException re)
		{
			throw new ResourcePropertyException(
				"Unknown resource exception in Genesis II.", re);
		}
	}
	
	static public ResourcePropertyRefresher createRPInterface(
		EndpointReferenceType target,
		Class<?> []interfaces,
		QName ...propertyHints) throws ResourcePropertyException
	{
		return createRPInterface(
			Thread.currentThread().getContextClassLoader(),
			target, interfaces, propertyHints);
	}
	
	static public ResourcePropertyRefresher createRPInterface(
			ClassLoader loader,
			EndpointReferenceType target,
			QName []propertyHints,
			Class<?>...interfaces) throws ResourcePropertyException
	{
		return createRPInterface(loader, target, interfaces, propertyHints);
	}
	
	static public ResourcePropertyRefresher createRPInterface(
			EndpointReferenceType target,
			QName []propertyHints,
			Class<?>...interfaces) throws ResourcePropertyException
	{
		return createRPInterface(target, interfaces, propertyHints);
	}
	
	static public ResourcePropertyRefresher createRPInterface(
		ClassLoader loader,
		EndpointReferenceType target,
		Class<?>...interfaces) throws ResourcePropertyException
	{
		return createRPInterface(loader, target, new QName[0], interfaces);		
	}
	
	static public ResourcePropertyRefresher createRPInterface(
		EndpointReferenceType target,
		Class<?>...interfaces) throws ResourcePropertyException
	{
		return createRPInterface(target, new QName[0], interfaces);		
	}
}