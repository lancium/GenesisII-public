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

import java.lang.reflect.Method;
import java.security.cert.X509Certificate;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.axis.MessageLevelSecurityRequirements;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;

/**
 * AuthZ provider implementation that returns true for all access-control decisions
 * 
 * @author dmerrill
 * 
 */
public class OpenAuthZProvider implements IAuthZProvider
{
	static protected final MessageLevelSecurityRequirements _defaultMinMsgSec = new MessageLevelSecurityRequirements(
		MessageLevelSecurityRequirements.NONE);

	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(OpenAuthZProvider.class);

	public OpenAuthZProvider()
	{
	}

	/**
	 * Presently configures the specified resource to have default access allowed for every
	 * credential in the bag of credentials. We may want to look at restricting this in the future
	 * to special credentials.
	 */
	public void setDefaultAccess(ICallingContext callingContext, IResource resource, X509Certificate[] serviceCertChain)
		throws AuthZSecurityException, ResourceException
	{
	}

	public boolean checkAccess(Collection<NuCredential> authenticatedCallerCredentials, IResource resource, RWXCategory category)
	{
		return true;
	}

	public boolean checkAccess(Collection<NuCredential> authenticatedCallerCredentials, IResource resource,
		Class<?> serviceClass, Method operation)
	{
		return true; // all is allowed.
	}

	public MessageLevelSecurityRequirements getMinIncomingMsgLevelSecurity(IResource resource) throws AuthZSecurityException,
		ResourceException
	{
		return _defaultMinMsgSec;
	}

	public AuthZConfig getAuthZConfig(IResource resource) throws AuthZSecurityException, ResourceException
	{

		return new AuthZConfig(null);
	}

	public AuthZConfig getAuthZConfig(IResource resource, boolean sanitize) throws AuthZSecurityException, ResourceException
	{
		return getAuthZConfig(resource);
	}

	public void setAuthZConfig(AuthZConfig config, IResource resource) throws AuthZSecurityException, ResourceException
	{
	}

	public void sendAuthZConfig(AuthZConfig oldConfig, AuthZConfig newConfig, IResource resource)
		throws AuthZSecurityException, ResourceException
	{
	}

	public void receiveAuthZConfig(NotificationMessageContents message, IResource resource) throws ResourceException,
		AuthZSecurityException
	{
	}
}
