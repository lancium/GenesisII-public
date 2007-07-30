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

package edu.virginia.vcgr.genii.container.resolver;

import org.ws.addressing.EndpointReferenceType;

public class Resolution
{
	private EndpointReferenceType _resolvedTargetEPR;
	private EndpointReferenceType _resolverEPR;
	
	public Resolution(EndpointReferenceType resolvedTargetEPR, EndpointReferenceType resolverEPR)
	{
		_resolvedTargetEPR = resolvedTargetEPR;
		_resolverEPR = resolverEPR;
	}

	public EndpointReferenceType getResolvedTargetEPR()
	{
		return _resolvedTargetEPR;
	}
	
	public EndpointReferenceType getResolverEPR()
	{
		return _resolverEPR;
	}
}
