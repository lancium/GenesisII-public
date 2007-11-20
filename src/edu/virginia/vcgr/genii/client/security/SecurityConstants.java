package edu.virginia.vcgr.genii.client.security;

import javax.xml.namespace.QName;

public class SecurityConstants {

	static public QName IDP_DELEGATED_IDENITY_QNAME = new QName(
			"http://vcgr.cs.virginia.edu/container/2007/11/x509-authn",
			"delegated-identity-param");
	
	static public QName IDP_USERNAME_QNAME = new QName(
			"http://vcgr.cs.virginia.edu/container/2007/11/x509-authn",
			"username-param");

	static public QName IDP_PASSWORD_QNAME = new QName(
			"http://vcgr.cs.virginia.edu/container/2007/11/x509-authn",
			"password-param");
	
}
