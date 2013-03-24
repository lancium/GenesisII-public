package edu.virginia.vcgr.genii.client.cmd.tools.login;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class CertEntry
{
	public X509Certificate[] _certChain;
	public String _alias;
	public PrivateKey _privateKey;
	public KeyStore _keyStore;
	public String _friendlyName;

	public CertEntry(X509Certificate[] certChain, String alias, PrivateKey privateKey, String friendlyName)
	{
		_certChain = certChain;
		_alias = alias;
		_privateKey = privateKey;
		_friendlyName = friendlyName;

		if (_friendlyName == null)
			_friendlyName = _certChain[0].getSubjectDN().getName();
	}

	public CertEntry(Certificate[] certChain, String alias, KeyStore keyStore)
	{
		if (certChain != null) {
			_certChain = new X509Certificate[certChain.length];
			for (int i = 0; i < certChain.length; i++) {
				_certChain[i] = (X509Certificate) certChain[i];
			}
		}

		_alias = alias;
		_keyStore = keyStore;

		_friendlyName = alias;
	}

	public String toString()
	{
		return _certChain[0].getSubjectDN().getName();
	}
}
