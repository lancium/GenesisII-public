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

package edu.virginia.vcgr.genii.client.security.x509;

import java.net.Socket;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.X509KeyManager;

/**
 * A simple in-memory key manager for a single X.509 key/cert
 * 
 * @author dmerrill
 */
public class SingleSSLX509KeyManager implements X509KeyManager
{
	
	private static final String CLIENT_ALIAS = "DefaultClientAlias"; 
	private static final String SERVER_ALIAS = "DefaultServerAlias"; 
	
	private KeyAndCertMaterial _keyAndCertMaterial = null; 

	/**
	 * Constructor
	 */
	public SingleSSLX509KeyManager(KeyAndCertMaterial keyAndCertMaterial)
	{
		_keyAndCertMaterial = keyAndCertMaterial;
	}

	/**
	 * Choose an alias to authenticate the client side of a secure socket given
	 * the public key type and the list of certificate issuer authorities
	 * recognized by the peer (if any).
	 */
	public String chooseClientAlias(String[] keyType, Principal[] issuers,
			Socket socket)
	{
		return CLIENT_ALIAS;
	}

	/**
	 * Choose an alias to authenticate the server side of a secure socket given
	 * the public key type and the list of certificate issuer authorities
	 * recognized by the peer (if any).
	 */
	public String chooseServerAlias(String keyType, Principal[] issuers,
			Socket socket)
	{
		return SERVER_ALIAS;
	}

	/**
	 * Returns the certificate chain associated with the given alias. Returns
	 * null if the certificate chain is invalid for any reason
	 * 
	 */
	public X509Certificate[] getCertificateChain(String alias)
	{
		return _keyAndCertMaterial._clientCertChain;
	}

	/**
	 * Get the matching aliases for authenticating the client side of a secure
	 * socket given the public key type and the list of certificate issuer
	 * authorities recognized by the peer (if any).
	 * 
	 */
	public String[] getClientAliases(String keyType, Principal[] issuers)
	{
		String[] retval = {CLIENT_ALIAS};
		return retval;
	}

	/**
	 * Get the matching aliases for authenticating the server side of a secure
	 * socket given the public key type and the list of certificate issuer
	 * authorities recognized by the peer (if any).
	 */
	public String[] getServerAliases(String keyType, Principal[] issuers)
	{
		String[] retval = {SERVER_ALIAS};
		return retval;
	}

	/**
	 * Returns the key associated with the given alias.
	 */
	public PrivateKey getPrivateKey(String alias)
	{
		return _keyAndCertMaterial._clientPrivateKey;
	}

}
