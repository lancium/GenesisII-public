/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.virginia.vcgr.genii.client.comm;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationUnloadedListener;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.KeystoreSecurityConstants;
import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.x509.CertTool;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.system.classloader.GenesisClassLoader;

/**
 * A utility class which allows users to create dynamic, client side proxies for talking to remote service endpoints.
 * 
 * @author mmm2a
 */
public class ClientUtils
{
	static private Log _logger = LogFactory.getLog(ClientUtils.class);

	private static final Pattern _PORT_PATTERN = Pattern.compile("^get.+");

	static private InheritableThreadLocal<Long> _TIMEOUTS = new InheritableThreadLocal<Long>();

	static private final String _PROXY_FACTORY_INSTANCE_NAME = "proxy-factory-instance";

	static private Integer __clientRsaKeyLength = null;

	/**
	 * Class to wipe our loaded config stuff in the event the config manager reloads.
	 */
	static {
		// register ourselves to renew our defaults if the configuration is
		// unloaded
		ConfigurationManager.addConfigurationUnloadListener(new ConfigUnloadListener());
	}

	public static class ConfigUnloadListener implements ConfigurationUnloadedListener
	{
		public void notifyUnloaded()
		{
			synchronized (ClientUtils.class) {
				__clientRsaKeyLength = null;
			}
		}
	}

	/**
	 * Retrieves the client's minimum allowable level of message security
	 */
	static public synchronized int getClientRsaKeyLength() throws AuthZSecurityException
	{
		if (__clientRsaKeyLength != null) {
			return __clientRsaKeyLength;
		}

		String rsaKeyLength = Installation.getDeployment(new DeploymentName()).security()
			.getProperty(KeystoreSecurityConstants.Client.CLIENT_RSA_KEY_LENGTH_PROP);

		__clientRsaKeyLength = Integer.parseInt(rsaKeyLength);
		return __clientRsaKeyLength;
	}

	static public void setDefaultTimeoutForThread(Long newTimeout)
	{
		_TIMEOUTS.set(newTimeout);
	}

	/**
	 * Generates transient key and certificate material to be used for outgoing message security.
	 */
	private static KeyAndCertMaterial generateKeyAndCertMaterial(long timeout, TimeUnit units) throws AuthZSecurityException
	{
		KeyPair keyPair;
		try {
			keyPair = CertTool.generateKeyPair(getClientRsaKeyLength());
		} catch (GeneralSecurityException e) {
			throw new AuthZSecurityException("failure generating keypair: " + e.getLocalizedMessage(), e);
		}
		X509Certificate[] clientCertChain = null;
		try {
			clientCertChain = new X509Certificate[] {
				CertTool.createMasterCert("C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=Client Cert " + (new GUID()).toString(),
					TimeUnit.MILLISECONDS.convert(timeout, units), keyPair.getPublic(), keyPair.getPrivate()) };
		} catch (GeneralSecurityException e) {
			throw new AuthZSecurityException("failure generating keypair: " + e.getLocalizedMessage(), e);

		}
		return new KeyAndCertMaterial(clientCertChain, keyPair.getPrivate());
	}

