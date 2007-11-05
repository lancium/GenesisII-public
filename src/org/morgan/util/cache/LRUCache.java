package org.morgan.util.cache;

import java.util.HashMap;

public class LRUCache<KeyType, DataType>
	extends AbstractCache<KeyType, DataType>
{
	private ICacheMissResolver<KeyType, DataType> _resolver;
	private ICacheDataVerifier<KeyType, DataType> _verifier;
	
	static private class DataTypeNode<KeyType, DataType>
	{
		private KeyType _key;
		private DataType _data;
		
		private DataTypeNode<KeyType, DataType> _previous;
		private DataTypeNode<KeyType, DataType> _next;
		
		public DataTypeNode(KeyType key, DataType data,
			DataTypeNode<KeyType, DataType> previous,
			DataTypeNode<KeyType, DataType> next)
		{
			_key = key;
			_data = data;
			_previous = previous;
			_next = next;
		}
		
		public DataTypeNode(KeyType key, DataType data)
		{
			this(key, data, null, null);
		}
		
		public KeyType getKey()
		{
			return _key;
		}
		
		public DataType getData()
		{
			return _data;
		}
		
		public void previous(DataTypeNode<KeyType, DataType> node)
		{
			_previous = node;
		}
		
		public DataTypeNode<KeyType, DataType> previous()
		{
			return _previous;
		}
		
		public void next(DataTypeNode<KeyType, DataType> node)
		{
			_next = node;
		}
		
		public DataTypeNode<KeyType, DataType> next()
		{
			return _next;
		}
	}
	
	private int _capacity;
	private HashMap<KeyType, DataTypeNode<KeyType, DataType>> _table;
	private DataTypeNode<KeyType, DataType> _head;
	private DataTypeNode<KeyType, DataType> _tail;
	
	public LRUCache(int capacity,
		ICacheMissResolver<KeyType, DataType> resolver,
		ICacheDataVerifier<KeyType, DataType> verifier)
	{
		if (resolver == null)
			throw new IllegalArgumentException(
				"\"resolver\" must be non-null.");
		if (verifier == null)
			throw new IllegalArgumentException(
				"\"verifier\" must be non-null.");
		if (capacity < 8)
			throw new IllegalArgumentException(
				"\"capacity\" must be at least 8 elements.");
		
		_resolver = resolver;
		_verifier = verifier;
		
		_capacity = capacity;
		_table = new HashMap<KeyType, DataTypeNode<KeyType,DataType>>(
			capacity);
		_head = _tail = null;
	}
	
	@Override
	protected DataType miss(KeyType key) throws CacheMissException
	{
		DataType ret;
		DataTypeNode<KeyType, DataType> node;
		try
		{
			ret = _resolver.resolve(key);
			while (_table.size() >= _capacity)
			{
				// throw out lru item
				node = _head;
				_head = _head.next();
				_head.previous(null);
				_table.remove(node.getKey());
			}
			
			node = new DataTypeNode<KeyType, DataType>(key, ret);
			_table.put(key, node);
			
			if (_head == null)
			{
				_head = _tail = node;
			} else
			{
				_tail.next(node);
				node.previous(_tail);
				_tail = node;
			}
			
			return ret;
		}
		catch (CacheMissException cme)
		{
			throw cme;
		}
		catch (Throwable cause)
		{
			throw new CacheMissException(cause);
		}
	}

	@Override
	protected DataType tableLookup(KeyType key)
	{
		DataTypeNode<KeyType, DataType> node;
		
		node = _table.get(key);
		if (node == null)
			return null;

		if (!_verifier.isValid(key, node.getData()))
		{
			_table.remove(key);
			
			if (_head == node)
				_head = node.next();
			if (_tail == node)
				_tail = node.previous();
			if (node.next() != null)
				node.next().previous(node.previous());
			if (node.previous() != null)
				node.previous().next(node.next());
			
			return null;
		}
		
		if (_tail != node)
		{
			if (_head == node)
			{
				_head = node.next();
				node.next().previous(null);
			} else
			{
				node.next().previous(node.previous());
				node.previous().next(node.next());
			}
			
			node.next(null);
			node.previous(_tail);
			_tail.next(node);
			_tail = node;
		}
		
		return node.getData();
	}

	@Override
	public void clear()
	{
		_table.clear();

		_head = _tail = null;
	}
}