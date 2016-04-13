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

package edu.virginia.vcgr.genii.client.comm.axis.security;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.axis.AxisProperties;
import org.apache.axis.components.net.DefaultCommonsHTTPClientProperties;
import org.apache.axis.transport.http.CommonsHTTPSender;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.ClientProperties;
import edu.virginia.vcgr.genii.client.cache.LRUCache;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.comm.socket.SocketConfigurer;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationUnloadedListener;
import edu.virginia.vcgr.genii.client.configuration.ContainerConfiguration;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.KeystoreManager;
import edu.virginia.vcgr.genii.client.security.TrustStoreLinkage;
import edu.virginia.vcgr.genii.security.x509.CertEntry;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.security.x509.RevocationAwareTrustManager;
import edu.virginia.vcgr.genii.security.x509.SingleSSLX509KeyManager;

/**
 * Wrapper for the generic SSLSocketFactory.
 * 
 * Selects the identity in the current calling context's client cert/key for use during SSL handshake. For containers, the TLS certificate is
 * chosen instead of resource certificates.
 * 
 * Allows us to re-read trust-stores when we detect changes in the client/server configuration.
 * 
 * @author dgm4d
 * @author cak0l
 */
public class VcgrSslSocketFactory extends SSLSocketFactory implements ConfigurationUnloadedListener
{
	static private Log _logger = LogFactory.getLog(VcgrSslSocketFactory.class);

	// this is the maximum number of sessions we should try to cache.
	public final static int DEFAULT_SESSION_CACHE_SIZE = 256;

	static {
		/*
		 * initializations to restrict types of TLS we will use (to disallow SSLv3 and thus avoid the POODLE exploit). this one is for
		 * https-client: evidence says this does not help for our case of the gffs client, but this does at least protect our normal web
		 * browsing from using SSLv3.
		 */
		java.lang.System.setProperty("https.protocols", "TLSv1");
	}

	static public InheritableThreadLocal<ICallingContext> threadCallingContext = new InheritableThreadLocal<ICallingContext>();

	// caches socket factories to avoid continually reconstructing.
	static private LRUCache<KeyAndCertMaterialCacheKey, SSLSocketFactory> _sslSocketFactoryCache =
		new LRUCache<KeyAndCertMaterialCacheKey, SSLSocketFactory>(ClientProperties.getClientProperties().getMaxSocketCacheElements());

	protected TrustManager[] _trustManagers;
	protected SocketConfigurer _clientSocketConfigurer = ClientProperties.getClientProperties().getClientSocketProperties();

	public VcgrSslSocketFactory()
	{
		notifyUnloaded();
		loadTrustManager();
	}

	@Override
	public void notifyUnloaded()
	{
		synchronized (_sslSocketFactoryCache) {
			_sslSocketFactoryCache.clear();
		}
	}

	public void loadTrustManager()
	{
		try {
			// we install a trust manager here that understands CRL checking.
			KeyStore trustStore = KeystoreManager.getTlsTrustStore();
			if (trustStore != null) {
				synchronized (_sslSocketFactoryCache) {
					_trustManagers = new TrustManager[1];
					// container key not used at this level.
					_trustManagers[0] = new RevocationAwareTrustManager(new TrustStoreLinkage());
				}
			}
		} catch (Exception ex) {
			_logger.warn("exception occurred in loadTrustManager", ex);
		}
	}

