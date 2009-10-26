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
package edu.virginia.vcgr.genii.client.comm;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateExpiredException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationUnloadedListener;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.client.configuration.SecurityConstants;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.client.security.credentials.TransientCredentials;
import edu.virginia.vcgr.genii.client.security.credentials.assertions.*;
import edu.virginia.vcgr.genii.client.security.x509.CertTool;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;

/**
 * A utility class which allows users to create dynamic, client side proxies for talking
 * to remote service endpoints.
 * 
 * @author mmm2a
 */
public class ClientUtils
{
	static private Log _logger = LogFactory.getLog(ClientUtils.class);

	static private InheritableThreadLocal<Long> _TIMEOUTS =
		new InheritableThreadLocal<Long>();
	
	static private final String _PROXY_FACTORY_INSTANCE_NAME =
		"proxy-factory-instance";
	
	static private Pattern _PORT_PATTERN = Pattern.compile(
	"^get.+");
	
	static private Integer __clientRsaKeyLength = null;

	/**
	 * Class to wipe our loaded config stuff in the event the config manager
	 * reloads. 
	 */
	static {
		// register ourselves to renew our defaults if the configuration is 
		// unloaded
		ConfigurationManager.addConfigurationUnloadListener(new ConfigUnloadListener());
	}
	public static class ConfigUnloadListener implements ConfigurationUnloadedListener {
		public void notifyUnloaded() {
			synchronized(ClientUtils.class) { 
				__clientRsaKeyLength = null;
			}
		}
	}
	
	/**
	 * Retrieves the client's minimum allowable level of message security
	 */	
	static public synchronized int getClientRsaKeyLength() throws GeneralSecurityException {

		if (__clientRsaKeyLength != null) { 
			return __clientRsaKeyLength;
		}
		
		String rsaKeyLength = 
			Installation.getDeployment(new DeploymentName()).security().getProperty(
				SecurityConstants.Client.CLIENT_RSA_KEY_LENGTH_PROP);
			
		__clientRsaKeyLength =  Integer.parseInt(rsaKeyLength);
		return __clientRsaKeyLength;
	}
	
	static public void setDefaultTimeoutForThread(Long newTimeout)
	{
		_TIMEOUTS.set(newTimeout);
	}
	
	/**
	 * Generates transient key and certificate material to be used 
	 * for outgoing message security.
	 */
	private static KeyAndCertMaterial generateKeyAndCertMaterial(
		long timeout, TimeUnit units) throws GeneralSecurityException {
	    KeyPair keyPair = CertTool.generateKeyPair(getClientRsaKeyLength());
	    X509Certificate[] clientCertChain = {CertTool.createMasterCert(
	    		"C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=Client Cert " + (new GUID()).toString(), 
				TimeUnit.MILLISECONDS.convert(timeout, units),
	    		keyPair.getPublic(), 
	    		keyPair.getPrivate()) };
	    
	    return new KeyAndCertMaterial(clientCertChain, keyPair.getPrivate());
	}

