package edu.virginia.vcgr.genii.security.axis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.NodeImpl;
import org.apache.axis.message.SOAPDocumentImpl;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xmlbeans.org.oasis.saml2.assertion.AssertionDocument;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;

/**
 * provides axis specific xml output for the credentials wallet.
 * 
 * @author myanhaona
 * @author ckoeritz
 */
public class AxisCredentialWallet
{
	private static Log _logger = LogFactory.getLog(AxisCredentialWallet.class);

	private CredentialWallet _realCreds = null;

	public AxisCredentialWallet()
	{
		_realCreds = new CredentialWallet();
	}

	/**
	 * constructor for creating a SAML credentials list manually: useful for the grid client.
	 */
	public AxisCredentialWallet(CredentialWallet realCreds)
	{
		_realCreds = realCreds;
	}

	/**
	 * constructor for re-creating SAML credentials from a received SOAP message: useful for the container.
	 */
	public AxisCredentialWallet(MessageElement encodedCredentials)
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
		if (_logger.isTraceEnabled())
			_logger.trace("encoded " + addedAny + " credentials for soap header.");

		boolean superNoisyDebug = false;
		if (superNoisyDebug) {
			try {
				AxisCredentialWallet awe = new AxisCredentialWallet(encodedCredentials);
				_logger.debug("SUCCESS turning soap header back into axis cred wallet: " + awe.toString());
			} catch (Exception e) {
				_logger.error("failure turning soap header back into axis cred wallet");
			}
		}

		return encodedCredentials;
	}

	/*
	 * The XML node representation of a trust delegation produced by UNICORE security library is not compatible with our Axis library.
	 * Therefore, a conversion is needed before we can use them in a SOAP message. This method does this conversion.
	 */
	public static NodeImpl convertToAxis(MessageElement placeHolder, AssertionDocument assertion)
	{
		try {
			Node nodeToConvert = assertion.newDomNode();
			SOAPDocumentImpl doc = (SOAPDocumentImpl) placeHolder.getOwnerDocument();
			if (doc == null) {
				_logger.error("document was not of expected type: SOAPDocumentImpl!");
				return null;
			}

			NodeImpl converted = (NodeImpl) doc.importNode(nodeToConvert.getLastChild(), true);

			boolean superNoisyDebug = false;
			if (superNoisyDebug) {
				try {
					TrustCredential newCred = new TrustCredential(converted);
					_logger.debug("SUCCESS chewing on trust cred in axis style node: " + newCred.describe(VerbosityLevel.LOW));
				} catch (Exception e) {
					_logger.error("failed to create trust cred from axis style node");
				}
			}

			return converted;
		} catch (Exception e) {
			_logger.error("failed to create axis style Node", e);
			return null;
		}
	}

	//hmmm: !!!! important place here; we need to not verify the whole chains we're getting back.
	// instead of returning a fully assembled credential wallet, we just need to give back everything we found in the
	// soap WITHOUT verifying it!....
	
	private void constructFromSOAPHeaderElement(MessageElement encodedCredentials)
	{
		/*
		 * CAK: this was our first crashing point with new unicore code. resolved, at least temporarily, by turning the message element into a
		 * document and using that instead of the original element. doing this keeps the expected xmlns:xs field in place on the
		 * AttributeValue, whereas iterating the child nodes of the original starts losing important namespace tags.
		 */

		Document diffway = null;
		try {
			diffway = encodedCredentials.getAsDocument();
		} catch (Exception e) {
			_logger.error("couldn't convert to document from messelem", e);
		}

		// throw error if the SOAP header is not an XML encoded SAML element
		if (!encodedCredentials.getQName().equals(GenesisIIConstants.DELEGATED_SAML_ASSERTIONS_QNAME)) {
			String msg = "failure; attempt to parse an invalid SAML credentials header.";
			_logger.error(msg);
			throw new SecurityException(msg);
		}

		// retrieve all trust delegations from the SOAPHeader and store them in a map.
		Map<String, TrustCredential> detachedDelegations = new HashMap<String, TrustCredential>();

		/*
		 * a desperate attempt to get something to work; this printed fine, so let's try using it... and that works. original traversal of
		 * getChildNodes or getChildElements using the original from axis was missing xmlns:xs fields, but traversing a copy is okay(?!).
		 */
		NodeList childIter = diffway.getDocumentElement().getChildNodes();
		int delegationCount = childIter.getLength();
		for (int index = 0; index < delegationCount; index++) {

			Node node = childIter.item(index);
			if (node == null)
				break;

			try {
				TrustCredential delegation = new TrustCredential(node);
				detachedDelegations.put(delegation.getId(), delegation);
			} catch (Throwable t) {
				_logger.error("failed to decode trust delegation from soap, dropping it.", t);
			}
		}
		getRealCreds().getAssertionChains().clear();
		getRealCreds().getAssertionChains().putAll(detachedDelegations);
		
		//hmmm: DONT VERIFY YET!!!!>......
		//hmmm: BUT DO VERIFY EACH ITEM AT LEAST ONCE!!!!
		
		//hmmm: perhaps the credential cache should actually cache higher order credential pieces too! then we would
		// not have to test those things, if we had already seen and verified them.  maybe only place to verify
		// is before putting the things into the cache.
		
		//hmmm: careful; can't allow the cred wallet reassembly code to remove things from our cached credentials list!!!!
		
		getRealCreds().flexReattachDelegations(true);
	}

}
