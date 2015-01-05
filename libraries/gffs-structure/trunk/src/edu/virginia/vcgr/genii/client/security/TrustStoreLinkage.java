package edu.virginia.vcgr.genii.client.security;

import java.security.KeyStore;
import java.security.cert.CertStore;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationUnloadedListener;
import edu.virginia.vcgr.genii.security.TrustStoreProvider;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

public class TrustStoreLinkage implements TrustStoreProvider
{
	KeyAndCertMaterial _containerKey = null;
	
	@Override
	public KeyStore getResourceTrustStore() throws Exception
	{
		return KeystoreManager.getResourceTrustStore();
	}

	@Override
	public KeyStore getTLSTrustStore() throws Exception
	{
		return KeystoreManager.getTlsTrustStore();
	}

	@Override
	public CertStore getCRLStore() throws Exception
	{
		return KeystoreManager.getCRLStore();
	}

	@Override
	public void addConfigurationUnloadListener(ConfigurationUnloadedListener listener)
	{
		ConfigurationManager.addConfigurationUnloadListener(listener);
	}
	
	@Override
	public KeyAndCertMaterial getContainerKey() {
		return _containerKey;
	}
	
	// containers should set this for enabling context substitution when old serialization found.
	public void setContainerKey(KeyAndCertMaterial containerKey)
	{
		_containerKey = containerKey;
	}
}
