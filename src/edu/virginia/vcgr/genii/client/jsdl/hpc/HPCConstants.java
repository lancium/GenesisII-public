package edu.virginia.vcgr.genii.client.jsdl.hpc;

import javax.xml.namespace.QName;

public interface HPCConstants
{
	static public final String HPC_NS = 
		"http://schemas.ggf.org/jsdl/2006/07/jsdl-hpcpa";
	static public final String HPCP_NS =
		"http://schemas.ogf.org/hpcp/2007/11/ac";
	
	static public final String USERNAME_TOKEN_NS =
		"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	static public final String USERNAME_TOKEN_NAME =
		"UsernameToken";
	
	static public final String HPC_APPLICATION_NAME =
		"HPCProfileApplication";
	
	static public final QName HPC_APPLICATION_QNAME =
		new QName(HPC_NS, HPC_APPLICATION_NAME);
	static public final QName HPCP_CREDENTIAL_QNAME =
		new QName(HPCP_NS, "Credential");
	static public final QName USERNAME_TOKEN_QNAME =
		new QName(USERNAME_TOKEN_NS, USERNAME_TOKEN_NAME);
}