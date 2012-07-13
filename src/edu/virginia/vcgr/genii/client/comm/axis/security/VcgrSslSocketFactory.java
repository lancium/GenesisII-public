/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package edu.virginia.vcgr.genii.client.comm.axis.security;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.net.SocketFactory;
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
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.comm.socket.SocketConfigurer;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationUnloadedListener;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.Security;
import edu.virginia.vcgr.genii.client.configuration.SecurityConstants;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.x509.CertTool;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.client.security.x509.SingleSSLX509KeyManager;

/**
 * Wrapper for the generic SSLSocketFactory.
 * 
 * Selects the identity in the current calling context's client cert/key
 * for use during SSL handshake.
 * 
 * Allows us to re-read trust-stores when we detect changes in the 
 * client/server configuration. 
 * 
 * @author dgm4d
 *
 */
public class VcgrSslSocketFactory 
		extends SSLSocketFactory 
		implements ConfigurationUnloadedListener
{
	static private Log _logger = LogFactory.getLog(VcgrSslSocketFactory.class);
	
	static private class KeyAndCertMaterialCacheKey
	{
		private X509Certificate _cert;
		
		private KeyAndCertMaterialCacheKey(KeyAndCertMaterial material)
		{
			if (material._clientCertChain == null || 
					material._clientCertChain.length == 0)
				_cert = null;
			else
				_cert = material._clientCertChain[0];
		}
		
		@Override
		final public int hashCode()
		{
			return (_cert == null) ? 0 : _cert.hashCode();
		}
		
		@Override
		final public boolean equals(Object tmpOther)
		{
			if (tmpOther instanceof KeyAndCertMaterialCacheKey)
			{
				KeyAndCertMaterialCacheKey other = 
					(KeyAndCertMaterialCacheKey)tmpOther;
				if (_cert == null)
				{
					if (other._cert == null)
						return true;
					else
						return false;
				} else
				{
					if (other._cert == null)
						return false;
				}
				
				return _cert.equals(other._cert);
			}
			
			return false;
		}
	}
	
	static final private String CACHE_SIZE_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.client.comm.axis.security.VcgrSslSocketFactory.max-cache-size";
	static final private int DEFAULT_MAX_CACHE_ELEMENTS = 1024;
	static public InheritableThreadLocal<ICallingContext> threadCallingContext =
		new InheritableThreadLocal<ICallingContext>();
	
	static private LRUCache<KeyAndCertMaterialCacheKey, SSLSocketFactory> 
		_sslSessionCache = null;
	
	static
	{
		InputStream in = null;
		int maxCacheElements = DEFAULT_MAX_CACHE_ELEMENTS;
		
		try
		{
			File sslCachePropertiesFile = Installation.getDeployment(
				new DeploymentName()).getConfigurationDirectory().lookupFile(
					"ssl-cache.properties");
			in = new FileInputStream(sslCachePropertiesFile);
			Properties props = new Properties();
			props.load(in);
			String value = props.getProperty(CACHE_SIZE_PROPERTY_NAME);
			if (value != null)
			{
				maxCacheElements = Integer.parseInt(value);
				if (maxCacheElements < 0)
					maxCacheElements = 0;
			}
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to lookup ssl-cache.properties " +
				"configuration file.  Using default values!", cause);
		}
		finally
		{
			StreamUtils.close(in);
		}
		
		_sslSessionCache = new LRUCache<KeyAndCertMaterialCacheKey, SSLSocketFactory>(
			maxCacheElements);
	}
	
    protected TrustManager[] _trustManagers;
    protected SecureRandom _random = null;
	protected SocketConfigurer _clientSocketConfigurer;
	
	public VcgrSslSocketFactory() {
		ConfigurationManager.addConfigurationUnloadListener(this);
		_clientSocketConfigurer = Installation.getDeployment(
			new DeploymentName()).clientSocketConfigurer();
		// reset cached key/trust stores
		notifyUnloaded();
	}
	
	public synchronized void notifyUnloaded() {
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

			// open the trust store and init the trust manager factory, if possible
			Security sslProps = getSSLProperties();
			KeyStore trustStore = null;
			
			String trustStoreLoc = sslProps.getProperty(
				SecurityConstants.Client.SSL_TRUST_STORE_LOCATION_PROP);
			String trustStoreType = sslProps.getProperty(
				SecurityConstants.Client.SSL_TRUST_STORE_TYPE_PROP, 
				SecurityConstants.TRUST_STORE_TYPE_DEFAULT);
			String trustStorePass = sslProps.getProperty(
				SecurityConstants.Client.SSL_TRUST_STORE_PASSWORD_PROP);
			
			char[] trustStorePassChars = null;
			
			if (trustStorePass != null) {
				trustStorePassChars = trustStorePass.toCharArray();
			}
			
			if (trustStoreLoc != null) {
				try {
					trustStore = CertTool.openStoreDirectPath(
						Installation.getDeployment(new DeploymentName()).security(
							).getSecurityFile(trustStoreLoc),
						trustStoreType, trustStorePassChars);
				} catch (Throwable cause) {
					_logger.info("Trust store failed to load from file " + trustStoreLoc + "; will attempt to load trusted certificates from directory.", cause);
				}
			}
			
			String trustedCertificatesDirectory = sslProps.getProperty(
					SecurityConstants.Client.SSL_TRUSTED_CERTIFICATES_LOCATION_PROP);
			if (trustedCertificatesDirectory == null) {
				if (trustStore == null) {
					_logger.warn("Complete failure to load trust store from file and no trusted certificate directory is set.");
				}
			} else {
				try {
					File certificatesDirectory = Installation.getDeployment(new DeploymentName()).security(
							).getSecurityFile(trustedCertificatesDirectory);
					List<Certificate> certificateList = loadCertificatesFromDirectory(certificatesDirectory);
					if (certificateList != null && !certificateList.isEmpty()) {
						if (trustStore == null) {
							trustStore = createTrustStoreFromCertificates(trustStoreType, 
									trustStorePass, certificateList);
						} else {
							int certificateIndex = 0;
							for (Certificate certificate : certificateList) {
								final String alias = "trusted_certificate_" + certificateIndex;
								trustStore.setCertificateEntry(alias, certificate);
								certificateIndex++;
							}
						}
					}
				} catch (Throwable cause) {
					_logger.info("Trust store failed to load trusted certificates from directory.", cause);
				}
			}
			
			if (trustStore != null) {
				Enumeration<String> aliases = trustStore.aliases();
				if (aliases != null) {
					while (aliases.hasMoreElements()) {
						_logger.debug("Trust-Store alias: " + aliases.nextElement());
					}
				}
				tmf.init(trustStore);
		    	_trustManagers = tmf.getTrustManagers();
			}
			
		} catch (Exception ex) {
			_logger.info("exception occurred in notifyUnloaded", ex);
		}
	}
	
	private List<Certificate> loadCertificatesFromDirectory(File directory) {
		
		if (directory == null || !directory.isDirectory()) return Collections.emptyList();
		
		List<Certificate> certificateList = new ArrayList<Certificate>();
		File[] certificateFiles = directory.listFiles();

		_logger.info("Loading Trusted Certificates ... ");
		
		for (File certificateFile : certificateFiles) {
			try {
				// skip hidden files, i.e. those that start with a dot.
				char testHidden = certificateFile.getName().charAt(0);
				if (testHidden == '.') continue;
				FileInputStream fis = new FileInputStream(certificateFile);
				BufferedInputStream bis = new BufferedInputStream(fis);
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				while (bis.available() > 0) {
					Certificate certificate = cf.generateCertificate(bis);
					certificateList.add(certificate);
				}
				fis.close();
				_logger.debug("Loaded trusted certificate(s) from file: " + certificateFile.getName());
			} catch (Exception ex) {
				_logger.warn("Failed to load certificates from file: " + certificateFile.getName(), ex);
			}
		}
		
		return certificateList;
	}
	
	private KeyStore createTrustStoreFromCertificates(String proposedTrustStoreType, 
			String password, List<Certificate> certificateList) {
		
		KeyStore trustStore = null;
		char[] trustStorePassword = (password == null) 
				? "genesisII".toCharArray() : password.toCharArray();
		
		Set<String> trustStoreTypes = new HashSet<String>();
		trustStoreTypes.add(proposedTrustStoreType);
		trustStoreTypes.add("JKS");
		trustStoreTypes.add(KeyStore.getDefaultType());

		for (String type : trustStoreTypes) {
			try {
				trustStore = KeyStore.getInstance(type);
				trustStore.load(null, trustStorePassword);
				int certificateIndex = 0;
				for (Certificate certificate : certificateList) {
					final String alias = "trusted_certificate_" + certificateIndex;
					trustStore.setCertificateEntry(alias, certificate);
					certificateIndex++;
				}
				break; // Successfully loaded all the certificates.
				       // Don't have to try the other options.
			} catch (Exception ex) {}
		}
		return trustStore;
	}
	
	public static SocketFactory getDefault()
	{
		return new VcgrSslSocketFactory();
	}

	protected SSLSocketFactory getSSLSocketFactory() throws IOException
	{
		// Use the current calling context's X.509 identity for 
		// SSL handshake
		SSLSocketFactory factory = null;
		KeyAndCertMaterialCacheKey cacheKey = null;
		
		try {
			
			ICallingContext callingContext = threadCallingContext.get();
			if (callingContext == null)
				throw new RuntimeException(
					"We got a null calling context which " +
					"means that client invocation handler " +
					"didn't set it up correctly.");
			
			KeyAndCertMaterial clientKeyMaterial = 
				ClientUtils.checkAndRenewCredentials(callingContext, 
				new Date(), new SecurityUpdateResults());
			cacheKey = new KeyAndCertMaterialCacheKey(clientKeyMaterial);
			synchronized(_sslSessionCache)
			{
				factory = _sslSessionCache.get(cacheKey);
				if (factory != null)
					return factory;
			}
			
			KeyManager[] kms = new KeyManager[1];
			kms[0] = new SingleSSLX509KeyManager(clientKeyMaterial);
			
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			
			SSLSessionContext sessionContext = sslcontext.getServerSessionContext();
			if (sessionContext == null)
				_logger.debug("Couldn't get a session context on which to set the cache size.");
			else
			{
				if (sessionContext.getSessionCacheSize() > 1000)
				{
					_logger.debug("Setting server ssl session context cache size to 1000.");
					sessionContext.setSessionCacheSize(1000);
				}
			}
			
			sslcontext.init(kms, _trustManagers, _random);
			
			sessionContext = sslcontext.getServerSessionContext();
			if (sessionContext == null)
				_logger.debug("Couldn't get a session context on which to set the cache size.");
			else
			{
				if (sessionContext.getSessionCacheSize() > 1000)
				{
					_logger.debug("Setting server ssl session context cache size to 1000.");
					sessionContext.setSessionCacheSize(1000);
				}
			}
			
			factory = (SSLSocketFactory) sslcontext.getSocketFactory();
			synchronized(_sslSessionCache)
			{
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
	
	public Socket createSocket(Socket socket, String s, int i, boolean flag)
		throws IOException 
	{
		return configureSocket(getSSLSocketFactory().createSocket(socket, s, i, flag));
	}

	public Socket createSocket(InetAddress inaddr, int i, InetAddress inaddr1,
		int j) throws IOException
	{
		return configureSocket(getSSLSocketFactory().createSocket(inaddr, i, inaddr1, j));
	}

	public Socket createSocket(InetAddress inaddr, int i) throws IOException 
	{
		return configureSocket(getSSLSocketFactory().createSocket(inaddr, i));
	}

	public Socket createSocket(String s, int i, InetAddress inaddr, int j)
		throws IOException 
	{
		return configureSocket(getSSLSocketFactory().createSocket(s, i, inaddr, j));
	}

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
	
	static private Security getSSLProperties()
	{
		return Installation.getDeployment(new DeploymentName()).security();
	}
}
