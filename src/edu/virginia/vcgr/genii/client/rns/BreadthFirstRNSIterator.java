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

class BreadthFirstRNSIterator extends AbstractRNSIterator
{
	BreadthFirstRNSIterator(RNSPath start, boolean ignoreExceptions,
		boolean requireWSNames)
	{
		super(start, ignoreExceptions, requireWSNames);
	}

	protected boolean iterate(IRNSIteratorHandler handler,
		RNSPath element) throws RNSException
	{
		if (!handler.handleRNSPath(element))
			return false;
		
		return __iterate(handler, element);
	}
	
	protected boolean __iterate(IRNSIteratorHandler handler,
		RNSPath element) throws RNSException
	{
		if (allowInternalIteration(element))
		{
			try
			{
				RNSPath []children = element.list(".*", 
					RNSPathQueryFlags.MUST_EXIST);
				for (RNSPath child : children)
				{
					if (!handler.handleRNSPath(child))
						return false;
				}
				
				for (RNSPath child : children)
				{
					if (!__iterate(handler, child))
						return false;
				}
			}
			catch (RNSException rne)
			{
				if (!_ignoreExceptions)
					throw rne;
			}
		}
		
		return true;
	}
}
