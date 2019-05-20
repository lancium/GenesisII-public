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
package edu.virginia.vcgr.genii.security.x509;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.identity.IdentityType;

import java.io.*;

public class KeyAndCertMaterial implements Serializable
{
	static final long serialVersionUID = 0L;

	public X509Certificate[] _clientCertChain = null;
	public PrivateKey _clientPrivateKey = null;

	public KeyAndCertMaterial()
	{
	}

	public KeyAndCertMaterial(X509Certificate[] clientCertChain, PrivateKey clientPrivateKey)
	{
		_clientCertChain = clientCertChain;
		_clientPrivateKey = clientPrivateKey;
	}
	
	public String toString()
	{
		X509Identity temp = new X509Identity(_clientCertChain, IdentityType.CONNECTION);
		return temp.toString();
	}
}