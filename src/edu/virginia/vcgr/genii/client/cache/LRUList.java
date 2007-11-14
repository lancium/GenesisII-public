package edu.virginia.vcgr.genii.client.cache;

public class LRUList<KeyType, DataType> extends CacheList<KeyType, DataType>
{
	public LRUList()
	{
		super(RoleBasedCacheNode.ROLE_LRU);
	}
	
	@Override
	public void insert(RoleBasedCacheNode<KeyType, DataType> node)
	{
		// LRU inserts ALWAYS go at the tail
		if (_tail == null)
		{
			_head = _tail = node;
			return;
		}
		
		_tail.setNext(_myRole, node);
		node.setPrevious(_myRole, _tail);
		_tail = node;
	}
	
	public void noteUse(RoleBasedCacheNode<KeyType, DataType> node)
	{
		remove(node);
		insert(node);
	}
}