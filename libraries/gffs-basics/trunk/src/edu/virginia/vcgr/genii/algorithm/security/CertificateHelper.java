package edu.virginia.vcgr.genii.algorithm.security;

import java.io.ByteArrayInputStream;

/**
 * some helpful functions for dealing with certificates.
 */
class CertificateHelper
{

	/**
	 * Converts certificates to java.security form from javax.security form.
	 */
	public static java.security.cert.X509Certificate convert(javax.security.cert.X509Certificate cert)
	{
		try {
			byte[] encoded = cert.getEncoded();
			ByteArrayInputStream bis = new ByteArrayInputStream(encoded);
			java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
			return (java.security.cert.X509Certificate) cf.generateCertificate(bis);
		} catch (java.security.cert.CertificateEncodingException e) {
		} catch (javax.security.cert.CertificateEncodingException e) {
		} catch (java.security.cert.CertificateException e) {
		}
		return null;
	}

	/**
	 * Converts certificates to javax.security form from java.security form.
	 */
	public static javax.security.cert.X509Certificate convert(java.security.cert.X509Certificate cert)
	{
		try {
			byte[] encoded = cert.getEncoded();
			return javax.security.cert.X509Certificate.getInstance(encoded);
		} catch (java.security.cert.CertificateEncodingException e) {
		} catch (javax.security.cert.CertificateEncodingException e) {
		} catch (javax.security.cert.CertificateException e) {
		}
		return null;
	}

}