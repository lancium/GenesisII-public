package edu.virginia.vcgr.genii.security.credentials;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xmlbeans.org.oasis.saml2.assertion.AssertionDocument;
import edu.virginia.vcgr.genii.algorithm.application.ProgramTools;
import edu.virginia.vcgr.genii.security.Describable;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.X500PrincipalUtilities;
import edu.virginia.vcgr.genii.security.identity.IdentityType;
import eu.unicore.security.etd.TrustDelegation;

/**
 * This class holds the credentials wallet for a grid session. Individual trust delegations in the
 * assertionChains member below are either: (1) isolated SAML assertions or (2) the most recently
 * delegated trust credential in a delegation chain. This is always true unless the object is being
 * deserialized or built from soap headers, in which case the reattachDelegations method can put
 * things right.
 * 
 * @author ckoeritz
 * @author myanhaona
 */
public class CredentialWallet implements Externalizable, Describable
{
	/*
	 * this is the auto-generated serial UID that we have to use, since the class did not initially
	 * have a serialization id. do not change this.*
	 */
	static public final long serialVersionUID = 2636486491170348968L;

	private static Log _logger = LogFactory.getLog(CredentialWallet.class);

	/*
	 * changing this value causes all existing (stored) calling contexts to be invalidated. this
	 * mainly affects existing jobs in a queue or on a BES.
	 */
	private final Long secretishCode = 177619762176L;

	/**
	 * the full set of trust credentials and delegation chains.
	 */
	transient private Map<String, TrustCredential> assertionChains = new HashMap<String, TrustCredential>();

	/**
	 * constructor for creating a SAML credentials list manually: useful for the grid client.
	 */
	public CredentialWallet()
	{
	}

	public CredentialWallet(List<NuCredential> creds)
	{
		for (NuCredential cred : creds) {
			if (cred instanceof TrustCredential) {
				TrustCredential tc = (TrustCredential) cred;
				assertionChains.put(tc.getId(), tc);
			}
		}
		flexReattachDelegations(true);
		if (_logger.isDebugEnabled())
			_logger.debug("added " + assertionChains.size() + " credentials into list");
	}

	/**
	 * fairly dangerous accessor function provided for generators based on this object.
	 */
	public Map<String, TrustCredential> getAssertionChains()
	{
		return assertionChains;
	}

	/**
	 * returns true if there are no assertion chains held in this object.
	 */
	public boolean isEmpty()
	{
		return assertionChains.isEmpty();
	}

	/**
	 * This method delegates trust to a new delegatee using an existing trust credential, which
	 * increases the length of the delegation chain by one element. The credential is returned, but
	 * it's also automatically added to this credential wallet.
	 */
	public TrustCredential delegateTrust(X509Certificate[] delegatee, IdentityType delegateeType, X509Certificate[] issuer,
		PrivateKey issuerPrivateKey, BasicConstraints restrictions, EnumSet<RWXCategory> accessCategories,
		TrustCredential priorDelegation)
	{
		try {
			TrustCredential assertion =
				CredentialCache.getCachedDelegationChain(delegatee, delegateeType, issuer, issuerPrivateKey, restrictions,
					accessCategories, priorDelegation);
			if (!assertionChains.containsKey(priorDelegation.getId())) {
				throw new SecurityException("failure to delegate trust; assertion is not part of credential wallet.");
			}
			assertionChains.remove(priorDelegation.getId());
			assertionChains.put(assertion.getId(), assertion);
			return assertion;
		} catch (Throwable e) {
			if (_logger.isDebugEnabled())
				_logger.debug("* dropping inappropriate extension for delegatee:\n" + delegatee[0].getSubjectDN()
					+ "\n...on chain of...\n" + priorDelegation.toString() + "\nexception message: " + e.getMessage());

			// stomp this errant credential.
			assertionChains.remove(priorDelegation.getId());
		}
		return null;
	}

	/**
	 * Adds a new credential into the credentials wallet.
	 */
	public void addCredential(TrustCredential assertion)
	{
		if (assertionChains.containsKey(assertion.getId())) {
			_logger.error("attempt to add identical assertion to the wallet; ignoring.");
			return;
		}
		if (assertion.getPriorDelegationId() != null) {
			assertionChains.remove(assertion.getPriorDelegationId());
		}
		assertionChains.put(assertion.getId(), assertion);
	}

	/**
	 * adds a collection of credentials into the wallet.
	 */
	public void addCredentials(List<TrustCredential> assertions)
	{
		for (TrustCredential assertion : assertions) {
			this.addCredential(assertion);
		}
	}

