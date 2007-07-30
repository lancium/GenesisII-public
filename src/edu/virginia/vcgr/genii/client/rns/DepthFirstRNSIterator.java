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

class DepthFirstRNSIterator extends AbstractRNSIterator
{
	DepthFirstRNSIterator(RNSPath start, boolean ignoreExceptions,
		boolean requireWSName)
	{
		super(start, ignoreExceptions, requireWSName);
	}

	protected boolean iterate(IRNSIteratorHandler handler, RNSPath element)
		throws RNSException
	{
		if (allowInternalIteration(element))
		{
			try
			{
				RNSPath []children = element.list(".*", 
					RNSPathQueryFlags.MUST_EXIST);
				for (RNSPath child : children)
				{
					if (!iterate(handler, child))
						return false;
				}
			}
			catch (RNSException rne)
			{
				if (!_ignoreExceptions)
					throw rne;
			}
		}
		
		return handler.handleRNSPath(element);
	}
}
