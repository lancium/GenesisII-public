package edu.virginia.vcgr.genii.client.security;

import javax.xml.namespace.QName;

public class SecurityConstants {

	static public final QName IDP_DELEGATED_IDENITY_QNAME = new QName(
			"http://vcgr.cs.virginia.edu/security/2007/11/x509-authn",
			"delegated-identity-param");
	
	static public final QName NEW_IDP_NAME_QNAME = new QName(
			"http://vcgr.cs.virginia.edu/security/2007/11/x509-authn",
			"new-idp-name-param");

	static public final String GAML_TOKEN_TYPE = 
		"http://vcgr.cs.virginia.edu/security/2007/11/delegated-saml";
}
