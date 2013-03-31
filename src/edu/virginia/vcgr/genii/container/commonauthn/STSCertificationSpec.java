package edu.virginia.vcgr.genii.container.commonauthn;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import edu.virginia.vcgr.genii.security.x509.CertCreationSpec;

public class STSCertificationSpec extends CertCreationSpec
{

	private PrivateKey subjectPrivateKey;

	public STSCertificationSpec(PublicKey subjectPublicKey, PrivateKey subjectPrivateKey, X509Certificate[] issuerChain,
		PrivateKey issuerPrivateKey, long validityMillis)
	{
		super(subjectPublicKey, issuerChain, issuerPrivateKey, validityMillis);
		this.subjectPrivateKey = subjectPrivateKey;
	}

	public STSCertificationSpec(KeyPair keyPair, X509Certificate[] issuerChain, PrivateKey issuerPrivateKey, long validityMillis)
	{
		super(keyPair.getPublic(), issuerChain, issuerPrivateKey, validityMillis);
		this.subjectPrivateKey = keyPair.getPrivate();
	}

	public PrivateKey getSubjectPrivateKey()
	{
		return subjectPrivateKey;
	}
}
