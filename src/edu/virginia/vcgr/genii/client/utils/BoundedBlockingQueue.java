package edu.virginia.vcgr.genii.client.utils;

import java.util.LinkedList;
import java.util.Queue;

public class BoundedBlockingQueue<Type>
{
	private Queue<Type> _elements;
	private int _capacity;
	
	public BoundedBlockingQueue(Queue<Type> queueInstance, int capacity)
	{
		if (capacity <= 0)
			throw new IllegalArgumentException("Capacity parameter must be positive.");
		
		_elements = queueInstance;
		_capacity = capacity;
	}
	
	public BoundedBlockingQueue(int capacity)
	{
		this(new LinkedList<Type>(), capacity);
	}
	
	public boolean isEmpty()
	{
		synchronized(_elements)
		{
			return _elements.isEmpty();
		}
	}
	
	public int size()
	{
		synchronized(_elements)
		{
			return _elements.size();
		}
	}
	
	public Type peek()
	{
		synchronized(_elements)
		{
			return _elements.peek();
		}
	}
	
	public Type remove() throws InterruptedException
	{
		synchronized(_elements)
		{
			while (_elements.isEmpty())
			{
				_elements.wait();
			}
			
			_elements.notify();
			return _elements.remove();
		}
	}
	 
	public void add(Type type) throws InterruptedException
	{
		synchronized(_elements)
		{
			while (_elements.size() >= _capacity)
			{
				_elements.wait();
			}
			
			_elements.notify();
			_elements.add(type);
		}
	}
	
	public int getCapacity()
	{
		return _capacity;
	}
}