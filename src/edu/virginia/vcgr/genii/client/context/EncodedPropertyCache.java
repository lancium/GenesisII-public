package edu.virginia.vcgr.genii.client.context;

import java.io.Serializable;

import edu.virginia.vcgr.genii.client.cache.LRUCache;

/**
 * Each time that the client sends any request to any resource,
 * the client serializes each property in the calling context,
 * so it can include the calling context in the request.
 * 
 * Since each property gets serialized a lot,
 * we cache the serialized objects.
 */
public class EncodedPropertyCache
{
	static protected LRUCache<Serializable, String> cache =
		new LRUCache<Serializable, String>(64);	

	static public void clear()
	{
		cache.clear();
	}
}
