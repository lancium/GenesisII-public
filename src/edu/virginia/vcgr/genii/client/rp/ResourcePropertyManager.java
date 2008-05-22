package edu.virginia.vcgr.genii.client.rp;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;

import javax.xml.namespace.QName; 

/**
 * This class is a "factory" class used to created resource property
 * client stubs.  Users submit RP interfaces to interact with and this
 * factory creates dynamically generated stubs that handle remote outcalls,
 * RP translation, etc.
 * 
 * @author mmm2a
 */
public class ResourcePropertyManager
{
	/**
	 * Create a new RP interface object that can make outcalls and translate
	 * RP values as described by the given interfaces.
	 * 
	 * @param loader The class loader in which to create the new, dynamically
	 * created, RP stub.
	 * @param target The target endpoint to communicate with for remote RP
	 * values.
	 * @param interfaces The list of interfaces (with associated RP 
	 * annotations) which describes the resource properties the target should
	 * have.
	 * @param propertyHints Any property hints that can help narrow down the
	 * resource properties which the client will be asking for.  If left blank,
	 * then all resource properties will be loaded.  Otherwise, only those in
	 * the hints will be asked for at first.  Note that the absence of a 
	 * property in the hints does NOT mean that you cannot get the property, 
	 * only that it won't be cached up initially.
	 * 
	 * @return A newcly created RP manager that can get/set and refresh cached
	 * resource properties.
	 * 
	 * @throws ResourcePropertyException
	 */
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
	
	/**
	 * Create a new RP interface object that can make outcalls and translate
	 * RP values as described by the given interfaces.
	 * 
	 * @param target The target endpoint to communicate with for remote RP
	 * values.
	 * @param interfaces The list of interfaces (with associated RP 
	 * annotations) which describes the resource properties the target should
	 * have.
	 * @param propertyHints Any property hints that can help narrow down the
	 * resource properties which the client will be asking for.  If left blank,
	 * then all resource properties will be loaded.  Otherwise, only those in
	 * the hints will be asked for at first.  Note that the absence of a 
	 * property in the hints does NOT mean that you cannot get the property, 
	 * only that it won't be cached up initially.
	 * 
	 * @return A newcly created RP manager that can get/set and refresh cached
	 * resource properties.
	 * 
	 * @throws ResourcePropertyException
	 */
	static public ResourcePropertyRefresher createRPInterface(
		EndpointReferenceType target,
		Class<?> []interfaces,
		QName ...propertyHints) throws ResourcePropertyException
	{
		return createRPInterface(
			Thread.currentThread().getContextClassLoader(),
			target, interfaces, propertyHints);
	}
	
	/**
	 * Create a new RP interface object that can make outcalls and translate
	 * RP values as described by the given interfaces.
	 * 
	 * @param loader The class loader in which to create the new, dynamically
	 * created, RP stub.
	 * @param target The target endpoint to communicate with for remote RP
	 * values.
	 * @param interfaces The list of interfaces (with associated RP 
	 * annotations) which describes the resource properties the target should
	 * have.
	 * @param propertyHints Any property hints that can help narrow down the
	 * resource properties which the client will be asking for.  If left blank,
	 * then all resource properties will be loaded.  Otherwise, only those in
	 * the hints will be asked for at first.  Note that the absence of a 
	 * property in the hints does NOT mean that you cannot get the property, 
	 * only that it won't be cached up initially.
	 * 
	 * @return A newcly created RP manager that can get/set and refresh cached
	 * resource properties.
	 * 
	 * @throws ResourcePropertyException
	 */
	static public ResourcePropertyRefresher createRPInterface(
			ClassLoader loader,
			EndpointReferenceType target,
			QName []propertyHints,
			Class<?>...interfaces) throws ResourcePropertyException
	{
		return createRPInterface(loader, target, interfaces, propertyHints);
	}
	
	/**
	 * Create a new RP interface object that can make outcalls and translate
	 * RP values as described by the given interfaces.
	 * 
	 * @param target The target endpoint to communicate with for remote RP
	 * values.
	 * @param interfaces The list of interfaces (with associated RP 
	 * annotations) which describes the resource properties the target should
	 * have.
	 * @param propertyHints Any property hints that can help narrow down the
	 * resource properties which the client will be asking for.  If left blank,
	 * then all resource properties will be loaded.  Otherwise, only those in
	 * the hints will be asked for at first.  Note that the absence of a 
	 * property in the hints does NOT mean that you cannot get the property, 
	 * only that it won't be cached up initially.
	 * 
	 * @return A newcly created RP manager that can get/set and refresh cached
	 * resource properties.
	 * 
	 * @throws ResourcePropertyException
	 */
	static public ResourcePropertyRefresher createRPInterface(
			EndpointReferenceType target,
			QName []propertyHints,
			Class<?>...interfaces) throws ResourcePropertyException
	{
		return createRPInterface(target, interfaces, propertyHints);
	}
	
	/**
	 * Create a new RP interface object that can make outcalls and translate
	 * RP values as described by the given interfaces.
	 * 
	 * @param loader The class loader in which to create the new, dynamically
	 * created, RP stub.
	 * @param target The target endpoint to communicate with for remote RP
	 * values.
	 * @param interfaces The list of interfaces (with associated RP 
	 * annotations) which describes the resource properties the target should
	 * have.
	 * 
	 * @return A newcly created RP manager that can get/set and refresh cached
	 * resource properties.
	 * 
	 * @throws ResourcePropertyException
	 */
	static public ResourcePropertyRefresher createRPInterface(
		ClassLoader loader,
		EndpointReferenceType target,
		Class<?>...interfaces) throws ResourcePropertyException
	{
		return createRPInterface(loader, target, new QName[0], interfaces);		
	}
	
	/**
	 * Create a new RP interface object that can make outcalls and translate
	 * RP values as described by the given interfaces.
	 * 
	 * @param target The target endpoint to communicate with for remote RP
	 * values.
	 * @param interfaces The list of interfaces (with associated RP 
	 * annotations) which describes the resource properties the target should
	 * have.
	 * 
	 * @return A newcly created RP manager that can get/set and refresh cached
	 * resource properties.
	 * 
	 * @throws ResourcePropertyException
	 */
	static public ResourcePropertyRefresher createRPInterface(
		EndpointReferenceType target,
		Class<?>...interfaces) throws ResourcePropertyException
	{
		return createRPInterface(target, new QName[0], interfaces);		
	}
}