package edu.virginia.vcgr.genii.client.security.x509;

import java.io.ObjectStreamException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * compatibility class that can read from old db blobs.
 */
class KeyAndCertMaterial extends edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial
{
	static final long serialVersionUID = 0L;

	public X509Certificate[] _clientCertChain = null;
	public PrivateKey _clientPrivateKey = null;

	KeyAndCertMaterial()
	{
	}

	private Object readResolve() throws ObjectStreamException
	{
		return new edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial(_clientCertChain, _clientPrivateKey);
	}

	private Object writeReplace() throws ObjectStreamException
	{
		return new edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial(_clientCertChain, _clientPrivateKey);
	}
}
