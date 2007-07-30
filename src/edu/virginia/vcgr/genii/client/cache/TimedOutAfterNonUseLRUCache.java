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

public class TimedOutAfterNonUseLRUCache<KeyType, DataType> extends TimedOutLRUCache<KeyType, DataType>
{
	static private final long _DEFAULT_NON_USE_TIMEOUT_MS = 1000 * 60 * 60 * 24; /* 1 day */
	
	private long _defaultTimeoutNonUseMS;
	
	protected void noteUse(Node<KeyType, DataType> node)
	{
		KeyType key = node._key;
		DataType data = node._data;
		remove(key);
		put(key, data, _defaultTimeoutNonUseMS);
	}
	
	public TimedOutAfterNonUseLRUCache(int maxElements)
	{
		super(maxElements);
		_defaultTimeoutNonUseMS = _DEFAULT_NON_USE_TIMEOUT_MS;
	}
	
	public TimedOutAfterNonUseLRUCache(int maxElements, long defaultTimeoutMS)
	{
		this(maxElements, defaultTimeoutMS, _DEFAULT_NON_USE_TIMEOUT_MS);
	}

	public TimedOutAfterNonUseLRUCache(int maxElements, long defaultTimeoutMS, long defaultTimeoutNonUseMS)
	{
		super(maxElements, defaultTimeoutMS);
		_defaultTimeoutNonUseMS = defaultTimeoutNonUseMS;
	}
	
	public void setDefaultTimeoutNonUse(long defaultTimeoutNonUseMS)
	{
		_defaultTimeoutNonUseMS = defaultTimeoutNonUseMS;
	}
}
