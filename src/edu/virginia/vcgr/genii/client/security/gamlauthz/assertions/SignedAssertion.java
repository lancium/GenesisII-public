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

package edu.virginia.vcgr.genii.client.security.gamlauthz.assertions;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.io.*;

import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlCredential;

public interface SignedAssertion extends Externalizable, GamlCredential {
	
	/**
	 * Returns the primary attribute that is being asserted
	 */
	public Attribute getAttribute();

	/**
	 * Returns the certchain of the identity authorized to use this 
	 * assertion (same as the asserter)
	 */
	public X509Certificate[] getAuthorizedIdentity();

	/**
	 * Validate the assertion.  It is validated if all signatures successfully
	 * authenticate the signed-in authorizing identities, it maps to a 
	 * trusted root, if applicable, and any other time/etc. contstraints are met
	 */	
	public void validateAssertion() throws GeneralSecurityException;
		
}
