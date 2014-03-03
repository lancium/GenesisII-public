package edu.virginia.vcgr.genii.security;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

public interface CertificateValidator {
	/**
	 * Verifies that the certificate chain is internally consistent.
	 */
	public boolean validateCertificateConsistency(X509Certificate[] certChain);

	/**
	 * Provides direct access to the local resource trust store.
	 */
	public KeyStore getResourceTrustStore();

	/**
	 * Verifies that the certificate is found rooted in our local resource trust
	 * store.
	 */
	public boolean validateIsTrustedResource(X509Certificate[] certChain);

	/**
	 * Verifies that the certificate is found rooted in the specified trust
	 * store.
	 */
	public boolean validateTrustedByKeystore(X509Certificate[] certChain,
			KeyStore store);
}
