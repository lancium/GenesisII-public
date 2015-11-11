package edu.virginia.vcgr.genii.container.axis;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityException;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.nio.SelectChannelEndPoint;
import org.eclipse.jetty.io.nio.SslConnection;
import org.eclipse.jetty.io.nio.SslConnection.SslEndPoint;
import org.eclipse.jetty.server.Request;

import edu.virginia.vcgr.genii.algorithm.structures.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.axis.security.GIIBouncyCrypto;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.security.PermissionDeniedException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.security.authz.providers.AuthZProviders;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;
import edu.virginia.vcgr.genii.network.NetworkConfigTools;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SAMLConstants;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.identity.IdentityType;

public class ServerAuthorizationManagement
{
	static Log _logger = LogFactory.getLog(ServerAuthorizationManagement.class);

	/*
	 * the most client sessions that we will try to cache. the set of credentials could be pretty big, but hopefully by storing just the roots
	 * of each chain we are not taking up too much memory.
	 */
	static public int MAXIMUM_SESSIONS_CACHED = 200;

	static public long SESSION_LIFETIME = 1000 * 60 * 10; // 10 minutes.

	/*
	 * a fairly high limit for the number of credentials the user can have. this really will support about 100 user and group credentials.
	 * even if they just changed logins, the old set will get flushed out by the new items.
	 */
	static public int MAXIMUM_CREDENTIALS_CACHED = 100;

	// how long each credential is kept around before timing out of the cache.
	static public long CREDENTIAL_LIFETIME = 1000 * 60 * 10; // 10 minutes.

	/**
	 * tracks our credentials similarly to a credential wallet, but can time out the individual items and remove them. this is important for
	 * handling a client's credential changes when they're still using the same tls session; any new credentials they have will get added to
	 * the list but the old ones will time out and be removed. the list is indexed by the GUID of the trust credential object.
	 */
	static public class TimedOutCredentialsCachePerSession extends TimedOutLRUCache<String, TrustCredential>
	{
		public TimedOutCredentialsCachePerSession()
		{
			super(MAXIMUM_CREDENTIALS_CACHED, CREDENTIAL_LIFETIME);
		}
	}

	/*
	 * hmmm: HUGE CAVEAT: signature checking is now no longer done in the 'from soap' pathway. we absolutely need to ensure that each chain is
	 * checked once and only once.

not yet actually.
	 */

	static private TimedOutLRUCache<X509Certificate, TimedOutCredentialsCachePerSession> _clientSessionCredentialsCache =
		new TimedOutLRUCache<X509Certificate, TimedOutCredentialsCachePerSession>(MAXIMUM_SESSIONS_CACHED, SESSION_LIFETIME);

	// hmmm: WILL THIS BE TOO SLOW because of using such a fat has key?
	// hmmm: test with hundreds of clients and see how it holds up for speed and for memory use.
	// hmmm: try just adding some timing around the lookup and set operations for now while in debug land!?!