	protected SSLSocketFactory getSSLSocketFactory() throws IOException
	{
		// Use the current calling context's X.509 identity for SSL handshake
		SSLSocketFactory factory = null;
		KeyAndCertMaterialCacheKey cacheKey = null;

		try {
			ICallingContext callingContext = threadCallingContext.get();
			if (callingContext == null) {
				throw new RuntimeException(
					"We got a null calling context which " + "means that client invocation handler " + "didn't set it up correctly.");
			}

			KeyAndCertMaterial clientKeyMaterial =
				ClientUtils.checkAndRenewCredentials(callingContext, BaseGridTool.credsValidUntil(), new SecurityUpdateResults());

			/*
			 * use only the container TLS cert rather than resource cert, unless we are acting as a client.
			 */
			if (ConfigurationManager.getCurrentConfiguration().isServerRole()) {
				CertEntry tlsKey = ContainerConfiguration.getContainerTLSCert();
				if (tlsKey != null) {
					// now tell the connection to use our tls cert also.
					clientKeyMaterial = new KeyAndCertMaterial(tlsKey._certChain, tlsKey._privateKey);
					if (_logger.isDebugEnabled())
						_logger.debug("container outgoing TLS cert is: " + tlsKey._certChain[0].getSubjectDN());
				}
			}

			cacheKey = new KeyAndCertMaterialCacheKey(clientKeyMaterial);
			synchronized (_sslSocketFactoryCache) {
				factory = _sslSocketFactoryCache.get(cacheKey);
				if (factory != null) {
					return factory;
				}
			}

			KeyManager[] kms = new KeyManager[1];
			kms[0] = new SingleSSLX509KeyManager(clientKeyMaterial);

			SSLContext sslcontext = SSLContext.getInstance("TLS");
			// _logger.debug("ssl context claims protocol is: " + sslcontext.getProtocol() + " protocol.");

			// we set the client and server session cache sizes to allow session reuse (hopefully).

			SSLSessionContext serverSessionContext = sslcontext.getServerSessionContext();
			if (serverSessionContext == null) {
				_logger.warn("Couldn't get a server session context on which to set the cache size.");
			} else {
				serverSessionContext.setSessionCacheSize(DEFAULT_SESSION_CACHE_SIZE);
				if (_logger.isTraceEnabled())
					_logger.debug("Set server ssl session context cache size to: " + serverSessionContext.getSessionCacheSize());
			}

			SSLSessionContext clientSessionContext = sslcontext.getClientSessionContext();
			if (clientSessionContext == null) {
				_logger.warn("Couldn't get a client session context on which to set the cache size.");
			} else {
				clientSessionContext.setSessionCacheSize(DEFAULT_SESSION_CACHE_SIZE);
				if (_logger.isTraceEnabled())
					_logger.debug("Set client ssl session context cache size to: " + clientSessionContext.getSessionCacheSize());
			}

			TrustManager[] mgrs = null;
			// ensure we get a stable version of this.
			synchronized (_sslSocketFactoryCache) {
				mgrs = _trustManagers;
			}
			sslcontext.init(kms, mgrs, null);

			/*
			 * show which trust managers we have loaded. we want at least one of these to be our crl handling replacement.
			 */
			if (_logger.isTraceEnabled()) {
				for (TrustManager m : mgrs) {
					_logger.debug("trust manager: " + m.toString() + " type: " + m.getClass().getCanonicalName());
				}
			}

			factory = (SSLSocketFactory) sslcontext.getSocketFactory();

			synchronized (_sslSocketFactoryCache) {
				_sslSocketFactoryCache.put(cacheKey, factory);
			}
			return factory;

		} catch (GeneralSecurityException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	final private Socket configureSocket(Socket socket)
	{
		_clientSocketConfigurer.configureSocket(socket);
		return socket;
	}

	// had to add this to make CommonsHttpSender in axis happy. after this method existed, connections started working again.
	public Socket createSocket() throws IOException
	{
		return configureSocket(getSSLSocketFactory().createSocket());
	}

	@Override
	public Socket createSocket(Socket socket, String s, int i, boolean autoClose) throws IOException
	{
		return configureSocket(getSSLSocketFactory().createSocket(socket, s, i, autoClose));
	}

	@Override
	public Socket createSocket(InetAddress inaddr, int i, InetAddress inaddr1, int j) throws IOException
	{
		return configureSocket(getSSLSocketFactory().createSocket(inaddr, i, inaddr1, j));
	}

	@Override
	public Socket createSocket(InetAddress inaddr, int i) throws IOException
	{
		return configureSocket(getSSLSocketFactory().createSocket(inaddr, i));
	}

	@Override
	public Socket createSocket(String s, int i, InetAddress inaddr, int j) throws IOException
	{
		return configureSocket(getSSLSocketFactory().createSocket(s, i, inaddr, j));
	}

	@Override
	public Socket createSocket(String address, int port) throws IOException
	{
		return configureSocket(getSSLSocketFactory().createSocket(address, port));
	}

	public String[] getDefaultCipherSuites()
	{
		try {
			return getSSLSocketFactory().getSupportedCipherSuites();
		} catch (Exception e) {
			_logger.info("exception occurred in getDefaultCipherSuites", e);
			return null;
		}
	}

	public String[] getSupportedCipherSuites()
	{
		try {
			return getSSLSocketFactory().getSupportedCipherSuites();
		} catch (Exception e) {
			_logger.info("exception occurred in getSupportedCipherSuites", e);
			return null;
		}
	}

	static public void setupConnectionPool()
	{
		// hmmm: make these into symbolic constants not hardcoded numbers!

		// "Total Connections" Pool size
		AxisProperties.setProperty(DefaultCommonsHTTPClientProperties.MAXIMUM_TOTAL_CONNECTIONS_PROPERTY_KEY, "1000");
		// huge! was set to like 150.

		// "Connections per host" pool size
		AxisProperties.setProperty(DefaultCommonsHTTPClientProperties.MAXIMUM_CONNECTIONS_PER_HOST_PROPERTY_KEY, "50");

		// max duration to wait for a connection from the pool.
		AxisProperties.setProperty(DefaultCommonsHTTPClientProperties.CONNECTION_POOL_TIMEOUT_KEY, "30000");

		// Timeout to establish connection in milliseconds.
		AxisProperties.setProperty(DefaultCommonsHTTPClientProperties.CONNECTION_DEFAULT_CONNECTION_TIMEOUT_KEY,
			"" + ClientProperties.getClientProperties().getClientTimeout());

		// Timeout "waiting for data" (read timeout)
		AxisProperties.setProperty(DefaultCommonsHTTPClientProperties.CONNECTION_DEFAULT_SO_TIMEOUT_KEY,
			"" + SocketConfigurer.DEFAULT_SOCKET_READ_TIMEOUT);
	}

	static public class KeyAndCertMaterialCacheKey
	{
		private X509Certificate _cert;

		public KeyAndCertMaterialCacheKey(KeyAndCertMaterial material)
		{
			if (material._clientCertChain == null || material._clientCertChain.length == 0)
				_cert = null;
			else
				_cert = material._clientCertChain[0];
		}

		public KeyAndCertMaterialCacheKey(X509Certificate cert)
		{
			_cert = cert;
		}

		@Override
		final public int hashCode()
		{
			return (_cert == null) ? 0 : _cert.hashCode();
		}

		@Override
		final public boolean equals(Object tmpOther)
		{
			if (tmpOther instanceof KeyAndCertMaterialCacheKey) {
				KeyAndCertMaterialCacheKey other = (KeyAndCertMaterialCacheKey) tmpOther;
				if (_cert == null) {
					if (other._cert == null)
						return true;
					else
						return false;
				} else {
					if (other._cert == null)
						return false;
				}

				return _cert.equals(other._cert);
			}

			return false;
		}
	}
	
	/**
	 * drops any connections that are not being used currently.  this causes the ssl information to be flushed out.
	 */
	public static void closeIdleConnections()
	{
		try {
			// drop any connections that are established to avoid keeping session alive with wrong creds.
			HttpConnectionManager connMgr = CommonsHTTPSender.getConnectionManager();
			if (connMgr != null) {
				// we close idle with an idle timeout of 0, which should mean everyone, even active connections.
				connMgr.closeIdleConnections(0);
			}

		} catch (Throwable t) {
			if (_logger.isTraceEnabled())
				_logger.debug("screwup from closing idle connections", t);
		}

	}
}
