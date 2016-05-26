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

package edu.virginia.vcgr.genii.container.axis;

import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Vector;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
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
import edu.virginia.vcgr.genii.security.SAMLConstants;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.axis.MessageLevelSecurityRequirements;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

@SuppressWarnings("deprecation")
public class ServerWSDoAllReceiver extends WSDoAllReceiver
{
	static final long serialVersionUID = 0L;

	static Log _logger = LogFactory.getLog(ServerWSDoAllReceiver.class);

	static private final String CRYTO_PASS = "pwd";
	static public final String SIG_CRYPTO_PROPERTY = GIIBouncyCrypto.class.getCanonicalName();

	private static PrivateKey _serverPrivateKey;

	// startup mode is true until the container tells us we are ready to go.
	static volatile Boolean _inStartupMode = new Boolean(true);

	// tracks how many clients are currently requesting RPC services.
	private static volatile Integer _concurrentCalls = new Integer(0);

	public ServerWSDoAllReceiver()
	{
	}

	public ServerWSDoAllReceiver(PrivateKey serverPrivateKey)
	{
		_serverPrivateKey = serverPrivateKey;
	}

	public void configure(PrivateKey serverPrivateKey)
	{
		_serverPrivateKey = serverPrivateKey;

		setOption(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN + " " + WSHandlerConstants.TIMESTAMP + " "
			+ WSHandlerConstants.SIGNATURE + " " + WSHandlerConstants.ENCRYPT);
		setOption(WSHandlerConstants.PW_CALLBACK_CLASS, ServerWSDoAllReceiver.ServerPWCallback.class.getName());
		setOption(WSHandlerConstants.USER, GenesisIIConstants.CRYPTO_ALIAS);
	}

	/**
	 * returns true if the container has finished starting up.
	 */
	public static boolean startedNormalRuntime()
	{
		synchronized (_inStartupMode) {
			return !_inStartupMode;
		}
	}

	public static void beginNormalRuntime()
	{
		_logger.info("Server starting normal runtime; allowing incoming off-machine connections.");
		synchronized (_inStartupMode) {
			_inStartupMode = false;
		}
	}

