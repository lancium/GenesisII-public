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

/**
 * A simple cache that knows how to resolve misses.
 *
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class AutoResolvingCache<KeyType, DataType> extends
		AbstractCache<KeyType, DataType>
{
	static final long serialVersionUID = 0;
	
	private IMissResolver<KeyType, DataType> _resolver;
	
	public AutoResolvingCache(IMissResolver<KeyType, DataType> resolver,
		int cacheSize)
	{
		super(cacheSize);
		
		_resolver = resolver;
	}
	
	@Override
	protected DataType resolveMiss(KeyType key) throws CannotResolveException
	{
		return _resolver.resolveMiss(key);
	}
}
