package edu.virginia.vcgr.genii.client.security.gamlauthz.identity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class IDPIdentity implements AssertableIdentity {

	static public final long serialVersionUID = 0L;
	
	protected X509Certificate[] _idProviderCertChain;
	protected String _name;
	                
	// zero-arg contstructor for externalizable use only!
	public IDPIdentity() {}
	
	public IDPIdentity(String name, X509Certificate[] idProviderCertChain) {
		_name = name;
		_idProviderCertChain = idProviderCertChain;
	}
	
	public X509Certificate[] getAssertingIdentityCertChain() {
		return _idProviderCertChain;
	}

	public String toString() {
		return "[IDPIdentity] Name: " + _name + ", IDP:" + _idProviderCertChain[0].getSubjectX500Principal();
	}
	
	public String getName() {
		return _name;
	}
	
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}

		if (!_name.equals(((IDPIdentity) other)._name)) {
			return false;
		}
		
		if ((_idProviderCertChain == null) || (((IDPIdentity) other)._idProviderCertChain == null)) {
			// one or the other is null
			if (_idProviderCertChain == ((IDPIdentity) other)._idProviderCertChain) {
				// they're not both null
				return false;
			}
		} else if (!_idProviderCertChain[0].equals(((IDPIdentity) other)._idProviderCertChain[0])) {
			// only check the first cert in the chain
			return false;
		}
		
		return true;
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(_name);
		out.writeInt(_idProviderCertChain.length);
		try {
			for (int i = 0; i < _idProviderCertChain.length; i++) {
				byte[] encoded = _idProviderCertChain[i].getEncoded();
				out.writeInt(encoded.length);
				out.write(encoded);
			}
		} catch (GeneralSecurityException e) { 
			throw new IOException(e.getMessage());
		}
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		_name = in.readUTF();
		int numCerts = in.readInt();
		_idProviderCertChain = new X509Certificate[numCerts];
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			for (int i = 0; i < numCerts; i++) {
				byte[] encoded = new byte[in.readInt()];
				in.readFully(encoded);
				_idProviderCertChain[i] = (X509Certificate) cf.generateCertificate(
						new ByteArrayInputStream(encoded));
			}
		} catch (GeneralSecurityException e) { 
			throw new IOException(e.getMessage());
		}
	}		
	
}
