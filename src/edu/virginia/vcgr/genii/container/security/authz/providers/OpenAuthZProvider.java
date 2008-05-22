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

package edu.virginia.vcgr.genii.container.security.authz.providers;

import java.security.cert.X509Certificate;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.context.*;
import edu.virginia.vcgr.genii.client.security.MessageLevelSecurity;
import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.*;
import edu.virginia.vcgr.genii.container.resource.*;
import edu.virginia.vcgr.genii.client.resource.*;

/**
 * 
 * NOTES: - Returns true for all access-control decisions
 * 
 * @author dmerrill
 * 
 */
public class OpenAuthZProvider implements IAuthZProvider
{

	static public final String GAML_ACL_PROPERTY_NAME =
			"genii.container.security.authz.gaml-acl";

	static protected final MessageLevelSecurity _defaultMinMsgSec =
			new MessageLevelSecurity(MessageLevelSecurity.NONE);

	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(OpenAuthZProvider.class);

	public OpenAuthZProvider()
	{
	}

	/**
	 * Presently configures the specified resource to have default access
	 * allowed for every GAML credential in the bag of credentials. We may want
	 * to look at restricting this in the future to special credentials.
	 */
	public void setDefaultAccess(ICallingContext callingContext,
			IResource resource, X509Certificate[] serviceCertChain)
			throws AuthZSecurityException, ResourceException
	{

	}

	@SuppressWarnings("unchecked")
	public boolean checkAccess(ICallingContext callingContext,
			X509Certificate callerCert, IResource resource, Method operation)
			throws AuthZSecurityException, ResourceException
	{

		return true;
	}

	public MessageLevelSecurity getMinIncomingMsgLevelSecurity(
			IResource resource) throws AuthZSecurityException,
			ResourceException
	{

		return _defaultMinMsgSec;
	}

	public AuthZConfig getAuthZConfig(IResource resource)
			throws AuthZSecurityException, ResourceException
	{

		return new AuthZConfig(null);
	}

	public void setAuthZConfig(AuthZConfig config, IResource resource)
			throws AuthZSecurityException, ResourceException
	{

	}

}
