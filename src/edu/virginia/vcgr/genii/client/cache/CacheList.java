package edu.virginia.vcgr.genii.client.cache;

public abstract class CacheList<KeyType, DataType>
{
	protected int _myRole;
	
	protected RoleBasedCacheNode<KeyType, DataType> _head;
	protected RoleBasedCacheNode<KeyType, DataType> _tail;
	
	protected CacheList(int role)
	{
		_myRole = role;
		
		_head = _tail = null;
	}
	
	public abstract void insert(RoleBasedCacheNode<KeyType, DataType> node);

	public RoleBasedCacheNode<KeyType, DataType> removeFirst()
	{
		if (_head == null)
			return null;
		
		RoleBasedCacheNode<KeyType, DataType> ret = _head;
		
		_head = _head.getNext(_myRole);
		if (_head != null)
			_head.setPrevious(_myRole, null);
		else
			_tail = null;
		
		ret.clearLinks(_myRole);
		return ret;
	}
	
	public RoleBasedCacheNode<KeyType, DataType> peekFirst()
	{
		if (_head == null)
			return null;
		
		return _head;
	}
	
	public void remove(RoleBasedCacheNode<KeyType, DataType> node)
	{
		if (node.getPrevious(_myRole) == null) // At the head of the list
			_head = node.getNext(_myRole);
		else
			node.getPrevious(_myRole).setNext(_myRole, node.getNext(_myRole));
		
		if (node.getNext(_myRole) == null) // At the tail of the list
			_tail = node.getPrevious(_myRole);
		else
			node.getNext(_myRole).setPrevious(_myRole, node.getPrevious(_myRole));
		
		node.clearLinks(_myRole);
	}
	
	public void clear()
	{
		_head = null;
		_tail = null;
	}
}