package edu.virginia.vcgr.genii.client.security;

import java.security.KeyStore;
import java.security.cert.CertStore;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationUnloadedListener;
import edu.virginia.vcgr.genii.security.TrustStoreProvider;

public class TrustStoreLinkage implements TrustStoreProvider
{
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

}
