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
package edu.virginia.vcgr.genii.container.util;

import java.util.Calendar;

import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.container.context.WorkingContext;

public class FaultManipulator
{
	static public <FaultType extends BaseFaultType>
		FaultType fillInFault(FaultType bft)
	{
		if (bft.getTimestamp() == null)
			bft.setTimestamp(Calendar.getInstance());
		if (bft.getOriginator() == null)
		{
			try
			{
				EndpointReferenceType originator =
					(EndpointReferenceType)WorkingContext.getCurrentWorkingContext(
						).getProperty(WorkingContext.EPR_PROPERTY_NAME);
				bft.setOriginator(originator);
			}
			catch (Throwable t)
			{
			}
		}
		if (bft.getDescription() == null)
			bft.setDescription(new BaseFaultTypeDescription[] {
				new BaseFaultTypeDescription(bft.toString())});
		
		return bft;
	}
}
