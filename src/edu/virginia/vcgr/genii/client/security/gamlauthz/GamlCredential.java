package edu.virginia.vcgr.genii.client.security.gamlauthz;

import java.security.GeneralSecurityException;
import java.util.Date;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.AttributeInvalidException;

/**
 * A credential object
 * 
 * @author dmerrill
 *
 */
public interface GamlCredential {

	public static final String ENCODED_GAML_CREDENTIALS_PROPERTY = 
		"genii.client.security.authz.encoded-gaml-credentials";
	public static final String CALLER_CREDENTIALS_PROPERTY = 
		"genii.client.security.authz.caller-credentials";

	/**
	 * Checks that the attribute time-valid with respect to the supplied 
	 * date
	 */
	public void checkValidity(Date date) throws AttributeInvalidException;
	
	/**
	 * Returns a URI (e.g., a WS-Security Token Profile URI) indicating the token type
	 */
	public String getTokenType();
	
	/**
	 * Converts this credential to an Axis Message Element
	 * @return
	 * @throws GeneralSecurityException
	 */
	public MessageElement toMessageElement() throws GeneralSecurityException;

}