	public List<TrustCredential> getCredentials()
	{
		if (assertionChains.isEmpty()) {
			return Collections.emptyList();
		}
		return new ArrayList<TrustCredential>(assertionChains.values());
	}

	public void removeInvalidCredentials()
	{
		if (assertionChains.isEmpty())
			return;

		Iterator<TrustCredential> iterator = assertionChains.values().iterator();
		while (iterator.hasNext()) {
			TrustCredential curr = iterator.next();
			if (!curr.isValid()) {
				if (_logger.isDebugEnabled())
					_logger.debug("actually found an invalid credential to remove: " + curr.toString());
				iterator.remove();
			}
		}
	}

	@Override
	public String describe(VerbosityLevel verbosity)
	{
		StringBuilder buffer = new StringBuilder();
		for (TrustCredential assertion : assertionChains.values()) {
			buffer.append(assertion.describe(verbosity)).append("\n");
		}
		return buffer.toString();
	}

	public static String describeCredentialMap(Map<String, TrustCredential> toShow, VerbosityLevel verbosity)
	{
		StringBuilder buffer = new StringBuilder();
		for (TrustCredential assertion : toShow.values()) {
			buffer.append(assertion.describe(verbosity)).append("\n");
		}
		return buffer.toString();
	}

	public static String showIdChains(Map<String, TrustCredential> toShow, VerbosityLevel verbosity)
	{
		StringBuilder buffer = new StringBuilder();
		for (TrustCredential assertion : toShow.values()) {
			buffer.append(assertion.showIdChain()).append("\n");
		}
		return buffer.toString();
	}

	public TrustCredential getFirstUserCredential()
	{
		for (TrustCredential assertion : assertionChains.values()) {
			// see if the credential is a user credential.
			if (assertion.getRootOfTrust().getIssuerType() == IdentityType.USER) {
				return assertion;
			}
		}
		return null;
	}

	public String getFirstUserName()
	{
		TrustCredential assertion = getFirstUserCredential();
		if (assertion != null) {
			// This is a USER assertion of the form A->B->... where A is a USER.
			String userName =
				X500PrincipalUtilities.getCN(assertion.getRootOfTrust().getOriginalAsserter()[0].getSubjectX500Principal());
			_logger.debug("calculated user name for byteio storage: '" + userName + "'");
			return userName;
		}
		return null;
	}

	public static boolean chainsAreIntact(TrustCredential credential)
	{
		try {
			credential.checkValidityUber(new Date(), false);
		} catch (Exception e) {
			// looking bad.
			return false;
		}
		// seems fine.
		return true;
	}

	/*
	 * old name preserved for unicore usage; does not remove invalid delegations, so reassembly
	 * works as expected even with expired credentials.
	 */
	public void reattachDelegations()
	{
		flexReattachDelegations(false);
	}

