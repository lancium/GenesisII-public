package edu.virginia.vcgr.genii.client.cache;

import java.util.Date;
import java.util.HashMap;

public class TimedOutLRUCache2<KeyType, DataType>
{
	private HashMap<KeyType, RoleBasedCacheNode<KeyType, DataType>> _map;
	private LRUList<KeyType, DataType> _lruList;
	private TimeoutList<KeyType, DataType> _timeoutList;
	
	private int _maxElements;
	private long _defaultTimeoutMS;
	
	public TimedOutLRUCache2(int maxElements, long defaultTimeoutMS)
	{
		if (maxElements < 1)
			throw new IllegalArgumentException("\"maxElements\" must be greater than 0.");
		
		_maxElements = maxElements;
		_defaultTimeoutMS = defaultTimeoutMS;
		_map = new HashMap<KeyType, RoleBasedCacheNode<KeyType, DataType>>(_maxElements);
		_lruList = new LRUList<KeyType, DataType>();
		_timeoutList = new TimeoutList<KeyType, DataType>();
	}
	
	public void put(KeyType key, DataType data, long timeoutMS)
	{
		RoleBasedCacheNode<KeyType, DataType> newNode =
			new RoleBasedCacheNode<KeyType, DataType>(key, data, 
				new Date(System.currentTimeMillis() + timeoutMS));
		
		RoleBasedCacheNode<KeyType, DataType> oldNode = _map.remove(key);
		if (oldNode != null)
		{
			_lruList.remove(oldNode);
			_timeoutList.remove(oldNode);
		}
		
		if (_map.size() >= _maxElements)
			clearStale();
		
		while (_map.size() >= _maxElements)
		{
			RoleBasedCacheNode<KeyType, DataType> node = _lruList.removeFirst();
			_timeoutList.remove(node);
			_map.remove(node.getKey());
		}
		
		_map.put(key, newNode);
		_lruList.insert(newNode);
		_timeoutList.insert(newNode);
	}
	
	public void put(KeyType key, DataType data)
	{
		put(key, data, _defaultTimeoutMS);
	}
	
	public DataType get(KeyType key)
	{
		Date now = new Date();
		RoleBasedCacheNode<KeyType, DataType> node = _map.get(key);
		if (node == null)
			return null;
		
		_lruList.remove(node);
		
		if (node.getInvalidationDate().before(now))
		{
			// stale
			_map.remove(key);
			_timeoutList.remove(node);
			return null;
		}
		
		_lruList.insert(node);
		return node.getData();
	}
	
	public void clearStale()
	{
		Date now = new Date();
		
		while (true)
		{
			RoleBasedCacheNode<KeyType, DataType> node = _timeoutList.peekFirst();
			if (node == null)
				break;
			
			if (node.getInvalidationDate().compareTo(now) <= 0)
			{
				_map.remove(node.getKey());
				_timeoutList.removeFirst();
				_lruList.remove(node);
			} else
			{
				break;
			}
		}
	}
	
	public void clear()
	{
		_map.clear();
		_lruList.clear();
		_timeoutList.clear();
	}
}