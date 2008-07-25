package edu.virginia.vcgr.genii.client.lease;

import java.util.NoSuchElementException;

public class LRUList<Type extends LRUList.LRUNode>
{
	static public class LRUNode
	{
		private LRUNode _next = null;
		private LRUNode _previous = null;
	}
	
	private int _size = 0;
	private LRUNode _head = null;
	private LRUNode _tail = null;
	
	public void add(Type d)
	{
		if (_tail == null)
		{
			_head = _tail = d;
			d._next = d._previous = null;
		} else
		{
			_tail._next = d;
			d._previous = _tail;
			_tail = d;
			d._next = null;
		}
		
		_size++;
	}
	
	public void remove(Type d)
	{
		if (d._next == null)
			_tail = d._previous;
		else
			d._next._previous = d._previous;
		
		if (d._previous == null)
			_head = d._next;
		else
			d._previous._next = d._next;
		
		d._next = null;
		d._previous = null;
		_size--;
	}
	
	public void noteUse(Type d)
	{
		remove(d);
		add(d);
	}
	
	public boolean isEmpty()
	{
		return _head == null;
	}
	
	public int size()
	{
		return _size;
	}
	
	@SuppressWarnings("unchecked")
	public Type pop()
	{
		if (_head == null)
			throw new NoSuchElementException("LRUList is empty.");
		
		Type ret = (Type)_head;
		remove(ret);
		return ret;
	}
}