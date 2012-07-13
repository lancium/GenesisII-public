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

package edu.virginia.vcgr.genii.container.axis;

import org.apache.ws.axis.security.WSDoAllReceiver;
import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.AbstractCrypto;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.*;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

import edu.virginia.vcgr.genii.client.comm.axis.security.GIIBouncyCrypto;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.client.context.*;

import org.morgan.util.configuration.*;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;

import edu.virginia.vcgr.genii.client.security.authz.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.container.security.authz.providers.*;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.security.MessageLevelSecurityRequirements;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.security.credentials.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.assertions.DelegatedAssertion;
import edu.virginia.vcgr.genii.security.credentials.assertions.SignedAssertion;
import edu.virginia.vcgr.genii.security.credentials.identity.*;

public class ServerWSDoAllReceiver extends WSDoAllReceiver
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(ServerWSDoAllReceiver.class);

	static public final String CRYPTO_ALIAS = "CRYPTO_ALIAS";
	static private final String CRYTO_PASS = "pwd";
	static private final String SIG_CRYPTO_PROPERTY = 
		GIIBouncyCrypto.class.getCanonicalName();

	private static PrivateKey _serverPrivateKey;

	public ServerWSDoAllReceiver()
	{
	}

	public void configure(PrivateKey serverPrivateKey)
	{
		_serverPrivateKey = serverPrivateKey;

		setOption(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN
				+ " " + WSHandlerConstants.TIMESTAMP + " "
				+ WSHandlerConstants.SIGNATURE + " "
				+ WSHandlerConstants.ENCRYPT);
		setOption(WSHandlerConstants.PW_CALLBACK_CLASS,
				ServerWSDoAllReceiver.ServerPWCallback.class.getName());
		setOption(WSHandlerConstants.USER, ServerWSDoAllReceiver.CRYPTO_ALIAS);
	}

	public ServerWSDoAllReceiver(PrivateKey serverPrivateKey)
	{
		_serverPrivateKey = serverPrivateKey;
	}

	public void invoke(MessageContext msgContext) throws AxisFault
	{
		try
		{
			IResource resource =
					ResourceManager.getCurrentResource().dereference();
			
			IAuthZProvider authZHandler =
					AuthZProviders.getProvider(resource.getParentResourceKey()
							.getServiceName());

			if ((authZHandler == null)
					|| (authZHandler.getMinIncomingMsgLevelSecurity(resource)
							.isNone()))
			{
				resource.commit();
				// We have no requirements for incoming message security. If
				// there
				// are no incoming headers, don't do any crypto processing

				Message sm = msgContext.getCurrentMessage();
				if (sm == null)
				{
					// We did not receive anything...Usually happens when we get
					// a
					// HTTP 202 message (with no content)
					return;
				}

				Document doc = sm.getSOAPEnvelope().getAsDocument();
				String actor = (String) getOption(WSHandlerConstants.ACTOR);
				SOAPConstants sc =
						WSSecurityUtil.getSOAPConstants(doc
								.getDocumentElement());
				if (WSSecurityUtil.getSecurityHeader(doc, actor, sc) == null)
				{
					// perform Authz
					performAuthz();		
					return;
				}
			}
			resource.commit();

		}
		catch (Exception e)
		{
			_logger.error(
				"An error occurred while trying to handler server-side, receiver security.",
				e);
			throw new AxisFault(
					"Exception thrown while retrieving security headers.", e);
		}

		// process all incoming security headers
		super.invoke(msgContext);
		
		// perform Authz
		performAuthz();		
	}

	protected boolean checkReceiverResults(Vector wsResult, Vector actions)
	{

		// checks to see if the security operations performed meet the minimum
		// required (as per the resource's authZ module)

		try
		{
			// get the resource's min messsage-sec level
			MessageLevelSecurityRequirements resourceMinMsgSec;
			IResource resource =
					ResourceManager.getCurrentResource().dereference();
			IAuthZProvider authZHandler =
					AuthZProviders.getProvider(resource.getParentResourceKey()
							.getServiceName());

			if (authZHandler == null)
			{
				resourceMinMsgSec = new MessageLevelSecurityRequirements();
			}
			else
			{
				resourceMinMsgSec =
						authZHandler.getMinIncomingMsgLevelSecurity(resource);
			}

			// retrieve what we required from the actions vector
			int performed = MessageLevelSecurityRequirements.NONE;
			for (int i = 0; i < wsResult.size(); i++)
			{
				int action =
						((WSSecurityEngineResult) wsResult.get(i)).getAction();
				switch (action)
				{
				case WSConstants.SIGN:
					performed |= MessageLevelSecurityRequirements.SIGN;
					break;
				case WSConstants.ENCR:
					performed |= MessageLevelSecurityRequirements.ENCRYPT;
					break;
				case WSConstants.UT:
					break;
				}
			}

			// check to make sure we met our min level
			if ((new MessageLevelSecurityRequirements(performed))
					.superset(resourceMinMsgSec))
			{
				return true;
			}

		}
		catch (ResourceException e)
		{
			_logger.info("ResourceException occurred in checkReceiverResults", e);
			return false;
		}
		catch (AuthZSecurityException e)
		{
			_logger.info("AuthZException occurred in checkReceiverResults", e);
			return false;
		}
		catch (AxisFault e)
		{
			_logger.info("AxisFault occurred in checkReceiverResults", e);
			return false;
		}

		return false;
	}

	/**
	 * Hook to allow subclasses to load their Signature Crypto however they see
	 * fit.
	 */
	public Crypto loadSignatureCrypto(RequestData reqData)
			throws WSSecurityException
	{

		AbstractCrypto crypto = null;
		try
		{
			// create an in-memory keystore for the incoming sig key material
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			crypto = new GIIBouncyCrypto();
			crypto.setKeyStore(keyStore);

			// store our sig crypto for use later in retrieving 
			// message-signing creds
			((MessageContext)reqData.getMsgContext()).
				setProperty(SIG_CRYPTO_PROPERTY, crypto);
			
			return crypto;

		}
		catch (IOException e)
		{
			throw new WSSecurityException(e.getMessage(), e);
		}
		catch (java.security.GeneralSecurityException e)
		{
			throw new WSSecurityException(e.getMessage(), e);
		}
		catch (org.apache.ws.security.components.crypto.CredentialException e)
		{
			throw new WSSecurityException(e.getMessage(), e);
		}
	}
	

	/**
	 * Hook to allow subclasses to load their Encryption Crypto however they see
	 * fit.
	 */
	protected Crypto loadDecryptionCrypto(RequestData reqData)
			throws WSSecurityException
	{

		AbstractCrypto crypto = null;
		try
		{
			// create an in-memory keystore for the server's key material
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			// place the resource's cert chain and epi in the working context --
			// necessary for
			// response message-security in case we actually delete this
			// resource
			// as part of this operation
			IResource resource =
					ResourceManager.getCurrentResource().dereference();
			Certificate[] targetCertChain =
					(Certificate[]) resource
							.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
			String epi =
					(resource
							.getProperty(IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME))
							.toString();
			WorkingContext wctxt = WorkingContext.getCurrentWorkingContext();
			wctxt.setProperty(WorkingContext.CERT_CHAIN_KEY, targetCertChain);
			wctxt.setProperty(WorkingContext.EPI_KEY, epi);

			if (targetCertChain != null)
			{
				keyStore.setKeyEntry(epi, _serverPrivateKey, CRYTO_PASS
						.toCharArray(), targetCertChain);
			}

			crypto = new GIIBouncyCrypto();
			crypto.setKeyStore(keyStore);

			return crypto;

		}
		catch (BaseFaultType bft)
		{
			BaseFaultTypeDescription[] desc = bft.getDescription();
			if (desc != null && desc.length >= 1)
				throw new WSSecurityException(desc[0].get_value(), bft);
			else
				throw new WSSecurityException(bft.dumpToString(), bft);
		}
		catch (IOException e)
		{
			throw new WSSecurityException(e.getMessage(), e);
		}
		catch (java.security.GeneralSecurityException e)
		{
			throw new WSSecurityException(e.getMessage(), e);
		}
		catch (org.apache.ws.security.components.crypto.CredentialException e)
		{
			throw new WSSecurityException(e.getMessage(), e);
		}
	}


	/**
	 * Authenticate any holder-of-key (i.e., signed) bearer credentials
	 * using the given authenticated certificate-chains.
	 * 
	 * Returns a cumulative collection identities composed from both 
	 * the bearer- and authenticated- credentials
	 * 
	 */
	public static Collection<GIICredential> authenticateBearerCredentials(
			ArrayList<GIICredential> bearerCredentials,
			ArrayList<X509Certificate[]> authenticatedCertChains, 
			X509Certificate[] targetCertChain) 
			
			throws AuthZSecurityException, GeneralSecurityException {
		
		HashSet<GIICredential> retval = new HashSet<GIICredential>();
		
		// Add the authenticated certificate chains
		for (X509Certificate[] certChain : authenticatedCertChains) {
			retval.add(new X509Identity(certChain, IdentityType.CONNECTION));
		}
		
		// Corroborate the bearer credentials
		for (GIICredential cred : bearerCredentials)
		{
			if (cred instanceof SignedAssertion)
			{
				// Holder-of-key token
				SignedAssertion signedAssertion = (SignedAssertion) cred;

				// Check validity and verify integrity
				signedAssertion.checkValidity(new Date());
				signedAssertion.validateAssertion();

				// If the assertion is pre-authorized for us, unwrap one
				// layer
				if ((targetCertChain != null)
						&& (signedAssertion.getAuthorizedIdentity()[0]
								.equals(targetCertChain[0])))
				{
					if (!(signedAssertion instanceof DelegatedAssertion))
					{
						throw new AuthZSecurityException(
								"GAML credential \""
										+ signedAssertion
										+ "\" does not match the incoming message sender");
					}
					signedAssertion =
							((DelegatedAssertion) signedAssertion).unwrap();
				}

				// Verify that the request message signer is the same as the
				// one of the holder-of-key certificates
				boolean match = false;
				for (X509Certificate[] callerCertChain : authenticatedCertChains) {
					if (callerCertChain[0].equals(signedAssertion.getAuthorizedIdentity()[0])) {
						match = true;
						break;
					}
				}
				
				if (!match) {
					throw new AuthZSecurityException(
							"GII credential \""
									+ signedAssertion
									+ "\" does not match the incoming message sender");
				}
			}

			retval.add(cred);
		}
		
		return retval;
	}
	
	/**
	 * Perform authorization for the callee resource.
	 * 
	 * @throws WSSecurityException upon access-denial
	 */
	@SuppressWarnings("unchecked")
	protected void performAuthz() throws AxisFault {

		try
		{
			// Grab working and message contexts
			WorkingContext workingContext =
				WorkingContext.getCurrentWorkingContext();
			MessageContext messageContext =
				(MessageContext) workingContext
						.getProperty(WorkingContext.MESSAGE_CONTEXT_KEY);
			
			// Extract our calling context (any decryption should be 
			// over with).  All GII message-level assertions and UT 
			// tokens should be within CALLER_CREDENTIALS_PROPERTY by now.
			ICallingContext callContext = ContextManager.getCurrentContext();

			// Get the destination certificate from the calling context
			KeyAndCertMaterial targetKeyMaterial =
					ContextManager.getCurrentContext(false)
							.getActiveKeyAndCertMaterial();
			X509Certificate[] targetCertChain = null;
			if (targetKeyMaterial != null)
			{
				targetCertChain = targetKeyMaterial._clientCertChain;
			}

			// Create a list of public certificate chains that have been 
			// verified as holder-of-key (e.g., though SSL or 
			// message-level security).
			ArrayList<X509Certificate[]> authenticatedCertChains = 
				new ArrayList<X509Certificate[]>();

			// Grab the client-hello authenticated SSL cert-chain (if there
			// was one)
			org.mortbay.jetty.Request req = (org.mortbay.jetty.Request) 
				messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
			Object transport = req.getConnection().getEndPoint().getTransport();
			if (transport instanceof SSLSocket) 
			{
				SSLSocket socket = (SSLSocket) transport;
				
				try { 
					X509Certificate[] clientSslCertChain = 
						(X509Certificate[]) socket.getSession().getPeerCertificates();
					if (clientSslCertChain != null) {
						authenticatedCertChains.add(clientSslCertChain);
					}
				}
				catch (SSLPeerUnverifiedException unverified)
				{
					
				}
			}
				
			// Retrieve the message-level cert-chains that have been 
			// recorded in the signature-Crypto instance. (Unfortunately 
			// this method is only called with the end-certificate; without
			// the chain, it's impossible to trust X.509 proxy certs)
			GIIBouncyCrypto sigCrypto = (GIIBouncyCrypto) 
				messageContext.getProperty(SIG_CRYPTO_PROPERTY);
			if (sigCrypto != null) 
			{
				authenticatedCertChains.addAll(sigCrypto.getLoadedCerts());
			}

			// Retrieve and authenticate other accumulated 
			// message-level credentials (e.g., GII delegated assertions, etc.)
			ArrayList<GIICredential> bearerCredentials =
				(ArrayList<GIICredential>) callContext
						.getTransientProperty(GIICredential.CALLER_CREDENTIALS_PROPERTY);
			Collection<GIICredential> authenticatedCallerCreds = 
				authenticateBearerCredentials(
					bearerCredentials,
					authenticatedCertChains, 
					targetCertChain);					
			
			// Finally add all of our callerIds to the calling-context's 
			// outgoing credentials 
			TransientCredentials transientCredentials = 
				TransientCredentials.getTransientCredentials(callContext); 
			transientCredentials._credentials.addAll(authenticatedCallerCreds);
			
			// Grab the operation method from the message context
			org.apache.axis.description.OperationDesc desc =
					messageContext.getOperation();
			if (desc == null)
			{
				// pretend security doesn't exist -- axis will do what it does
				// when it can't figure out how to dispatch to a non-existant 
				// method
				return;
			}
			JavaServiceDesc jDesc = null;
			ServiceDesc serviceDescription = desc.getParent();
			if (serviceDescription != null && 
				(serviceDescription instanceof JavaServiceDesc))
					jDesc = (JavaServiceDesc)serviceDescription;
			Method operation = desc.getMethod();			
			_logger.debug(operation.getDeclaringClass().getName() + "." + operation.getName() + "()");

			// Get the resource's authz handler
			IResource resource =
					ResourceManager.getCurrentResource().dereference();
			IAuthZProvider authZHandler =
					AuthZProviders.getProvider(resource.getParentResourceKey()
							.getServiceName());
			
			// Let the authZ handler make the decision
			authZHandler.checkAccess(
					authenticatedCallerCreds, 
					resource,
					(jDesc == null) ? operation.getDeclaringClass() : jDesc.getImplClass(),
					operation);

			resource.commit();
		}
		catch (IOException e)
		{
			throw new AxisFault(e.getMessage(), e);
		}
		catch (GeneralSecurityException e)
		{
			throw new AxisFault(e.getMessage(), e);
		}
		
	}
	
    /**
     * Evaluate whether a given certificate should be trusted.
     * Hook to allow subclasses to implement custom validation methods however they see fit.
     *
     * @param cert the certificate that should be validated against the keystore
     * @return true if the certificate is trusted, false if not (AxisFault is thrown for exceptions during CertPathValidation)
     * @throws WSSecurityException
     */
    protected boolean verifyTrust(
    		X509Certificate cert, 
    		RequestData reqData) 
    	throws WSSecurityException 
    {
    	// Return true for now.  performAuthz() will grab the creds retrieved
    	// via message signature (and elsewhere) and make the actual trust/authz 
    	// decision.
    	return true;
    }

    	
	/**
	 * Callback class to stash any username-token credentials into the   
	 * calling context's CALLER_CREDENTIALS_PROPERTY.
	 * 
	 * @author dgm4d
	 */
	static public class ServerPWCallback implements CallbackHandler
	{

		/**
		 * 
		 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
		 * 
		 */
		@SuppressWarnings("unchecked")
		public void handle(Callback[] callbacks) throws IOException,
				UnsupportedCallbackException
		{

			for (int i = 0; i < callbacks.length; i++)
			{
				if (callbacks[i] instanceof WSPasswordCallback)
				{
					WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];

					switch (pc.getUsage())
					{
					case WSPasswordCallback.USERNAME_TOKEN:
						// broken, but WSS4J seems to call USERNAME_TOKEN_UNKNOWN
						// case below anyway 
						
						/*
						 * // return password from file to make sure of match
						 * pc.setPassword("mooch"); return;
						 */
						break;

					case WSPasswordCallback.USERNAME_TOKEN_UNKNOWN:

						// Grab the supplied username token
						UsernamePasswordIdentity identity =
							new UsernamePasswordIdentity(pc.getIdentifer(),
									pc.getPassword());

						// Extract our calling context (any decryption 
						// should be over with) 
						ICallingContext callContext = null;
						try
						{
							callContext = ContextManager.getCurrentContext();
						}
						catch (ConfigurationException e)
						{
							throw new IOException(e.getMessage());
						}

						// add the UT to the caller's credential list
						ArrayList<GIICredential> callerCredentials =
								(ArrayList<GIICredential>) callContext
										.getTransientProperty(GIICredential.CALLER_CREDENTIALS_PROPERTY);
						callerCredentials.add(identity);
						
						break;

					case WSPasswordCallback.DECRYPT:
					case WSPasswordCallback.SIGNATURE:
						pc.setPassword(CRYTO_PASS);
						break;
					default:
						throw new UnsupportedCallbackException(callbacks[i],
								"Unrecognized Callback");
					}

				}
				else
				{
					throw new UnsupportedCallbackException(callbacks[i],
							"Unrecognized Callback");
				}
			}
		}
	}

	
	/**
	 * When an operation is open access, but it requires that the caller have read, write, or execute
	 * permission to process certain parameters, the operation should call this method to verify that
	 * the caller has the right.
	 */
	public static boolean checkAccess(IResource resource, RWXCategory category)
		throws IOException
	{
		ICallingContext callContext = ContextManager.getCurrentContext();
		TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(callContext); 
		String serviceName = resource.getParentResourceKey().getServiceName();
		IAuthZProvider authZHandler = AuthZProviders.getProvider(serviceName);
		return authZHandler.checkAccess(transientCredentials._credentials, resource, category);
	}
}
