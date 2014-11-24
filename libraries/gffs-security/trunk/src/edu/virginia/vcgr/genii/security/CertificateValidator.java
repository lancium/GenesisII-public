package edu.virginia.vcgr.genii.security;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

public interface CertificateValidator
{
	/**
	 * Verifies that the certificate chain is internally consistent.
	 */
	public boolean validateCertificateConsistency(X509Certificate[] certChain) throws Exception;

	/**
	 * Provides direct access to the local resource trust store.
	 */
	public KeyStore getResourceTrustStore() throws Exception;

	/*
	 * Provides access to the trust stores and crl store for the application. 
	 */
	public TrustStoreProvider getTrustStoreProvider();
	
	/**
	 * Verifies that the certificate is found rooted in our local resource trust store.
	 */
	public boolean validateIsTrustedResource(X509Certificate[] certChain) throws Exception;

	/**
	 * Verifies that the certificate is found rooted in the specified trust store.
	 */
	public boolean validateTrustedByKeystore(X509Certificate[] certChain, KeyStore store) throws Exception;
	
	/**
	 * Required for communicating the grid-certificates directory to pattern-based trust stores.
	 */
//	public String getGridCertificatesDir();
}
