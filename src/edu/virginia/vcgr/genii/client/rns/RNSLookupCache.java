package edu.virginia.vcgr.genii.client.rns;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.algorithm.structures.cache.TimedOutLRUCache;

/**
 * This singleton class is a cache of pathname -> EPR mappings in the RNS namespace. This cache is
 * read by RNSPath.lookup(). Typically, entries are added to the cache by RNSPath.lookup(), but any
 * client component is free to add and delete entries.
 */
public class RNSLookupCache
{
	/**
	 * Singleton instance.
	 */
	private static TimedOutLRUCache<String, RNSPath> _lookupCache = new TimedOutLRUCache<String, RNSPath>(128, 1000L * 48);

	/**
	 * Singleton methods.
	 */
	public static synchronized RNSPath get(String key)
	{
		return _lookupCache.get(key);
	}

	public static synchronized void put(String key, RNSPath value)
	{
		_lookupCache.put(key, value);
	}

	public static synchronized void put(String parent, String name, EndpointReferenceType epr)
	{
		RNSPath parentRNS = _lookupCache.get(parent);
		if (parentRNS != null) {
			RNSPath childRNS = new RNSPath(parentRNS, name, epr, true);
			_lookupCache.put(childRNS.pwd(), childRNS);
		}
	}

	public static synchronized void remove(String key)
	{
		Set<String> keySet = _lookupCache.keySet();
		List<String> rmList = new ArrayList<String>();
		for (String candidateKey : keySet) {
			if (candidateKey.startsWith(key))
				rmList.add(candidateKey);
		}
		for (String candidateKey : rmList) {
			_lookupCache.remove(candidateKey);
		}
	}

	public static synchronized void clear()
	{
		_lookupCache.clear();
	}
}