	/**
	 * Note that it is possible for a calling context not to have ANY key and cert material,
	 * in which case this method does nothing.
	 * @param callContext The calling context containing the 
	 * @return The client's key and cert material, re-generated if necessary.  (Upon refresh, 
	 *   all previous attributes will be renewed if possible, discarded otherwise)  
	 * @throws GeneralSecurityException
	 */
	public static KeyAndCertMaterial checkAndRenewCredentials(
		ICallingContext callContext, Date validUntil, 
		SecurityUpdateResults results) throws GeneralSecurityException 
	{
		if (callContext == null) {
			// we never had any client identity
			throw new CertificateExpiredException("No calling-context in which to store credentials.");
		}

		boolean updated = false;
		KeyAndCertMaterial retval = callContext.getActiveKeyAndCertMaterial();
		ArrayList <GIICredential> credentials = 
			TransientCredentials.getTransientCredentials(callContext)._credentials;

		// Ensure client identity is valid
		try
		{
			if (retval != null) 
			{
				// check the time validity of our client identity
				for (X509Certificate cert : retval._clientCertChain) 
				{
					// (Check 10 seconds into the future so as to avoid the credential
					// expiring in-flight)
					cert.checkValidity(new Date(validUntil.getTime() + 10000));
				}
			} else if (ConfigurationManager.getCurrentConfiguration().isClientRole())
			{
				throw new CertificateExpiredException("Client role with no certificate.");
			}
		}
		catch (CertificateExpiredException e) 
		{
			if (!ConfigurationManager.getCurrentConfiguration().isClientRole())
			{
				// We're a resource operating inside this container with 
				// a specific identity that has now expired.
				throw e;
			} else
			{	
				// We're in the client role, meaning we can generate our own 
				// new client identity.

				_logger.warn("Renewing client tool identity.");
				// We create an identity for either 24 hours, or until the valid
				// duration expires (which ever is longer) + 10 seconds of slop for
				// in transit time outs.
				retval = generateKeyAndCertMaterial(
					Math.max(
						GenesisIIConstants.CredentialExpirationMillis, // valid 24 hours
						validUntil.getTime() - System.currentTimeMillis() + 10000),
					TimeUnit.MILLISECONDS);
				callContext.setActiveKeyAndCertMaterial(retval);
				updated = true;
				
				// Any delegated credentials must be discarded or renewed
				Iterator<GIICredential> itr = credentials.iterator();
				while (itr.hasNext())
				{
					GIICredential cred = itr.next();
					if (cred instanceof DelegatedAssertion) 
					{
						if (cred instanceof Renewable) 
						{
							_logger.warn(e.getMessage() + " : Attempting to renew credential " + cred);
							TransientCredentials._logger.debug("Attempting to renew stale credential from current calling context credentials.");
							((Renewable) cred).renew();
							results.noteRenewedCredential(cred);
						} else 
						{
							_logger.warn("Discarding non-renewable delegated credential " + cred);
							TransientCredentials._logger.debug("Removing stale non-renewable credential from current calling context credentials.");
							itr.remove();
							results.noteRemovedCredential(cred);
						}
					}
				}
			}
		}
		
		// remove stale credentials
		Iterator<GIICredential> itr = credentials.iterator();
		while (itr.hasNext()) 
		{
			GIICredential cred = itr.next();
			try 
			{
				// (Check 10 seconds into the future so as to avoid the credential
				// expiring in-flight)
				cred.checkValidity(new Date(System.currentTimeMillis() + (10 * 1000)));
			}
			catch (AttributeExpiredException e) 
			{
				updated = true;
				if (cred instanceof Renewable)
				{
					_logger.warn(e.getMessage() + " : Attempting to renew credential " + cred);
					((Renewable) cred).renew();
					results.noteRenewedCredential(cred);
				} else 
				{
					_logger.warn(e.getMessage() + " : Discarding credential " + cred);
					itr.remove();
					results.noteRemovedCredential(cred);
				}
			}
		}

		// persist any updates
		try 
		{
			if (updated) 
			{
				ContextManager.storeCurrentContext(callContext);
			}
		} 
		catch (Exception ex) 
		{
			_logger.warn(ex, ex);
		}
		
		return retval;
	}
	
	static public Method getLocatorPortTypeMethod(Class<?> locator)
		throws ResourceException
	{	
		Method []methods = locator.getMethods();
		Method targetMethod = null;
		
		for (Method m : methods)
		{
			Class<?> []parmTypes = m.getParameterTypes();
			if (parmTypes.length == 1)
			{
				if (URL.class.isAssignableFrom(parmTypes[0]))
				{
					Matcher matcher = _PORT_PATTERN.matcher(m.getName());
					if (matcher.matches())
					{
						targetMethod = m;
						break;
					}
				}
			}
		}
		

		if (targetMethod == null)
			throw new ResourceException("It doesn't look like \"" +
				locator.getName() + 
				"\" is an Axis generated locator.");
		
		return targetMethod;
	}
	
	static private Class<?> getLocatorPortType(Class<?> locator)
		throws ResourceException
	{
		return getLocatorPortTypeMethod(locator).getReturnType();
	}
	
	static public Class<?>[] getLocatorPortTypes(Class<?> []locators)
		throws ResourceException
	{
		Class<?> []ret = new Class[locators.length];
		for (int lcv = 0; lcv < locators.length; lcv++)
		{
			ret[lcv] = getLocatorPortType(locators[lcv]);
		}
		
		return ret;
	}

	static private IProxyFactory _proxyFactory = null;
	
	synchronized static private IProxyFactory getProxyFactory()
		throws ResourceException
	{
		if (_proxyFactory == null)
		{
			_proxyFactory = (IProxyFactory)NamedInstances.getClientInstances().lookup(
				_PROXY_FACTORY_INSTANCE_NAME);
		}
		
		return _proxyFactory;
	}
	
