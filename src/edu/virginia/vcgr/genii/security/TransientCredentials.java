package edu.virginia.vcgr.genii.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.algorithm.application.ProgramTools;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.identity.Identity;

/**
 * Class for holding and managing a set of "outgoing" credentials within the current calling
 * context. This is a combination of all authenticated certificate chains, caller credentials and
 * credentials for the target.
 * 
 * @author dmerrill
 * 
 */
public class TransientCredentials implements Serializable
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(TransientCredentials.class);

	private static final String TRANSIENT_CRED_PROP_NAME = "genii.client.security.transient-cred";

	private ArrayList<NuCredential> _credentials = new ArrayList<NuCredential>();

	public ArrayList<NuCredential> getCredentials()
	{
		return _credentials;
	}

	public void add(NuCredential cred)
	{
		if (cred == null) {
			_logger.error("credential being passed in is null!  ignoring it.");
			return;
		}
		if (!(cred instanceof TrustCredential)) {
			if (cred instanceof Identity) {
				if (cred instanceof X509Identity) {
					if (_logger.isDebugEnabled())
						_logger.debug("ignoring bare x509 identity.");
					return;
				}
				// this is probably okay; may be username/password identity or x509 identity.
			} else {
				if (_logger.isDebugEnabled())
					_logger.debug("skipping unknown type of credential!: " + cred.toString());
				return;
			}
		}
		if (_logger.isTraceEnabled())
			_logger.trace("storing cred into transient set: " + cred.toString() + " via " + ProgramTools.showLastFewOnStack(6));
		_credentials.add(cred);
	}

	public void addAll(Collection<NuCredential> newCreds)
	{
		// we intentionally invoke our own function, rather than addAll on the list, in order to
		// check on things.
		for (NuCredential cred : newCreds)
			add(cred);
	}

	public void remove(NuCredential cred)
	{
		_credentials.remove(cred);
	}

	public boolean isEmpty()
	{
		return _credentials.isEmpty();
	}

	/**
	 * Retrieves the credentials from the calling context. Guaranteed to not be null (may be empty,
	 * however)
	 * 
	 * @param callingContext
	 * @return
	 */
	public static synchronized TransientCredentials getTransientCredentials(ICallingContext callingContext)
	{
		TransientCredentials retval = (TransientCredentials) callingContext
			.getTransientProperty(TransientCredentials.TRANSIENT_CRED_PROP_NAME);
		if (retval == null) {
			retval = new TransientCredentials();
			callingContext.setTransientProperty(TransientCredentials.TRANSIENT_CRED_PROP_NAME, retval);
		}
		return retval;
	}

	public static synchronized void globalLogout(ICallingContext callingContext)
	{
		callingContext.removeTransientProperty(TransientCredentials.TRANSIENT_CRED_PROP_NAME);
		if (_logger.isDebugEnabled())
			_logger.debug("Clearing current calling context credentials.");
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		for (NuCredential cred : _credentials) {
			if (builder.length() != 0)
				builder.append("\n");
			builder.append(cred.toString());
		}

		return builder.toString();
	}
}
