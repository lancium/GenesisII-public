package edu.virginia.vcgr.genii.security.axis;

import java.security.cert.X509Certificate;
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

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.credentials.ClientCredentialTracker;
import edu.virginia.vcgr.genii.security.credentials.CredentialCache;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;
import edu.virginia.vcgr.genii.security.credentials.ServerSideStreamliningCredentialCache;
import edu.virginia.vcgr.genii.security.credentials.TimedOutCredentialsCachePerSession;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.faults.CredentialOmittedException;
import xmlbeans.org.oasis.saml2.assertion.AssertionDocument;

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

	private TimedOutCredentialsCachePerSession _sideCache = null;

	// tracks references to credentials that are already expected to be known on the container side.
	private ArrayList<String> _credentialReferences = new ArrayList<String>();

	public AxisCredentialWallet()
	{
		_realCreds = new CredentialWallet();
	}

	/**
	 * this constructor allows the container to pass in its cache of credentials which allows the chains we find in soap to be shorthand for
	 * the chains, which relies on the previously cached credentials that we got from the client. if we can't put together the chains from a
	 * combination of the soap and the cache, then we are broken and the client will have to re-send the whole chains.
	 */
	public AxisCredentialWallet(TimedOutCredentialsCachePerSession sideCache)
	{
		_realCreds = new CredentialWallet();
		_sideCache = sideCache;
	}

	/**
	 * constructor for creating a SAML credentials list manually: useful for the grid client.
	 */
	public AxisCredentialWallet(CredentialWallet realCreds)
	{
		_realCreds = realCreds;
	}

	/**
	 * constructor for re-creating SAML credentials from a received SOAP message: useful for the container. the sideCache and clientTlsCert
	 * will be used for recording the client's credentials, if streamlining is enabled.
	 */
	public AxisCredentialWallet(MessageElement encodedCredentials, MessageElement encodedReferences,
		TimedOutCredentialsCachePerSession sideCache, X509Certificate clientTlsCert) throws CredentialOmittedException
	{
		this(sideCache);
		addInCredentialReferencesFromSOAPHeaderElement(encodedReferences);
		constructFromSOAPHeaderElement(encodedCredentials, clientTlsCert);
	}

	/**
	 * provides access to the report of omitted credentials from the last parsing of the soap header for credential references.
	 */
	public List<String> getLastAttachmentReferences()
	{
		return _credentialReferences;
	}

	/**
	 * use with care to access the underlying saml credentials object.
	 */
	public CredentialWallet getRealCreds()
	{
		return _realCreds;
	}

	/**
	 * blurts out a bunch of xml for the credentials that need to be sent. this also tracks the credentials that will not be sent since they
	 * were seen before, in the credReferences parameter. those references need to be emitted as a separate soap header.
	 */
	public SOAPHeaderElement convertToSOAPElement(String containerGUID, List<String> credReferences)
	{
		SOAPHeaderElement encodedCredentials = new SOAPHeaderElement(GenesisIIConstants.DELEGATED_SAML_ASSERTIONS_QNAME);
		SOAPHeaderElement unicoreToAxisConverter = new SOAPHeaderElement(GenesisIIConstants.DELEGATED_SAML_ASSERTIONS_QNAME);
		encodedCredentials.setActor(null);
		encodedCredentials.setMustUnderstand(false);
		// creds that the current collection process should guarantee that the server has now seen.
		ArrayList<String> newlySent = new ArrayList<String>();
		int addedAny = 0;
		for (TrustCredential trustDelegation : _realCreds.getAssertionChains().values()) {
			List<AssertionDocument> assertionChain = new ArrayList<AssertionDocument>();
			trustDelegation.getXMLChain(assertionChain, credReferences, newlySent, containerGUID);
			for (AssertionDocument assertion : assertionChain) {
				addedAny++;
				encodedCredentials.appendChild(convertToAxis(unicoreToAxisConverter, assertion));
			}
		}
		
		/*
		 * hmmm: it's not safe!  not until the rpc has happened!
		 * 
		 * actually this concern may be slightly overblown, since we always have the mechanism for handling
		 * missing credentials.
		 */
		
		// now it should be safe to add the references as "seen", since we've generated the set for all the chains.
		for (String ref : newlySent) {
			if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
				_logger.debug("recording that container saw cred: " + ref);
			ClientCredentialTracker.recordContainerSawCred(containerGUID, ref);
		}

		if (_logger.isTraceEnabled())
			_logger.trace("encoded " + addedAny + " credentials for soap header.");

		boolean superNoisyDebug = false;
		if (superNoisyDebug) {
			try {
				AxisCredentialWallet awe = new AxisCredentialWallet(encodedCredentials, null, null, null);
				if (_logger.isTraceEnabled())
					_logger.debug("SUCCESS turning soap header back into axis cred wallet: " + awe.toString());
			} catch (Exception e) {
				_logger.error("failure turning soap header back into axis cred wallet");
			}
		}
		return encodedCredentials;
	}

	public SOAPHeaderElement emitReferencesAsSoap(List<String> credReferences)
	{
		if (!CredentialCache.CLIENT_CREDENTIAL_STREAMLINING_ENABLED) {
			// credential streamlining is not enabled.
			return null;
		}

		if ((credReferences == null) || credReferences.isEmpty()) {
			if (_logger.isTraceEnabled())
				_logger.debug("there are no credential references to put in soap header");
			return null;
		}

		SOAPHeaderElement encodedCredentials = new SOAPHeaderElement(GenesisIIConstants.REFERENCED_SAML_CREDENTIALS_QNAME);
		encodedCredentials.setActor(null);
		encodedCredentials.setMustUnderstand(false);
		int addedAny = 0;

		// MessageElement topLevel = new MessageElement(GenesisIIConstants.REFERENCED_SAML_CREDENTIALS_QNAME, "");
		for (String refId : credReferences) {
			encodedCredentials.appendChild(new MessageElement(GenesisIIConstants.ONE_CREDENTIAL_REFERENCE_QNAME, refId));
			addedAny++;
		}
		// encodedCredentials.appendChild(topLevel);
		if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
			_logger.debug("encoded " + addedAny + " references to previously sent credentials in soap header.");

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
					if (_logger.isTraceEnabled())
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

	/**
	 * takes the encoded trust delegation objects from UNICORE that we find in SOAP and translates them back into a normal credential wallet
	 * full of our own trust credentials.
	 */
	private void constructFromSOAPHeaderElement(MessageElement encodedCredentials, X509Certificate clientTlsCert)
		throws CredentialOmittedException
	{
		Document diffway = null;
		try {
			/*
			 * CAK: this was our first crashing point with new unicore code. resolved, at least temporarily, by turning the message element
			 * into a document and using that instead of the original element. doing this keeps the expected xmlns:xs field in place on the
			 * AttributeValue, whereas iterating the child nodes of the original starts losing important namespace tags.
			 */
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
		 * CAK: a desperate attempt to get something to work; this printed fine, so let's try using it... and that works. original traversal
		 * of getChildNodes or getChildElements using the original from axis was missing xmlns:xs fields, but traversing a copy is okay(?!).
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
				// this is a useful testing metric; if we are not sending as many credentials, performance should be better.
				if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
					_logger.debug("credential seen in SOAP: " + delegation.getId());

				if ((clientTlsCert != null) && CredentialCache.SERVER_CREDENTIAL_STREAMLINING_ENABLED) {
					// list that item in the cache also.
					ServerSideStreamliningCredentialCache.getServerSideStreamliningCredentialCache()
						.addCredentialForClientSession(clientTlsCert.getSubjectDN().toString(), delegation);
				}

			} catch (Throwable t) {
				_logger.error("failed to decode trust delegation from soap, dropping it.", t);
			}
		}

		// add in everything we scraped out of the soap header.
		getRealCreds().getAssertionChains().putAll(detachedDelegations);

		/*
		 * this code pre-fills the credential wallet with all the credentials that were referenced in the soap header, if they are not already
		 * listed.
		 */
		for (String refId : _credentialReferences) {
			// we only need to add something if it's not already included.
			if (getRealCreds().getCredential(refId) == null) {

				/*
				 * lookup the credential in the side cache.
				 */
				TrustCredential credReferenced = _sideCache.get(refId);
				if (credReferenced != null) {
					getRealCreds().getAssertionChains().put(refId, credReferenced);
					if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
						_logger.debug("located referenced credential and added it to the wallet: " + refId);
				} else {
					if (_logger.isDebugEnabled())
						_logger.debug("problem detected: referenced credential is not included in SOAP and is not in side cache: " + refId);
				}
			}
		}

		getRealCreds().flexReattachDelegations(true, _sideCache);

		if (CredentialCache.SERVER_CREDENTIAL_STREAMLINING_ENABLED) {
			/*
			 * make sure that all the referenced credentials were actually found. otherwise we need to emit an exception about this.
			 */
			StringBuilder missingList = new StringBuilder();
			for (String idSought : _credentialReferences) {
				// see if we find it in the set we just created.
				TrustCredential cred = getRealCreds().getCredential(idSought);
				if (cred == null) {
					// didn't find it in the wallet itself, so look in the side cache.
					cred = _sideCache.get(idSought);
					if (cred == null) {
						// record that this one was missing.
						missingList.append(idSought);
						missingList.append(" ");
					}
				}
			}
			if (missingList.length() > 0) {
				// we were supposed to find an already known credential, so fault out.
				String msg =
					CredentialWallet.OMMITTED_CREDENTIAL_SENTINEL + ": could not locate credentials with ids: " + missingList.toString();
				if (_logger.isDebugEnabled())
					_logger.debug(msg);
				throw new CredentialOmittedException(msg);
			}

			if (CredentialCache.BEHAVE_IRRATIONALLY_WITH_STREAMLINING) {
				_logger.info("IRRATIONAL MODE: forgetting side cache after successfully piecing together things this time.");
				_sideCache.clear();
			}
		}
	}

	/**
	 * supports credential streamlining by grabbing the list of referenced credentials out of the soap header. all that this does is to create
	 * a list of the referenced credentials; they should be added at a later point if they were not included in soap.
	 */
	private void addInCredentialReferencesFromSOAPHeaderElement(MessageElement encodedReferences) throws CredentialOmittedException
	{
		// clear the list first, since this method is the first part of preparing the wallet.
		getRealCreds().getAssertionChains().clear();
		if (!CredentialCache.SERVER_CREDENTIAL_STREAMLINING_ENABLED) {
			// we don't do anything with these headers if the streamlining support is disabled.
			return;
		}

		// toss any previously know references.
		_credentialReferences.clear();

		if ((encodedReferences == null) || (_sideCache == null)) {
			// nothing to look up or nowhere to look it up in.
			return;
		}

		Document diffway = null;
		try {
			diffway = encodedReferences.getAsDocument();
		} catch (Exception e) {
			_logger.error("couldn't convert to document from message element", e);
		}

		// throw error if the SOAP header is not an XML encoded SAML element
		if (!encodedReferences.getQName().equals(GenesisIIConstants.REFERENCED_SAML_CREDENTIALS_QNAME)) {
			String msg = "failure; attempt to parse an invalid credential references header.";
			_logger.error(msg);
			throw new SecurityException(msg);
		}

		NodeList childIter = diffway.getDocumentElement().getChildNodes();
		int refsCount = childIter.getLength();
		for (int index = 0; index < refsCount; index++) {
			Node node = childIter.item(index);
			if (node == null)
				break;
			try {
				String refFound = node.getTextContent();
				if (refFound != null) {
					if (refFound.length() == 0) {
						// skip that one; it's busted somehow.
						if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
							_logger.debug("BAD!  somehow the credential reference is empty!");
					} else {
						_credentialReferences.add(refFound);
						if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
							_logger.debug("credential reference listed in SOAP: " + refFound);
					}
				}
			} catch (Throwable t) {
				_logger.error("failed to decode credential reference from soap, dropping it.", t);
			}
		}
	}
}
