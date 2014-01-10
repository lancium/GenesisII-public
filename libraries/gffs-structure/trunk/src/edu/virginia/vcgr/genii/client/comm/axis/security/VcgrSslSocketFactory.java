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

package edu.virginia.vcgr.genii.client.comm.axis.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.cache.LRUCache;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.comm.socket.SocketConfigurer;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationUnloadedListener;
import edu.virginia.vcgr.genii.client.configuration.ContainerConfiguration;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.KeystoreManager;
import edu.virginia.vcgr.genii.security.x509.CertEntry;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.security.x509.SingleSSLX509KeyManager;

/**
 * Wrapper for the generic SSLSocketFactory.
 * 
 * Selects the identity in the current calling context's client cert/key for use during SSL
 * handshake. For containers, the TLS certificate is chosen instead of resource certificates.
 * 
 * Allows us to re-read trust-stores when we detect changes in the client/server configuration.
 * 
 * @author dgm4d
 */
public class VcgrSslSocketFactory extends SSLSocketFactory implements ConfigurationUnloadedListener
{
	static private Log _logger = LogFactory.getLog(VcgrSslSocketFactory.class);

	static final private String CACHE_SIZE_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.client.comm.axis.security.VcgrSslSocketFactory.max-cache-size";
	
	static final private int DEFAULT_MAX_CACHE_ELEMENTS = 64; // cak: reduced from 1024.

	static final private int SESSION_CACHE_SIZE_MAX = 256; // cak: reduced from 1000.

	// holds the maximum elements for the socket cache, rather than reading it from file every time.
	static private Integer _maxCacheElements = -1;

	static public InheritableThreadLocal<ICallingContext> threadCallingContext = new InheritableThreadLocal<ICallingContext>();

	static private LRUCache<KeyAndCertMaterialCacheKey, SSLSocketFactory> _sslSessionCache =
		new LRUCache<KeyAndCertMaterialCacheKey, SSLSocketFactory>(getMaxCacheElements());

	protected TrustManager[] _trustManagers;
	protected SocketConfigurer _clientSocketConfigurer = Installation.getDeployment(new DeploymentName())
		.clientSocketConfigurer();

	public VcgrSslSocketFactory()
	{
		// reset cached key/trust stores
		notifyUnloaded();
		// add a hook for notifications in the future.
		ConfigurationManager.addConfigurationUnloadListener(this);
	}

	public void notifyUnloaded()
	{
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			KeyStore trustStore = KeystoreManager.getTlsTrustStore();
			if (trustStore != null) {
				tmf.init(trustStore);
				synchronized (_sslSessionCache) {
					_trustManagers = tmf.getTrustManagers();
				}
			}
		} catch (Exception ex) {
			_logger.info("exception occurred in notifyUnloaded", ex);
		}
	}

	static public int getMaxCacheElements()
	{
		InputStream in = null;
		synchronized (_maxCacheElements) {
			if (_maxCacheElements > -1)
				return _maxCacheElements;
			_maxCacheElements = DEFAULT_MAX_CACHE_ELEMENTS;
			try {
				File sslCachePropertiesFile =
					Installation.getDeployment(new DeploymentName()).getConfigurationDirectory()
						.lookupFile("ssl-cache.properties");
				in = new FileInputStream(sslCachePropertiesFile);
				Properties props = new Properties();
				props.load(in);
				String value = props.getProperty(CACHE_SIZE_PROPERTY_NAME);
				if (value != null) {
					_maxCacheElements = Integer.parseInt(value);
					if (_maxCacheElements < 0)
						_maxCacheElements = 0;
				}
			} catch (Throwable cause) {
				_logger.warn("Unable to lookup ssl-cache.properties configuration file.  Using default values!", cause);
			} finally {
				StreamUtils.close(in);
			}
		}

		return _maxCacheElements;
	}

	protected SSLSocketFactory getSSLSocketFactory() throws IOException
	{
		// Use the current calling context's X.509 identity for SSL handshake
		SSLSocketFactory factory = null;
		KeyAndCertMaterialCacheKey cacheKey = null;

		try {
			ICallingContext callingContext = threadCallingContext.get();
			if (callingContext == null)
				throw new RuntimeException("We got a null calling context which " + "means that client invocation handler "
					+ "didn't set it up correctly.");

			KeyAndCertMaterial clientKeyMaterial =
				ClientUtils.checkAndRenewCredentials(callingContext, BaseGridTool.credsValidUntil(),
					new SecurityUpdateResults());

			/*
			 * use only the container TLS cert rather than resource cert, unless we are acting as a
			 * client.
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
			synchronized (_sslSessionCache) {
				factory = _sslSessionCache.get(cacheKey);
				if (factory != null) {
					return factory;
				}
			}

			KeyManager[] kms = new KeyManager[1];
			kms[0] = new SingleSSLX509KeyManager(clientKeyMaterial);

			SSLContext sslcontext = SSLContext.getInstance("TLS");

			SSLSessionContext sessionContext = sslcontext.getServerSessionContext();
			if (sessionContext == null) {
				if (_logger.isDebugEnabled())
					_logger.debug("Couldn't get a session context on which to set the cache size.");
			} else {
				if (sessionContext.getSessionCacheSize() > SESSION_CACHE_SIZE_MAX) {
					if (_logger.isDebugEnabled())
						_logger.debug("Setting server ssl session context cache size to max.");
					sessionContext.setSessionCacheSize(SESSION_CACHE_SIZE_MAX);
				}
			}

			TrustManager[] mgrs = null;
			// ensure we get a stable version of this.
			synchronized (_sslSessionCache) {
				mgrs = _trustManagers;
			}
			sslcontext.init(kms, mgrs, null);

			sessionContext = sslcontext.getServerSessionContext();
			if (sessionContext == null) {
				if (_logger.isDebugEnabled())
					_logger.debug("Couldn't get a session context on which to set the cache size.");
			} else {
				if (sessionContext.getSessionCacheSize() > SESSION_CACHE_SIZE_MAX) {
					if (_logger.isDebugEnabled())
						_logger.debug("Setting server ssl session context cache size to max.");
					sessionContext.setSessionCacheSize(SESSION_CACHE_SIZE_MAX);
				}
			}

			factory = (SSLSocketFactory) sslcontext.getSocketFactory();
			synchronized (_sslSessionCache) {
				_sslSessionCache.put(cacheKey, factory);
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

	@Override
	public Socket createSocket(Socket socket, String s, int i, boolean flag) throws IOException
	{
		return configureSocket(getSSLSocketFactory().createSocket(socket, s, i, flag));
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
	public Socket createSocket(String s, int i) throws IOException
	{
		return configureSocket(getSSLSocketFactory().createSocket(s, i));
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
}
