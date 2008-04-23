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

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Date;


/**
 * Signed Attribute Assertion.  If you trust the asserting identity, 
 * then you establish the attribute as fact.
 * 
 * @author dmerrill
 */
public class SignedAttributeAssertion extends SignedAssertionBaseImpl {

	static public final long serialVersionUID = 0L;

	// A serializable statement
	protected Attribute _attribute;

	// The signature of the above attribute by the above
	// asserting identity
	protected byte[] _signature = null;
	
	// zero-arg contstructor for externalizable use only!
	public SignedAttributeAssertion() {}
	
	public SignedAttributeAssertion(
			Attribute attribute, 
			PrivateKey privateKey) throws GeneralSecurityException {
		
		_attribute = attribute;
		_signature = sign(attribute, privateKey);
	}
	
	/**
	 * Returns the certchain of the identity authorized to use this 
	 * assertion (same as the asserter)
	 */
	public X509Certificate[] getAuthorizedIdentity() {
		return _attribute.getAssertingIdentityCertChain();
	}
	
	/**
	 * Returns the attribute that is being asserted
	 */
	public Attribute getAttribute() {
		return _attribute;
	}
	
	/**
	 * Checks that the assertion is time-valid with respect to the supplied 
	 * date
	 */
	public void checkValidity(Date date) throws AttributeInvalidException {
 		// check the validity of the attribute
		_attribute.checkValidity(0, date);
	}
		
	/**
	 * Verify the assertion.  It is verified if all signatures successfully
	 * authenticate the signed-in authorizing identities
	 */	
	public void validateAssertion() throws GeneralSecurityException {
		
		if ((_signature == null) || (_attribute == null)) {
 			throw new GeneralSecurityException("No signature or data to verify");
 		}
		
 		try { 
 			// verify that the signature is from the authorizing identity 
	 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	 		ObjectOutputStream oos = new ObjectOutputStream(baos);
	 		oos.writeObject(_attribute);
	 		
			Signature rsa = Signature.getInstance("SHA1withRSA");
	 		rsa.initVerify(getAuthorizedIdentity()[0]);
	 		rsa.update(baos.toByteArray());
	 		if (!rsa.verify(_signature)) {
	 			throw new AssertionInvalidException("Delegation signature does not authenticate authorizing identity");
	 		}
	 		
 		} catch (IOException e) {
 			throw new GeneralSecurityException(e.getMessage(), e);
 		}
	}	
	
	public String toString() {
		return "(SignedAttributeAssertion)\n attribute : [" + _attribute + "]";
	}	
	
	
	/**
	 * Signs the assertion with the specified private key
	 */
	static byte[] sign(Attribute attribute, PrivateKey privateKey) throws GeneralSecurityException {
 		if (attribute == null) {
 			return null;
 		}
		
 		try { 
	 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	 		ObjectOutputStream oos = new ObjectOutputStream(baos);
	 		oos.writeObject(attribute);
	 		oos.close();

			Signature rsa = Signature.getInstance("SHA1with" + privateKey.getAlgorithm());
	 		rsa.initSign(privateKey);
	 		rsa.update(baos.toByteArray());
	 		return rsa.sign();
 		} catch (IOException e) {
 			throw new GeneralSecurityException(e.getMessage(), e);
 		}
	}		
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(_attribute);
		out.writeInt(_signature.length);
		out.write(_signature);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		_attribute = (Attribute) in.readObject();
		int sigLen = in.readInt();
		_signature = new byte[sigLen];
		in.readFully(_signature);
	}	
	
	
}
