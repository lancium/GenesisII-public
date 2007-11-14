package edu.virginia.vcgr.genii.client.cache;

import java.util.Date;
import java.util.HashMap;

/**
 * This cache attempt to efficiently handle cached items that may time out after a certain
 * period of time.  It does this by maintaining 3 seperate data structures.  The first is a
 * HashMap which allows for quick access to the cached data based off of the key.  The second
 * is a linked list which maintains the LRU property of the items.  The third is a list ordered
 * by timeout so that items that have timed out can be identified quickly (i.e., a straight
 * traversal of this list).  All data structures share the exact same data node (not equivalent, but
 * identical instances of the node class).  This means that once a node is identified through
 * any of the means indicated (key, LRU property, timeout property), the node for all three
 * data structures has been identified and does not need to be looked up in the others.
 * 
 * @author mmm2a
 *
 * @param <KeyType>
 * @param <DataType>
 */
public class TimedOutLRUCache<KeyType, DataType>
{
	private HashMap<KeyType, RoleBasedCacheNode<KeyType, DataType>> _map;
	private LRUList<KeyType, DataType> _lruList;
	private TimeoutList<KeyType, DataType> _timeoutList;
	
	private int _maxElements;
	private long _defaultTimeoutMS;
	private Thread _activeTimeoutThread = null;
	
	public TimedOutLRUCache(int maxElements, long defaultTimeoutMS)
	{
		if (maxElements < 1)
			throw new IllegalArgumentException("\"maxElements\" must be greater than 0.");
		
		_maxElements = maxElements;
		_defaultTimeoutMS = defaultTimeoutMS;
		_map = new HashMap<KeyType, RoleBasedCacheNode<KeyType, DataType>>(_maxElements);
		_lruList = new LRUList<KeyType, DataType>();
		_timeoutList = new TimeoutList<KeyType, DataType>();
	}
	
	public void activelyTimeoutElements(boolean activelyTimeout)
	{
		synchronized(_map)
		{
			if (_activeTimeoutThread == null)
			{
				if (activelyTimeout)
					startActiveTimeout();
			} else
			{
				if (!activelyTimeout)
					stopActiveTimeout();
			}
		}
	}
	
	public void put(KeyType key, DataType data, long timeoutMS)
	{
		RoleBasedCacheNode<KeyType, DataType> newNode =
			new RoleBasedCacheNode<KeyType, DataType>(key, data, 
				new Date(System.currentTimeMillis() + timeoutMS));
		
		synchronized(_map)
		{
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
			
			_map.notify();
		}
	}
	
	public void put(KeyType key, DataType data)
	{
		put(key, data, _defaultTimeoutMS);
	}
	
	public DataType get(KeyType key)
	{
		Date now = new Date();
		
		synchronized(_map)
		{
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
	}
	
	public void clearStale()
	{
		Date now = new Date();
		
		synchronized(_map)
		{
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
	}
	
	public void remove(KeyType key)
	{
		synchronized(_map)
		{
			RoleBasedCacheNode<KeyType, DataType> node = _map.remove(key);
			if (node != null)
			{
				_lruList.remove(node);
				_timeoutList.remove(node);
			}
		}
	}
	
	public void clear()
	{
		synchronized(_map)
		{
			_map.clear();
			_lruList.clear();
			_timeoutList.clear();
		}
	}
	
	public void startActiveTimeout()
	{
		_activeTimeoutThread = new Thread(
			new ActiveTimeoutWorker(), "Active Cache Timeout Thread");
		_activeTimeoutThread.setDaemon(true);
		_activeTimeoutThread.start();
	}
	
	public void stopActiveTimeout()
	{
		Thread tmp = _activeTimeoutThread;
		_activeTimeoutThread = null;
		synchronized(_map)
		{
			_map.notify();
		}
		
		try { tmp.join(); } catch (InterruptedException cause) {}
	}
	
	private class ActiveTimeoutWorker implements Runnable
	{
		public void run()
		{
			synchronized(_map)
			{
				while (_activeTimeoutThread != null)
				{
					try
					{
						clearStale();
						RoleBasedCacheNode<KeyType, DataType> firstNode = _timeoutList.peekFirst();
						if (firstNode == null)
							_map.wait();
						else
						{
							Date nextStale = firstNode.getInvalidationDate();
							long timeout = nextStale.getTime() - System.currentTimeMillis();
							if (timeout > 0)
								_map.wait(timeout);
						}
					}
					catch (InterruptedException ie)
					{
					}
				}
			}
		}
	}
}