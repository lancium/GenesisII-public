package edu.virginia.vcgr.genii.security;

import java.security.GeneralSecurityException;

import org.w3c.dom.Element;

/**
 * This kind of object can produce web-ready Element objects. It's an interface abstracted out of
 * the old credential system.
 * 
 * @author ckoeritz
 */
public interface XMLCompatible
{
	/**
	 * Returns a URI (e.g., a WS-Security Token Profile URI) indicating the token type.
	 */
	public String getTokenType();

	/**
	 * Converts this credential to an Element. This could be an Axis MessageElement or a different
	 * type depending on the generator.
	 */
	public Element convertToMessageElement() throws GeneralSecurityException;
}
