package edu.virginia.vcgr.genii.client.security;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.identity.IdentityType;

public class WalletUtilities
{
	private static Log _logger = LogFactory.getLog(WalletUtilities.class);

	/**
	 * retrieves the owner identities that we care about for exports. this should at most be two
	 * items. the first is the grid user who is creating the export (so this function only makes
	 * sense to call at creation time!) and the second is any memorable TLS identity creating the
	 * export. the second identity should not be populated if this is just a self-signed
	 * certificate.  if the "justChooseFirst" parameter is true, then a missing filter will be
	 * ignored when there is more than one USER credential and the first found will be returned.
	 * note that the first user credential found is *not* necessarily the real owner.
	 */
	public static ArrayList<String> extractOwnersFromCredentials(String filter, boolean justChooseFirst) throws IOException
	{
		ArrayList<String> toReturn = new ArrayList<String>();

		// get the calling context.
		ICallingContext callContext = ContextManager.getCurrentContext();

		// get the credential wallet.
		TransientCredentials tc = TransientCredentials.getTransientCredentials(callContext);

		ArrayList<NuCredential> creds = tc.getCredentials();

		// get the list of USER names from the credential wallet.
		if (creds.size() == 0) {
			_logger.error("The list of credentials is empty; cannot extract owner.");
			return null;
		}

		CredentialWallet wallet = new CredentialWallet(creds);
		_logger.debug("got wallet info with:\n" + wallet.describe(VerbosityLevel.HIGH));

		ArrayList<TrustCredential> allUsersFound = new ArrayList<TrustCredential>();

		for (NuCredential cred : wallet.getCredentials()) {
			if (cred instanceof TrustCredential) {
				TrustCredential trustCred = (TrustCredential) cred;
				if (trustCred.getRootOfTrust().getIssuerType() == IdentityType.USER) {
					_logger.debug("found a user in list: " + trustCred.describe(VerbosityLevel.HIGH));
					allUsersFound.add(trustCred);
				}
			}
		}

		if ((allUsersFound.size() > 1) && (filter == null) && (justChooseFirst != true)) {
			String msg =
				"There are too many USER credentials to determine the owning user, and no filter was provided.";
			_logger.error(msg);
			throw new IOException(msg);
		}

		for (TrustCredential cred : allUsersFound) {
			X509Certificate[] userIdentity = cred.getRootOfTrust().getIssuer();
			String thisDN = X509Identity.getOpensslRdn(userIdentity[0]); 
				//old userIdentity[0].getSubjectX500Principal().toString();
			_logger.debug("found a listed user's DN as: " + thisDN);
			
			// we'll use this guy if there's only one listed, or if the filter is a match.
			if ((allUsersFound.size() == 1) || justChooseFirst || ((filter != null) && (thisDN.contains(filter)))) {
				toReturn.add(thisDN);
				
				// add the TLS identity if it's not just a self-signed throw-away.
				if (cred.getRootOfTrust().getDelegateeType() == IdentityType.CONNECTION) {
					X509Certificate[] tlsIdentity = cred.getRootOfTrust().getDelegatee();
					String tlsDn = X509Identity.getOpensslRdn(tlsIdentity[0]);
					if (tlsIdentity[0].getIssuerDN().equals(tlsIdentity[0].getSubjectDN())) {
						_logger.debug("seeing TLS cert as self-signed and ignoring: " + tlsDn);
					} else {
						_logger.debug("found a non-self-signed TLS cert for that user: " + tlsDn);
						toReturn.add(tlsDn);
					}
				}
				break;
			}

		}

		// found nothing useful. that's not good.
		if (toReturn.size() == 0) {
			_logger.warn("found no suitable USER credential in the list.");
			return null;
		}

		if (_logger.isDebugEnabled()) {
			_logger.debug("Found owners of resource are:");
			for (String dn : toReturn) {
				_logger.debug("\t" + dn);
			}
		}

		return toReturn;
	}

}
