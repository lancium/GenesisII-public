package edu.virginia.vcgr.genii.client.security.gamlauthz.identity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.GeneralSecurityException;
import java.security.cert.*;

import java.util.*;

import org.apache.axis.message.MessageElement;


import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.*;
import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.client.security.WSSecurityUtils;

public class X509Identity implements AssertableIdentity, SignedAssertion {

	static public final long serialVersionUID = 0L;
	
	protected X509Certificate[] _identity;
	                
	// zero-arg contstructor for externalizable use only!
	public X509Identity() {}
	
	public X509Identity(X509Certificate[] identity) {
		_identity = identity;
	}
	
	public X509Identity(MessageElement secRef) throws GeneralSecurityException {
		_identity = WSSecurityUtils.getChainFromPkiPathSecTokenRef(secRef);
	}

	public X509Certificate[] getAssertingIdentityCertChain() {
		// X509 certificates assert themselves via their own 
		// corresponding private key
		return _identity;
	}

	/**
	 * Returns a URI (e.g., a WS-Security Token Profile URI) indicating the token type
	 */
	public String getTokenType() {
		return WSSecurityUtils.X509PKIPathv1_URI;
	}	
	
	public MessageElement toMessageElement() throws GeneralSecurityException {
		return WSSecurityUtils.makePkiPathSecTokenRef(_identity);
	}
	
	/**
	 * Returns the primary attribute that is being asserted
	 */
	public Attribute getAttribute() {
		return new IdentityAttribute(this);
	}

	/**
	 * Returns the certchain of the identity authorized to use this 
	 * assertion (same as the asserter)
	 */
	public X509Certificate[] getAuthorizedIdentity() {
		return _identity;
	}

	/**
	 * Verify the assertion.  It is verified if all signatures successfully
	 * authenticate the signed-in authorizing identities
	 */	
	public void validateAssertion() throws GeneralSecurityException {
		edu.virginia.vcgr.genii.client.security.x509.CertTool.loadBCProvider();

		SecurityUtils.validateCertPath(_identity);
	}

	
	/**
	 * Checks that the attribute is time-valid with respect to the supplied 
	 * date and any delegation depth requirements are met by the supplied
	 * delegationDepth.
	 */
	public void checkValidity(Date date) throws AttributeInvalidException {
		
		try {
			for (X509Certificate cert : getAssertingIdentityCertChain()) {
				cert.checkValidity(date);
			}
		} catch (CertificateException e) {
			throw new AttributeInvalidException("Security attribute asserting identity contains an invalid certificate: " + e.getMessage(), e);
		}
	}	
	
	
	public String toString() {
		return "[X509Identity] \"" + _identity[0].getSubjectX500Principal() + "\"";
	}
	
	
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}

		if (!(other instanceof X509Identity)) {
			return false;
		}
		
		if ((_identity == null) || (((X509Identity) other)._identity == null)) {
			// one or the other is null
			if (_identity == ((X509Identity) other)._identity) {
				// they're not both null
				return false;
			}
		} else if (!_identity[0].equals(((X509Identity) other)._identity[0])) {
			// only check the first cert in the chain
			return false;
		}
		
		return true;
	}	
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(_identity.length);
		try {
			for (int i = 0; i < _identity.length; i++) {
				byte[] encoded = _identity[i].getEncoded();
				out.writeInt(encoded.length);
				out.write(encoded);
			}
		} catch (GeneralSecurityException e) { 
			throw new IOException(e.getMessage());
		}
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int numCerts = in.readInt();
		_identity = new X509Certificate[numCerts];
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			for (int i = 0; i < numCerts; i++) {
				byte[] encoded = new byte[in.readInt()];
				in.readFully(encoded);
				_identity[i] = (X509Certificate) cf.generateCertificate(
						new ByteArrayInputStream(encoded));
			}
		} catch (GeneralSecurityException e) { 
			throw new IOException(e.getMessage());
		}
	}			
	
}
