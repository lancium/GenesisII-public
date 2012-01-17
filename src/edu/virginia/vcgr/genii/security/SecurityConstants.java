package edu.virginia.vcgr.genii.security;

import javax.xml.namespace.QName;

public class SecurityConstants
{

	// --- SECURE ADDRESSING CONSTANTS --------------------------------

	static public final String MUTUAL_X509_URI =
			"http://www.ogf.org/ogsa/2007/05/secure-communication#MutualX509";
	static public final String USERNAME_TOKEN_URI =
			"http://www.ogf.org/ogsa/2007/05/secure-communication#UsernameToken";
	static public final String SERVER_TLS_URI =
			"http://www.ogf.org/ogsa/2007/05/secure-communication#ServerTLS";
	static public final String GAML_CLAIMS_URI =
			"http://vcgr.cs.virginia.edu/security/gaml-claim/v1.0";

	// --- IDP CONSTANTS ----------------------------------------------

	static public final QName IDP_DELEGATED_CREDENTIAL_QNAME =
			new QName(
					"http://vcgr.cs.virginia.edu/security/2007/11/x509-authn",
					"delegated-credential-param");

	static public final QName IDP_VALID_MILLIS_QNAME =
			new QName(
					"http://vcgr.cs.virginia.edu/security/2007/11/x509-authn",
					"valid-millis");

	static public final QName NEW_IDP_NAME_QNAME =
			new QName(
					"http://vcgr.cs.virginia.edu/security/2007/11/x509-authn",
					"new-idp-name-param");
	
	static public final QName NEW_IDP_TYPE_QNAME =
		new QName(
				"http://vcgr.cs.virginia.edu/security/2007/11/x509-authn",
				"new-idp-type-param");

	// --- JNDI STS CONSTANTS ----------------------------------------------

	static public final QName NEW_JNDI_STS_NAME_QNAME =
			new QName(
					"http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn",
					"new-sts-name-param");

	static public final QName NEW_JNDI_STS_SEARCHBASE_QNAME =
			new QName(
					"http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn",
					"new-sts-searchbase-param");

	static public final QName NEW_JNDI_STS_TYPE_QNAME =
			new QName(
					"http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn",
					"new-sts-type-param");

	static public final QName NEW_JNDI_STS_HOST_QNAME =
			new QName(
					"http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn",
					"new-sts-host-param");

	static public final QName NEW_JNDI_NISDOMAIN_QNAME =
			new QName(
					"http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn",
					"new-sts-nisdomain-param");

	// --- KERB STS CONSTANTS ----------------------------------------------
	
	static public final QName NEW_KERB_IDP_REALM_QNAME =
		new QName(
				"http://vcgr.cs.virginia.edu/security/2007/11/kerb-authn",
				"new-idp-realm-param");

	static public final QName NEW_KERB_IDP_KDC_QNAME =
		new QName(
				"http://vcgr.cs.virginia.edu/security/2007/11/kerb-authn",
				"new-idp-kdc-param");
	
}
