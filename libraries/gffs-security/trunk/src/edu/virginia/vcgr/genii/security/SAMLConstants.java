package edu.virginia.vcgr.genii.security;

import javax.xml.namespace.QName;

/**
 * This class hosts all SAML related constants that are used in various places
 * in the code.
 * 
 * @author myanhaona
 */
public class SAMLConstants {
	/*
	 * this property name is used for storing the credentials while they are
	 * still in the working context, and not yet in the calling context.
	 */
	public static final String SAML_CREDENTIALS_WORKING_CONTEXT_CREDS_PROPERTY_NAME = "creds-from-wire";

	/*
	 * this wallet property is a generally client-side property that holds onto
	 * any credentials that the client has managed to create or be granted by a
	 * container. it lives in the calling context.
	 */
	public static final String SAML_CREDENTIALS_WALLET_PROPERTY_NAME = "saml-credentials-wallet";

	/**
	 * this property holds the client's ssl certificate info in the calling
	 * context.
	 */
	public static final String SAML_CLIENT_SSL_CERTIFICATE_PROPERTY_NAME = "saml-client-ssl-certiticate";

	/**
	 * a property that holds the "caller-only" credentials. this is mainly used
	 * on the container side for tracking the credentials that were originally
	 * presented at the time of a request from a client. this plus a few other
	 * credentials are stored into the TransientCredentials.
	 */
	public static final String CALLER_CREDENTIALS_PROPERTY = "genii.security.caller-credentials";

	public static final long ASSERTION_VALIDITY_SAFETY_INTERVAL = 60 * 1000;

	/**
	 * the value type within xml for our collection of saml trust delegations.
	 */
	public static final String SAML_DELEGATION_TOKEN_TYPE = "http://vcgr.cs.virginia.edu/security/saml-delegation";

	public static final String ASSERTION_ID_ATTRIBUTE_NAME = "saml.attribute.id";
	public static final String PRIOR_ASSERTION_ID_ATTRIBUTE_NAME = "saml.attribute.prior-assertion-id";
	public static final String PRIOR_DSIG_ATTRIBUTE_NAME = "saml.attribute.prior-dsig";
	public static final String ACCESS_RIGHT_MASK_ATTRIBUTE_NAME = "saml.attribute.access-mask";
	public static final String DELEGATEE_IDENTITY_TYPE_ATTRIBUTE_NAME = "saml.attribute.id-delegatee-type";
	public static final String ISSUER_IDENTITY_TYPE_ATTRIBUTE_NAME = "saml.attribute.id-issuer-type";
	public static final String PLACEHOLDER_FOR_NAME_FORMAT = ".*";

	public static final String SAMLNamespace = "urn:oasis:names:tc:SAML:2.0:assertion";
	public static final QName SAML_ASSERTION_QNAME = new QName(SAMLNamespace,
			"Assertion");

	// ws-security 1.1 namespace name.
	public static final String WS_SECURITY_NS11_URI = "http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd";

	public static final QName SECURITY_TOKEN_QNAME = new QName(
			WS_SECURITY_NS11_URI, "SecurityTokenReference");
	public static final QName EMBEDDED_TOKEN_QNAME = new QName(
			WS_SECURITY_NS11_URI, "Embedded");
}
