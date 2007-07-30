package edu.virginia.vcgr.genii.client.security.gamlauthz.identity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class X509Identity implements AssertableIdentity {

	static public final long serialVersionUID = 0L;
	
	private X509Certificate[] _identity;
	                
	// zero-arg contstructor for externalizable use only!
	public X509Identity() {}
	
	public X509Identity(X509Certificate[] identity) {
		_identity = identity;
	}
	
	public X509Certificate[] getAssertingIdentityCertChain() {
		// X509 certificates assert themselves via their own 
		// corresponding private key
		return _identity;
	}
	
	public String toString() {
		return "[X509Identity] " + _identity[0].getSubjectX500Principal();
	}
	
	
	public boolean equals(Object other) {
		if (other == null) {
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
