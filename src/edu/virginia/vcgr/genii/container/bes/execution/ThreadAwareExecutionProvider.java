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
package edu.virginia.vcgr.genii.container.bes.execution;

import org.apache.axis.AxisFault;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.context.WorkingContext;

public abstract class ThreadAwareExecutionProvider implements IExecutionProvider
{
	protected Thread _executingThread;
	private EndpointReferenceType _resourceEPR;
	private String _targetedServiceName;
	
	protected ThreadAwareExecutionProvider(EndpointReferenceType resourceEPR)
	{
		_executingThread = null;
		_resourceEPR = resourceEPR;
		try
		{
			_targetedServiceName = EPRUtils.extractServiceName(_resourceEPR);
		}
		catch (AxisFault af)
		{
			throw new RuntimeException(af.getLocalizedMessage(), af);
		}
	}
	
	public void step() throws ResourceUnknownFaultType, ResourceException
	{
		try
		{
			WorkingContext.setCurrentWorkingContext(new WorkingContext());
			WorkingContext.getCurrentWorkingContext().setProperty(
				WorkingContext.EPR_PROPERTY_NAME, _resourceEPR);
			WorkingContext.getCurrentWorkingContext().setProperty(
				WorkingContext.TARGETED_SERVICE_NAME, _targetedServiceName);
			synchronized(this)
			{
				_executingThread = Thread.currentThread();
			}
			
			performOperation();
		}
		finally
		{
			WorkingContext.setCurrentWorkingContext(null);
			synchronized(this)
			{
				_executingThread = null;
			}
		}
	}
	
	protected abstract void performOperation() 
		throws ResourceUnknownFaultType, ResourceException;
}
