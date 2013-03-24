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
package edu.virginia.vcgr.genii.container.lifetime;

import org.apache.axis.types.URI;
import java.util.Comparator;
import java.util.Date;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class LifetimePrey
{
	private URI _epi;
	private EndpointReferenceType _target;
	private Date _termintationTime;

	LifetimePrey(URI epi, EndpointReferenceType target, Date termintationTime)
	{
		_epi = epi;
		_target = target;
		_termintationTime = termintationTime;
	}

	void destroy() throws ResourceUnknownFaultType, ResourceException
	{
		ResourceManager.getTargetResource(_target).destroy();
	}

	URI getName()
	{
		return _epi;
	}

	Date getTermintationTime()
	{
		return _termintationTime;
	}

	static Comparator<LifetimePrey> createComparor()
	{
		return new Comparator<LifetimePrey>()
		{
			public int compare(LifetimePrey arg0, LifetimePrey arg1)
			{
				int value = arg0._termintationTime.compareTo(arg1._termintationTime);
				if (value == 0)
					value = arg0._epi.toString().compareTo(arg1._epi.toString());

				return value;
			}
		};
	}
}
