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
package edu.virginia.vcgr.genii.client.rns;

import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;

public abstract class AbstractRNSIterator implements IRNSIterator
{
	static private Log _logger = LogFactory.getLog(
		AbstractRNSIterator.class);
	
	private HashSet<WSName> _visited = new HashSet<WSName>();
	
	protected RNSPath _start;
	protected boolean _ignoreExceptions;
	protected boolean _requireWSNames;
	
	protected AbstractRNSIterator(RNSPath start,
		boolean ignoreExceptions,
		boolean requireWSNames)
	{
		_start = start;
		_ignoreExceptions = ignoreExceptions;
		_requireWSNames = requireWSNames;
	}

	protected boolean allowInternalIteration(RNSPath path)
	{
		TypeInformation ti;
		WSName name;
		
		try
		{
			ti = new TypeInformation(path.getEndpoint());
			if (!ti.isRNS())
				return false;
			
			name = new WSName(path.getEndpoint());
		}
		catch (Throwable t)
		{
			return false;
		}
		
		if (!name.isValidWSName())
		{
			if (_requireWSNames)
			{
				_logger.error("Unable to iterate into \"" + path.pwd()
					+ "\" because the endpoint is not a WS-Name.");
				return false;
			} else
			{
				_logger.warn("Allowing iteration into \"" + path.pwd()
					+ "\" which doesn't appear to be a valid WS-Name.");
				return true;
			}
		}
		
		if (_visited.contains(name))
			return false;
		
		_visited.add(name);
		return true;
	}
	
	protected abstract boolean iterate(IRNSIteratorHandler handler, 
		RNSPath element) throws RNSException;
	
	public boolean iterate(IRNSIteratorHandler handler) 
		throws RNSException
	{
		_visited.clear();
		
		return iterate(handler, _start);
	}
}