	@Override
	public void invoke(MessageContext msgContext) throws AxisFault
	{
		IResource resource;
		try {
			resource = ResourceManager.getCurrentResource().dereference();
		} catch (Throwable e) {
			/*
			 * hmmm: we throw this error rather than a more recognizable error like RESOURCE NOT FOUND or some such. that's not so good.
			 */
			String msg = "could not dereference resource in invoke";
			_logger.debug(msg, e);
			throw new AxisFault(msg, e);
		}

		int concurrencyLevel; // snapshot for logging the client count.
		synchronized (_concurrentCalls) {
			_concurrentCalls++;
			// snapshot client count here to avoid logging inside synchronization.
			concurrencyLevel = _concurrentCalls.intValue();
		}
		if (_logger.isTraceEnabled())
			_logger.trace("rpc clients up to " + concurrencyLevel);

		IAuthZProvider authZHandler;
		try {
			authZHandler = AuthZProviders.getProvider(((ResourceKey) resource.getParentResourceKey()).getServiceName());
		} catch (ResourceException e) {
			String msg = "failure to get authorization provider for resource " + ResourceManager.getResourceName(resource);
			_logger.error(msg, e);
			synchronized (_concurrentCalls) {
				_concurrentCalls--;
				concurrencyLevel = _concurrentCalls.intValue();
			}
			if (_logger.isTraceEnabled())
				_logger.trace("after authz failure, rpc clients down to " + concurrencyLevel);
			throw new AxisFault(msg, e);
		}

		try {
			if ((authZHandler == null) || (authZHandler.getMinIncomingMsgLevelSecurity(resource).isNone())) {
				/*
				 * We have no requirements for incoming message security. If there are no incoming headers, don't do any crypto processing.
				 */
				resource.commit();

				Message sm = msgContext.getCurrentMessage();
				if (sm == null) {
					/*
					 * We did not receive anything...Usually happens when we get a HTTP 202 message (with no content).
					 */
					return;
				}

				Document doc = sm.getSOAPEnvelope().getAsDocument();
				String actor = (String) getOption(WSHandlerConstants.ACTOR);
				SOAPConstants sc = WSSecurityUtil.getSOAPConstants(doc.getDocumentElement());
				if (WSSecurityUtil.getSecurityHeader(doc, actor, sc) == null) {
					// check on the authorization.
					ServerAuthorizationManagement.performAuthorizationChecks();
					return;
				}
			}
			resource.commit();

			// process all incoming security headers
			super.invoke(msgContext);
			// check authorization.
			ServerAuthorizationManagement.performAuthorizationChecks();
		} catch (AxisFault e) {
			if (e instanceof PermissionDeniedException) {
				// print a much calmer report of this fault, since we know exactly what happened.
				PermissionDeniedException pde = (PermissionDeniedException) e;
				_logger.info(GenesisIIConstants.ACCESS_DENIED_SENTINEL + " for method '"
					+ PermissionDeniedException.extractMethodName(pde.getMessage()) + "' on asset: "
					+ PermissionDeniedException.extractAssetDenied(pde.getMessage()));

				try {
					ICallingContext context = ContextManager.getCurrentContext();
					TransientCredentials tc = TransientCredentials.getTransientCredentials(context);
					if (_logger.isDebugEnabled())
						_logger.debug("failed access attempt had these credentials: "
							+ TrustCredential.showCredentialList(tc.getCredentials(), VerbosityLevel.HIGH));
				} catch (Throwable t) {
					_logger.error("failed to get calling context or show credentials", t);
				}

			} else {
				// re-throw and also hit the finally clause to decrement concurrency counter.
				String msg = "An AxisFault occurred during authorization: " + e.getMessage();
				_logger.error(msg);
				if (_logger.isDebugEnabled()) {
					_logger.error("AxisFault full trace: ", e);
				}
			}
			throw e;
		} catch (Throwable e) {
			// wrap this exception and re-throw.
			String msg = "An exception occurred during authorization: " + e.getMessage();
			_logger.error(msg, e);
			throw new AxisFault(msg, e);
		} finally {
			synchronized (_concurrentCalls) {
				_concurrentCalls--;
				concurrencyLevel = _concurrentCalls.intValue();
			}
			if (_logger.isTraceEnabled())
				_logger.trace("rpc clients down to " + concurrencyLevel);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected boolean checkReceiverResults(Vector wsResult, Vector actions)
	{

		// checks to see if the security operations performed meet the minimum
		// required (as per the resource's authZ module)

		try {
			// get the resource's min messsage-sec level
			MessageLevelSecurityRequirements resourceMinMsgSec;
			IResource resource = ResourceManager.getCurrentResource().dereference();
			IAuthZProvider authZHandler = AuthZProviders.getProvider(((ResourceKey) resource.getParentResourceKey()).getServiceName());

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
	@Override
	public Crypto loadSignatureCrypto(RequestData reqData) throws WSSecurityException
	{

		AbstractCrypto crypto = null;
		try {
			// create an in-memory keystore for the incoming sig key material
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			crypto = new GIIBouncyCrypto();
			crypto.setKeyStore(keyStore);

			// store our sig crypto for use later in retrieving message-signing creds
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
	@Override
	protected Crypto loadDecryptionCrypto(RequestData reqData) throws WSSecurityException
	{
		AbstractCrypto crypto = null;
		try {
			// create an in-memory keystore for the server's key material
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			/*
			 * place the resource's cert chain and epi in the working context -- necessary for response message-security in case we actually
			 * delete this resource as part of this operation.
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
	 * Evaluate whether a given certificate should be trusted. Hook to allow subclasses to implement custom validation methods however they
	 * see fit.
	 * 
	 * @param cert
	 *            the certificate that should be validated against the keystore
	 * @return true if the certificate is trusted, false if not (AxisFault is thrown for exceptions during CertPathValidation)
	 * @throws WSSecurityException
	 */
	@Override
	protected boolean verifyTrust(X509Certificate cert, RequestData reqData) throws WSSecurityException
	{
		/*
		 * Return true for now. performAuthz() will grab the creds retrieved via message signature (and elsewhere) and make the actual
		 * trust/authz decision.
		 */
		return true;
	}

	/**
	 * Callback class to stash any username-token credentials into the calling context's CALLER_CREDENTIALS_PROPERTY.
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
		@Override
		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
		{

			for (int i = 0; i < callbacks.length; i++) {
				if (callbacks[i] instanceof WSPasswordCallback) {
					WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];

					switch (pc.getUsage()) {
						case WSPasswordCallback.USERNAME_TOKEN:
							/*
							 * broken, but WSS4J seems to call USERNAME_TOKEN_UNKNOWN case below anyway.
							 */
							break;

						case WSPasswordCallback.USERNAME_TOKEN_UNKNOWN:
							// Grab the supplied username token
							UsernamePasswordIdentity identity = new UsernamePasswordIdentity(pc.getIdentifer(), pc.getPassword());

							// Extract our calling context (any decryption should be finished).
							ICallingContext callContext = null;
							try {
								callContext = ContextManager.getExistingContext();
							} catch (ConfigurationException e) {
								throw new IOException(e.getMessage());
							}

							// add the UT to the caller's credential list
							ArrayList<NuCredential> callerCredentials =
								(ArrayList<NuCredential>) callContext.getTransientProperty(SAMLConstants.CALLER_CREDENTIALS_PROPERTY);
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
}
