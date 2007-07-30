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
package org.morgan.util.cache;

import java.io.Serializable;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public abstract class AbstractCache<KeyType, DataType> implements Serializable
{
	final long serialVersionUID = 0;
	
	static private class Node
	{
		public Object key;
		public Object data;
		
		public Node(Object k, Object d)
		{
			key = k;
			data = d;
		}
	}
	
	private int _entries = 0;
	private Node []_cache;
	
	protected AbstractCache(int numElements)
	{
		if (numElements < 1)
			throw new IllegalArgumentException(
				"numElements parameter must be positive.");
		
		_cache = new Node[numElements];
		for (int lcv = 0; lcv < numElements; lcv++)
			_cache[lcv] = null;
	}
	
	private int index(KeyType key)
	{
		return key.hashCode() % _cache.length;
	}
	
	public int size()
	{
		return _entries;
	}
	
	public boolean containsKey(KeyType key)
	{
		Node n = _cache[index(key)];
		return (n != null && n.key.equals(key));
	}
	
	@SuppressWarnings("unchecked")
	public DataType remove(KeyType key)
	{
		Node n = _cache[index(key)];
		_cache[index(key)] = null;
		return (n != null && n.key.equals(key)) ? (DataType)n.data : null;
	}
	
	@SuppressWarnings("unchecked")
	public DataType get(KeyType key) throws CannotResolveException
	{
		int i = index(key);
		Node n = _cache[i];
		if (n != null && n.key.equals(key))
			return (DataType)n.data;
		
		return (DataType)(_cache[i] = new Node(key, resolveMiss(key))).data;
	}

	protected abstract DataType resolveMiss(KeyType key)
		throws CannotResolveException;
}
