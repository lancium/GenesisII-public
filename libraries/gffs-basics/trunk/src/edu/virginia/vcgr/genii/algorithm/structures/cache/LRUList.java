package edu.virginia.vcgr.genii.algorithm.structures.cache;

class LRUList<KeyType, DataType> extends CacheList<KeyType, DataType>
{
	public LRUList()
	{
		super(RoleBasedCacheNode.ROLE_LRU);
	}

	@Override
	public synchronized void insert(RoleBasedCacheNode<KeyType, DataType> node)
	{
		// LRU inserts ALWAYS go at the tail
		if (_tail == null) {
			_head = _tail = node;
			return;
		}

		_tail.setNext(_myRole, node);
		node.setPrevious(_myRole, _tail);
		_tail = node;
	}

	public synchronized void noteUse(RoleBasedCacheNode<KeyType, DataType> node)
	{
		remove(node);
		insert(node);
	}
}