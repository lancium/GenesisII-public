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

package edu.virginia.vcgr.genii.client.comm.axis.security;

import java.security.cert.X509Certificate;

import org.apache.axis.MessageContext;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.security.axis.MessageLevelSecurityRequirements;

/**
 * Data-structure and utility methods for handling the details of GII message-level security.
 * 
 * @author dgm4d
 * 
 */
public class MessageSecurity
{
	private static Log _logger = LogFactory.getLog(MessageSecurity.class);

	public MessageLevelSecurityRequirements _neededMsgSec;
	public X509Certificate[] _resourceCertChain;
	public URI _resourceEpi;

	public MessageSecurity(MessageLevelSecurityRequirements neededMsgSec, X509Certificate[] resourceCertChain, URI resourceEpi)
	{
		_resourceCertChain = resourceCertChain;
		_neededMsgSec = neededMsgSec;
		_resourceEpi = resourceEpi;
	}

	/**
	 * Prepares outgoing credentials contained within the calling-context's TransientCredentials,
	 * performing pre-delegation and serialization steps.
	 */
	public static void messageSendPrepareHandler(ICallingContext callingContext, MessageContext msgContext,
		MessageSecurity msgSecData) throws GenesisIISecurityException
	{

		if (_logger.isDebugEnabled())
			_logger.debug("removed message send prep handler.");
	}
}
