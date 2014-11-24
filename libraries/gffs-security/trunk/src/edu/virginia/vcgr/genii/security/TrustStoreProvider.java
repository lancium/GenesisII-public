package edu.virginia.vcgr.genii.security;

import java.security.KeyStore;
import java.security.cert.CertStore;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationUnloadedListener;

public interface TrustStoreProvider
{
	/**
	 * Provides access to the local resource trust store.
	 */
	public KeyStore getResourceTrustStore() throws Exception;
	
	/**
	 * Provides access to the TLS trust store.
	 */
	public KeyStore getTLSTrustStore() throws Exception;
	
	/**
	 * Provides access to the list of CRLs currently defined.
	 */
	public CertStore getCRLStore() throws Exception;
	
	/**
	 * Allows hooking into the configuration unload alerts, to reconfigure object.
	 */
	public void addConfigurationUnloadListener(ConfigurationUnloadedListener listener);
}
