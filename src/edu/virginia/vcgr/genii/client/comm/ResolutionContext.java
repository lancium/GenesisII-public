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

import org.apache.axis.types.URI;
import java.util.ListIterator;

import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.WSName;

public class ResolutionContext
{
	private EndpointReferenceType _origEPR = null;
	private WSName _origName = null;
	private URI _epi = null;
	private ListIterator<ResolverDescription> _resolversIter = null;
	private boolean _triedCache = false;
	private boolean _triedOriginalEPR = false;
	private Throwable _errorToReport = null;
	private boolean _rebindAllowed;
	
	public ResolutionContext(EndpointReferenceType origEPR, boolean rebindAllowed)
	{
		_origEPR = origEPR;
		_origName = new WSName(_origEPR);
		_epi = _origName.getEndpointIdentifier();
		_resolversIter = _origName.getResolvers().listIterator();
		_errorToReport = null;
		_rebindAllowed = rebindAllowed;
	}
	
	public AttributedURIType getOriginalAddress()
	{
		return _origName.getEndpoint().getAddress();
	}
	
	public boolean rebindAllowed()
	{
		return _rebindAllowed;
	}
	
	public boolean triedCache()
	{
		return _triedCache;
	}
	
	public boolean triedOriginalEPR()
	{
		return _triedOriginalEPR;
	}
	
	public URI getEPI()
	{
		return _epi;
	}
	
	public ListIterator<ResolverDescription> getResolversIter()
	{
		return _resolversIter;
	}
	
	public void setTriedCache()
	{
		_triedCache = true;
	}
	
	public void setTriedOriginalEPR()
	{
		_triedOriginalEPR = true;
	}
	
	public Throwable getErrorToReport()
	{
		return _errorToReport;
	}

	public void setErrorToReport(Throwable errorToReport)
	{
		_errorToReport = errorToReport;
	}
}
