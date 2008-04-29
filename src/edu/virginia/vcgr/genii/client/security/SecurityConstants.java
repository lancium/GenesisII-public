package edu.virginia.vcgr.genii.client.security;

import javax.xml.namespace.QName;

public class SecurityConstants {

	static public final String GAML_TOKEN_TYPE = 
		"http://vcgr.cs.virginia.edu/security/2007/11/delegated-saml";


	//--- IDP CONSTANTS ----------------------------------------------

	static public final QName IDP_DELEGATED_IDENITY_QNAME = new QName(
			"http://vcgr.cs.virginia.edu/security/2007/11/x509-authn",
			"delegated-identity-param");

	static public final QName IDP_IDENITY_QNAME = new QName(
			"http://vcgr.cs.virginia.edu/security/2007/11/x509-authn",
			"identity-param");
	
	static public final QName IDP_VALID_MILLIS_QNAME = new QName(
			"http://vcgr.cs.virginia.edu/security/2007/11/x509-authn",
			"valid-millis");
	
	static public final QName NEW_IDP_NAME_QNAME = new QName(
			"http://vcgr.cs.virginia.edu/security/2007/11/x509-authn",
			"new-idp-name-param");


	//--- JNDI STS CONSTANTS ----------------------------------------------

	static public final QName NEW_JNDI_STS_NAME_QNAME = new QName(
			"http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn",
			"new-sts-name-param");

	static public final QName NEW_JNDI_STS_SEARCHBASE_QNAME = new QName(
			"http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn",
			"new-sts-searchbase-param");

	static public final QName NEW_JNDI_STS_TYPE_QNAME = new QName(
			"http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn",
			"new-sts-type-param");

	static public final QName NEW_JNDI_STS_HOST_QNAME = new QName(
			"http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn",
			"new-sts-host-param");

	static public final QName NEW_JNDI_NISDOMAIN_QNAME = new QName(
			"http://vcgr.cs.virginia.edu/security/2007/11/jndi-authn",
			"new-sts-nisdomain-param");

}