	/**
	 * Note that it is possible for a calling context not to have ANY key and cert material, in which case this method does nothing.
	 * 
	 * @param callContext
	 *            The calling context containing the
	 * @return The client's key and cert material, re-generated if necessary. (Upon refresh, all previous attributes will be renewed if
	 *         possible, discarded otherwise)
	 */
	public static KeyAndCertMaterial checkAndRenewCredentials(ICallingContext callContext, Date validUntil, SecurityUpdateResults results)
		throws AuthZSecurityException
	{
		if (callContext == null) {
			// we never had any client identity.
			String msg = "No calling-context in which to store credentials.";
			_logger.warn(msg);
			throw new AuthZSecurityException(msg);
		}

		boolean updated = false;
		KeyAndCertMaterial retval = callContext.getActiveKeyAndCertMaterial();
		ArrayList<NuCredential> credentials = null;

		// Ensure client identity is valid
		try {
			credentials = TransientCredentials.getTransientCredentials(callContext).getCredentials();
			if (retval != null) {
				// check the time validity of our client identity
				for (X509Certificate cert : retval._clientCertChain) {
					// Check 10 seconds into the future so as to avoid the credential expiring in-flight.
					cert.checkValidity(new Date(System.currentTimeMillis() + 10000));
				}
			} else if (ConfigurationManager.getCurrentConfiguration().isClientRole()) {
				throw new CertificateExpiredException("Client role with no certificate.");
			}
		} catch (CertificateNotYetValidException e) {
			throw new AuthZSecurityException("certificate is not yet valid: " + e.getLocalizedMessage(), e);
		} catch (CertificateExpiredException e) {
			if (!ConfigurationManager.getCurrentConfiguration().isClientRole()) {
				// We're a resource operating inside this container with a specific identity that has now expired.
				throw new AuthZSecurityException("certificate is no longer valid: " + e.getLocalizedMessage(), e);
			} else {
				// We're in the client role, meaning we can generate our own new client identity.

				_logger.info("Renewing client tool identity until " + validUntil);
				/*
				 * old rule: We create an identity for either 24 hours, or until the valid duration expires (which ever is longer) + 10
				 * seconds of slop for in transit time outs.
				 * 
				 * new rule: we create an identity for the duration requested or our current default time-out, based on whichever is longer,
				 * plus 10 seconds of slop for in transit time outs.
				 */
				retval =
					generateKeyAndCertMaterial(Math.max(edu.virginia.vcgr.genii.security.SecurityConstants.defaultCredentialExpirationMillis,
						validUntil.getTime() - System.currentTimeMillis() + 10000), TimeUnit.MILLISECONDS);
				callContext.setActiveKeyAndCertMaterial(retval);
				updated = true;

				try {
					if (credentials != null) {
						// *Any* delegated credentials must be discarded.
						Iterator<NuCredential> itr = credentials.iterator();
						while (itr.hasNext()) {
							NuCredential cred = itr.next();
							if (cred instanceof TrustCredential) {
								itr.remove();
								try {
									if (_logger.isDebugEnabled()) {
										_logger.debug("Discarding delegated credential " + cred);
									}
								} catch (Exception e2) {
									// ignored; we just can't show it.
								}
								results.noteRemovedCredential(cred);
							}
						}
					}
				} catch (Exception e2) {
					// badness in the credentials means dump them.
					credentials = null;
					TransientCredentials.globalLogout(callContext);
				}
			}
		}

		if (credentials != null) {
			// remove any stale credentials in the set.
			Iterator<NuCredential> itr = credentials.iterator();
			while (itr.hasNext()) {
				NuCredential cred = itr.next();
				try {
					/*
					 * Check 10 seconds into the future so as to avoid the credential expiring in-flight. Here we are only checking that the
					 * credentials date is still good; we should have checked the credentials more thoroughly when they were first added to
					 * the context.
					 */
					cred.checkValidity(new Date(System.currentTimeMillis() + (10 * 1000)));
				} catch (Exception e) {
					updated = true;
					itr.remove();
					try {
						_logger.debug("Discarding credential " + cred, e);
						results.noteRemovedCredential(cred);
					} catch (Exception t) {
						// ignore if really fouled up; we already dropped it but cannot print it.
					}
				}
			}
		}

		// persist any updates
		try {
			if (updated && ConfigurationManager.getCurrentConfiguration().isClientRole()) {
				ContextManager.storeCurrentContext(callContext);
			}
		} catch (Exception ex) {
			_logger.warn(ex, ex);
		}

		return retval;
	}

	/**
	 * Throws out any current credentials. This will ensure that the next call to checkAndRenewCredentials() causes a new TLS certificate to
	 * be created.
	 */
	public static void invalidateCredentials(ICallingContext callContext) throws AuthZSecurityException
	{
		if (callContext == null) {
			// we never had any client identity.
			String msg = "No calling-context in which to store credentials.";
			_logger.warn(msg);
			throw new AuthZSecurityException(msg);
		}
		if (!ConfigurationManager.getCurrentConfiguration().isClientRole()) {
			String msg = "attempting to adjust context as non-client!";
			_logger.warn(msg);
			throw new AuthZSecurityException(msg);
		}

		// whack any current key and certificate.
		callContext.setActiveKeyAndCertMaterial(null);

		TransientCredentials.globalLogout(callContext);
	}