	/**
	 * if credentials have been added willy nilly, possibly without their being linked together,
	 * this will find and relink all of them properly. the only thing remaining in the wallet will
	 * be isolated assertions or an assertion chain's most recent element. if removeInvalid is true,
	 * then any expired or invalid delegations will be trashed. it is important not to clear those
	 * out during deserialization though or one will not get back any credential, which leads to
	 * unanticipated exceptions.
	 */
	public void flexReattachDelegations(boolean removeInvalid)
	{
		Map<String, TrustCredential> credentialsToConsume = new HashMap<String, TrustCredential>();
		credentialsToConsume.putAll(assertionChains);
		assertionChains.clear();

		if (_logger.isTraceEnabled()) {
			_logger.debug("this is list before reattach:");
			_logger.debug(describeCredentialMap(credentialsToConsume, VerbosityLevel.HIGH));

			_logger.debug("these are ids involved before reattach:");
			_logger.debug(showIdChains(credentialsToConsume, VerbosityLevel.HIGH));
		}

		/*
		 * we postpone cleaning of the prior delegations until after we've gotten everyone
		 * reattached.
		 */
		ArrayList<String> idsToWhack = new ArrayList<String>();

		// construct delegation chain from the map of detached delegations collected above
		while (!credentialsToConsume.isEmpty()) {
			/*
			 * if we don't actually change the remaining detached set, then this stays false, and we
			 * know we will not make more progress.
			 */
			boolean progressMade = false;
			Iterator<TrustCredential> delegationIterator = credentialsToConsume.values().iterator();

			while (delegationIterator.hasNext()) {
				TrustCredential delegation = delegationIterator.next();
				if (delegation.isTerminalDelegation()) {
					if (_logger.isTraceEnabled())
						_logger.trace("storing terminal delegation: " + delegation.describe(VerbosityLevel.HIGH));
					assertionChains.put(delegation.getId(), delegation);
					delegationIterator.remove();
					progressMade = true;
					break;
				} else {
					String priorDelegationId = delegation.getPriorDelegationId();
					// make sure we're not operating on a wallet that's already been fully
					// reattached.
					if ((priorDelegationId != null) && (delegation.getPriorDelegation() != null) && chainsAreIntact(delegation)) {
						// this one looks okay already, so we'll just add it.
						if (_logger.isTraceEnabled()) {
							_logger.debug("found complete credential during reassembly; adding directly: "
								+ delegation.describe(VerbosityLevel.HIGH));
						}
						assertionChains.put(delegation.getId(), delegation);
						delegationIterator.remove();
						progressMade = true;
						break;
					} else {
						/*
						 * we pull in the stranded prior delegation if we can find it. otherwise we
						 * haven't gotten it off the wire or from serialization yet.
						 */
						if (assertionChains.containsKey(priorDelegationId)) {
							TrustCredential priorDelegation = assertionChains.get(priorDelegationId);
							/*
							 * we used to remove that prior guy right away, but that breaks some
							 * credential wallets. instead we choose to wait until the end before
							 * removing the consumed prior delegations.
							 */
							idsToWhack.add(priorDelegationId);
							try {
								delegation.extendTrustChain(priorDelegation);
								if (_logger.isTraceEnabled()) {
									_logger.debug("extended trust chain for prior delegation: "
										+ delegation.describe(VerbosityLevel.HIGH));
								}
								assertionChains.put(delegation.getId(), delegation);
								delegationIterator.remove();
							} catch (Throwable e) {
								_logger.info("problem with credential; discarding it");
							}
							progressMade = true;
							break;
						}
					}
				}
			}
			if (!progressMade) {
				if (_logger.isDebugEnabled()) {
					_logger.debug("this is what remains in FAILURE case:");
					_logger.debug(describeCredentialMap(credentialsToConsume, VerbosityLevel.HIGH));
					_logger.debug("these are ids involved for FAILURE case:");
					_logger.debug(showIdChains(credentialsToConsume, VerbosityLevel.HIGH));
				}

				String msg = "failure; there are missing links in the encoded SAML credentials!";
				_logger.error(msg);
				throw new SecurityException(msg);
			}
		}

		/*
		 * remove the credentials that were linked into larger chains during the above processing.
		 */
		for (String toToss : idsToWhack) {
			if (_logger.isTraceEnabled())
				_logger.debug("removing consumed prior credential: " + toToss);
			assertionChains.remove(toToss);
		}

		if (removeInvalid) {
			// finally, remove all credentials that are invalid/expired.
			removeInvalidCredentials();
		}
	}

	/**
	 * tests that an xml version of a trust delegation can be reparsed into a valid object.
	 */
	private static boolean testXmlDump(String xmlDump)
	{
		// checking what we just did before storing it.
		try {
			AssertionDocument ad = AssertionDocument.Factory.parse(xmlDump);
			boolean valid = ad.validate();

			if (!valid) {
				String msg = "FAILED to validate assertion document created just now!";
				_logger.error(msg);
				throw new RuntimeException(msg);
			}

			// now the ultimate test; get the trust credential back again.
			TrustDelegation tedious = new TrustDelegation(ad);
			TrustCredential newCred = new TrustCredential(tedious);
			newCred.checkValidity(new Date());
			_logger.debug("SUCCESS deserializing newly created serialization.");
			return true;
		} catch (Throwable e) {
			_logger.error("parsing failure of xml string dump", e);
			return false;
		}
	}