	/**
	 * Create a new, dynamically generated client stub which has the interface
	 * specified and is prepared to talk to the endpoint given.
	 * 
	 * @param iface The class which represents the java interface that the client stub
	 * should implement for communication.
	 * @param epr The EndpointReferenceType that indicates the target of the newly
	 * generated client stub.
	 * @return An dynamically generated client proxy which implements the passed in
	 * interface and is configured to communicate to the given EPR.
	 */
	static public <IFace> IFace createProxy(Class<IFace> iface, 
		EndpointReferenceType epr) 
		throws ResourceException, GenesisIISecurityException
	{
		try
		{
			ICallingContext context = null;
			
			try
			{
				context = ContextManager.getCurrentContext(false);
			}
			catch (Throwable t)
			{
				_logger.warn("Unknown exception occurred trying to create a client proxy.", t);
			}
			
			return createProxy(iface, epr, context);
		}
		catch (IOException ioe)
		{
			throw new ResourceException(ioe.getMessage(), ioe);
		}
	}
	
	/**
	 * Create a new, dynamically generated client stub which has the interface
	 * specified and is prepared to talk to the endpoint given.
	 * 
	 * @param loader The class loader to use when generating the new class for this
	 * client stub.
	 * @param iface The class which represents the java interface that the client stub
	 * should implement for communication.
	 * @param epr The EndpointReferenceType that indicates the target of the newly
	 * generated client stub.
	 * @return An dynamically generated client proxy which implements the passed in
	 * interface and is configured to communicate to the given EPR.
	 */
	static public <IFace> IFace createProxy(ClassLoader loader,
		Class<IFace> iface, EndpointReferenceType epr)
			throws ResourceException, GenesisIISecurityException
	{
		try
		{
			return createProxy(loader, iface, epr, ContextManager.getCurrentContext(false));
		}
		catch (IOException ioe)
		{
			throw new ResourceException(ioe.getMessage(), ioe);
		}
	}
		
	/**
	 * Create a new, dynamically generated client stub which has the interface
	 * specified and is prepared to talk to the endpoint given.
	 * 
	 * @param iface The class which represents the java interface that the client stub
	 * should implement for communication.
	 * @param epr The EndpointReferenceType that indicates the target of the newly
	 * generated client stub.
	 * @param callContext A calling context to use instead of the current context when
	 * making out calls.
	 * @return An dynamically generated client proxy which implements the passed in
	 * interface and is configured to communicate to the given EPR.
	 */
	static public <IFace> IFace createProxy(Class<IFace> iface, 
		EndpointReferenceType epr, ICallingContext callContext)
			throws ResourceException, GenesisIISecurityException
	{
		return createProxy(Thread.currentThread().getContextClassLoader(),
			iface, epr, callContext);
	}
	
	/**
	 * Create a new, dynamically generated client stub which has the interface
	 * specified and is prepared to talk to the endpoint given.
	 * 
	 * @param loader The class loader to use when generating the new class for this
	 * client stub.
	 * @param iface The class which represents the java interface that the client stub
	 * should implement for communication.
	 * @param epr The EndpointReferenceType that indicates the target of the newly
	 * generated client stub.
	 * @param callContext A calling context to use instead of the current context when
	 * making out calls.
	 * @return An dynamically generated client proxy which implements the passed in
	 * interface and is configured to communicate to the given EPR.
	 */
	static public <IFace> IFace createProxy(ClassLoader loader,
		Class<IFace> iface, EndpointReferenceType epr, ICallingContext callContext)
			throws ResourceException, GenesisIISecurityException
	{
		IProxyFactory factory = getProxyFactory();
		IFace face = factory.createProxy(loader, iface, epr, callContext);
		
		Long timeout = _TIMEOUTS.get();
		if (timeout != null)
			factory.setTimeout(face, timeout.intValue());
		
		return face;
	}
	
	/**
	 * Given a dynamically generated proxy generated by the ClientUtils class, return
	 * the EPR which the given proxy is configured to communicate with.
	 * 
	 * @param clientProxy The client proxy object generated earlier by this class.
	 * @return The EndpointReferenceType that the given proxy is configured to 
	 * communicate with.
	 */
	static public EndpointReferenceType extractEPR(Object clientProxy)
		throws ResourceException
	{
		return getProxyFactory().extractTargetEPR(clientProxy);
	}
	
	static public void setAttachments(
		Object clientProxy,
		Collection<GeniiAttachment> attachments,
		AttachmentType attachmentType) 
			throws ResourceException
	{
		getProxyFactory().setAttachments(clientProxy, attachments,
			attachmentType);
	}
	
	static public Collection<GeniiAttachment> getAttachments(
		Object clientProxy) 
			throws ResourceException
	{
		return getProxyFactory().getAttachments(clientProxy);
	}
	
	static public GenesisIIEndpointInformation getLastEndpointInformation(
		Object clientProxy) throws ResourceException
	{
		return getProxyFactory().getLastEndpointInformation(clientProxy);
	}
	
	static public void setTimeout(Object clientProxy, int timeoutMillis)
		throws ResourceException
	{
		getProxyFactory().setTimeout(clientProxy, timeoutMillis);
	}
}
