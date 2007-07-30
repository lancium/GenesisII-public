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

package edu.virginia.vcgr.genii.container.axis;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.context.ContextType;

public class WorkingContextHandler extends BasicHandler
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(WorkingContextHandler.class);
	
	static private final String _FLOW_SIDE_KEY = "flow-side";
	static private final String _FLOW_SIDE_REQUEST_VALUE = "request";
	static private final String _FLOW_SIDE_RESPONSE_VALUE = "response";
	
	private Boolean _isRequest = null;
	
	private boolean isRequest() throws AxisFault
	{
		synchronized(this)
		{
			if (_isRequest == null)
			{
				AxisFault fault = null;
				
				String value = (String)getOption(_FLOW_SIDE_KEY);
				if (value != null)
				{
					if (value.equals(_FLOW_SIDE_REQUEST_VALUE))
						_isRequest = Boolean.TRUE;
					else if (value.equals(_FLOW_SIDE_RESPONSE_VALUE))
						_isRequest = Boolean.FALSE;
					else
						fault = new AxisFault(_FLOW_SIDE_KEY + 
							" property not recognized.  Expected " + 
							_FLOW_SIDE_REQUEST_VALUE + " or " + _FLOW_SIDE_RESPONSE_VALUE);
				} else
				{
					fault = new AxisFault("Couldn't find " + 
						_FLOW_SIDE_KEY + " parameter.");
				}
				
				if (fault != null)
					throw fault;
			}
		}
		
		return _isRequest.booleanValue();
	}
	
	public void invoke(MessageContext ctxt) throws AxisFault
	{
		if (isRequest())
			handleRequest(ctxt);
		else
			handleResponse(ctxt);
	}
	
	protected void handleRequest(MessageContext ctxt) throws AxisFault
	{
		_logger.debug("Setting the working context for an incoming message.");
		
		WorkingContext newContext = new WorkingContext();
		WorkingContext.setCurrentWorkingContext(newContext);

		EndpointReferenceType epr = (EndpointReferenceType)ctxt.getProperty(
			WSAddressingExtractor.AXIS_MESSAGE_CTXT_EPR_PROPERTY);
		if (epr == null)
		{
			throw new AxisFault("Couldn't find \"" +
				WSAddressingExtractor.AXIS_MESSAGE_CTXT_EPR_PROPERTY + 
				"\" property in message context.");
		}
		newContext.setProperty(WorkingContext.EPR_PROPERTY_NAME, epr);
		
		ContextType ct = (ContextType)ctxt.getProperty(
			WSAddressingExtractor.AXIS_MESSAGE_CTXT_CALLING_CONTEXT_PROPERTY);
		if (ct != null)
			newContext.setProperty(WorkingContext.CALLING_CONTEXT_KEY, ct);
		
		newContext.setProperty(WorkingContext.TARGETED_SERVICE_NAME, 
			EPRUtils.extractServiceName(epr));

		newContext.setProperty(WorkingContext.MESSAGE_CONTEXT_KEY, ctxt);
		
		// Many points in the code look for the target EPR here in the 
		// working context.  Unfortunately, we cannot rely on clients 
		// to provide us with enough ws-addressing metadata to create a 
		// complete (and thus desireable) EPR.  Here we try and load more 
		// specific meta-data from the resource's state.  Unfortunately, 
		// we cannot get the implemented port types, as they are only known 
		// to the service implementation *instance*, which may not yet have 
		// been created.  We may have to investigate a delegate/wrapper
		// pattern to know this information (the service class is known 
		// from the message context's operation). 
		try {
			ResourceKey rKey = ResourceManager.getCurrentResource();
			epr = ResourceManager.createEPR(
					rKey, 
					epr.getAddress().get_value().toString(), 
					EPRUtils.getImplementedPortTypes(epr));
			newContext.setProperty(WorkingContext.EPR_PROPERTY_NAME, epr);
		} catch (Throwable t) {}
		
	}
	
	protected void handleResponse(MessageContext ctxt)
	{
		_logger.debug("Clearing the working context for a message.");
		
		cleanupWorkingContext(ctxt);
	}
	
	protected void cleanupWorkingContext(MessageContext ctxt)
	{
		WorkingContext.setCurrentWorkingContext(null);
	}
	
	public void onFault(MessageContext ctxt)
	{
		_logger.debug("On fault called.");
		
		cleanupWorkingContext(ctxt);
	}
}