	/*
	 * This is serialization based on xml text coming from the unicore trust delegations, as opposed
	 * to the old scheme of relying on the unicore serialization process (which has changed
	 * drastically since activity 126 and is expected to continue to change or be removed).
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		int howManyTotal = 0;
		for (TrustCredential trustDelegation : assertionChains.values())
			howManyTotal += trustDelegation.getDelegationDepth();
		if (howManyTotal == 0) {
			_logger.warn("writeExternal saw no credentials to write during wallet serialization.");
			return;
		}
		try {
			out.writeInt(howManyTotal);
		} catch (Throwable e) {
			String msg = "writeExternal failed to serialize an int.  an int!";
			_logger.error(msg, e);
			throw new IOException(msg, e);
		}

		int addedAny = 0;
		for (TrustCredential curr : assertionChains.values()) {
			/*
			 * start iterating at the most delegated end of this credential and save all prior
			 * delegations too.
			 */
			while (curr != null) {
				addedAny++;

				if (_logger.isTraceEnabled())
					_logger.debug("writing assertion #" + addedAny);

				try {
					/*
					 * new implementation for serializing the trust delegations from unicore. pretty
					 * much just the xml text with an ad hoc version identifier.
					 */
					if (_logger.isTraceEnabled())
						_logger.debug("writing assertion #" + addedAny + ": wrote sentinel");

					out.writeLong(secretishCode);
					TrustDelegation td = curr.getDelegation();
					String xmlDump = td.getXMLBeanDoc().xmlText();

					boolean superNoisyDebug = false;
					if (superNoisyDebug) {
						boolean worked = testXmlDump(xmlDump);
						if (!worked) {
							_logger
								.error("validation of xml dump that was just created from TrustDelegation failed to be parsed back into object.");
						}
					}

					/*
					 * just write the string as an object. this works well. our prior implementation
					 * using writeUTF/readUTF was found to be unreliable.
					 */
					out.writeObject(xmlDump);

					if (_logger.isTraceEnabled())
						_logger.debug("writing assertion #" + addedAny + ": wrote " + xmlDump.length() + " byte string");

				} catch (IOException e) {
					_logger.error("failed to serialize assertion: " + e.getMessage());
				}

				curr = curr.getPriorDelegation();
			}
		}

		 // and a last sentinel, for good luck.
//		 out.writeLong(secretishCode);
//		 _logger.debug("wrote final sentinel");
		 
		 out.flush();

		if (_logger.isTraceEnabled())
			_logger.debug("serialized " + addedAny + " credentials.");
		if (_logger.isTraceEnabled())
			_logger.trace("serialization track: " + ProgramTools.showLastFewOnStack(6));
		if (addedAny != howManyTotal) {
			String msg = "failure to count number of assertions properly";
			_logger.error(msg);
		}
	}

	/*
	 * This method is called when java recreates the SOAPCredentials wallet from a serialized
	 * stream. First the no-argument constructor is been called. Then this method is invoked to
	 * properly restore the properties of the wallet.
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		if (assertionChains.size() > 0)
			throw new SecurityException("ERROR: somehow an external read op already has assertions");

		int assertionsToRead = 0;
		try {
			assertionsToRead = in.readInt();
		} catch (Throwable e) {
			_logger.info("not loading any credentials as there was no count stored.");
		}

		if (_logger.isTraceEnabled())
			_logger.debug("going to read in " + assertionsToRead + " assertions.");

		for (int i = 0; i < assertionsToRead; i++) {
			if (_logger.isTraceEnabled())
				_logger.debug("reading assertion #" + (i + 1));

			try {
				/*
				 * new way we serialize trust delegations; we do not use unicore serialization code,
				 * since it can change (and has). instead we're dumping the trust delegation to text
				 * and using that, plus our not so secret code below that lets us know if we're
				 * deserializing the right version of delegations.
				 */
				long codeForNewSerialization = in.readLong();
				if (codeForNewSerialization != secretishCode) {
					_logger.warn("old school trust delegation serialization found, or other corruption, wrong code was: "
						+ codeForNewSerialization);
					// how would this happen? we're hosed here too since we expect more credential
					// data following.
					continue;
				}

				if (_logger.isTraceEnabled())
					_logger.debug("reading assertion #" + (i + 1) + ": read proper sentinel");

				// we recreate the trust delegation from unicore via xml text.

				// using simple object serialization on the string works fine.
				String xmlString = (String) in.readObject();
				
				if (_logger.isTraceEnabled())
					_logger.debug("reading assertion #" + (i + 1) + ": read string with " + xmlString.length() + " bytes");

				AssertionDocument ad = AssertionDocument.Factory.parse(xmlString);
				boolean valid = ad.validate();
				if (!valid) {
					_logger
						.error("assertion document created from loaded assertion was not valid.  dropping trust delegation.");
					continue;
				}

				TrustDelegation td = new TrustDelegation(ad);
				TrustCredential newCred = new TrustCredential(td);

				if (_logger.isTraceEnabled())
					_logger.debug("fluffed out credential: " + newCred.describe(VerbosityLevel.HIGH));

				this.addCredential(newCred);
			} catch (Exception e) {
				_logger.warn("probable old school trust delegation serialization threw error: " + e.getLocalizedMessage());
				continue;
			}

		}

		// removed since changes serialization structure contract with existing 149.
		// long finalCode = in.readLong();
		// if (finalCode != secretishCode) {
		// _logger.warn("old school trust delegation serialization found or other corruption, wrong code was: "
		// + finalCode);
		// // how would this happen?
		// }
		// _logger.debug("read final sentinel.");

		flexReattachDelegations(false);
	}
}
