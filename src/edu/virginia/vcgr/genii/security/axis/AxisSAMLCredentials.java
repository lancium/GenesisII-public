package edu.virginia.vcgr.genii.security.axis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.NodeImpl;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xmlbeans.org.oasis.saml2.assertion.AssertionDocument;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;

/**
 * provides axis specific xml output for the credentials wallet.
 * 
 * @author myanhaona
 * @author ckoeritz
 */
public class AxisSAMLCredentials
{
	private static Log _logger = LogFactory.getLog(AxisSAMLCredentials.class);

	private CredentialWallet _realCreds = null;

	public AxisSAMLCredentials()
	{
		_realCreds = new CredentialWallet();
	}

	/**
	 * constructor for creating a SAML credentials list manually: useful for the grid client.
	 */
	public AxisSAMLCredentials(CredentialWallet realCreds)
	{
		_realCreds = realCreds;
	}

	/**
	 * constructor for re-creating SAML credentials from a received SOAP message: useful for the
	 * container.
	 */
	public AxisSAMLCredentials(MessageElement encodedCredentials)
	{
		this(new CredentialWallet());
		constructFromSOAPHeaderElement(encodedCredentials);
	}

	/**
	 * use with care to access the underlying saml credentials object.
	 */
	public CredentialWallet getRealCreds()
	{
		return _realCreds;
	}

	public SOAPHeaderElement convertToSOAPElement()
	{
		SOAPHeaderElement encodedCredentials = new SOAPHeaderElement(GenesisIIConstants.DELEGATED_SAML_ASSERTIONS_QNAME);
		SOAPHeaderElement unicoreToAxisConverter = new SOAPHeaderElement(GenesisIIConstants.DELEGATED_SAML_ASSERTIONS_QNAME);
		encodedCredentials.setActor(null);
		encodedCredentials.setMustUnderstand(false);
		int addedAny = 0;
		for (TrustCredential trustDelegation : _realCreds.getAssertionChains().values()) {
			List<AssertionDocument> assertionChain = new ArrayList<AssertionDocument>();
			trustDelegation.getXMLChain(assertionChain);
			for (AssertionDocument assertion : assertionChain) {
				addedAny++;
				encodedCredentials.appendChild(convertToAxis(unicoreToAxisConverter, assertion));
			}
		}
		if (addedAny == 0) {
			_logger.error("failed to encode any credentials for soap header.");
		} else {
			if (_logger.isDebugEnabled())
				_logger.debug("encoded " + addedAny + " credentials for soap header.");
		}
		return encodedCredentials;
	}

	/*
	 * The XML node representation of a trust delegation produced by UNICORE security library is not
	 * compatible with our Axis library. Therefore, a conversion is needed before we can use them in
	 * a SOAP message. This method does this conversion.
	 */
	public static NodeImpl convertToAxis(MessageElement placeHolder, AssertionDocument assertion)
	{
		try {
			Node nodeToConvert = assertion.newDomNode();
			Document doc = placeHolder.getOwnerDocument();
			NodeImpl converted = (NodeImpl) doc.importNode(nodeToConvert.getLastChild(), true);
			return converted;
		} catch (Exception e) {
			_logger.error("failed to create axis style Node", e);
			return null;
		}
	}

	private void constructFromSOAPHeaderElement(MessageElement encodedCredentials)
	{
		// throw error if the SOAP header is not an XML encoded SAML element
		if (!encodedCredentials.getQName().equals(GenesisIIConstants.DELEGATED_SAML_ASSERTIONS_QNAME)) {
			String msg = "failure; attempt to parse an invalid SAML credentials header.";
			_logger.error(msg);
			throw new SecurityException(msg);
		}
		// retrieve all delegated trust delegations from the SOAPHeader and store them in a map
		Map<String, TrustCredential> detachedDelegations = new HashMap<String, TrustCredential>();
		NodeList trustDelegationList = encodedCredentials.getChildNodes();
		int delegationCount = trustDelegationList.getLength();
		for (int index = 0; index < delegationCount; index++) {
			Node node = trustDelegationList.item(index);
			TrustCredential delegation = new TrustCredential(node);
			detachedDelegations.put(delegation.getId(), delegation);
		}
		getRealCreds().getAssertionChains().clear();
		getRealCreds().getAssertionChains().putAll(detachedDelegations);
		getRealCreds().reattachDelegations();
	}

}
