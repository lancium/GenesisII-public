package edu.virginia.vcgr.genii.client.comm;

public class CommConstants
{
	static public final String TARGET_EPR_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.client.comm.target-epr";
	static public final String CALLING_CONTEXT_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.client.comm.calling-context";
	static public final String MESSAGE_SEC_CALL_DATA = 
		"edu.virginia.vcgr.genii.client.comm.message-sec-call-data";
	static public final String MESSAGE_SEC_SIGN_PARTS = 
		"edu.virginia.vcgr.genii.client.comm.message-sec-signParts";

	// added here because the wss4j 1.5.4 library no longer has getType on the
	// X509Security class.
	static public final String X509_SECURITY_TYPE =
		"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3";

}
