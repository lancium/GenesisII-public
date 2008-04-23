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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import java.net.URI;

public class ResolverDescription
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(ResolverDescription.class);
	static public enum ResolverType { REFERENCE_RESOLVER, EPI_RESOLVER };
	
	private EndpointReferenceType _epr;
	private URI _epi ;
	private ResolverType _type;
	
	public ResolverDescription(
			URI epi, 
			EndpointReferenceType epr, 
			ResolverType type)
	{
		_epr = epr;
		_type = type;
		_epi = epi;
	}
	
	public EndpointReferenceType getEPR()
	{
		return _epr;
	}
	
	public URI getEPI()
	{
		return _epi;
	}

	public ResolverType getType()
	{
		return _type;
	}
	
	public boolean equals(ResolverDescription other)
	{
		if (_epr != null && other.getEPR() != null && _epr.equals(other.getEPR()) &&
				_type != null && other.getType() != null && _type.equals(other.getType()))
			return true;
		return false;
	}
	
	public boolean equals(Object other)
	{
		if (!(other instanceof ResolverDescription))
			return false;
		
		return equals((ResolverDescription)other);
	}
}