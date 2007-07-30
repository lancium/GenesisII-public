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

import java.security.*;
import java.security.cert.X509Certificate;

public class CertCreationSpec {

	public PublicKey newPublicKey;
	public X509Certificate[] issuerChain;
	public PrivateKey issuerPrivateKey;
	public long validityMillis = 0;
	
	public CertCreationSpec(
			PublicKey newPublicKey,
			X509Certificate[] issuerChain,
			PrivateKey issuerPrivateKey,
			long validityMillis) {
		
		this.newPublicKey = newPublicKey;
		this.issuerChain = issuerChain;
		this.issuerPrivateKey = issuerPrivateKey;
		this.validityMillis = validityMillis;
	}
	
	
}
