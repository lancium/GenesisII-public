package edu.virginia.vcgr.genii.client.security.gamlauthz.identity;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.cert.X509Certificate;
import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlCredential;

public class UsernameTokenIdentity implements Identity, GamlCredential {

	static public final long serialVersionUID = 0L;
	
	protected String _userName;
	protected String _token;
	
	// zero-arg contstructor for externalizable use only!
	public UsernameTokenIdentity() {}
	
	public UsernameTokenIdentity(String userName, String token) {
		_userName = userName;
		_token = token;
	}

	public String toString() {
		return "[Username-Token] Username: " + _userName + ", Token: " + _token;
	}
	
	public X509Certificate[] getAssertingIdentityCertChain() {
		// Username tokens are not asserted by anything
		return null;
	}
	
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}

		if (!_userName.equals(((UsernameTokenIdentity) other)._userName)) {
			return false;
		}
		if (!_token.equals(((UsernameTokenIdentity) other)._token)) {
			return false;
		}
		
		return true;
	}	
	
	public String getUserName() {
		return _userName;
	}

	public String getToken() {
		return _token;
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(_userName);
		out.writeUTF(_token);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		_userName = in.readUTF();
		_token = in.readUTF();
	}			
	
}
