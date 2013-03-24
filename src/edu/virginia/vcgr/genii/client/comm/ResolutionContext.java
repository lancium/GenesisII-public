/*
 * Copyright 2007 University of Virginia
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
package edu.virginia.vcgr.genii.client.comm;

import java.rmi.RemoteException;
import org.ws.addressing.EndpointReferenceType;
import edu.virginia.vcgr.genii.client.naming.ResolverUtils;

public class ResolutionContext
{
	private EndpointReferenceType _origEPR;
	private boolean _triedOriginalEPR;
	private boolean _rebindAllowed;
	private EndpointReferenceType _resolvedEPR;
	private int _knownEndpoints;
	private int _endpointCount;

	public ResolutionContext(EndpointReferenceType origEPR, boolean rebindAllowed)
	{
		_origEPR = origEPR;
		_rebindAllowed = rebindAllowed;
		// We know about the original EPR.
		_knownEndpoints = 1;
	}

	public EndpointReferenceType getOriginalEPR()
	{
		return _origEPR;
	}

	public boolean rebindAllowed()
	{
		return _rebindAllowed;
	}

	public boolean triedOriginalEPR()
	{
		return _triedOriginalEPR;
	}

	public void setTriedOriginalEPR()
	{
		_triedOriginalEPR = true;
	}

	public EndpointReferenceType resolve() throws RemoteException
	{
		// If this is the first call to resolve(), then resolve the original EPR.
		if (_resolvedEPR == null) {
			_resolvedEPR = ResolverUtils.resolve(_origEPR);
			_knownEndpoints++;
			return _resolvedEPR;
		}
		// The client has already tried origEPR and resolvedEPR, and neither worked.
		// Are there more endpoints to resolve?
		if (_endpointCount == 0) {
			_endpointCount = ResolverUtils.getEndpointCount(_origEPR);
		}
		if (_knownEndpoints < _endpointCount) {
			_resolvedEPR = ResolverUtils.resolve(_resolvedEPR);
			_knownEndpoints++;
			return _resolvedEPR;
		}
		// There are no more endpoints to resolve.
		return null;
	}
}
