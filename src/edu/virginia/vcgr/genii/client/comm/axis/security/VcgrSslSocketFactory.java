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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.SocketFactory;

import java.security.*;
import java.util.Date;

import javax.net.ssl.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.comm.socket.SocketConfigurer;
import edu.virginia.vcgr.genii.client.configuration.*;
import edu.virginia.vcgr.genii.client.configuration.Security;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.x509.*;

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
	
	static public InheritableThreadLocal<ICallingContext> threadCallingContext =
		new InheritableThreadLocal<ICallingContext>();
	
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
				KeyStore ks = CertTool.openStoreDirectPath(
					Installation.getDeployment(new DeploymentName()).security(
						).getSecurityFile(trustStoreLoc),
					trustStoreType, trustStorePassChars);
		    	tmf.init(ks);
		    	_trustManagers = tmf.getTrustManagers();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static SocketFactory getDefault()
	{
		return new VcgrSslSocketFactory();
	}

	protected SSLSocketFactory getSSLSocketFactory() throws IOException
	{
		// Use the current calling context's X.509 identity for 
		// SSL handshake
		
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
			
			return (SSLSocketFactory) sslcontext.getSocketFactory();
	
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
			e.printStackTrace();
			return null;
		}
	}

	public String[] getSupportedCipherSuites() 
	{
		try {
			return getSSLSocketFactory().getSupportedCipherSuites();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static private Security getSSLProperties()
	{
		return Installation.getDeployment(new DeploymentName()).security();
	}
}