	// hmmm: maybe separate a new object for the creds cache out of this.
	/**
	 * adds any root trust credentials in "newCreds" that are not previously known for this client and refreshes any existing ones that are
	 * still in "newCreds".
	 */
	public static void addCredentialsForClientSession(X509Certificate x509ForSession, CredentialWallet newCreds)
	{
		if (_logger.isDebugEnabled())
			_logger.debug("adding credentials for session cert: " + x509ForSession.getSubjectDN());

		synchronized (_clientSessionCredentialsCache) {
			TimedOutCredentialsCachePerSession sessionCredsCache = _clientSessionCredentialsCache.get(x509ForSession);
			if (sessionCredsCache == null) {
				// add a new wallet in for this unknown client.
				_clientSessionCredentialsCache.put(x509ForSession, new TimedOutCredentialsCachePerSession());
				sessionCredsCache = _clientSessionCredentialsCache.get(x509ForSession);
				if (sessionCredsCache == null) {
					_logger.error("serious error in client credential cache's timed-out LRU cache; newly added creds cache is missing");
					return; // hosed.
				}
				// refresh this cache since the client is using the tls cert still.
				_clientSessionCredentialsCache.refresh(x509ForSession);

				// hmmm: lower the logging levels in this method once code is proven out.
				if (_logger.isDebugEnabled())
					_logger.debug("added new creds cache for tls cert for: " + x509ForSession.getSubjectDN());
			}
			// add these new credentials in, but only the root trust credential of each chain.
			for (TrustCredential tc : newCreds.getCredentials()) {
				TrustCredential rootCred = tc.getRootOfTrust();
				if (sessionCredsCache.get(rootCred.getId()) == null) {
					// not yet in the cache, so add it.
					sessionCredsCache.put(rootCred.getId(), rootCred);
					if (_logger.isDebugEnabled())
						_logger.debug("added new credential " + rootCred.getId() + " to creds cache: "
							+ rootCred.describe(VerbosityLevel.LOW));
				} else {
					// already in the cache, so refresh it.
					sessionCredsCache.refresh(rootCred.getId());
					if (_logger.isDebugEnabled())
						_logger.debug("refreshed credential " + rootCred.getId() + " in creds cache: "
							+ rootCred.describe(VerbosityLevel.LOW));
				}
			}
		}
	}

	/**
	 * Authenticate any holder-of-key (i.e., signed) bearer credentials using the given authenticated certificate-chains. Returns a cumulative
	 * collection identities composed from both the bearer- and authenticated- credentials
	 */
	public static Collection<NuCredential> authenticateBearerCredentials(ArrayList<NuCredential> bearerCredentials,
		ArrayList<X509Certificate[]> authenticatedCertChains, X509Certificate[] callerTLSCert, ICallingContext callContext)
		throws AuthZSecurityException, GeneralSecurityException
	{
		if (_logger.isTraceEnabled()) {
			_logger.debug("entered authBearCred with caller: " + callerTLSCert[0].getSubjectDN());

			String dumpedCreds = "";
			for (X509Certificate[] x509 : authenticatedCertChains) {
				dumpedCreds = dumpedCreds.concat(x509[0].getSubjectDN() + "\n");
			}
			_logger.debug("auth chains initially are:\n" + dumpedCreds);

			for (NuCredential cred : bearerCredentials) {
				dumpedCreds = dumpedCreds.concat(cred.describe(VerbosityLevel.HIGH) + "\n");
			}
			_logger.debug("initial untested credential set is:\n" + dumpedCreds);
		}

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
					_logger.debug("caught exception testing certificate validity; dropping this credential: " + assertion.toString(), t);
					continue; // no longer anything to check on that one; we don't want it.
				}

				// Verify that the request message signer is the same as one of the holder-of-key certificates.
				boolean match = false;
				if (_logger.isTraceEnabled())
					_logger.debug("credential to test has first delegatee: " + assertion.getRootOfTrust().getDelegatee()[0].getSubjectDN()
						+ "\n...and original issuer: " + assertion.getOriginalAsserter()[0].getSubjectDN());
				for (X509Certificate[] callerCertChain : authenticatedCertChains) {
					if (_logger.isTraceEnabled())
						_logger.debug("...comparing with " + callerCertChain[0].getSubjectDN());
					try {
						int position = assertion.findDelegateeInChain(callerCertChain[0]);
						if (position >= 0) {
							if (_logger.isTraceEnabled())
								_logger.debug("...found delegatee at position "
									+ ((position == TrustCredential.TO_FIND_WAS_ISSUER) ? "{issuer}" : position)
									+ " to be the same as incoming tls cert.");
							match = true;
							break;
						} else {
							if (_logger.isTraceEnabled())
								_logger.debug("...found them to be different.");
						}
					} catch (Throwable e) {
						_logger.error("failure: exception thrown during holder of key checks", e);
					}
				}

				if (!match) {
					if (_logger.isDebugEnabled()) {
						String msg =
							"WARN: dropping credential which did not match incoming message sender: '"
								+ assertion.describe(VerbosityLevel.HIGH) + "'";
						_logger.debug(msg);
					}
					// skip adding it.
					continue;
				}

