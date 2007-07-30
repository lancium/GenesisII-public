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
package edu.virginia.vcgr.genii.client.security.wincrypto;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import java.security.cert.*;

import java.util.*;

/**
 * Responsible for managing the trust material that is used when 
 * making trust decisions, and for deciding whether credentials 
 * presented by a peer should be accepted. 
 *
 * @author dmerrill
 */
public class WinX509TrustManager implements X509TrustManager {

	static WinCryptoLib cryptoLib = new WinCryptoLib();

	/** 
	 * Constructor 
	 */
	WinX509TrustManager() {
	}

	/**
	 * Return an array of certificate authority certificates which 
	 * are trusted for authenticating peers.
	 */
	public X509Certificate[] getAcceptedIssuers() {

		ArrayList<X509Certificate> validIssuers = new ArrayList<X509Certificate>();
		String[] issuerStores = {"CA", "Root"};

		// iterate though the stores of issuer certificates
		for (int i = 0; i < issuerStores.length; i++) {
			
			// get the aliases in the store
			ArrayList<String> issuerAliases;
			try {
				issuerAliases = cryptoLib.getAliases(issuerStores[i]);
			} catch (WinCryptoException e) {
				continue;
			}

			// iterate though them, adding valid ones
			Iterator<String> itr = issuerAliases.iterator();
			while (itr.hasNext()) {
				try {
					// get the zero'th element of the alias's certchain: getting
					// the certchain validates the cert
					String alias = itr.next();
					X509Certificate[] chain = cryptoLib.getCertificateChain(issuerStores[i], alias);
					validIssuers.add(chain[0]);
				} catch (WinCryptoException e) {
					continue;
				} catch (CertificateException e) {
					continue;
				}
			}
			
		}
		
		return validIssuers.toArray(new X509Certificate[0]);
	}


	/**
	 * Given the partial or complete certificate chain provided by the peer, 
	 * build a certificate path to a trusted root and return if it can be 
	 * validated and is trusted for client SSL authentication based on the 
	 * authentication type. 
	 */
	public void checkClientTrusted(X509Certificate chain[], String authType) 
			throws CertificateException {

		// iterate through the chain, making sure that each certificate is trusted
		try {
			for (int i = 0; i < chain.length; i++) {
				cryptoLib.isCertTrusted(chain[i]);
			}
		} catch (Exception e) {
			throw new CertificateException(e.getMessage(), e); 
		}
	}


	/**
	 * Given the partial or complete certificate chain provided by the peer, 
	 * build a certificate path to a trusted root and return if it can be 
	 * validated and is trusted for server SSL authentication based on the 
	 * authentication type
	 */
	public void checkServerTrusted(X509Certificate chain[], String authType) 
			throws CertificateException {
		
		// iterate through the chain, making sure that each certificate is trusted
		try {
			for (int i = 0; i < chain.length; i++) {
				cryptoLib.isCertTrusted(chain[i]);
			}
		} catch (Exception e) {
			throw new CertificateException(e.getMessage(), e); 
		}
	}
}
