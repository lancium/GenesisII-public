package edu.virginia.vcgr.genii.security;

import javax.xml.namespace.QName;

public class SecurityConstants
{
	static public final int IDP_RESOURCE_KEY_LENGTH = 1024;

	// General security properties...

	static public final long oneDayInMs = 1000L * 60L * 60L * 24L; // one day.

	// credentials are valid 10 years by default.
	static public final long CredentialExpirationMillis = oneDayInMs * 365L * 10L; // 10 years.
	static public final long CredentialGoodFromOffset = 1000L * 60L * 15L; // 15 minutes ago
	static public final long CredentialCacheTimeout = 1000L * 60L * 60L; // 1 hour lifetime in cache

	// temporary credentials for our client connection TLS certificate lifetime.
	static public final long defaultCredentialExpirationMillis = oneDayInMs * 32L;

	// maximum number of delegations we accept in a delegation chain.
	static public final int MaxDelegationDepth = 10;

	// Secure addressing constants...

	static public final String MUTUAL_X509_URI = "http://www.ogf.org/ogsa/2007/05/secure-communication#MutualX509";
	static public final String USERNAME_TOKEN_URI = "http://www.ogf.org/ogsa/2007/05/secure-communication#UsernameToken";
	static public final String SERVER_TLS_URI = "http://www.ogf.org/ogsa/2007/05/secure-communication#ServerTLS";
	static public final String SAML_CLAIMS_URI = "http://vcgr.cs.virginia.edu/security/saml-claim/v1.0";

	// IDP Constants...

	static public final QName STORED_CALLING_CONTEXT_QNAME = new QName(
		"http://vcgr.cs.virginia.edu/security/2013/03/sts-authn", "stored-calling-context");

	static public final QName CERTIFICATE_CHAIN_QNAME = new QName("http://vcgr.cs.virginia.edu/security/2013/03/sts-authn",
		"certificate-chain");

	static public final QName IDP_PRIVATE_KEY_QNAME = new QName("http://vcgr.cs.virginia.edu/security/2013/03/sts-authn",
		"private-key");

	/*
	 * a stored credential that either represents a simple x509 identity or a delegated trust
	 * credential. if this grid was created with 2.6 or older versions, then there will be no entry
	 * for this name, and we will regenerate a credential as needed.
	 */
	static public final QName IDP_STORED_CREDENTIAL_QNAME = new QName(
		"http://vcgr.cs.virginia.edu/security/2007/11/x509-authn", "delegated-credentials");

	static public final QName IDP_VALID_MILLIS_QNAME = new QName("http://vcgr.cs.virginia.edu/security/2007/11/x509-authn",
		"valid-millis");

	static public final QName NEW_IDP_NAME_QNAME = new QName("http://vcgr.cs.virginia.edu/security/2007/11/x509-authn",
		"new-idp-name-param");

	static public final QName NEW_IDP_TYPE_QNAME = new QName("http://vcgr.cs.virginia.edu/security/2007/11/x509-authn",
		"new-idp-type-param");

	// JNDI STS constants...

	static public final QName NEW_JNDI_STS_NAME_QNAME = new QName("http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn",
		"new-sts-name-param");

	static public final QName NEW_JNDI_STS_SEARCHBASE_QNAME = new QName(
		"http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn", "new-sts-searchbase-param");

	static public final QName NEW_JNDI_STS_TYPE_QNAME = new QName("http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn",
		"new-sts-type-param");

	static public final QName NEW_JNDI_STS_HOST_QNAME = new QName("http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn",
		"new-sts-host-param");

	static public final QName NEW_JNDI_NISDOMAIN_QNAME = new QName("http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn",
		"new-sts-nisdomain-param");

	// Kerberos STS constants...

	static public final QName NEW_KERB_IDP_REALM_QNAME = new QName("http://vcgr.cs.virginia.edu/security/2007/11/kerb-authn",
		"new-idp-realm-param");

	static public final QName NEW_KERB_IDP_KDC_QNAME = new QName("http://vcgr.cs.virginia.edu/security/2007/11/kerb-authn",
		"new-idp-kdc-param");
}
