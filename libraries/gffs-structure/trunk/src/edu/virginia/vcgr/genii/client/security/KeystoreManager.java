package edu.virginia.vcgr.genii.client.security;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.certGenerator.CertificateChainType;
import edu.virginia.vcgr.genii.client.InstallationProperties;
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

public class KeystoreManager
{
	static private Log _logger = LogFactory.getLog(KeystoreManager.class);

	static private KeyStore _resourceTrustStore = null;
	static private KeyStore _tlsTrustStore = null;
	static private Object _trustLock = new Object();
	static private CertStore _crlStore = null;

	/**
	 * Class to wipe our loaded config stuff in the event the config manager reloads.
	 */
	static {
		ConfigurationManager.addConfigurationUnloadListener(new ConfigUnloadListener());
	}

	public static class ConfigUnloadListener implements ConfigurationUnloadedListener
	{
		public void notifyUnloaded()
		{
			synchronized (_trustLock) {
				_resourceTrustStore = null;
				_tlsTrustStore = null;
				_crlStore = null;
			}
		}
	}

	public KeystoreManager()
	{
	}

	/**
	 * Creates a KeyStore for use in verifying resource identities.
	 */
	static public KeyStore getResourceTrustStore() throws AuthZSecurityException
	{
		synchronized (_trustLock) {
			if (_resourceTrustStore != null) {
				return _resourceTrustStore;
			}

			KeyStore trustStore = null;

			{
				// first get the resource trust store file.
				trustStore = loadResourceTrustStoreFromFile();
			}

			Security sslProps = getSSLProperties();

			String trustStoreType =
				sslProps.getProperty(KeystoreSecurityConstants.Client.SSL_TRUST_STORE_TYPE_PROP,
					KeystoreSecurityConstants.TRUST_STORE_TYPE_DEFAULT);

			{
				// load the local certificates from the state directory. this is the only place
				// those are found.
				File localCertsDir = new File(InstallationProperties.getUserDir() + "/local-certificates");
				trustStore = addCertificatesToKeystore(trustStore, localCertsDir, "local-certificates", trustStoreType);
			}

			{
				// load the trusted-certificates from the deployment, which is the only place these
				// live.
				String trustedCertificatesDirectory =
					sslProps.getProperty(KeystoreSecurityConstants.Client.RESOURCE_TRUSTED_CERTIFICATES_LOCATION_PROP);
				if (trustedCertificatesDirectory != null) {
					File trustedCertsDir =
						Installation.getDeployment(new DeploymentName()).security().getSecurityFile(trustedCertificatesDirectory);
					trustStore = addCertificatesToKeystore(trustStore, trustedCertsDir, "trusted-certificates", trustStoreType);
				}
			}

			if (trustStore == null) {
				String msg = "Complete failure to load resource trust store from file and no trusted certificates directories were found.";
				_logger.warn(msg);
				throw new SecurityException(msg);
			}

			_resourceTrustStore = trustStore;

			if (_logger.isTraceEnabled()) {
				_logger.debug("trust store for resources:\n" + SecurityUtilities.showTrustStore(trustStore));
			}

			return _resourceTrustStore;
		}
	}

	/**
	 * retrieves the current CRL list from the grid certificates directory.
	 */
	static public CertStore getCRLStore()
	{
		synchronized (_trustLock) {
			if (_crlStore != null) {
				return _crlStore;
			}

			ThreadAndProcessSynchronizer.acquireLock(CertUpdateHelpers.CONSISTENCY_LOCK_FILE);
			try {
				String crlDirectory = getGridCertsDir();
				if (crlDirectory != null) {
					List<X509CRL> crls = SecurityUtilities.loadCRLsFromDirectory(new File(crlDirectory));
					_crlStore = SecurityUtilities.createCertStoreFromCRLs(crls);
				}
			} catch (Exception e) {
				_logger.error("failed while loading CRL entries", e);
			}
			ThreadAndProcessSynchronizer.releaseLock(CertUpdateHelpers.CONSISTENCY_LOCK_FILE);

			return _crlStore;
		}
	}

	/**
	 * returns the folder where we should load the grid-certificates, which should provide *.r0 files for CRL lists.
	 */
	static public String getGridCertsDir()
	{
		Security sslProps = getSSLProperties();

		/*
		 * if grid certs dir is absolute path, then we don't allow an override, as the host should be providing the CRL repository and that
		 * directory should be kept up to date.
		 */
		String gridCertificatesDirectory = sslProps.getProperty(KeystoreSecurityConstants.Client.SSL_GRID_CERTIFICATES_LOCATION_PROP);
		if (gridCertificatesDirectory == null) {
			_logger.warn("no grid-certificates folder listed for property: "
				+ KeystoreSecurityConstants.Client.SSL_GRID_CERTIFICATES_LOCATION_PROP);
			return null;
		}
		boolean absolutePath = gridCertificatesDirectory.startsWith("/");
		// if not an absolute path in config, then load the grid certificates from grid-certificates
		// in the state directory first, if that exists. otherwise try loading it from the
		// deployment.
		String localGridCerts = InstallationProperties.getUserDir() + "/grid-certificates";
		File gridCertsDir = new File(localGridCerts);
		if (absolutePath || !gridCertsDir.isDirectory()) {
			return Installation.getDeployment(new DeploymentName()).security().getSecurityFile(gridCertificatesDirectory).getAbsolutePath();
		} else {
			return localGridCerts;
		}
	}

