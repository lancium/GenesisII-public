package edu.virginia.vcgr.genii.security.credentials.assertions;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cache.TimedOutLRUCache;

public class SignedCredentialCache
{

	// cache of signed, serialized delegation assertions
	static public final int CACHE_SIZE = 32;
	static public final long TIMEOUT_MILLIS = 1000L * 60L * 60L; // 1 hour lifetime in cache

	static public TimedOutLRUCache<DelegatedAttribute, DelegatedAssertion> delegationAssertions =
			new TimedOutLRUCache<DelegatedAttribute, DelegatedAssertion>(
					CACHE_SIZE, GenesisIIConstants.CredentialCacheTimeout);

	public static DelegatedAssertion getCachedDelegateAssertion(
			DelegatedAttribute delegatedAttribute, PrivateKey privateKey)
			throws GeneralSecurityException
	{

		// check the cache to see if this assertion exists already
		synchronized (delegationAssertions)
		{
			DelegatedAssertion signedAssertion =
					delegationAssertions.get(delegatedAttribute);
			if (signedAssertion == null)
			{
				// not in cache: create a new delegated assertion
				signedAssertion =
						new DelegatedAssertion(delegatedAttribute, privateKey);

				delegationAssertions.put(delegatedAttribute, signedAssertion);
			}

			return signedAssertion;
		}

	}

}