	static public Method getLocatorPortTypeMethod(Class<?> locator) throws ResourceException
	{
		Method[] methods = locator.getMethods();
		Method targetMethod = null;

		for (Method m : methods) {
			Class<?>[] parmTypes = m.getParameterTypes();
			if (parmTypes.length == 1) {
				if (URL.class.isAssignableFrom(parmTypes[0])) {
					Matcher matcher = _PORT_PATTERN.matcher(m.getName());
					if (matcher.matches()) {
						targetMethod = m;
						break;
					}
				}
			}
		}

		if (targetMethod == null)
			throw new ResourceException("It doesn't look like \"" + locator.getName() + "\" is an Axis generated locator.");

		return targetMethod;
	}

	static private Class<?> getLocatorPortType(Class<?> locator) throws ResourceException
	{
		return getLocatorPortTypeMethod(locator).getReturnType();
	}

	static public Class<?>[] getLocatorPortTypes(Class<?>[] locators) throws ResourceException
	{
		Class<?>[] ret = new Class[locators.length];
		for (int lcv = 0; lcv < locators.length; lcv++) {
			ret[lcv] = getLocatorPortType(locators[lcv]);
		}

		return ret;
	}

	static private IProxyFactory _proxyFactory = null;

	synchronized static private IProxyFactory getProxyFactory() throws ResourceException
	{
		if (_proxyFactory == null) {
			_proxyFactory = (IProxyFactory) NamedInstances.getClientInstances().lookup(_PROXY_FACTORY_INSTANCE_NAME);
		}

		return _proxyFactory;
	}

	/**
	 * Create a new, dynamically generated client stub which has the interface specified and is prepared to talk to the endpoint given.
	 * 
	 * @param iface
	 *            The class which represents the java interface that the client stub should implement for communication.
	 * @param epr
	 *            The EndpointReferenceType that indicates the target of the newly generated client stub.
	 * @return An dynamically generated client proxy which implements the passed in interface and is configured to communicate to the given
	 *         EPR.
	 */
	static public <IFace> IFace createProxy(Class<IFace> iface, EndpointReferenceType epr)
		throws ResourceException, GenesisIISecurityException
	{
		try {
			ICallingContext context = null;
			try {
				context = ContextManager.getExistingContext();
			} catch (Throwable t) {
				_logger.warn("Unknown exception occurred trying to create a client proxy.", t);
			}
			return createProxy(iface, epr, context);
		} catch (IOException ioe) {
			throw new ResourceException(ioe.getMessage(), ioe);
		}
	}

	/**
	 * Create a new, dynamically generated client stub which has the interface specified and is prepared to talk to the endpoint given.
	 * 
	 * @param loader
	 *            The class loader to use when generating the new class for this client stub.
	 * @param iface
	 *            The class which represents the java interface that the client stub should implement for communication.
	 * @param epr
	 *            The EndpointReferenceType that indicates the target of the newly generated client stub.
	 * @return An dynamically generated client proxy which implements the passed in interface and is configured to communicate to the given
	 *         EPR.
	 */
	static public <IFace> IFace createProxy(ClassLoader loader, Class<IFace> iface, EndpointReferenceType epr)
		throws ResourceException, GenesisIISecurityException
	{
		try {
			return createProxy(loader, iface, epr, ContextManager.getExistingContext());
		} catch (IOException ioe) {
			throw new ResourceException(ioe.getMessage(), ioe);
		}
	}