	/**
	 * Creates a KeyStore for use in verifying container TLS identities.
	 */
	static public KeyStore getTlsTrustStore()
	{
		synchronized (_trustLock) {
			if (_tlsTrustStore != null) {
				return _tlsTrustStore;
			}

			Security sslProps = getSSLProperties();

			KeyStore trustStore = null;
			try {
				trustStore = loadTLSTrustStoreFile();

				String trustStoreType =
					sslProps.getProperty(KeystoreSecurityConstants.Client.SSL_TRUST_STORE_TYPE_PROP,
						KeystoreSecurityConstants.TRUST_STORE_TYPE_DEFAULT);

				{
					// load the local certificates from the state directory. this is the only place
					// those are found.
					File localCertsDir = new File(InstallationProperties.getUserDir() + "/local-certificates");
					trustStore = addCertificatesToKeystore(trustStore, localCertsDir, "local-certificates", trustStoreType);
				}

				{
					// load the trusted-certificates from the deployment, which is the only place
					// these live.
					String trustedCertificatesDirectory =
						sslProps.getProperty(KeystoreSecurityConstants.Client.SSL_TRUSTED_CERTIFICATES_LOCATION_PROP);
					if (trustedCertificatesDirectory != null) {
						File trustedCertsDir =
							Installation.getDeployment(new DeploymentName()).security().getSecurityFile(trustedCertificatesDirectory);
						trustStore = addCertificatesToKeystore(trustStore, trustedCertsDir, "trusted-certificates", trustStoreType);
					}
				}

				{
					// load the grid-wide certificates, which is where we support loading CRL lists
					// also.

					String gridCertificatesDirectory = getGridCertsDir();
					_logger.debug("found the grid-certificates dir at: " + gridCertificatesDirectory);
					if (gridCertificatesDirectory != null) {
						// need filelock here to ensure we don't get a partial directory.
						ThreadAndProcessSynchronizer.acquireLock(CertUpdateHelpers.CONSISTENCY_LOCK_FILE);
						try {
							File gridCertsDir = new File(gridCertificatesDirectory);
							trustStore = addCertificatesToKeystore(trustStore, gridCertsDir, "grid-certificates", trustStoreType);
						} catch (Exception e) {
							_logger.error("failed while adding grid-certificates to TLS trust store", e);
						}
						ThreadAndProcessSynchronizer.releaseLock(CertUpdateHelpers.CONSISTENCY_LOCK_FILE);
					}
				}

			} catch (Exception ex) {
				_logger.info("exception occurred in KeystoreManager notifyUnloaded while loading trust store", ex);
			}

			if (trustStore == null) {
				String msg = "Complete failure to load TLS trust store from file and no trusted certificates directories were found.";
				_logger.warn(msg);
				throw new SecurityException(msg);
			}

			_tlsTrustStore = trustStore;
			if (_logger.isTraceEnabled()) {
				_logger.debug("trust store for tls:\n" + SecurityUtilities.showTrustStore(trustStore));
			}
		}
		return _tlsTrustStore;
	}

	/**
	 * forgets the existing TLS trust store and the CRL store, which will cause them to be reloaded on next access.
	 */
	static public void dropTrustStores()
	{
		// make sure we know to re-acquire these *before* other things do their reload of config.
		synchronized (_trustLock) {
			_resourceTrustStore = null;
			_tlsTrustStore = null;
			_crlStore = null;
		}

		ConfigurationManager.reloadConfiguration();
	}

	static public KeyStore loadResourceTrustStoreFromFile() throws AuthZSecurityException
	{
		KeyStore trustStore = null;
		try {
			Security security = Installation.getDeployment(new DeploymentName()).security();
			String trustStoreLoc = security.getProperty(KeystoreSecurityConstants.Client.RESOURCE_IDENTITY_TRUST_STORE_LOCATION_PROP);
			String trustStoreType =
				security.getProperty(KeystoreSecurityConstants.Client.RESOURCE_IDENTITY_TRUST_STORE_TYPE_PROP,
					KeystoreSecurityConstants.TRUST_STORE_TYPE_DEFAULT);
			String trustStorePass = security.getProperty(KeystoreSecurityConstants.Client.RESOURCE_IDENTITY_TRUST_STORE_PASSWORD_PROP);

			// open the trust store
			if (trustStoreLoc == null) {
				throw new GenesisIISecurityException("Could not load TrustManager: no identity trust store location specified");
			}
			char[] trustStorePassChars = null;
			if (trustStorePass != null) {
				trustStorePassChars = trustStorePass.toCharArray();
			}
			trustStore =
				CertTool.openStoreDirectPath(Installation.getDeployment(new DeploymentName()).security().getSecurityFile(trustStoreLoc),
					trustStoreType, trustStorePassChars);
			synchronized (_trustLock) {
				_resourceTrustStore = trustStore;
			}
		} catch (GeneralSecurityException e) {
			throw new AuthZSecurityException("Could not load TrustManager: " + e.getLocalizedMessage(), e);
			// } catch (ConfigurationException e) {
			// throw new AuthZSecurityException("Could not load TrustManager: " + e.getMessage(),
			// e);
		} catch (IOException e) {
			throw new AuthZSecurityException("Could not load TrustManager: " + e.getMessage(), e);
		}

		return trustStore;

	}

