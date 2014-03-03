package edu.virginia.vcgr.genii.client.security;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.certGenerator.CertificateChainType;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationUnloadedListener;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.KeystoreSecurityConstants;
import edu.virginia.vcgr.genii.client.configuration.Security;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.identity.Identity;
import edu.virginia.vcgr.genii.security.utils.SecurityUtilities;
import edu.virginia.vcgr.genii.security.x509.CertTool;

public class KeystoreManager {
	static private Log _logger = LogFactory.getLog(KeystoreManager.class);

	static private KeyStore _resourceTrustStore = null;
	static private KeyStore _tlsTrustStore = null;
	static private Object _trustLock = new Object();

	/**
	 * Class to wipe our loaded config stuff in the event the config manager
	 * reloads.
	 */
	static {
		ConfigurationManager
				.addConfigurationUnloadListener(new ConfigUnloadListener());
	}

	public static class ConfigUnloadListener implements
			ConfigurationUnloadedListener {
		public void notifyUnloaded() {
			synchronized (_trustLock) {
				_resourceTrustStore = null;
				_tlsTrustStore = null;
			}
		}
	}

	public KeystoreManager() {
	}

	/**
	 * Creates a KeyStore for use in verifying resource identities.
	 */
	static public KeyStore getResourceTrustStore()
			throws GeneralSecurityException {
		synchronized (_trustLock) {
			if (_resourceTrustStore != null) {
				return _resourceTrustStore;
			}
		}

		KeyStore trustStore = null;
		try {
			Security security = Installation
					.getDeployment(new DeploymentName()).security();
			String trustStoreLoc = security
					.getProperty(KeystoreSecurityConstants.Client.RESOURCE_IDENTITY_TRUST_STORE_LOCATION_PROP);
			String trustStoreType = security
					.getProperty(
							KeystoreSecurityConstants.Client.RESOURCE_IDENTITY_TRUST_STORE_TYPE_PROP,
							KeystoreSecurityConstants.TRUST_STORE_TYPE_DEFAULT);
			String trustStorePass = security
					.getProperty(KeystoreSecurityConstants.Client.RESOURCE_IDENTITY_TRUST_STORE_PASSWORD_PROP);

			// open the trust store
			if (trustStoreLoc == null) {
				throw new GenesisIISecurityException(
						"Could not load TrustManager: no identity trust store location specified");
			}
			char[] trustStorePassChars = null;
			if (trustStorePass != null) {
				trustStorePassChars = trustStorePass.toCharArray();
			}
			trustStore = CertTool.openStoreDirectPath(Installation
					.getDeployment(new DeploymentName()).security()
					.getSecurityFile(trustStoreLoc), trustStoreType,
					trustStorePassChars);
			synchronized (_trustLock) {
				_resourceTrustStore = trustStore;
			}

		} catch (ConfigurationException e) {
			throw new GeneralSecurityException("Could not load TrustManager: "
					+ e.getMessage(), e);
		} catch (IOException e) {
			throw new GeneralSecurityException("Could not load TrustManager: "
					+ e.getMessage(), e);
		}
		if (_logger.isTraceEnabled()) {
			_logger.trace("trust store for resources:\n"
					+ SecurityUtilities.showTrustStore(trustStore));
		}
		return trustStore;
	}

