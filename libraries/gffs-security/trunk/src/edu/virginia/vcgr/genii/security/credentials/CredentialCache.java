package edu.virginia.vcgr.genii.security.credentials;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.EnumSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;

import edu.virginia.vcgr.genii.algorithm.structures.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.identity.IdentityType;

/**
 * A cache that attempts to improve runtime performance by re-using previously delegated credential chains. If an issuer has delegated trust
 * to a particular credential before and that was for the same delegatee, then we believe it to be the same credential. We also provide a
 * cache for singleton credentials of one trust credential.
 * 
 * @author ckoeritz
 */
public class CredentialCache
{
	static private Log _logger = LogFactory.getLog(CredentialCache.class);

	static public final int CRED_CACHE_SIZE = 200;
	static public final long CRED_CACHE_TIMEOUT_MS = 1000L * 60L * 10L; // 10 minute lifetime in cache

	// hmmm: move these selector booleans out to a better place, maybe even genesis ii constants? ugh, no, but someplace else.

	/*
	 * this flag must be CHANGED at startup time if the client supports credential streamlining. the security code is lower level than the
	 * client properties file, so we need to track the feature like this.
	 * 
	 * note that the default should stay false here! it will automatically be updated from configuration files.
	 */
	public static boolean CLIENT_CREDENTIAL_STREAMLINING_ENABLED = false; // default should be left as false.

	/*
	 * this flag also must be changed at startup time to reflect the configured value. the default is to be disabled, and this default should
	 * be left defined as false. the configuration for whether it's enabled or not is read at container startup time.
	 */
	public static boolean SERVER_CREDENTIAL_STREAMLINING_ENABLED = false; // default should be left as false.

	/*
	 * if this flag is enabled, then the container will process everything normally for credential streamlining, but it will never look up
	 * anything in the side cache, nor will it resolve any of the credential references passed in the soap header. this has the effect of
	 * making the container cause the client to fault on every request where the client thought streamlining was enabled. it is for debugging
	 * only!
	 */
	public static boolean BEHAVE_IRRATIONALLY_WITH_STREAMLINING = false;

	/*
	 * if true, turns on the more comprehensive logging for credential streamlining actions, but still at debug level.
	 */
	public static boolean SHOW_CREDENTIAL_STREAMLINING_ACTIONS = false;

	// cache of credential chains for reusing previously signed delegations.
	static private TimedOutLRUCache<ChainsCacheKey, TrustCredential> credentialChains =
		new TimedOutLRUCache<ChainsCacheKey, TrustCredential>(CRED_CACHE_SIZE, SecurityConstants.CredentialCacheTimeout, "credential chains cache");

	/**
	 * the key to the cache for delegation chains is a 2-tuple with the guid of the base credential and the delegatee's x509 certificate DN.
	 * this sloppily assumes that the restrictions and access would be the same for any credential chain based on the same guid, because
	 * currently they always are.
	 */
	public static class ChainsCacheKey extends Pair<String, String>
	{
		private static final long serialVersionUID = 1L;

		ChainsCacheKey(String s, String c)
		{
			super(s, c);
		}
	};

	/**
	 * clears all memory of any credentials we had seen to this point.
	 */
	public static void flushCredentialCaches()
	{
		synchronized (credentialChains) {
			credentialChains.clear();
		}
		ClientCredentialTracker.flushEntireTracker();
	}

	/**
	 * the key for the cache of isolated trust credentials is the x509 of the delegatee and the issuer certificates. this sloppily assumes
	 * that the restrictions and access would be the same for any credential created for the delegatee and issuer, because currently they
	 * always are.
	 */
//	public static class SingletonCacheKey extends Pair<String, String>
//	{
//		private static final long serialVersionUID = 1L;
//
//		SingletonCacheKey(String delegatee, String issuer)
//		{
//			super(delegatee, issuer);
//		}
//	};

	/**
	 * this returns a cached credential that matches the requested trust delegation, or it creates a new delegation.
	 */
	public static TrustCredential getCachedDelegationChain(X509Certificate[] delegatee, IdentityType delegateeType, X509Certificate[] issuer,
		PrivateKey issuerPrivateKey, BasicConstraints restrictions, EnumSet<RWXCategory> accessCategories, TrustCredential cred)
	{
		// check the cache to see if the credential we would create exists already.
		synchronized (credentialChains) {
			ChainsCacheKey seek = new ChainsCacheKey(cred.getId(), delegatee[0].getSubjectDN().toString());
			TrustCredential delegation = credentialChains.get(seek);
			if (delegation != null) {
				if (_logger.isTraceEnabled())
					_logger.trace("credential chain cache hit--found existing delegation.");
				// refresh the credential since we are using it just now.
				credentialChains.refresh(seek);
			} else {
				// not in cache: create a new linked trust credential.
				delegation = new TrustCredential(delegatee, delegateeType, issuer, cred.getDelegateeType(), restrictions, accessCategories);
				delegation.extendTrustChain(cred);
				delegation.signAssertion(issuerPrivateKey);
				if (_logger.isTraceEnabled())
					_logger.trace("credential chain cache miss--created new delegation.");
				credentialChains.put(seek, delegation);

				boolean paranoidChecking = false;
				if (paranoidChecking) {
					boolean worked = TrustCredential.paranoidSerializationCheck(delegation);
					if (!worked) {
						_logger.error("failed paranoid serialization check!  see logging in prior lines.");
					}
				}
			}

			return delegation;
		}
	}

	//hmmm: could move this method to someplace else; it's no longer involved in caching.
	/**
	 * this creates a new delegated credential for the delegatee and issuer.
	 */
	public static TrustCredential generateCredential(X509Certificate[] delegatee, IdentityType delegateeType, X509Certificate[] issuer,
		PrivateKey issuerPrivateKey, BasicConstraints restrictions, EnumSet<RWXCategory> accessCategories)
	{
		// create a new isolated trust credential.
		TrustCredential delegation =
			new TrustCredential(delegatee, IdentityType.CONNECTION, issuer, IdentityType.OTHER, restrictions, accessCategories);
		delegation.signAssertion(issuerPrivateKey);
		if (_logger.isTraceEnabled())
			_logger.trace("singleton credential cache miss--created new delegation.");
		return delegation;
	}
}