	static public KeyStore addCertificatesToKeystore(KeyStore ks, File certDirectory, String prefix, String trustStoreType)
	{
		// counter for giving unique names to certificates in combined trust store.
		int certificateIndex = 1;

		if (ks != null) {
			// always number past any possible existing number.
			try {
				certificateIndex = ks.size() + 1;
			} catch (KeyStoreException e) {
				_logger.warn("failed to test size of existing keystore file", e);
			}
		}

		if (!certDirectory.isDirectory()) {
			// no change.
			return ks;
		}

		try {
			_logger.debug("found " + prefix + " certs folder in: " + certDirectory);
			List<Certificate> certificateList = SecurityUtilities.loadCertificatesFromDirectory(certDirectory);
			if (certificateList != null && !certificateList.isEmpty()) {
				if (ks == null) {
					ks = SecurityUtilities.createTrustStoreFromCertificates(trustStoreType, null, certificateList);
				} else {
					for (Certificate certificate : certificateList) {
						final String alias = prefix + "_" + certificateIndex;
						ks.setCertificateEntry(alias, certificate);
						certificateIndex++;
					}
				}
			}
		} catch (Throwable cause) {
			_logger.warn("Trust store failed to load local certificates from directory: " + certDirectory, cause);
		}
		return ks;
	}

	static public KeyStore loadTLSTrustStoreFile()
	{
		Security sslProps = getSSLProperties();

		// open the trust store file.
		String trustStoreLoc = sslProps.getProperty(KeystoreSecurityConstants.Client.SSL_TRUST_STORE_LOCATION_PROP);
		String trustStoreType =
			sslProps.getProperty(KeystoreSecurityConstants.Client.SSL_TRUST_STORE_TYPE_PROP,
				KeystoreSecurityConstants.TRUST_STORE_TYPE_DEFAULT);
		String trustStorePass = sslProps.getProperty(KeystoreSecurityConstants.Client.SSL_TRUST_STORE_PASSWORD_PROP);

		KeyStore trustStore = null;

		char[] trustStorePassChars = null;
		if (trustStorePass != null) {
			trustStorePassChars = trustStorePass.toCharArray();
		}

		if (trustStoreLoc != null) {
			try {
				trustStore =
					CertTool.openStoreDirectPath(Installation.getDeployment(new DeploymentName()).security().getSecurityFile(trustStoreLoc),
						trustStoreType, trustStorePassChars);
			} catch (Throwable cause) {
				_logger.warn("Trust store failed to load from file " + trustStoreLoc
					+ "; will attempt to load trusted certificates from directory.", cause);
			}
		}

		return trustStore;
	}

	static public final X509Certificate[] decodeCertificateChain(CertificateChainType certChain) throws AuthZSecurityException
	{
		int numCerts = certChain.getCount();
		X509Certificate[] certs = new X509Certificate[numCerts];

		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			for (int i = 0; i < numCerts; i++) {
				byte[] encoded = certChain.getCertificate(i);
				certs[i] = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(encoded));
			}
		} catch (CertificateException e) {
			throw new AuthZSecurityException(e.getLocalizedMessage(), e);
		}
		return certs;
	}

	static public Collection<Identity> getCallerIdentities(ICallingContext callingContext) throws AuthZSecurityException
	{
		try {
			Collection<Identity> ret = new ArrayList<Identity>();

			if (callingContext == null)
				throw new AuthZSecurityException("Error processing credential: No calling context");

			// remove/renew stale creds/attributes
			ClientUtils.checkAndRenewCredentials(callingContext, BaseGridTool.credsValidUntil(), new SecurityUpdateResults());

			TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(callingContext);

			for (NuCredential cred : transientCredentials.getCredentials()) {
				/*
				 * If the cred is an Identity, then we simply add that identity to our identity list.
				 */
				if (cred instanceof Identity) {
					ret.add((Identity) cred);
				} else if (cred instanceof TrustCredential) {
					/*
					 * If the cred is a signed identity assertion, then we have to get the identity out of the assertion.
					 */
					TrustCredential tc = (TrustCredential) cred;
					X509Identity identityAttr = (X509Identity) tc.getRootIdentity();

					ret.add(identityAttr);
				}
			}

			return ret;
		} catch (IOException ioe) {
			throw new AuthZSecurityException("Unable to load current context.", ioe);
		}
	}

	static public Collection<Identity> getCallerIdentities() throws AuthZSecurityException, IOException, GeneralSecurityException
	{
		return getCallerIdentities(ContextManager.getExistingContext());
	}

	static private Security getSSLProperties()
	{
		return Installation.getDeployment(new DeploymentName()).security();
	}
}