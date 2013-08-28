/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.virginia.vcgr.genii.container.axis;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.axis.security.WSDoAllReceiver;
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
import org.morgan.util.configuration.ConfigurationException;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.w3c.dom.Document;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.axis.security.GIIBouncyCrypto;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.PermissionDeniedException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.security.authz.providers.AuthZProviders;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;
import edu.virginia.vcgr.genii.network.NetworkConfigTools;
import edu.virginia.vcgr.genii.security.CertificateValidatorFactory;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SAMLConstants;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.axis.MessageLevelSecurityRequirements;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.security.identity.IdentityType;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

public class ServerWSDoAllReceiver extends WSDoAllReceiver
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(ServerWSDoAllReceiver.class);

	static private final String CRYTO_PASS = "pwd";
	static private final String SIG_CRYPTO_PROPERTY = GIIBouncyCrypto.class.getCanonicalName();

	private static PrivateKey _serverPrivateKey;

	// startup mode is true until the container tells us we are ready to go.
	private static volatile Boolean _inStartupMode = new Boolean(true);

	public final static int MAXIMUM_CONCURRENT_CLIENTS = 32;

	// tracks how many clients are currently requesting RPC services.
	private static volatile Integer _concurrentCalls = new Integer(0);

	public ServerWSDoAllReceiver()
	{
	}

	public void configure(PrivateKey serverPrivateKey)
	{
		_serverPrivateKey = serverPrivateKey;

		setOption(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN + " " + WSHandlerConstants.TIMESTAMP + " "
			+ WSHandlerConstants.SIGNATURE + " " + WSHandlerConstants.ENCRYPT);
		setOption(WSHandlerConstants.PW_CALLBACK_CLASS, ServerWSDoAllReceiver.ServerPWCallback.class.getName());
		setOption(WSHandlerConstants.USER, GenesisIIConstants.CRYPTO_ALIAS);
	}

	public static void beginNormalRuntime()
	{
		_logger.info("Server starting normal runtime; allowing incoming off-machine connections.");
		synchronized (_inStartupMode) {
			_inStartupMode = false;
		}
	}

	public ServerWSDoAllReceiver(PrivateKey serverPrivateKey)
	{
		_serverPrivateKey = serverPrivateKey;
	}

	public void invoke(MessageContext msgContext) throws AxisFault
	{
		int currentClients = 0;
		boolean shouldnt_be_here = false;
		synchronized (_concurrentCalls) {
			// snapshot here for check...
			currentClients = _concurrentCalls.intValue();
		}
		if (_concurrentCalls.intValue() >= MAXIMUM_CONCURRENT_CLIENTS) {
			String msg = "Refusing call due to too many concurrent clients; please try again.";
			_logger.warn(msg);
			shouldnt_be_here = true;
			throw new AuthZSecurityException(msg);
		}
		synchronized (_concurrentCalls) {
			_concurrentCalls = new Integer(_concurrentCalls.intValue() + 1);
			// snapshot client count here to avoid logging inside synchronization.
			currentClients = _concurrentCalls.intValue();
		}
		if (_logger.isDebugEnabled())
			_logger.debug("rpc clients up to " + currentClients);

		IResource resource;
		try {
			resource = ResourceManager.getCurrentResource().dereference();
		} catch (Throwable e) {
			String msg = "failure to dereference resource: " + e.getMessage();
			_logger.error(msg, e);
			throw new AxisFault(msg);
		}

		IAuthZProvider authZHandler;
		try {
			authZHandler = AuthZProviders.getProvider(((ResourceKey) resource.getParentResourceKey()).getServiceName());
		} catch (ResourceException e) {
			String msg =
				"failure to get authorization provider for resource " + ResourceManager.getResourceName(resource) + ": "
					+ e.getMessage();
			_logger.error(msg, e);
			throw new AxisFault(msg);
		}

		try {
			if ((authZHandler == null) || (authZHandler.getMinIncomingMsgLevelSecurity(resource).isNone())) {
				/*
				 * We have no requirements for incoming message security. If there are no incoming
				 * headers, don't do any crypto processing.
				 */
				resource.commit();
				// hmmm: why commit before doing anything?

				Message sm = msgContext.getCurrentMessage();
				if (sm == null) {
					/*
					 * We did not receive anything...Usually happens when we get a HTTP 202 message
					 * (with no content).
					 */
					return;
				}

				Document doc = sm.getSOAPEnvelope().getAsDocument();
				String actor = (String) getOption(WSHandlerConstants.ACTOR);
				SOAPConstants sc = WSSecurityUtil.getSOAPConstants(doc.getDocumentElement());
				if (WSSecurityUtil.getSecurityHeader(doc, actor, sc) == null) {
					// check on the authorization.
					performAuthz();
					return;
				}
			}
			resource.commit();

			// process all incoming security headers
			super.invoke(msgContext);
			// check authorization.
			performAuthz();

		} catch (AxisFault e) {
			// re-throw and also hit the finally clause to decrement concurrency counter.
			String msg = "An AxisFault occurred during authorization: " + e.getMessage();
			_logger.error(msg);
			if (_logger.isDebugEnabled()) {
				_logger.error("AxisFault full trace: ", e);
			}
			throw e;
		} catch (Throwable e) {
			// wrap this exception and re-throw.
			String msg = "An exception occurred during authorization: " + e.getMessage();
			_logger.error(msg);
			throw new AxisFault(msg, e);
		} finally {
			if (shouldnt_be_here) {
				_logger.debug("that explains some things; the shouldn't be here check was activated for rpc client count.");
			}
			synchronized (_concurrentCalls) {
				_concurrentCalls = new Integer(_concurrentCalls.intValue() - 1);
				currentClients = _concurrentCalls.intValue();
			}
			if (_logger.isDebugEnabled())
				_logger.debug("rpc clients down to " + currentClients);

		}
	}

	@SuppressWarnings("rawtypes")
	protected boolean checkReceiverResults(Vector wsResult, Vector actions)
	{

		// checks to see if the security operations performed meet the minimum
		// required (as per the resource's authZ module)

		try {
			// get the resource's min messsage-sec level
			MessageLevelSecurityRequirements resourceMinMsgSec;
			IResource resource = ResourceManager.getCurrentResource().dereference();
			IAuthZProvider authZHandler =
				AuthZProviders.getProvider(((ResourceKey) resource.getParentResourceKey()).getServiceName());

			if (authZHandler == null) {
				resourceMinMsgSec = new MessageLevelSecurityRequirements();
			} else {
				resourceMinMsgSec = authZHandler.getMinIncomingMsgLevelSecurity(resource);
			}

			// retrieve what we required from the actions vector
			int performed = MessageLevelSecurityRequirements.NONE;
			for (int i = 0; i < wsResult.size(); i++) {
				int action = ((WSSecurityEngineResult) wsResult.get(i)).getAction();
				switch (action) {
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
			if ((new MessageLevelSecurityRequirements(performed)).superset(resourceMinMsgSec)) {
				return true;
			}

		} catch (ResourceException e) {
			_logger.info("ResourceException occurred in checkReceiverResults", e);
			return false;
		} catch (AuthZSecurityException e) {
			_logger.info("AuthZException occurred in checkReceiverResults", e);
			return false;
		} catch (AxisFault e) {
			_logger.info("AxisFault occurred in checkReceiverResults", e);
			return false;
		}

		return false;
	}

	/**
	 * Hook to allow subclasses to load their Signature Crypto however they see fit.
	 */
	public Crypto loadSignatureCrypto(RequestData reqData) throws WSSecurityException
	{

		AbstractCrypto crypto = null;
		try {
			// create an in-memory keystore for the incoming sig key material
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			crypto = new GIIBouncyCrypto();
			crypto.setKeyStore(keyStore);

			// store our sig crypto for use later in retrieving
			// message-signing creds
			((MessageContext) reqData.getMsgContext()).setProperty(SIG_CRYPTO_PROPERTY, crypto);

			return crypto;

		} catch (IOException e) {
			throw new WSSecurityException(e.getMessage(), e);
		} catch (java.security.GeneralSecurityException e) {
			throw new WSSecurityException(e.getMessage(), e);
		} catch (org.apache.ws.security.components.crypto.CredentialException e) {
			throw new WSSecurityException(e.getMessage(), e);
		}
	}

	/**
	 * Hook to allow subclasses to load their Encryption Crypto however they see fit.
	 */
	protected Crypto loadDecryptionCrypto(RequestData reqData) throws WSSecurityException
	{

		AbstractCrypto crypto = null;
		try {
			// create an in-memory keystore for the server's key material
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			/*
			 * place the resource's cert chain and epi in the working context -- necessary for
			 * response message-security in case we actually delete this resource as part of this
			 * operation.
			 */
			IResource resource = ResourceManager.getCurrentResource().dereference();
			Certificate[] targetCertChain = (Certificate[]) resource.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
			String epi = (resource.getProperty(IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME)).toString();
			WorkingContext wctxt = WorkingContext.getCurrentWorkingContext();
			wctxt.setProperty(WorkingContext.CERT_CHAIN_KEY, targetCertChain);
			wctxt.setProperty(WorkingContext.EPI_KEY, epi);

			if (targetCertChain != null) {
				keyStore.setKeyEntry(epi, _serverPrivateKey, CRYTO_PASS.toCharArray(), targetCertChain);
			}

			crypto = new GIIBouncyCrypto();
			crypto.setKeyStore(keyStore);

			return crypto;

		} catch (BaseFaultType bft) {
			BaseFaultTypeDescription[] desc = bft.getDescription();
			if (desc != null && desc.length >= 1)
				throw new WSSecurityException(desc[0].get_value(), bft);
			else
				throw new WSSecurityException(bft.dumpToString(), bft);
		} catch (IOException e) {
			throw new WSSecurityException(e.getMessage(), e);
		} catch (java.security.GeneralSecurityException e) {
			throw new WSSecurityException(e.getMessage(), e);
		} catch (org.apache.ws.security.components.crypto.CredentialException e) {
			throw new WSSecurityException(e.getMessage(), e);
		}
	}

	/**
	 * Authenticate any holder-of-key (i.e., signed) bearer credentials using the given
	 * authenticated certificate-chains.
	 * 
	 * Returns a cumulative collection identities composed from both the bearer- and authenticated-
	 * credentials
	 * 
	 */
	public static Collection<NuCredential> authenticateBearerCredentials(ArrayList<NuCredential> bearerCredentials,
		ArrayList<X509Certificate[]> authenticatedCertChains, X509Certificate[] targetCertChain) throws AuthZSecurityException,
		GeneralSecurityException
	{
		HashSet<NuCredential> retval = new HashSet<NuCredential>();

		// Add the authenticated certificate chains
		for (X509Certificate[] certChain : authenticatedCertChains) {
			X509Identity authCred = new X509Identity(certChain, IdentityType.CONNECTION);
			if (_logger.isDebugEnabled())
				_logger.debug("adding CONNECTION type for this identity: " + authCred.toString());
			retval.add(authCred);
		}

		// Corroborate the bearer credentials
		for (NuCredential cred : bearerCredentials) {
			if (cred instanceof TrustCredential) {
				// Holder-of-key token
				TrustCredential assertion = (TrustCredential) cred;

				try {
					// Check validity and verify integrity
					assertion.checkValidity(new Date());
				} catch (Throwable t) {
					_logger.debug("caught exception testing certificate validity; dropping this credential: " + t.getMessage());
					continue; // no longer anything to check on that one; we don't want it.
				}

				/*
				 * this section came from the days when we were always running on tls certificates
				 * as the resource identities. it cannot be in place any more because it hinders the
				 * new usage of trust delegations, which are not always from the connecting
				 * identity, but which are still totally valid.
				 */
				boolean enable_old_credential_checking = false;
				if (enable_old_credential_checking) {
					if (_logger.isTraceEnabled())
						_logger.trace("credential to test has first delegatee: "
							+ assertion.getRootOfTrust().getDelegatee()[0].getSubjectDN() + "\n...and original issuer: "
							+ assertion.getOriginalAsserter()[0].getSubjectDN());

					// Verify that the request message signer is the same as the
					// one of the holder-of-key certificates.
					boolean match = false;
					for (X509Certificate[] callerCertChain : authenticatedCertChains) {
						if (_logger.isTraceEnabled())
							_logger.trace("...comparing with " + callerCertChain[0].getSubjectDN());
						try {
							if (assertion.findDelegateeInChain(callerCertChain[0]) >= 0) {
								if (_logger.isDebugEnabled())
									_logger.debug("...found delegatee at position "
										+ assertion.findDelegateeInChain(callerCertChain[0])
										+ " to be the same as incoming tls cert.");
								match = true;
								break;
							} else if (CertificateValidatorFactory.getValidator().validateIsTrustedResource(
								assertion.getOriginalAsserter()) == true) {
								if (_logger.isTraceEnabled())
									_logger
										.trace("...allowed incoming message using resource trust store for original asserter.");
								match = true;
								break;
							} else {
								if (_logger.isTraceEnabled())
									_logger.trace("...found them to be different.");
							}
						} catch (Throwable e) {
							_logger.error("failure: exception thrown during holder of key checks", e);
						}
					}

					if (!match) {
						String msg =
							"credential failed to match incoming message sender: '" + assertion.describe(VerbosityLevel.HIGH)
								+ "'";
						_logger.error(msg);
						throw new AuthZSecurityException(msg);
					}
				}
			}

			retval.add(cred);
		}

		return retval;
	}

	/**
	 * Perform authorization for the callee resource.
	 * 
	 * @throws WSSecurityException
	 *             upon access-denial
	 */
	@SuppressWarnings("unchecked")
	protected void performAuthz() throws AxisFault
	{
		try {
			// Grab working and message contexts
			WorkingContext workingContext = WorkingContext.getCurrentWorkingContext();
			MessageContext messageContext = (MessageContext) workingContext.getProperty(WorkingContext.MESSAGE_CONTEXT_KEY);

			// Extract our calling context (any decryption should be
			// over with). All GII message-level assertions and UT
			// tokens should be within CALLER_CREDENTIALS_PROPERTY by now.
			ICallingContext callContext = ContextManager.getExistingContext();

			/*
			 * Create a list of public certificate chains that have been verified as holder-of-key
			 * (e.g., though SSL or message-level security).
			 */
			ArrayList<X509Certificate[]> authenticatedCertChains = new ArrayList<X509Certificate[]>();

			// Grab the client-hello authenticated SSL cert-chain (if there
			// was one)
			org.mortbay.jetty.Request req =
				(org.mortbay.jetty.Request) messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
			Object transport = req.getConnection().getEndPoint().getTransport();
			if (transport instanceof SSLSocket) {
				boolean stillStarting;
				synchronized (_inStartupMode) {
					stillStarting = _inStartupMode;
				}
				if (stillStarting == true) {
					Collection<String> ips = NetworkConfigTools.getIPAddresses();
					SocketAddress addr = ((SSLSocket) transport).getRemoteSocketAddress();
					String clientIP = "";
					if (addr instanceof InetSocketAddress) {
						clientIP = ((InetSocketAddress) addr).getAddress().getHostAddress();
					}
					if (ips.contains(clientIP) == true) {
						if (_logger.isDebugEnabled())
							_logger.debug("startup: allowing client on local address: " + clientIP);
					} else {
						if (_logger.isDebugEnabled())
							_logger.debug("startup: rejecting client at remote address: " + clientIP);
						throw new AxisFault("container in startup mode; access temporarily denied for remote connections.");
					}
				}

				try {
					X509Certificate[] clientSslCertChain =
						(X509Certificate[]) ((SSLSocket) transport).getSession().getPeerCertificates();
					if (clientSslCertChain != null) {
						authenticatedCertChains.add(clientSslCertChain);
					}
				} catch (SSLPeerUnverifiedException unverified) {
				}
			}

			populateSAMLPropertiesInContext(workingContext, callContext);

			// Get the destination certificate from the calling context
			KeyAndCertMaterial targetKeyMaterial = ContextManager.getExistingContext().getActiveKeyAndCertMaterial();
			X509Certificate[] targetCertChain = null;
			if (targetKeyMaterial != null) {
				targetCertChain = targetKeyMaterial._clientCertChain;
			}

			// Retrieve the message-level cert-chains that have been
			// recorded in the signature-Crypto instance. (Unfortunately
			// this method is only called with the end-certificate; without
			// the chain, it's impossible to trust X.509 proxy certs)
			GIIBouncyCrypto sigCrypto = (GIIBouncyCrypto) messageContext.getProperty(SIG_CRYPTO_PROPERTY);
			if (sigCrypto != null) {
				authenticatedCertChains.addAll(sigCrypto.getLoadedCerts());
			}

			// Retrieve and authenticate other accumulated
			// message-level credentials (e.g., GII delegated assertions, etc.)
			ArrayList<NuCredential> bearerCredentials =
				(ArrayList<NuCredential>) callContext.getTransientProperty(SAMLConstants.CALLER_CREDENTIALS_PROPERTY);
			Collection<NuCredential> authenticatedCallerCreds =
				authenticateBearerCredentials(bearerCredentials, authenticatedCertChains, targetCertChain);

			// Finally add all of our callerIds to the calling-context's
			// outgoing credentials
			TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(callContext);
			transientCredentials.addAll(authenticatedCallerCreds);
			// Grab the operation method from the message context
			org.apache.axis.description.OperationDesc desc = messageContext.getOperation();
			if (desc == null) {
				// pretend security doesn't exist -- axis will do what it does
				// when it can't figure out how to dispatch to a non-existent
				// method.
				_logger.warn("bailing out due to a null operation description.");
				return;
			}
			JavaServiceDesc jDesc = null;
			ServiceDesc serviceDescription = desc.getParent();
			if (serviceDescription != null && (serviceDescription instanceof JavaServiceDesc))
				jDesc = (JavaServiceDesc) serviceDescription;
			Method operation = desc.getMethod();
			if (_logger.isDebugEnabled())
				_logger.debug("client invokes " + operation.getDeclaringClass().getName() + "." + operation.getName() + "()");

			// Get the resource's authz handler
			IResource resource = ResourceManager.getCurrentResource().dereference();
			IAuthZProvider authZHandler =
				AuthZProviders.getProvider(((ResourceKey) resource.getParentResourceKey()).getServiceName());

			// Let the authZ handler make the decision.
			String errorText = "";
			boolean accessOkay =
				authZHandler.checkAccess(authenticatedCallerCreds, resource, (jDesc == null) ? operation.getDeclaringClass()
					: jDesc.getImplClass(), operation, errorText);

			if (accessOkay) {
				resource.commit();
			} else {
				PermissionDeniedException temp =
					new PermissionDeniedException(operation.getName(), ResourceManager.getResourceName(resource));
				_logger.error("failed to check access: " + temp.getMessage());
				_logger.error("error text is ==>" + errorText);
				throw new AxisFault(temp.getMessage());
			}
		} catch (IOException e) {
			_logger.error("failing request due to: " + e.getMessage());
			throw new AxisFault(e.getMessage(), e);
		} catch (GeneralSecurityException e) {
			_logger.error("failing request due to: " + e.getMessage());
			throw new AxisFault(e.getMessage(), e);
		}
	}

	/*
	 * As the name suggests, this method stores the client's SAML credentials in the calling
	 * context.
	 */
	private void populateSAMLPropertiesInContext(WorkingContext workingContext, ICallingContext callContext)
	{

		// We retrieve delegated SAML credentials from the working context and
		// store them in the
		// calling context. This is done because we traditionally use the
		// calling context, not the
		// working context, for all security related purposes. Furthermore, this
		// usage matches the
		// client-side credentials handling logic.
		CredentialWallet credentialsWallet =
			(CredentialWallet) workingContext.getProperty(SAMLConstants.SAML_CREDENTIALS_WORKING_CONTEXT_CREDS_PROPERTY_NAME);
		callContext.setTransientProperty(SAMLConstants.SAML_CREDENTIALS_WALLET_PROPERTY_NAME, credentialsWallet);

		// Like the credential wallet, we copy client's SSL certificate from the
		// working context to
		// the calling context.
		X509Certificate[] clientSSLCertificate =
			(X509Certificate[]) workingContext.getProperty(SAMLConstants.SAML_CLIENT_SSL_CERTIFICATE_PROPERTY_NAME);
		callContext.setTransientProperty(SAMLConstants.SAML_CLIENT_SSL_CERTIFICATE_PROPERTY_NAME, clientSSLCertificate);
	}

	/**
	 * Evaluate whether a given certificate should be trusted. Hook to allow subclasses to implement
	 * custom validation methods however they see fit.
	 * 
	 * @param cert
	 *            the certificate that should be validated against the keystore
	 * @return true if the certificate is trusted, false if not (AxisFault is thrown for exceptions
	 *         during CertPathValidation)
	 * @throws WSSecurityException
	 */
	protected boolean verifyTrust(X509Certificate cert, RequestData reqData) throws WSSecurityException
	{
		// Return true for now. performAuthz() will grab the creds retrieved
		// via message signature (and elsewhere) and make the actual trust/authz
		// decision.
		return true;
	}

	/**
	 * Callback class to stash any username-token credentials into the calling context's
	 * CALLER_CREDENTIALS_PROPERTY.
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
		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
		{

			for (int i = 0; i < callbacks.length; i++) {
				if (callbacks[i] instanceof WSPasswordCallback) {
					WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];

					switch (pc.getUsage()) {
						case WSPasswordCallback.USERNAME_TOKEN:
							// broken, but WSS4J seems to call
							// USERNAME_TOKEN_UNKNOWN
							// case below anyway

							/*
							 * // return password from file to make sure of match
							 * pc.setPassword("mooch"); return;
							 */
							break;

						case WSPasswordCallback.USERNAME_TOKEN_UNKNOWN:

							// Grab the supplied username token
							UsernamePasswordIdentity identity =
								new UsernamePasswordIdentity(pc.getIdentifer(), pc.getPassword());

							// Extract our calling context (any decryption
							// should be over with)
							ICallingContext callContext = null;
							try {
								callContext = ContextManager.getExistingContext();
							} catch (ConfigurationException e) {
								throw new IOException(e.getMessage());
							}

							// add the UT to the caller's credential list
							ArrayList<NuCredential> callerCredentials =
								(ArrayList<NuCredential>) callContext
									.getTransientProperty(SAMLConstants.CALLER_CREDENTIALS_PROPERTY);
							callerCredentials.add(identity);

							break;

						case WSPasswordCallback.DECRYPT:
						case WSPasswordCallback.SIGNATURE:
							pc.setPassword(CRYTO_PASS);
							break;
						default:
							throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
					}

				} else {
					throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
				}
			}
		}
	}

	/**
	 * When an operation is open access, but it requires that the caller have read, write, or
	 * execute permission to process certain parameters, the operation should call this method to
	 * verify that the caller has the right.
	 */
	public static boolean checkAccess(IResource resource, RWXCategory category) throws IOException
	{
		ICallingContext callContext = ContextManager.getExistingContext();
		TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(callContext);
		String serviceName = ((ResourceKey) resource.getParentResourceKey()).getServiceName();
		IAuthZProvider authZHandler = AuthZProviders.getProvider(serviceName);
		String errorText = "";
		boolean success = authZHandler.checkAccess(transientCredentials.getCredentials(), resource, category, errorText);
		if (!success) {
			_logger.error("authorization failure with message:" + errorText);
		}
		return success;
	}
}