				/*
				 * discover when the sender trusts a credential, and include it if there's a matching pass through identity.
				 */
				if (callerTLSCert[0].equals(assertion.getOriginalAsserter()[0])) {
					if (_logger.isTraceEnabled())
						_logger.debug("found an assertion matching the target TLS cert chain: " + assertion.toString());
					X509Certificate passThrough =
						(X509Certificate) callContext.getSingleValueProperty(GenesisIIConstants.PASS_THROUGH_IDENTITY);
					if (passThrough != null) {
						if (_logger.isTraceEnabled())
							_logger.debug("got a pass through cert, checking delegatee: " + passThrough.getSubjectDN().toString());
						if (assertion.getDelegatee()[0].equals(passThrough)) {
							X509Certificate[] pt = new X509Certificate[1];
							pt[0] = passThrough;
							// found a matching pass-through identity, so allow the caller to act as
							// this.
							X509Identity x509 = new X509Identity(pt, IdentityType.CONNECTION);
							if (_logger.isTraceEnabled())
								_logger.debug("adding matching pass-through identity for: " + x509.toString());
							retval.add(x509);
						} else {
							if (_logger.isTraceEnabled())
								_logger.debug("saying pass-through (1) not matching delegatee (2): '" + passThrough + "' vs. '"
									+ assertion.getDelegatee()[0] + "'");
						}
					} else {
						if (_logger.isTraceEnabled())
							_logger.trace("did not get a pass through credential.");
					}
				}
			}

			retval.add(cred);
		}

		if (_logger.isTraceEnabled()) {
			String dumpedCreds = "";
			for (NuCredential cred : retval) {
				dumpedCreds = dumpedCreds.concat(cred.describe(VerbosityLevel.HIGH) + "\n");
			}
			_logger.debug("before leaving authbearcred credential set is:\n" + dumpedCreds);
		}

		return retval;
	}

	/**
	 * We retrieve delegated SAML credentials from the working context and store them in the calling context. This is done because we
	 * traditionally use the calling context, not the working context, for all security related purposes. Furthermore, this usage matches the
	 * client-side credentials handling logic.
	 */
	public static void populateSAMLPropertiesInContext(WorkingContext workingContext, ICallingContext callContext)
	{
		// migrate the credentials from working context to the calling context.
		CredentialWallet credentialsWallet =
			(CredentialWallet) workingContext.getProperty(SAMLConstants.SAML_CREDENTIALS_WORKING_CONTEXT_CREDS_PROPERTY_NAME);
		callContext.setTransientProperty(SAMLConstants.SAML_CREDENTIALS_WALLET_PROPERTY_NAME, credentialsWallet);

		// also copy the client's SSL certificate from the working context to the calling context.
		X509Certificate[] clientSSLCertificate =
			(X509Certificate[]) workingContext.getProperty(SAMLConstants.SAML_CLIENT_SSL_CERTIFICATE_PROPERTY_NAME);
		callContext.setTransientProperty(SAMLConstants.SAML_CLIENT_SSL_CERTIFICATE_PROPERTY_NAME, clientSSLCertificate);
	}

	/**
	 * Perform authorization for the callee resource.
	 * 
	 * @throws WSSecurityException
	 *             upon access-denial
	 */
	@SuppressWarnings("unchecked")
	public static void performAuthorizationChecks() throws AxisFault
	{
		boolean accessOkay = false; // assume access is disallowed until proven otherwise.
		IResource resource = null; // the resource in question.
		Method operation = null; // the method being called on the resource.

		try {
			// Grab working and message contexts
			WorkingContext workingContext = WorkingContext.getCurrentWorkingContext();
			MessageContext messageContext = (MessageContext) workingContext.getProperty(WorkingContext.MESSAGE_CONTEXT_KEY);

			/*
			 * Extract our calling context (any decryption should be over with). All GII message-level assertions and UT tokens should be
			 * within CALLER_CREDENTIALS_PROPERTY by now.
			 */
			ICallingContext callContext = ContextManager.getExistingContext();

			/*
			 * Create a list of public certificate chains that have been verified as holder-of-key (e.g., though SSL or message-level
			 * security).
			 */
			ArrayList<X509Certificate[]> authenticatedCertChains = new ArrayList<X509Certificate[]>();

			/*
			 * ensure that we aren't fooled by a malicious client into accepting their view of reality.
			 */
			callContext.removeProperty(GenesisIIConstants.LAST_TLS_CERT_FROM_CLIENT);

			// Grab the client-hello authenticated SSL cert-chain (if there was one)
			org.eclipse.jetty.server.Request req = (Request) messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);

			// req.getConnection().

			Object transport = req.getConnection().getEndPoint().getTransport();
			X509Certificate[] clientSslCertChain = null;
			if (transport instanceof SelectChannelEndPoint) {
				boolean stillStarting;
				synchronized (ServerWSDoAllReceiver._inStartupMode) {
					stillStarting = ServerWSDoAllReceiver._inStartupMode;
				}
				if (stillStarting == true) {
					Collection<String> ips = NetworkConfigTools.getIPAddresses();
					String clientIP = ((SelectChannelEndPoint) transport).getRemoteAddr();

					if (ips.contains(clientIP) == true) {
						/*
						 * future: do we ever see this logging happen? outcalls from self might still have non-localhost IP. or we skip making
						 * an outcall at all, which supposedly exists in the codebase somewhere.
						 */
						if (_logger.isDebugEnabled())
							_logger.debug("startup: allowing client on local address: " + clientIP);
					} else {
						if (_logger.isDebugEnabled())
							_logger.debug("startup: rejecting client at remote address: " + clientIP);
						//hmmm: make this something they can handle by trying again, not the try again fault so much, but something like that.
						throw new AxisFault("container in startup mode; access temporarily denied for remote connections.");
					}
				}

				try {
					if (((SelectChannelEndPoint) transport).getConnection() instanceof SslConnection) {
						if (req.getConnection().getEndPoint() instanceof EndPoint) {
							EndPoint ep = req.getConnection().getEndPoint();
							SSLSession session = ((SslEndPoint) ep).getSslEngine().getSession();
							if (session != null) {
								Certificate[] peerCerts = null;
								try {
									peerCerts = session.getPeerCertificates();
								} catch (SSLPeerUnverifiedException e) {
									_logger.error("Peer unverified while attempting to extract peer certificates.", e);
								}

								clientSslCertChain = new X509Certificate[peerCerts.length];
								int i = 0;
								for (Certificate c : peerCerts) {
									if (c instanceof X509Certificate) {
										clientSslCertChain[i++] = (X509Certificate) c;
									} else {
										throw new AxisFault("found wrong type of certificates for peer!");
									}
								}
							}

							if (_logger.isTraceEnabled())
								_logger.debug("incoming client ssl cert chain is: " + clientSslCertChain[0].getSubjectDN());
						}

						if (clientSslCertChain != null) {
							authenticatedCertChains.add(clientSslCertChain);

							// remember last TLS certificate from this client, so we can compare later.
							callContext.setSingleValueProperty(GenesisIIConstants.LAST_TLS_CERT_FROM_CLIENT, clientSslCertChain[0]);

							if (_logger.isTraceEnabled()) {
								X509Identity id = new X509Identity(clientSslCertChain);
								_logger.debug("decided that the client's peer certificate is this: " + id.toString());
							}
						} else {
							// hmmm: TEMPORARY!!!! remove this!!!! what to do instead though? we failed to find certificate.
							throw new SSLPeerUnverifiedException("argh!");
						}
					}
				} catch (SSLPeerUnverifiedException unverified) {
				}
			}

			populateSAMLPropertiesInContext(workingContext, callContext);

			/*
			 * Retrieve the message-level cert-chains that have been recorded in the signature-Crypto instance. (Unfortunately this method is
			 * only called with the end-certificate; without the chain, it's impossible to trust X.509 proxy certs).
			 */
			GIIBouncyCrypto sigCrypto = (GIIBouncyCrypto) messageContext.getProperty(ServerWSDoAllReceiver.SIG_CRYPTO_PROPERTY);
			if (sigCrypto != null) {
				authenticatedCertChains.addAll(sigCrypto.getLoadedCerts());
			}

			/*
			 * Retrieve and authenticate other accumulated message-level credentials (e.g., GII delegated assertions, etc.)
			 */
			ArrayList<NuCredential> bearerCredentials =
				(ArrayList<NuCredential>) callContext.getTransientProperty(SAMLConstants.CALLER_CREDENTIALS_PROPERTY);
			Collection<NuCredential> authenticatedCallerCreds =
				authenticateBearerCredentials(bearerCredentials, authenticatedCertChains, clientSslCertChain, callContext);

			if (_logger.isTraceEnabled()) {
				String dumpedCreds = "";
				for (NuCredential cred : authenticatedCallerCreds) {
					dumpedCreds.concat(cred.describe(VerbosityLevel.HIGH) + "\n");
				}

				_logger.debug("performauthz: full authenticated credential set is:\n" + dumpedCreds);
			}

			// Finally add all of our callerIds to the calling-context's outgoing credentials.
			TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(callContext);
			transientCredentials.addAll(authenticatedCallerCreds);

			// Grab the operation method from the message context
			org.apache.axis.description.OperationDesc desc = messageContext.getOperation();
			if (desc == null) {
				// pretend security doesn't exist -- axis will do what it does
				// when it can't figure out how to dispatch to a non-existent
				// method.
				_logger.debug("deferring to axis for null operation description.");
				return;
			}
			JavaServiceDesc jDesc = null;
			ServiceDesc serviceDescription = desc.getParent();
			if (serviceDescription != null && (serviceDescription instanceof JavaServiceDesc))
				jDesc = (JavaServiceDesc) serviceDescription;
			operation = desc.getMethod();
			if (_logger.isDebugEnabled())
				_logger.debug("client invokes " + operation.getDeclaringClass().getName() + "." + operation.getName() + "()");

			// Get the resource's authz handler
			resource = ResourceManager.getCurrentResource().dereference();
			IAuthZProvider authZHandler = AuthZProviders.getProvider(((ResourceKey) resource.getParentResourceKey()).getServiceName());

			// Let the authZ handler make the decision.
			accessOkay =
				authZHandler.checkAccess(authenticatedCallerCreds, resource,
					(jDesc == null) ? operation.getDeclaringClass() : jDesc.getImplClass(), operation);

			if (accessOkay) {
				addCredentialsForClientSession(clientSslCertChain[0], transientCredentials.generateWallet());
				resource.commit();
			}
		} catch (IOException e) {
			_logger.error("failing request due to: " + e.getMessage());
			throw new AxisFault(e.getMessage(), e);
		} catch (GeneralSecurityException e) {
			_logger.error("failing request due to: " + e.getMessage());
			throw new AxisFault(e.getMessage(), e);
		}
		if (!accessOkay) {
			throw new PermissionDeniedException(operation.getName(), ResourceManager.getResourceName(resource));
		}
	}

	/**
	 * When an operation is open access, but it requires that the caller have read, write, or execute permission to process certain
	 * parameters, the operation should call this method to verify that the caller has the right.
	 */
	public static boolean checkAccess(IResource resource, RWXCategory category) throws IOException
	{
		ICallingContext callContext = ContextManager.getExistingContext();
		TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(callContext);
		String serviceName = ((ResourceKey) resource.getParentResourceKey()).getServiceName();
		IAuthZProvider authZHandler = AuthZProviders.getProvider(serviceName);
		boolean success = authZHandler.checkAccess(transientCredentials.getCredentials(), resource, category);
		if (!success) {
			_logger.error("authorization failure on " + ResourceManager.getResourceName(resource) + " for access " + category.toString());
		}
		return success;
	}

}
