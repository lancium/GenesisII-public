package edu.virginia.vcgr.genii.security.x509;

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationUnloadedListener;
import edu.virginia.vcgr.genii.security.TrustStoreProvider;
import edu.virginia.vcgr.genii.security.utils.SecurityUtilities;

/**
 * A wrapper for the PKIX x509 trust manager that also checks revocation.
 */
public class RevocationAwareTrustManager implements ConfigurationUnloadedListener, X509TrustManager
{
	static private Log _logger = LogFactory.getLog(RevocationAwareTrustManager.class);

	// our list of the real trust managers that do work for us. should only be one element.
	private TrustManager[] _trustManagers = null;
	// synchronizes access to the trust managers.
	private Object _trustLock = new Object();
	// external object that provides the actual trust stores.
	private TrustStoreProvider _tsp = null;

	public RevocationAwareTrustManager(TrustStoreProvider tsp)
	{
		if (tsp == null)
			throw new RuntimeException("constructor given a null TrustStoreProvider");

		_tsp = tsp;

		notifyUnloaded();
		_tsp.addConfigurationUnloadListener(this);
	}

	@Override
	public void notifyUnloaded()
	{
		KeyStore trustStore = null;
		try {
			trustStore = _tsp.getTLSTrustStore();
		} catch (Throwable t) {
			_logger.error("notifyUnloaded failed to acquire trust store.");
		}
		synchronized (_trustLock) {
			try {
				_trustManagers = null;
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
				tmf.init(trustStore);
				_trustManagers = tmf.getTrustManagers();
			} catch (Throwable t) {
				_logger.error("notifyUnloaded failed to establish trust managers.");
			}
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers()
	{
		synchronized (_trustLock) {
			if (_trustManagers != null) {
				if (_trustManagers.length > 0) {
					((X509TrustManager) _trustManagers[0]).getAcceptedIssuers();
				}
			}
		}
		return new X509Certificate[0];
	}

	@Override
	public void checkClientTrusted(X509Certificate chain[], String authType) throws CertificateException
	{
		if (_logger.isTraceEnabled())
			_logger.debug("seeing client: " + chain[0].getSubjectDN());
		synchronized (_trustLock) {
			if (_trustManagers != null) {
				if (_trustManagers.length > 0) {
					X509TrustManager tm = (X509TrustManager) _trustManagers[0];
					tm.checkClientTrusted(chain, authType);
					try {
						if (_tsp.getCRLStore() != null)
							SecurityUtilities.isCertChainRevoked(_tsp.getCRLStore(), chain);
					} catch (Exception e) {
						throw new CertificateException(e.getLocalizedMessage(), e);
					}
				}
			} else {
				throw new CertificateException("trust manager has not been established properly");
			}
		}
	}

	@Override
	public void checkServerTrusted(X509Certificate chain[], String authType) throws CertificateException
	{
		if (_logger.isTraceEnabled())
			_logger.debug("seeing server: " + chain[0].getSubjectDN());
		synchronized (_trustLock) {
			if (_trustManagers != null) {
				if (_trustManagers.length > 0) {
					X509TrustManager tm = (X509TrustManager) _trustManagers[0];
					tm.checkServerTrusted(chain, authType);
					try {
						if (_tsp.getCRLStore() != null)
							SecurityUtilities.isCertChainRevoked(_tsp.getCRLStore(), chain);
					} catch (Exception e) {
						throw new CertificateException(e.getLocalizedMessage(), e);
					}
				}
			} else {
				throw new CertificateException("trust manager has not been established properly");
			}
		}
	}

}
