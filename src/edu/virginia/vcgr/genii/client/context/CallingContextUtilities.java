package edu.virginia.vcgr.genii.client.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.security.SAMLConstants;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;

final public class CallingContextUtilities
{
	private static Log _logger = LogFactory.getLog(CallingContextUtilities.class);

	static public ICallingContext setupCallingContextAfterCombinedExtraction(ICallingContext context)
	{
		WorkingContext workingContext = null;
		try {
			workingContext = WorkingContext.getCurrentWorkingContext();
		} catch (ContextException e) {
			_logger.error("failed to retrieve working context");
		}
		CredentialWallet creds = null;
		if (workingContext != null) {
			creds = (CredentialWallet) workingContext
				.getProperty(SAMLConstants.SAML_CREDENTIALS_WORKING_CONTEXT_CREDS_PROPERTY_NAME);
		}
		ArrayList<NuCredential> callerCredentials = new ArrayList<NuCredential>();
		if (creds != null) {
			Collection<TrustCredential> assertions = creds.getCredentials();

			// Deserialize the encoded caller-credentials and add them
			// to a "caller-only" cred-set: they will be added to the transient
			// cred-set later (along with any other creds conveyed outside the
			// calling-context) during security-processing.
			if (assertions != null) {
				Iterator<TrustCredential> itr = assertions.iterator();
				while (itr.hasNext()) {
					NuCredential cred = (NuCredential) itr.next();
					if (_logger.isDebugEnabled())
						_logger.debug("found credential in context: " + cred.toString());
					callerCredentials.add(cred);
				}
			} else {
				_logger.warn("got no credentials from calling context.");
			}
		}
		context.setTransientProperty(SAMLConstants.CALLER_CREDENTIALS_PROPERTY, callerCredentials);

		return context;
	}

	/*
	 * This method augments any new trust delegation retrieved from some RPC exchange in the calling
	 * context of the current GRID user.
	 */
	public static void updateCallingContext(TrustCredential assertion) throws Exception
	{
		// get the calling context (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext();
		if (callContext == null) {
			callContext = new CallingContextImpl(new ContextType());
			ContextManager.storeCurrentContext(callContext);
		}

		// retrieve the credentials wallet from the context and update it
		CredentialWallet wallet = (CredentialWallet) callContext
			.getTransientProperty(SAMLConstants.SAML_CREDENTIALS_WALLET_PROPERTY_NAME);
		if (wallet == null) {
			wallet = new CredentialWallet();
		}
		wallet.addCredential(assertion);

		// restore the credentials wallet in the calling context
		callContext.setTransientProperty(SAMLConstants.SAML_CREDENTIALS_WALLET_PROPERTY_NAME, wallet);
	}
}
