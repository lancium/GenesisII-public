package edu.virginia.vcgr.genii.security.axis;

import java.security.GeneralSecurityException;

import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.message.token.BinarySecurity;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.virginia.vcgr.genii.security.SAMLConstants;
import edu.virginia.vcgr.genii.security.XMLCompatible;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;

/**
 * Produces an Axis MessageElement from the TrustCredential.
 * 
 * @author myanhaona
 * @author ckoeritz
 */
public class AxisTrustCredential implements XMLCompatible
{
	static private Log _logger = LogFactory.getLog(AxisTrustCredential.class);

	transient private TrustCredential _realCred;

	public AxisTrustCredential(TrustCredential cred)
	{
		_realCred = cred;
	}

	@Override
	public String getTokenType()
	{
		return SAMLConstants.SAML_DELEGATION_TOKEN_TYPE;
	}

	/**
	 * This is the method for converting a trust delegation into a form that can be transmitted over
	 * the wire using Axis web-services facilities.
	 */
	public Element convertToMessageElement() throws GeneralSecurityException
	{
		// this location also did not need a synchronization.
		Node nodeRepresentation = null;
		synchronized (_realCred.getDelegation()) {
			MessageElement template = new MessageElement(BinarySecurity.TOKEN_BST);
			nodeRepresentation = AxisSAMLCredentials.convertToAxis(template, _realCred.getDelegation().getXML());
		}
		MessageElement securityToken = new MessageElement(BinarySecurity.TOKEN_BST);
		securityToken.appendChild(nodeRepresentation);
		securityToken.setAttributeNS(null, "ValueType", SAMLConstants.SAML_DELEGATION_TOKEN_TYPE);
		MessageElement wseTokenRef = null;
		try {
			MessageElement embedded = new MessageElement(SAMLConstants.EMBEDDED_TOKEN_QNAME);
			embedded.addChild(securityToken);
			wseTokenRef = new MessageElement(SAMLConstants.SECURITY_TOKEN_QNAME);
			wseTokenRef.addChild(embedded);
		} catch (SOAPException e) {
			_logger.error("failure to create MessageElement: " + e.getMessage());
			throw new GeneralSecurityException(e.getMessage(), e);
		}

		return wseTokenRef;
	}
}
