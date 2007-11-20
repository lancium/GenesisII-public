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
package edu.virginia.vcgr.genii.container.context;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.IContextResolver;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.context.ContextType;

public class AxisBasedContextResolver implements IContextResolver
{
	
	public ICallingContext load() throws ResourceException, IOException,
			FileNotFoundException
	{
		WorkingContext wc = WorkingContext.getCurrentWorkingContext();

		// cache it in the working context
		ICallingContext retval;
		if ((retval = (ICallingContext) wc.getProperty(WorkingContext.CURRENT_CONTEXT_KEY)) != null) {
			return retval;
		}
		
		CallingContextImpl callingContext = 
			(CallingContextImpl) wc.getProperty(WorkingContext.CALLING_CONTEXT_KEY);
		CallingContextImpl resourceContext = 
			(CallingContextImpl)ResourceManager.getCurrentResource().dereference().getProperty(
				IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME);

		if (resourceContext == null) {
			if (callingContext == null) {
				retval = new CallingContextImpl(new ContextType());
			} else {
				retval = callingContext.deriveNewContext();
			}
		} else {
			if (callingContext == null) {
				retval = resourceContext.deriveNewContext();
			} else {
				retval = new CallingContextImpl(callingContext);
			}
		}
		
		wc.setProperty(WorkingContext.CURRENT_CONTEXT_KEY, retval);
		return retval;
	}

	public void store(ICallingContext ctxt) throws ResourceException, IOException
	{
		ResourceManager.getCurrentResource().dereference().setProperty(
			IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME, ctxt);
	}
}
