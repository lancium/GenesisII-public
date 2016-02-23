package edu.virginia.vcgr.genii.client.comm.jetty;

import java.security.KeyStore;
import java.security.cert.CRL;
import java.util.Collection;

import javax.net.ssl.TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import edu.virginia.vcgr.genii.client.comm.axis.security.VcgrSslSocketFactory;
import edu.virginia.vcgr.genii.security.x509.TrustAllX509TrustManager;

public class TrustAllSslContextFactory extends SslContextFactory
{
	static private Log _logger = LogFactory.getLog(TrustAllSslContextFactory.class);

	TrustManager[] _manglers = null;

	public TrustAllSslContextFactory(String keystoreName, String password, String keyPassword, String storeType)
	{
		try {
			// use our trust-all trust manager
			TrustManager trustAll = new TrustAllX509TrustManager();
			_manglers = new TrustManager[1];
			_manglers[0] = trustAll;

			String protocol = "TLSv1.1";
			this.setProtocol(protocol);

			setKeyStorePath(keystoreName);
			setKeyStoreType(storeType);

			setKeyStorePassword(password);
			setKeyManagerPassword(keyPassword);

			// apparently we need this set to true if we are to know the client side at all.
			setNeedClientAuth(true);

			setSessionCachingEnabled(true);
			setSslSessionCacheSize(VcgrSslSocketFactory.DEFAULT_SESSION_CACHE_SIZE);
			if (_logger.isTraceEnabled())
				_logger.debug("ssl session cache size now set to " + getSslSessionCacheSize());

		} catch (Exception e) {
			_logger.error("failed during trust all ssl context factory setup", e);
		}

	}

	protected TrustManager[] getTrustManagers(KeyStore trustStore, Collection<? extends CRL> crls) throws Exception
	{
		return _manglers;
	}

}