	/**
	 * Creates a KeyStore for use in verifying container TLS identities.
	 */
	static public KeyStore getTlsTrustStore() {
		synchronized (_trustLock) {
			if (_tlsTrustStore != null) {
				return _tlsTrustStore;
			}
		}

		KeyStore trustStore = null;
		try {
			// open the trust store and init the trust manager factory, if
			// possible
			Security sslProps = getSSLProperties();

			String trustStoreLoc = sslProps
					.getProperty(KeystoreSecurityConstants.Client.SSL_TRUST_STORE_LOCATION_PROP);
			String trustStoreType = sslProps.getProperty(
					KeystoreSecurityConstants.Client.SSL_TRUST_STORE_TYPE_PROP,
					KeystoreSecurityConstants.TRUST_STORE_TYPE_DEFAULT);
			String trustStorePass = sslProps
					.getProperty(KeystoreSecurityConstants.Client.SSL_TRUST_STORE_PASSWORD_PROP);

			char[] trustStorePassChars = null;
			if (trustStorePass != null) {
				trustStorePassChars = trustStorePass.toCharArray();
			}

			if (trustStoreLoc != null) {
				try {
					trustStore = CertTool.openStoreDirectPath(Installation
							.getDeployment(new DeploymentName()).security()
							.getSecurityFile(trustStoreLoc), trustStoreType,
							trustStorePassChars);
				} catch (Throwable cause) {
					_logger.warn(
							"Trust store failed to load from file "
									+ trustStoreLoc
									+ "; will attempt to load trusted certificates from directory.",
							cause);
				}
			}

			String trustedCertificatesDirectory = sslProps
					.getProperty(KeystoreSecurityConstants.Client.SSL_TRUSTED_CERTIFICATES_LOCATION_PROP);
			if (trustedCertificatesDirectory == null) {
				if (trustStore == null) {
					_logger.warn("Complete failure to load trust store from file and no trusted certificate directory is set.");
				}
			} else {
				try {
					File certificatesDirectory = Installation
							.getDeployment(new DeploymentName()).security()
							.getSecurityFile(trustedCertificatesDirectory);
					List<Certificate> certificateList = SecurityUtilities
							.loadCertificatesFromDirectory(certificatesDirectory);
					if (certificateList != null && !certificateList.isEmpty()) {
						if (trustStore == null) {
							trustStore = SecurityUtilities
									.createTrustStoreFromCertificates(
											trustStoreType, trustStorePass,
											certificateList);
						} else {
							int certificateIndex = 0;
							for (Certificate certificate : certificateList) {
								final String alias = "trusted_certificate_"
										+ certificateIndex;
								trustStore.setCertificateEntry(alias,
										certificate);
								certificateIndex++;
							}
						}
					}
				} catch (Throwable cause) {
					_logger.warn(
							"Trust store failed to load trusted certificates from directory.",
							cause);
				}
			}
		} catch (Exception ex) {
			_logger.info("exception occurred in notifyUnloaded", ex);
		}

		synchronized (_trustLock) {
			_tlsTrustStore = trustStore;
		}
		if (_logger.isTraceEnabled()) {
			_logger.trace("trust store for tls:\n"
					+ SecurityUtilities.showTrustStore(trustStore));
		}
		return trustStore;
	}

	static public final X509Certificate[] decodeCertificateChain(
			CertificateChainType certChain) throws GeneralSecurityException {
		int numCerts = certChain.getCount();
		X509Certificate[] certs = new X509Certificate[numCerts];

		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		for (int i = 0; i < numCerts; i++) {
			byte[] encoded = certChain.getCertificate(i);
			certs[i] = (X509Certificate) cf
					.generateCertificate(new ByteArrayInputStream(encoded));
		}
		return certs;
	}

	static public Collection<Identity> getCallerIdentities(
			ICallingContext callingContext) throws AuthZSecurityException,
			GeneralSecurityException {
		try {
			Collection<Identity> ret = new ArrayList<Identity>();

			if (callingContext == null)
				throw new AuthZSecurityException(
						"Error processing credential: No calling context");

			// remove/renew stale creds/attributes
			ClientUtils
					.checkAndRenewCredentials(callingContext,
							BaseGridTool.credsValidUntil(),
							new SecurityUpdateResults());

			TransientCredentials transientCredentials = TransientCredentials
					.getTransientCredentials(callingContext);

			for (NuCredential cred : transientCredentials.getCredentials()) {
				/*
				 * If the cred is an Identity, then we simply add that identity
				 * to our identity list.
				 */
				if (cred instanceof Identity) {
					ret.add((Identity) cred);
				} else if (cred instanceof TrustCredential) {
					/*
					 * If the cred is a signed identity assertion, then we have
					 * to get the identity out of the assertion.
					 */
					TrustCredential tc = (TrustCredential) cred;
					X509Identity identityAttr = (X509Identity) tc
							.getRootIdentity();

					ret.add(identityAttr);
				}
			}

			return ret;
		} catch (IOException ioe) {
			throw new AuthZSecurityException("Unable to load current context.",
					ioe);
		}
	}

	static public Collection<Identity> getCallerIdentities()
			throws AuthZSecurityException, IOException,
			GeneralSecurityException {
		return getCallerIdentities(ContextManager.getExistingContext());
	}

	static private Security getSSLProperties() {
		return Installation.getDeployment(new DeploymentName()).security();
	}
}