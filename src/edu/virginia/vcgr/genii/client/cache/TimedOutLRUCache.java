/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.client.cache;

import java.util.Date;
import java.util.HashMap;

public class TimedOutLRUCache<KeyType, DataType>
{
	static private final long _DEFAULT_TIMEOUT_MS = 1000 * 30;
	
	static protected class Node<KeyType, DataType>
	{
		Node<KeyType, DataType> _lruPrev;
		Node<KeyType, DataType> _lruNext;
		Node<KeyType, DataType> _timeoutPrev;
		Node<KeyType, DataType> _timeoutNext;
		
		Date _invalidationDate;
		KeyType _key;
		DataType _data;
		
		Node(KeyType key, DataType data, long timeoutMS)
		{
			_key = key;
			_data = data;
			_lruPrev = null;
			_lruNext = null;
			_timeoutPrev = null;
			_timeoutNext = null;
			_invalidationDate = new Date(
				new Date().getTime() + timeoutMS);
		}
	}
	
	private int _maxElements;
	private long _defaultTimeoutMS;
	private HashMap<KeyType, Node<KeyType, DataType> > _cacheMap;
	private Node<KeyType, DataType> _lruHead = null;
	private Node<KeyType, DataType> _lruTail = null;
	private Node<KeyType, DataType> _timeoutHead = null;
	private Node<KeyType, DataType> _timeoutTail = null;
	
	protected void noteUse(Node<KeyType, DataType> node)
	{
		// unhook it
		if (node._lruNext != null)
		{
			node._lruNext._lruPrev = node._lruPrev;
			if (node._lruPrev != null)
				node._lruPrev._lruNext = node._lruNext;
			else
				_lruHead = node._lruNext;
		} else
		{
			// It's already at the tail, so leave it alone
			return;
		}
		
		// hook it back in at the tail
		if (_lruTail != null)
			_lruTail._lruNext = node;
		node._lruPrev = _lruTail;
		node._lruNext = null;
		_lruTail = node;
	}
	
	private void insertTimeoutElement(Node<KeyType, DataType> node)
	{
		if (_timeoutTail == null)
		{
			_timeoutTail = _timeoutHead = node;
			node._timeoutNext = null;
			node._timeoutPrev = null;
			return;
		}
		
		if (_timeoutTail._invalidationDate.before(
			node._invalidationDate) || _timeoutTail._invalidationDate.equals(node._invalidationDate))
		{
			node._timeoutPrev = _timeoutTail;
			node._timeoutNext = null;
			_timeoutTail._timeoutNext = node;
			return;
		}
		
		if (_timeoutHead._invalidationDate.after(
			node._invalidationDate) || _timeoutHead._invalidationDate.equals(node._invalidationDate))
		{
			node._timeoutPrev = null;
			node._timeoutNext = _timeoutHead;
			_timeoutHead._timeoutPrev = node;
			return;
		}
		
		Node<KeyType, DataType> preElement = _timeoutTail;
		while (true)
		{
			preElement = preElement._timeoutPrev;
			if (preElement._invalidationDate.before(
				node._invalidationDate))
			{
				node._timeoutNext = preElement._timeoutNext;
				preElement._timeoutNext._timeoutPrev = node;
				node._timeoutPrev = preElement;
				preElement._timeoutNext = node;
				return;
			}
		}
	}
	
	private void makeRoom()
	{
		int removed = 0;
		
		// first, get rid of timed out elements
		Date now = new Date();
		while (_timeoutHead != null && 
			_timeoutHead._invalidationDate.before(now))
		{
			remove(_timeoutHead._key);
			removed++;
		}
		
		if (_timeoutHead == null)
			_timeoutTail = null;
		
		if (removed > 0)
			return;
		
		// nothing timed out, so take the first one off the LRU list.
		if (_lruHead == null)
		{
			// Hmm, nothing here, can't make any room, so we have a
			// problem
			throw new IllegalStateException(
				"Asked to throw out elements from an empty cache.");
		}
		
		remove(_lruHead._key);
	}
	
	public TimedOutLRUCache(int maxElements)
	{
		this(maxElements, _DEFAULT_TIMEOUT_MS);
	}
	
	public TimedOutLRUCache(int maxElements, long defaultTimeoutMS)
	{
		_maxElements = maxElements;
		_defaultTimeoutMS = defaultTimeoutMS;
		
		_cacheMap = new HashMap<KeyType, Node<KeyType, DataType> >(
			_maxElements);
	}
	
	public DataType get(KeyType key)
	{
		Node<KeyType, DataType> node = _cacheMap.get(key);
		if (node == null)
			return null;
		
		Date now = new Date();
		if (node._invalidationDate.before(now))
		{
			remove(key);
			return null;
		}
		
		DataType data = node._data;
		noteUse(node);
		return data;
	}
	
	public void put(KeyType key, DataType data)
	{
		put(key, data, _defaultTimeoutMS);
	}
	
	public void put(KeyType key, DataType data, long timeoutMS)
	{
		if (_cacheMap.containsKey(key))
			remove(key);
		
		while (_cacheMap.size() >= _maxElements)
			makeRoom();
		
		Node<KeyType, DataType> node = new Node<KeyType, DataType>(
			key, data, timeoutMS);
		_cacheMap.put(key, node);
		
		// insert into lru list
		if (_lruTail == null)
		{
			_lruHead = _lruTail = node;
		} else
		{
			_lruTail._lruNext = node;
			node._lruPrev = _lruTail;
			_lruTail = node;
		}
		
		// insert into timeout list
		insertTimeoutElement(node);
	}
	
	public DataType remove(KeyType key)
	{
		Node<KeyType, DataType> node = _cacheMap.remove(key);
		if (node == null)
			return null;
		
		// remove from LRUList
		if (node._lruPrev == null)
		{	// head of list
			_lruHead = node._lruNext;
		} else
		{
			node._lruPrev._lruNext = node._lruNext;
		}
		
		if (node._lruNext == null)
		{ // tail of list
			_lruTail = node._lruPrev;
		} else
		{
			node._lruNext._lruPrev = node._lruPrev;
		}
		
		// remove from timeoutList
		if (node._timeoutPrev == null)
		{	// head of list
			_timeoutHead = node._timeoutNext;
		} else
		{
			node._timeoutPrev._timeoutNext = node._timeoutNext;
		}
		
		if (node._timeoutNext == null)
		{ // tail of list
			_timeoutTail = node._timeoutPrev;
		} else
		{
			node._timeoutNext._timeoutPrev = node._timeoutPrev;
		}
		
		return node._data;
	}
	
	public void clear()
	{
		_cacheMap.clear();
		_lruTail = _lruHead = null;
		_timeoutTail = _timeoutHead = null;
	}
}
