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

import java.net.Socket;
import java.util.*;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.X509KeyManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Responsible for managing the key material which is used to authenticate the local SSLSocket to
 * its peer. If no key material is available, the socket will be unable to present authentication
 * credentials.
 * 
 * This particular key manager uses key material from the "My" Windows cryptography key provider
 * 
 * @author dmerrill
 */
public class WinX509KeyManager implements X509KeyManager
{
	static private Log _logger = LogFactory.getLog(WinX509KeyManager.class);

	static WinCryptoLib cryptoLib = new WinCryptoLib();

	/**
	 * Constructor
	 */
	public WinX509KeyManager()
	{
	}

	/**
	 * Choose an alias to authenticate the client side of a secure socket given the public key type
	 * and the list of certificate issuer authorities recognized by the peer (if any).
	 */
	public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket)
	{

		String alias = null;

		try {
			String[] aliases = getClientAliases(keyType[0], issuers);
			if (aliases == null) {
				return null;
			}

			// pick one that has a public key
			for (int i = 0; i < aliases.length; i++) {
				try {
					if (cryptoLib.getPrivateKey("My", aliases[i]) != null) {
						alias = aliases[i];
					}
				} catch (Exception e) {
				}
			}

		} catch (Exception e) {
			_logger.info("exception occurred in chooseClientAlias", e);
		}

		return alias;
	}

	/**
	 * Choose an alias to authenticate the server side of a secure socket given the public key type
	 * and the list of certificate issuer authorities recognized by the peer (if any).
	 */
	public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket)
	{

		String alias = null;

		try {
			String[] aliases = getServerAliases(keyType, issuers);
			if (aliases == null) {
				return null;
			}

			// pick one that has a public key
			for (int i = 0; i < aliases.length; i++) {
				try {
					if (cryptoLib.getPrivateKey("My", aliases[i]) != null) {
						alias = aliases[i];
					}
				} catch (Exception e) {
				}
			}

		} catch (Exception e) {
			_logger.info("exception occurred in chooseServerAlias", e);
		}

		return alias;
	}

	/**
	 * Returns the certificate chain associated with the given alias. Returns null if the
	 * certificate chain is invalid for any reason
	 * 
	 */
	public X509Certificate[] getCertificateChain(String alias)
	{
		try {
			return cryptoLib.getCertificateChain("My", alias);

		} catch (WinCryptoException e) {
		} catch (CertificateException e) {
		}

		return null;
	}

	/**
	 * Get the matching aliases for authenticating the client side of a secure socket given the
	 * public key type and the list of certificate issuer authorities recognized by the peer (if
	 * any).
	 * 
	 */
	public String[] getClientAliases(String keyType, Principal[] issuers)
	{
		// get the aliases within the local user keystore
		ArrayList<String> aliases;
		try {
			aliases = cryptoLib.getAliases("My");
		} catch (WinCryptoException e) {
			return null;
		}

		// put the issuers into a set for easy matching
		HashSet<Principal> issuerSet = null;
		if (issuers != null) {
			issuerSet = new HashSet<Principal>(Arrays.asList(issuers));
		}

		// iterate through the aliases...
		Iterator<String> itr = aliases.iterator();
		while (itr.hasNext()) {
			String alias = itr.next();
			try {
				// iterate though the cert chain, checking issuers. Invalid
				// alias certs
				// will throw exceptions during cert chain lookup.
				boolean matchingIssuer = false;
				X509Certificate[] aliasCertChain = cryptoLib.getCertificateChain("My", alias);
				if (issuerSet == null) {
					// no issuers specified and no exceptions during chain
					// lookup
					matchingIssuer = true;
				} else {
					for (int i = 0; i < aliasCertChain.length; i++) {
						if (issuerSet.contains(aliasCertChain[i].getIssuerX500Principal())) {
							matchingIssuer = true;
							break;
						}
					}
				}

				if (!matchingIssuer) {
					// no matching issuer found
					itr.remove();
				}
			} catch (WinCryptoException e) {
			} catch (CertificateException e) {
			}
		}

		// return the resulting set
		return aliases.toArray(new String[0]);
	}

	/**
	 * Get the matching aliases for authenticating the server side of a secure socket given the
	 * public key type and the list of certificate issuer authorities recognized by the peer (if
	 * any).
	 */
	public String[] getServerAliases(String keyType, Principal[] issuers)
	{
		// same implementation currently as getClientAliases()
		return getClientAliases(keyType, issuers);
	}

	/**
	 * Returns the key associated with the given alias.
	 */
	public PrivateKey getPrivateKey(String alias)
	{

		try {
			return cryptoLib.getPrivateKey("My", alias);
		} catch (WinCryptoException e) {
		}

		return null;

	}

	public String getFriendlyName(String alias)
	{
		try {
			return cryptoLib.getFriendlyName("My", alias);
		} catch (WinCryptoException e) {
		}

		return null;
	}
}
