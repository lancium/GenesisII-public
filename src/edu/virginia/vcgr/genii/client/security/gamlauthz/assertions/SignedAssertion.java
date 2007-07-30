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
import java.util.*;

import edu.virginia.vcgr.genii.client.cache.LRUCache;
import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlCredential;

import edu.virginia.vcgr.genii.client.ser.Base64;

public abstract class SignedAssertion implements Externalizable, GamlCredential {
	
	static public final long serialVersionUID = 0L;
	
	public static final String ENCODED_GAML_ASSERTIONS_PROPERTY = 
		"genii.client.security.authz.encoded-gaml-assertions";
	
	/** Cache of verified assertions, keyed by _encodedValues */
	static protected int VERIFIED_CACHE_SIZE = 16;
	static protected LRUCache<String, SignedAssertion> verifiedAssertionsCache = 
		new LRUCache<String, SignedAssertion>(VERIFIED_CACHE_SIZE);

	/** Cached serialized value for comparison checking **/
	public transient String _encodedValue = null;
	
	
	/**
	 * Returns the primary attribute that is being asserted
	 */
	abstract public Attribute getAttribute();

	/**
	 * Returns the certchain of the identity authorized to use this 
	 * assertion (same as the asserter)
	 */
	public abstract X509Certificate[] getAuthorizedIdentity();

	/**
	 * Checks that the attribute time-valid with respect to the supplied 
	 * date
	 */
	public void checkValidity(Date date) throws AttributeInvalidException {
		checkValidity(0, date);
	}
	
	/**
	 * Checks that the assertion is time-valid with respect to the supplied 
	 * date and any delegation depth requirements are met by the supplied
	 * delegationDepth.
	 */
	abstract public void checkValidity(int delegationDepth, Date date) throws AttributeInvalidException;
	
	/**
	 * Verify the assertion.  It is verified if all signatures successfully
	 * authenticate the signed-in authorizing identities
	 */	
	static public void verifyAssertion(SignedAssertion assertion) throws GeneralSecurityException {
		// check cache first
		if ((assertion._encodedValue != null) && 
				(verifiedAssertionsCache.get(assertion._encodedValue) != null)) {
			return;
		}
		
		// verify assertion
		assertion.verifyAssertion();
		
		// insert into verified cache since no exception was thrown
		if (assertion._encodedValue != null) {
			verifiedAssertionsCache.put(assertion._encodedValue, assertion);
		}
	}
	
	/**
	 * Verify the assertion.  It is verified if all signatures successfully
	 * authenticate the signed-in authorizing identities
	 */	
	abstract protected void verifyAssertion() throws GeneralSecurityException;
		
	/**
	 * Displays the credential to the specified output streams
	 */
	public String toString() {
		return "GAML: " + getAttribute().getAssertingIdentityCertChain()[0].getSubjectDN().getName();
	}	
	
	
	
	
	public static String base64encodeAssertion(SignedAssertion signedAssertion) throws IOException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		ObjectOutputStream oos = new ObjectOutputStream(baos);
 		oos.writeObject(signedAssertion);
 		oos.close();

		return Base64.byteArrayToBase64(baos.toByteArray());
	}
	
	public static String base64encodeAssertions(ArrayList<SignedAssertion> signedAssertions) throws IOException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		ObjectOutputStream oos = new ObjectOutputStream(baos);
 		oos.writeObject(signedAssertions);
 		oos.close();

		return Base64.byteArrayToBase64(baos.toByteArray());
	}

	@SuppressWarnings("unchecked")
	public static SignedAssertion base64decodeAssertion(String encoded) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
			Base64.base64ToByteArray(encoded)));
		
		return (SignedAssertion) ois.readObject();
	}	

	@SuppressWarnings("unchecked")
	public static ArrayList<SignedAssertion> base64decodeAssertions(String encoded) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
			Base64.base64ToByteArray(encoded)));
		
		return (ArrayList<SignedAssertion>) ois.readObject();
	}	
}
