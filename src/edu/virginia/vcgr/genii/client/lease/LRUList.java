package edu.virginia.vcgr.genii.client.lease;

import java.util.NoSuchElementException;

/**
 * This special LRU list is designed to make maintaining an LRU
 * relationship constant time.  Other than that, it is a relatively
 * straight forward doubly-linked list.
 * 
 * @author mmm2a
 *
 * @param <Type>
 */
public class LRUList<Type extends LRUList.LRUNode>
{
	/**
	 * The node type of the linked list.
	 * 
	 * @author mmm2a
	 */
	static public class LRUNode
	{
		private LRUNode _next = null;
		private LRUNode _previous = null;
	}
	
	private int _size = 0;
	private LRUNode _head = null;
	private LRUNode _tail = null;
	
	/**
	 * Add a new element to the list.  This will append the element to the tail.
	 * @param d The element to add.
	 */
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
	
	/**
	 * Remove a specific element from the list.
	 * @param d The element to remove.
	 */
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
	
	/**
	 * Note the use of some element in the list (moving that element to the
	 * tail so that it is the most recently used).
	 * @param d The element to "use".
	 */
	public void noteUse(Type d)
	{
		remove(d);
		add(d);
	}
	
	/**
	 * Tests whether or not the list is empty.
	 * @return True if the list is empty, false otherwise.
	 */
	public boolean isEmpty()
	{
		return _head == null;
	}
	
	/**
	 * Returns the size of this list (how many elements are in it).
	 * 
	 * @return The number of elements currently in the list.
	 */
	public int size()
	{
		return _size;
	}
	
	/**
	 * Remove and return the first (or least recently used) item from the
	 * list.
	 * 
	 * @return The removed (least recently used) item from the list.
	 */
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