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
 * A cache that attempts to improve runtime performance by re-using previously
 * delegated credential chains. If an issuer has delegated trust to a particular
 * credential before and that was for the same delegatee, then we believe it to
 * be the same credential. We also provide a cache for singleton credentials of
 * one trust credential.
 * 
 * @author ckoeritz
 */
public class CredentialCache {
	static private Log _logger = LogFactory.getLog(CredentialCache.class);

	static public final int CACHE_SIZE = 64;
	static public final long TIMEOUT_MS = 1000L * 60L * 2L; // 2 minute lifetime
															// in cache

	static private TimedOutLRUCache<ChainsCacheKey, TrustCredential> credentialChains = new TimedOutLRUCache<ChainsCacheKey, TrustCredential>(
			CACHE_SIZE, SecurityConstants.CredentialCacheTimeout);

	static private TimedOutLRUCache<SingletonCacheKey, TrustCredential> isolatedCreds = new TimedOutLRUCache<SingletonCacheKey, TrustCredential>(
			CACHE_SIZE, SecurityConstants.CredentialCacheTimeout);

	/**
	 * the key to the cache for delegation chains is a 2-tuple with the guid of
	 * the base credential and the delegatee's x509 certificate. this sloppily
	 * assumes that the restrictions and access would be the same for any
	 * credential chain based on the same guid, because currently they always
	 * are.
	 */
	public static class ChainsCacheKey extends Pair<String, X509Certificate> {
		private static final long serialVersionUID = 1L;

		ChainsCacheKey(String s, X509Certificate c) {
			super(s, c);
		}
	};

	/**
	 * the key for the cache of isolated trust credentials is the x509 of the
	 * delegatee and the issuer certificates. this sloppily assumes that the
	 * restrictions and access would be the same for any credential created for
	 * the delegatee and issuer, because currently they always are.
	 */
	public static class SingletonCacheKey extends
			Pair<X509Certificate, X509Certificate> {
		private static final long serialVersionUID = 1L;

		SingletonCacheKey(X509Certificate delegatee, X509Certificate issuer) {
			super(delegatee, issuer);
		}
	};

	/**
	 * this returns a cached credential that matches the requested trust
	 * delegation, or it creates a new delegation.
	 */
	public static TrustCredential getCachedDelegationChain(
			X509Certificate[] delegatee, IdentityType delegateeType,
			X509Certificate[] issuer, PrivateKey issuerPrivateKey,
			BasicConstraints restrictions,
			EnumSet<RWXCategory> accessCategories, TrustCredential cred) {
		// check the cache to see if the credential we would create exists
		// already.
		synchronized (credentialChains) {
			ChainsCacheKey seek = new ChainsCacheKey(cred.getId(), delegatee[0]);
			TrustCredential delegation = credentialChains.get(seek);
			if (delegation != null) {
				if (_logger.isTraceEnabled())
					_logger.trace("credential chain cache hit--found existing delegation.");
			} else {
				// not in cache: create a new linked trust credential.
				delegation = new TrustCredential(delegatee, delegateeType,
						issuer, cred.getDelegateeType(), restrictions,
						accessCategories);
				delegation.extendTrustChain(cred);
				delegation.signAssertion(issuerPrivateKey);
				if (_logger.isTraceEnabled())
					_logger.trace("credential chain cache miss--created new delegation.");
				credentialChains.put(seek, delegation);
			}

			return delegation;
		}
	}

	/**
	 * this returns a cached credential that matches the requested trust
	 * delegation, or it creates a new delegation.
	 */
	public static TrustCredential getCachedCredential(
			X509Certificate[] delegatee, IdentityType delegateeType,
			X509Certificate[] issuer, PrivateKey issuerPrivateKey,
			BasicConstraints restrictions, EnumSet<RWXCategory> accessCategories) {
		// check the cache to see if the credential we would create exists
		// already.
		synchronized (isolatedCreds) {
			SingletonCacheKey seek = new SingletonCacheKey(delegatee[0],
					issuer[0]);
			TrustCredential delegation = isolatedCreds.get(seek);
			if (delegation != null) {
				if (_logger.isTraceEnabled())
					_logger.trace("singleton credential cache hit--found existing delegation.");
			} else {
				// not in cache: create a new isolated trust credential.
				delegation = new TrustCredential(delegatee,
						IdentityType.CONNECTION, issuer, IdentityType.OTHER,
						restrictions, accessCategories);
				delegation.signAssertion(issuerPrivateKey);
				if (_logger.isTraceEnabled())
					_logger.trace("singleton credential cache miss--created new delegation.");
				isolatedCreds.put(seek, delegation);
			}
			return delegation;
		}
	}
}
