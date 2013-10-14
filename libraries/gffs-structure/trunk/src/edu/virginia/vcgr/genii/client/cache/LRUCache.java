package edu.virginia.vcgr.genii.client.cache;

import java.util.*;

/**
 * Simple LRU cache
 * 
 * @author dgm4d
 */
public class LRUCache<KeyType, DataType> extends LinkedHashMap<KeyType, DataType>
{

	private static final long serialVersionUID = 3801124242820219131L;

	/** The max size of the lru cache **/
	protected int _maxElements = 0;

	public LRUCache(int maxElements)
	{

		super(maxElements, (float) 0.75, true);

		_maxElements = maxElements;
	}

	/**
	 * Returns <tt>true</tt> if this map should remove its eldest entry. This method is invoked by
	 * <tt>put</tt> and <tt>putAll</tt> after inserting a new entry into the map. It provides the
	 * implementer with the opportunity to remove the eldest entry each time a new one is added.
	 */
	protected boolean removeEldestEntry(Map.Entry<KeyType, DataType> eldest)
	{
		return size() > _maxElements;
	}

}
