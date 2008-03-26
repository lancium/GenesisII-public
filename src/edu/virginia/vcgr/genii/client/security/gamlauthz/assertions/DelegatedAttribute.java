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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * A delegated attribute.  The pairing of an existing signed
 * assertion with the identity of a delegatee
 *  
 * @author dmerrill
 */
public class DelegatedAttribute implements Attribute {

	static public final long serialVersionUID = 0L;
	
	// An existing signed assertion
	protected SignedAssertion _assertion;
	
	// The certchain of the identity to be authorized for the above assertion
	protected X509Certificate[] _delegateeIdentity = null;
	
	// The constraints placed upon this attribute
	protected AttributeConstraints _constraints = null;

	// Serialization of this attribute for hashcode and comparison purposes
	protected transient String _encodedValue = null;

	// zero-arg contstructor for externalizable use only!
	public DelegatedAttribute() {}
	
	public DelegatedAttribute(
			AttributeConstraints constraints, 
			SignedAssertion assertion, 
			X509Certificate[] delegateeIdentity) {
		
		if ((assertion == null) || (delegateeIdentity == null)) {
			throw new java.lang.IllegalArgumentException(
					"DelegatedAttribute constructor cannot accept null parameters");
		}
		
		_assertion = assertion;
		_delegateeIdentity = delegateeIdentity;
		_constraints = constraints;
	}

	/**
	 * Returns the identity needed to authorize the delegatee
	 */
	public X509Certificate[] getAuthorizedIdentity() {
		return _assertion.getAuthorizedIdentity();
	}
	
	/**
	 * Returns the identity of the original attribute asserter
	 */
	public X509Certificate[] getAssertingIdentityCertChain() {
		return _assertion.getAttribute().getAssertingIdentityCertChain();
	}
	
	/**
	 * Returns the delegatee identity
	 */
	public X509Certificate[] getDelegateeIdentity() {
		return _delegateeIdentity;
	}
	
	/**
	 * Checks that the attribute is time-valid with respect to the supplied 
	 * date and any delegation depth requirements are met by the supplied
	 * delegationDepth.
	 */
	public void checkValidity(int delegationDepth, Date date) throws AttributeInvalidException {

		// check constraints if they exist
		if (_constraints != null) { 
			_constraints.checkValidity(delegationDepth, date);
		}
		
		// check the encapsulated assertion
		if (_assertion instanceof DelegatedAssertion) {
			((DelegatedAssertion) _assertion).checkValidity(delegationDepth + 1, date);
		} else {
			_assertion.checkValidity(date);
		}

		// make sure the delegatee's identity is still valid
		try {
			for (X509Certificate cert : _delegateeIdentity) {
				cert.checkValidity();
			}
		} catch (CertificateException e) {
			throw new AttributeInvalidException("Delegatee identity contains an invalid certificate: " + e.getMessage(), e);
		}	
	}
	
	/**
	 * Returns the signed assertion component
	 */
	public SignedAssertion getSignedAssertion() {
		return _assertion;
	}
	
	public String toString() {
		return "(DelegatedAttribute) delegateeIdentity(" + _delegateeIdentity.length + "): \"" + 
			_delegateeIdentity[0].getSubjectX500Principal().getName() + "\" " + 
			((_constraints == null) ? "" : _constraints.toString() + " ") +  
			" subAssertion: [" + _assertion + "]";
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(_assertion);
		out.writeObject(_constraints);
		out.writeInt(_delegateeIdentity.length);
		try {
			for (int i = 0; i < _delegateeIdentity.length; i++) {
				byte[] encoded = _delegateeIdentity[i].getEncoded();
				out.writeInt(encoded.length);
				out.write(encoded);
			}
		} catch (GeneralSecurityException e) { 
			throw new IOException(e.getMessage());
		}
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		_assertion = (SignedAssertion) in.readObject();
		_constraints = (AttributeConstraints) in.readObject();
		int numCerts = in.readInt();
		_delegateeIdentity = new X509Certificate[numCerts];
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			for (int i = 0; i < numCerts; i++) {
				byte[] encoded = new byte[in.readInt()];
				in.readFully(encoded);
				_delegateeIdentity[i] = (X509Certificate) cf.generateCertificate(
						new ByteArrayInputStream(encoded));
			}
		} catch (GeneralSecurityException e) { 
			throw new IOException(e.getMessage());
		}
	}	
	
	public int hashCode() {
		return _assertion.hashCode();
	}
	
	public boolean equals(Object o) {
		DelegatedAttribute other = (DelegatedAttribute) o;


		// force encoded values to represent signed assertion
		if (!_assertion.equals(other._assertion)) {
			return false;
		}
		
		// check for delegatee equiv
		if (!java.util.Arrays.equals(_delegateeIdentity, other._delegateeIdentity)) {
			return false;
		}
		
		return true;
	}
	
}
