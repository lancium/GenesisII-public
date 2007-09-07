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
import java.util.Properties;

import javax.net.ssl.*;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.security.x509.CertTool;
import edu.virginia.vcgr.genii.client.utils.deployment.DeploymentRelativeFile;

public class VcgrSslSocketFactory extends SSLSocketFactory 
{
	private SSLSocketFactory factory;

	public VcgrSslSocketFactory() 
	{
		try
		{
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

			// open the trust store and init the trust manager factory, if possible
			Properties sslProps = getSSLProperties();
			String trustStoreLoc = sslProps.getProperty(
				GenesisIIConstants.TRUST_STORE_LOCATION_PROPERTY);
			String trustStoreType = sslProps.getProperty(
				GenesisIIConstants.TRUST_STORE_TYPE_PROPERTY, 
				GenesisIIConstants.TRUST_STORE_TYPE_DEFAULT);
			String trustStorePass = sslProps.getProperty(
				GenesisIIConstants.TRUST_STORE_PASSWORD_PROPERTY);
			
			char[] trustStorePassChars = null;
			
			if (trustStorePass != null)
				trustStorePassChars = trustStorePass.toCharArray();
			
			if (trustStoreLoc != null)
			{
				KeyStore ks = CertTool.openStoreDirectPath(new DeploymentRelativeFile(trustStoreLoc),
					trustStoreType, trustStorePassChars);
		    	tmf.init(ks);

		    	// create the ssl context with the new trust manager factory
		    	SSLContext sslcontext = SSLContext.getInstance("TLS");
				sslcontext.init(null, tmf.getTrustManagers(), null);
				
				// get the factory from the context
				factory = (SSLSocketFactory) sslcontext.getSocketFactory();
			} else 
			{
				// no trust store provided, use the default ssl socket factory
				factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static SocketFactory getDefault()
	{
		return new VcgrSslSocketFactory();
	}

	public Socket createSocket(Socket socket, String s, int i, boolean flag)
		throws IOException 
	{
		return factory.createSocket(socket, s, i, flag);
	}

	public Socket createSocket(InetAddress inaddr, int i, InetAddress inaddr1,
		int j) throws IOException
	{
		return factory.createSocket(inaddr, i, inaddr1, j);
	}

	public Socket createSocket(InetAddress inaddr, int i) throws IOException 
	{
		return factory.createSocket(inaddr, i);
	}

	public Socket createSocket(String s, int i, InetAddress inaddr, int j)
		throws IOException 
	{
		return factory.createSocket(s, i, inaddr, j);
	}

	public Socket createSocket(String s, int i) throws IOException 
	{
		return factory.createSocket(s, i);
	}

	public String[] getDefaultCipherSuites() 
	{
		return factory.getSupportedCipherSuites();
	}

	public String[] getSupportedCipherSuites() 
	{
		return factory.getSupportedCipherSuites();
	}
	
	static private Properties getSSLProperties()
		throws ConfigurationException
	{
		XMLConfiguration conf = 
			ConfigurationManager.getCurrentConfiguration().getClientConfiguration();
		return (Properties)conf.retrieveSection(
			GenesisIIConstants.SSL_PROPERTIES_SECTION_NAME);
	}
}
