package edu.virginia.vcgr.genii.security.x509;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.security.utils.SecurityUtilities;

/**
 * A wrapper for the PKIX x509 trust manager that also checks revocation.
 */
public class RevocationAwareTrustManager implements X509TrustManager
{
	static private Log _logger = LogFactory.getLog(X509TrustManager.class);

	// our list of the real trust managers that do work for us.  should only be one element.
	private TrustManager[] _trustManagers = null;
	// the certificate store that tracks our CRL certificates.
	private CertStore _crlStore = null;
	
	public RevocationAwareTrustManager(KeyStore trustStore, String crlDirectory)
	{
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
			if (trustStore != null) {
				tmf.init(trustStore);
				_trustManagers = tmf.getTrustManagers();
			} 
		} catch (Throwable t) {
			// it's bad to throw exceptions from constructors, but we are basically dysfunctional now.
			_logger.error("constructor failed to establish trust managers.");
		}
		
		// read the crls from the crldirectory and turn them into a certstore.
		if (crlDirectory != null) {
			List<X509CRL> crls = SecurityUtilities.loadCRLsFromDirectory(new File(crlDirectory));
			_crlStore = SecurityUtilities.createCertStoreFromCRLs(crls);
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers()
	{
		if (_trustManagers !=  null) {
			if (_trustManagers.length > 0) {
				((X509TrustManager)_trustManagers[0]).getAcceptedIssuers();
			}
		}
		return new X509Certificate[0];
	}

	@Override
	public void checkClientTrusted(X509Certificate chain[], String authType) throws CertificateException
	{
		_logger.debug("seeing client: " + chain[0].getSubjectDN());
		if (_trustManagers !=  null) {
			if (_trustManagers.length > 0) {
				X509TrustManager tm = (X509TrustManager )_trustManagers[0]; 
				tm.checkClientTrusted(chain, authType);
				if (_crlStore != null)
					SecurityUtilities.isCertChainRevoked(_crlStore, chain);
			}
		} else {
			throw new CertificateException("trust manager has not been established properly");
		}
	}

	@Override
	public void checkServerTrusted(X509Certificate chain[], String authType) throws CertificateException
	{
		_logger.debug("seeing server: " + chain[0].getSubjectDN());
		if (_trustManagers !=  null) {
			if (_trustManagers.length > 0) {
				X509TrustManager tm = (X509TrustManager )_trustManagers[0]; 
				tm.checkServerTrusted(chain, authType);
				if (_crlStore != null)
					SecurityUtilities.isCertChainRevoked(_crlStore, chain);
			}
		} else {
			throw new CertificateException("trust manager has not been established properly");
		}
	}

}
