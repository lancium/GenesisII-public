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
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.BouncyCastle;
import org.apache.ws.security.components.crypto.CredentialException;

import edu.virginia.vcgr.genii.security.x509.CertTool;

/**
 * A wrapper class for X.509 WSS4J crypto.
 * 
 * Allows us to stash away the certs seen during sig/enc processsing.
 * 
 * @author dgm4d
 */
public class GIIBouncyCrypto extends BouncyCastle
{
	// ArrayList containing all of the message-level cert-chains that were
	// extracted using the WSS4J signature processor
	protected ArrayList<X509Certificate[]> loadedCerts = new ArrayList<X509Certificate[]>();

	static {
		CertTool.loadBCProvider();
	}

	public GIIBouncyCrypto() throws CredentialException, IOException
	{
		super(null);

		// Set the JCE provider (i.e., cert-factory) to BC. (For some reason
		// we get bizarre exceptions when using SUN provider, possibly because
		// of our use of client-side self-signed creds... idk.)

		properties = new Properties();
		properties.setProperty("org.apache.ws.security.crypto.merlin.cert.provider", "BC");
	}

	/**
	 * load a X509Certificate from the input stream.
	 * <p/>
	 * 
	 * @param in
	 *            The <code>InputStream</code> array containg the X509 data
	 * @return Returns a X509 certificate
	 * @throws org.apache.ws.security.WSSecurityException
	 * 
	 */
	public X509Certificate loadCertificate(InputStream in) throws WSSecurityException
	{
		X509Certificate cert = super.loadCertificate(in);

		if (cert != null) {
			X509Certificate[] certs = new X509Certificate[1];
			certs[0] = cert;
			loadedCerts.add(certs);
		}

		return cert;
	}

	/**
	 * Construct an array of X509Certificate's from the byte array.
	 * <p/>
	 * 
	 * @param data
	 *            The <code>byte</code> array containg the X509 data
	 * @param reverse
	 *            If set the first certificate in input data will the last in the array
	 * @return An array of X509 certificates, ordered according to the reverse flag
	 * @throws WSSecurityException
	 */
	public X509Certificate[] getX509Certificates(byte[] data, boolean reverse) throws WSSecurityException
	{

		X509Certificate[] certs = super.getX509Certificates(data, reverse);

		if (certs != null) {
			loadedCerts.add(certs);
		}

		return certs;
	}

	public ArrayList<X509Certificate[]> getLoadedCerts()
	{
		return loadedCerts;
	}

}