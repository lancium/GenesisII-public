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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlCredential;
import edu.virginia.vcgr.genii.client.security.gamlauthz.TransientCredentials;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.*;
import edu.virginia.vcgr.genii.client.security.x509.CertTool;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;

/**
 * A utility class which allows users to create dynamic, client side proxies for talking
 * to remote service endpoints.
 * 
 * @author mmm2a
 */
public class ClientUtils
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(ClientUtils.class);

	static private final String _PROXY_FACTORY_INSTANCE_NAME =
		"proxy-factory-instance";

	public static final String CLIENT_KEY_MATERIAL_CALL_CONTEXT_DATA = 
		"edu.virginia.vcgr.genii.client.security.client-key-material-call-context-data";
	
	static private Pattern _PORT_PATTERN = Pattern.compile(
	"^get.+");
	
	/**
	 * Generates transient key and certificate material to be used 
	 * for outgoing message security.
	 */
	private static KeyAndCertMaterial generateKeyAndCertMaterial() throws GeneralSecurityException {
	    KeyPair keyPair = CertTool.generateKeyPair();
	    X509Certificate[] clientCertChain = {CertTool.createMasterCert(
	    		"C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=Client Cert " + (new GUID()).toString(), 
				GenesisIIConstants.CredentialExpirationMillis,	// valid 24 hours
	    		keyPair.getPublic(), 
	    		keyPair.getPrivate()) };
	    
	    return new KeyAndCertMaterial(clientCertChain, keyPair.getPrivate());
	}

	public static void checkAndRenewCredentials(ICallingContext callContext) throws GeneralSecurityException {

		boolean updated = false;

		ArrayList <GamlCredential> credentials = 
			TransientCredentials.getTransientCredentials(callContext)._credentials;
		
		KeyAndCertMaterial clientKeyMaterial = getActiveKeyAndCertMaterial(callContext);
		
		try {
			// check the time validity of our message signing creds
			for (X509Certificate cert : clientKeyMaterial._clientCertChain) {
				// (Check 10 seconds into the future so as to avoid the credential
				// expiring in-flight)
				cert.checkValidity(new Date(System.currentTimeMillis() + (10 * 1000)));
			}
			
			// if we got here, now we need to check the time validity of our
			// assertion creds
			Iterator<GamlCredential> itr = credentials.iterator();
			while (itr.hasNext()) {
				GamlCredential cred = itr.next();
				if (cred instanceof SignedAssertion) {
					SignedAssertion sa = (SignedAssertion) cred;
					try {
						// (Check 10 seconds into the future so as to avoid the credential
						// expiring in-flight)
						sa.checkValidity(new Date(System.currentTimeMillis() + (10 * 1000)));
					} catch (AttributeExpiredException e) {
						updated = true;
						if (sa instanceof RenewableAssertion) {
							_logger.warn(e.getMessage() + " : Attempting to renew");
							((RenewableAssertion) sa).renew();
						} else {
							_logger.warn(e.getMessage() + " : Discarding");
						}
					}
				}
			}

		} catch (CertificateExpiredException e) {
			// renew the transient message signing creds
			_logger.warn("Renewing transient message signature credentials and renewable assertions, shedding non-renewable credentials.");
			setClientKeyAndCertMaterial(callContext, clientKeyMaterial = generateKeyAndCertMaterial());
			
			// renew signatures for any signed attribute credentials (otherwise toss 'em)
			Iterator<GamlCredential> itr = credentials.iterator();
			while (itr.hasNext()) {
				GamlCredential cred = itr.next();
				if (cred instanceof RenewableAssertion) {
					((RenewableAssertion) cred).renew();
					updated = true;						
				} else if (cred instanceof SignedAssertion) {
					itr.remove();
					updated = true;						
				}
			}
		}

		// persist any updates
		try {
			if (updated) {
				ContextManager.storeCurrentContext(callContext);
			}
		} catch (Exception ex) {
			_logger.warn(ex, ex);
		}
	}
	
	/**
	 * Retrieves the well known key and certificate material in the specified 
	 * calling context that is to be used for outgoing message security.
	 * If none has been explicity set, new material is generated.  
	 */
	synchronized public static KeyAndCertMaterial getActiveKeyAndCertMaterial(ICallingContext callContext) throws GeneralSecurityException {

		KeyAndCertMaterial clientKeyMaterial = (KeyAndCertMaterial) 
		callContext.getTransientProperty(CLIENT_KEY_MATERIAL_CALL_CONTEXT_DATA);
		if (clientKeyMaterial == null) {
			setClientKeyAndCertMaterial(callContext, clientKeyMaterial = generateKeyAndCertMaterial());
		}
		return clientKeyMaterial;
	}
	
	/**
	 * Explicitly sets the well-known key and certificate material in the 
	 * specified calling context that is to be used for outgoing message 
	 * security. 
	 */
	public static void setClientKeyAndCertMaterial(
			ICallingContext callContext, 
			KeyAndCertMaterial clientKeyMaterial) throws GeneralSecurityException {
		callContext.setTransientProperty(CLIENT_KEY_MATERIAL_CALL_CONTEXT_DATA, clientKeyMaterial);
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
	@SuppressWarnings("unchecked")
	synchronized static private IProxyFactory getProxyFactory()
		throws ResourceException, ConfigurationException
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
		throws ResourceException, ConfigurationException, GenesisIISecurityException
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
			throws ResourceException, ConfigurationException, GenesisIISecurityException
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
			throws ResourceException, ConfigurationException, GenesisIISecurityException
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
			throws ResourceException, ConfigurationException, GenesisIISecurityException
	{
		return getProxyFactory().createProxy(loader, iface, epr, callContext);
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
		throws ResourceException, ConfigurationException
	{
		return getProxyFactory().extractTargetEPR(clientProxy);
	}
	
	static public void setAttachments(
		Object clientProxy,
		Collection<GeniiAttachment> attachments,
		AttachmentType attachmentType) 
			throws ResourceException, ConfigurationException
	{
		getProxyFactory().setAttachments(clientProxy, attachments,
			attachmentType);
	}
	
	static public Collection<GeniiAttachment> getAttachments(
		Object clientProxy) 
			throws ResourceException, ConfigurationException
	{
		return getProxyFactory().getAttachments(clientProxy);
	}
}
