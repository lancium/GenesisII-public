package edu.virginia.vcgr.genii.security.credentials;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.algorithm.structures.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.security.VerbosityLevel;

/**
 * a class that tracks previously seen credentials for a particular session credential.
 */
public class ServerSideStreamliningCredentialCache extends TimedOutLRUCache<String, TimedOutCredentialsCachePerSession>
{
	static private Log _logger = LogFactory.getLog(ServerSideStreamliningCredentialCache.class);

	// hmmm: put the max sessions cached and session lifetime into a config file!

	/*
	 * the most client sessions that we will try to cache.
	 */
	static public int MAXIMUM_SESSIONS_CACHED = 40;
	static public long SESSION_LIFETIME = 1000 * 60 * 5; // 5 minutes.

	// our singleton listing for credentials sent by clients previously, where each client is listed by its x509 tls cert.
	public static ServerSideStreamliningCredentialCache _clientSessionCredentialsCache =
		new ServerSideStreamliningCredentialCache(MAXIMUM_SESSIONS_CACHED, SESSION_LIFETIME);

	static public ServerSideStreamliningCredentialCache getServerSideStreamliningCredentialCache()
	{
		return _clientSessionCredentialsCache;
	}

	private ServerSideStreamliningCredentialCache(int maxElements, long defaultTimeoutMS)
	{
		super(maxElements, defaultTimeoutMS, "server side streamlining credential cache");
	}

	public TimedOutCredentialsCachePerSession get(String sessionCertDN)
	{
		synchronized (this) {
			TimedOutCredentialsCachePerSession toReturn = super.get(sessionCertDN);
			if (toReturn == null) {
				// if the container isn't listed yet, we will list it now.
				_logger.debug("adding creds session cache for session cert: " + sessionCertDN);
				toReturn = new TimedOutCredentialsCachePerSession();
				super.put(sessionCertDN, toReturn);
			} else {
				// it was already there, so keep it fresh.
				super.refresh(sessionCertDN);
			}
			return toReturn;
		}
	}

	/**
	 * adds in the trust credential for the client session if not previously known, or refreshes it.
	 */
	public void addCredentialForClientSession(String cliCertDN, TrustCredential newCred)
	{
		if (!CredentialCache.SERVER_CREDENTIAL_STREAMLINING_ENABLED) {
			return;
		}

		if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
			_logger.debug("adding credential " + newCred.getId() + " for session cert: " + cliCertDN);

		synchronized (_clientSessionCredentialsCache) {
			TimedOutCredentialsCachePerSession sessionCredsCache = _clientSessionCredentialsCache.get(cliCertDN);

			// add the new credential into the list.
			if (sessionCredsCache.get(newCred.getId()) == null) {
				// not yet in the cache, so add it.
				sessionCredsCache.put(newCred.getId(), newCred);
				if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
					_logger.debug("=> added new credential " + newCred.getId() + " to creds cache: " + newCred.describe(VerbosityLevel.LOW));
			} else {
				// already in the cache, so refresh it.
				sessionCredsCache.refresh(newCred.getId());
				if (CredentialCache.SHOW_CREDENTIAL_STREAMLINING_ACTIONS && _logger.isDebugEnabled())
					_logger.debug("=> refreshed credential " + newCred.getId() + " in creds cache: " + newCred.describe(VerbosityLevel.LOW));
			}
		}
	}

}
