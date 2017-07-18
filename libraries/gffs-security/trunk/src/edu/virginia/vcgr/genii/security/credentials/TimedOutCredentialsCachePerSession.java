package edu.virginia.vcgr.genii.security.credentials;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.algorithm.structures.cache.TimedOutLRUCache;

/**
 * tracks our credentials similarly to a credential wallet, but can time out the individual items and remove them. this is important for
 * handling a client's credential changes when they're still using the same tls session; any new credentials they have will get added to the
 * list but the old ones will time out and be removed. the list is indexed by the GUID of the trust credential object.
 */
public class TimedOutCredentialsCachePerSession extends TimedOutLRUCache<String, TrustCredential>
{
	private static Log _logger = LogFactory.getLog(TimedOutCredentialsCachePerSession.class);

	// hmmm: make these configurable values from a config file!

	/*
	 * this is the limit for the number of credentials that the container remembers coming from the client. it should be tuned to be a bit
	 * larger and to last a bit longer than what the client might expect (in client side tracking of what it told the container).
	 */
	static public int MAXIMUM_CREDENTIALS_CACHED = 200;

	/*
	 * how long each credential is kept around before timing out of the cache. we use the client side's default tracking lifetime plus an
	 * additional few seconds.
	 */
	static public long CREDENTIAL_LIFETIME = ClientCredentialTracker.CONTAINER_MEMORY_GUESSTIMATE + (1000 * 60 * 15); // 15 minutes
	// hmmm: should come from config file, but also be checked for rational choice against the client cred tracker's config.

	public TimedOutCredentialsCachePerSession()
	{
		super(MAXIMUM_CREDENTIALS_CACHED, CREDENTIAL_LIFETIME, "per-session timed out credentials cache");
	}

	/**
	 * specialized get method will not only find the credential id at the top level, but it will also search into prior delegations to find
	 * the credential.
	 */
	public TrustCredential get(String credId)
	{
		synchronized (this) {
			// search for top-level first.
			TrustCredential simple = super.get(credId);
			if (simple != null) {
				if (_logger.isTraceEnabled())
					_logger.debug("found as most delegated top-level credential: " + credId);
				super.refresh(credId);
				return simple;
			}
			// well, now we need to search any prior delegations also.
			for (String curr : super.keySet()) {
				TrustCredential isThisIt = super.get(curr);
				String mostDelegatedId = null;
				// save the most delegated identity for later since this will be the cred id listed.
				if (isThisIt != null) {
					mostDelegatedId = isThisIt.getId();
				} else {
					if (_logger.isDebugEnabled())
						_logger.debug("credential missing now: " + curr);
				}
				while (isThisIt != null) {
					if (isThisIt.getId().equals(credId)) {
						if (_logger.isTraceEnabled())
							_logger.debug("found as previously delegated credential: " + credId);
						super.refresh(mostDelegatedId);
						return isThisIt;
					} else {
						// walk back along the chain. at some point this must be null.
						isThisIt = isThisIt.getPriorDelegation();
						if (isThisIt != null) {
							if (_logger.isTraceEnabled())
								_logger.debug("walking back in chain to see if prior cred is right one.");
						}
					}
				}
			}
		}
		return null;
	}

}
