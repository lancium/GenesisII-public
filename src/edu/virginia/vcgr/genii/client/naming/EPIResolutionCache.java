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
package edu.virginia.vcgr.genii.client.naming;


import java.net.URI;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cache.TimedOutLRUCache;

/**
 * The EPIResolutionCache class to maintain a cache of resolved EPRs for EPIs.  The methods
 * are currently all static and this class manages synchronization isssues, etc. 
 * 
 * @author John Karpovich
 */
public class EPIResolutionCache
{
	static private TimedOutLRUCache<URI, EndpointReferenceType>
		_cache = new TimedOutLRUCache<URI, EndpointReferenceType>(
			1024, 1000*60*60*24);
		
	
	static public synchronized EndpointReferenceType get(URI epi)
	{
		return _cache.get(epi);
	}
	
	static public synchronized void put(URI epi, EndpointReferenceType epr)
	{
		_cache.put(epi, epr);
	}
	
	static public synchronized void remove(URI epi)
	{
		_cache.remove(epi);
	}
	
	static public synchronized void badEPR(URI epi, EndpointReferenceType epr)
	{
		if (epi == null)
			return;
		EndpointReferenceType currentEPR = _cache.get(epi);
		if (currentEPR == null)
			return;
		if (currentEPR.equals(epr))
			_cache.remove(epi);
	}
}
