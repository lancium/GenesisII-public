package edu.virginia.vcgr.genii.security.credentials;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import edu.virginia.vcgr.genii.security.identity.IdentityType;

/**
 * this class not only tracks a user identity, but also the private key of the
 * identity, where it is possible to know that.
 * 
 * @author fred
 * 
 */
public class FullX509Identity extends X509Identity {
	static public final long serialVersionUID = 0L;

	private PrivateKey _key;

	public FullX509Identity() {
		super();
		_key = null;
	}

	public FullX509Identity(X509Certificate[] identity, IdentityType type,
			PrivateKey key) {
		super(identity, type);
		_key = key;
	}

	public PrivateKey getKey() {
		return _key;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(_key);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		_key = (PrivateKey) in.readObject();
	}
}