	/**
	 * Create a new, dynamically generated client stub which has the interface specified and is prepared to talk to the endpoint given.
	 * 
	 * @param iface
	 *            The class which represents the java interface that the client stub should implement for communication.
	 * @param epr
	 *            The EndpointReferenceType that indicates the target of the newly generated client stub.
	 * @param callContext
	 *            A calling context to use instead of the current context when making out calls.
	 * @return An dynamically generated client proxy which implements the passed in interface and is configured to communicate to the given
	 *         EPR.
	 */
	static public <IFace> IFace createProxy(Class<IFace> iface, EndpointReferenceType epr, ICallingContext callContext)
		throws ResourceException, GenesisIISecurityException
	{
		if (epr==null || epr.getAddress()==null) {
			throw new ResourceException("ClientUtils:CreateProxy received a null EPR or it has a null Address");
		}
		return createProxy(GenesisClassLoader.classLoaderFactory(), iface, epr, callContext);
	}

	/**
	 * Create a new, dynamically generated client stub which has the interface specified and is prepared to talk to the endpoint given.
	 * 
	 * @param loader
	 *            The class loader to use when generating the new class for this client stub.
	 * @param iface
	 *            The class which represents the java interface that the client stub should implement for communication.
	 * @param epr
	 *            The EndpointReferenceType that indicates the target of the newly generated client stub.
	 * @param callContext
	 *            A calling context to use instead of the current context when making out calls.
	 * @return An dynamically generated client proxy which implements the passed in interface and is configured to communicate to the given
	 *         EPR.
	 */
	static public <IFace> IFace createProxy(ClassLoader loader, Class<IFace> iface, EndpointReferenceType epr, ICallingContext callContext)
		throws ResourceException, GenesisIISecurityException
	{
		// this is the root createProxy (within this class) which all the others depend on.
		IProxyFactory factory = getProxyFactory();
		synchronized (epr) {
			IFace face = factory.createProxy(loader, iface, epr, callContext);
			Long timeout = _TIMEOUTS.get();
			if (timeout != null)
				factory.setTimeout(face, timeout.intValue());
			return face;
		}
	}

	/**
	 * Given a dynamically generated proxy generated by the ClientUtils class, return the EPR which the given proxy is configured to
	 * communicate with.
	 * 
	 * @param clientProxy
	 *            The client proxy object generated earlier by this class.
	 * @return The EndpointReferenceType that the given proxy is configured to communicate with.
	 */
	static public EndpointReferenceType extractEPR(Object clientProxy) throws ResourceException
	{
		return getProxyFactory().extractTargetEPR(clientProxy);
	}

	static public void setAttachments(Object clientProxy, Collection<GeniiAttachment> attachments, AttachmentType attachmentType)
		throws ResourceException
	{
		getProxyFactory().setAttachments(clientProxy, attachments, attachmentType);
	}

	static public Collection<GeniiAttachment> getAttachments(Object clientProxy) throws ResourceException
	{
		return getProxyFactory().getAttachments(clientProxy);
	}

	static public GenesisIIEndpointInformation getLastEndpointInformation(Object clientProxy) throws ResourceException
	{
		return getProxyFactory().getLastEndpointInformation(clientProxy);
	}

	static public void setTimeout(Object clientProxy, int timeoutMillis) throws ResourceException
	{
		getProxyFactory().setTimeout(clientProxy, timeoutMillis);
	}

	/**
	 * validates all of the credentials in the calling context. returns true if there are any valid credentials still listed, after possible
	 * cleaning of old / broken ones.
	 */
	public static boolean areCredentialsOkay(ICallingContext callingContext)
	{
		KeyAndCertMaterial clientKeyMaterial;
		try {
			clientKeyMaterial = checkAndRenewCredentials(callingContext, BaseGridTool.credsValidUntil(), new SecurityUpdateResults());
		} catch (AuthZSecurityException e) {
			_logger.error("got an exception when trying to load calling context", e);
			return false;
		}
		if (clientKeyMaterial == null) {
			throw new RuntimeException("failed to retrieve a valid TLS certificate for the client");
		}
		TransientCredentials tranCreds = TransientCredentials.getTransientCredentials(callingContext);
		return (tranCreds != null) && !tranCreds.isEmpty();
	}
}
