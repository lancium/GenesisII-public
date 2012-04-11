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

package edu.virginia.vcgr.genii.container.jndiauthn;

import java.security.cert.X509Certificate;

import java.util.*;
import java.io.*;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.context.*;
import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.*;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;

import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.security.authz.providers.*;
import edu.virginia.vcgr.genii.security.MessageLevelSecurityRequirements;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

/**
 * 
 * NOTES: - The presence of a NULL certificate in the ACL indicates open access. -
 * A NULL ACL indicates no access
 * 
 * @author dmerrill
 * 
 */
public class JNDIAuthZProvider implements IAuthZProvider
{

	static protected final MessageLevelSecurityRequirements _defaultMinMsgSec =
			new MessageLevelSecurityRequirements(MessageLevelSecurityRequirements.SIGN
					| MessageLevelSecurityRequirements.ENCRYPT);

	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(JNDIAuthZProvider.class);
	static private GamlAclAuthZProvider _gamlAclProvider = null;

	public JNDIAuthZProvider()
		throws GeneralSecurityException, IOException
	{
		_gamlAclProvider = new GamlAclAuthZProvider();
	}

	/**
	 * Not applicable for virtualized NIS identities
	 */
	public void setDefaultAccess(ICallingContext callingContext,
			IResource resource, X509Certificate[] serviceCertChain)
			throws AuthZSecurityException, ResourceException
	{

		JNDIResource jndiResource = (JNDIResource) resource;

		if (!jndiResource.isIdpResource())
		{
			_gamlAclProvider.setDefaultAccess(callingContext, resource,
					serviceCertChain);
		}
	}

	/**
	 * Check that the caller has access to the given operation.
	 */
	public void checkAccess(
			Collection<GIICredential> authenticatedCallerCredentials,
			IResource resource, Class<?> serviceClass, Method operation)
		throws PermissionDeniedException, AuthZSecurityException, ResourceException
	{
		JNDIResource jndiResource = (JNDIResource) resource;
		if (!jndiResource.isIdpResource())
		{
			_gamlAclProvider.checkAccess(authenticatedCallerCredentials,
				resource, serviceClass, operation);
			return;
		}
		if (!checkJndiAccess(jndiResource))
		{
			throw new PermissionDeniedException(operation.getName());
		}
	}
	
	/**
	 * Check that the caller has a type of access to the given resource.
	 */
	public boolean checkAccess(
			Collection<GIICredential> authenticatedCallerCredentials,
			IResource resource, RWXCategory category)
		throws AuthZSecurityException, ResourceException
	{
		JNDIResource jndiResource = (JNDIResource) resource;
		if (!jndiResource.isIdpResource())
		{
			_gamlAclProvider.checkAccess(authenticatedCallerCredentials, resource, category);
			return true;
		}
		return checkJndiAccess(jndiResource);
	}

	@SuppressWarnings("unchecked")
	private boolean checkJndiAccess(JNDIResource jndiResource)
		throws AuthZSecurityException, ResourceException
	{
		ICallingContext callingContext;
		try 
		{
			callingContext = ContextManager.getCurrentContext(false);
		}
		catch (IOException e)
		{
			throw new AuthZSecurityException(
				"Calling context exception in JNDIAuthZProvider.", e);
		}

		// try each identity in the caller's credentials
		ArrayList<GIICredential> callerCredentials =
				(ArrayList<GIICredential>) callingContext
						.getTransientProperty(GIICredential.CALLER_CREDENTIALS_PROPERTY);
		for (GIICredential cred : callerCredentials)
		{
			if (cred instanceof UsernamePasswordIdentity)
			{
				try
				{
					UsernamePasswordIdentity utIdentity =
							(UsernamePasswordIdentity) cred;
					String userName = jndiResource.getIdpName();

					Properties jndiEnv = new Properties();
					String providerUrl = null;
					String queryUri = null;

					switch (jndiResource.getStsType())
					{
					case NIS:

						jndiEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY,
								"com.sun.jndi.nis.NISCtxFactory");
						providerUrl =
								"nis://"
										+ jndiResource
												.getProperty(SecurityConstants.NEW_JNDI_STS_HOST_QNAME
														.getLocalPart())
										+ "/"
										+ jndiResource
												.getProperty(SecurityConstants.NEW_JNDI_NISDOMAIN_QNAME
														.getLocalPart());
						jndiEnv.setProperty(Context.PROVIDER_URL, providerUrl);

						InitialDirContext initialContext =
								new InitialDirContext(jndiEnv);
						queryUri = providerUrl + "/system/passwd/" + userName;
						String[] attrIDs = { "userPassword" };
						Attributes attrs =
								initialContext.getAttributes(queryUri, attrIDs);
						initialContext.close();

						Attribute passwordAttr = attrs.get("userPassword");
						byte[] ypPasswordBytes = (byte[]) passwordAttr.get();

						String ypPassword =
								(new String(ypPasswordBytes, "UTF8"));
						ypPassword = ypPassword.substring("{crypt}".length());
						String utPassword = utIdentity.getPassword();

						if (org.mortbay.jetty.security.UnixCrypt.crypt(utPassword,
								ypPassword).equals(ypPassword))
						{
							return true;
						}
						break;

					case LDAP:
						jndiEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY,
								"com.sun.jndi.ldap.LdapCtxFactory");

						throw new ResourceException(
								"\"LDAP not implemented\" not applicable.");

					default:
						throw new ResourceException("Unknown STS type.");
					}

				}
				catch (NamingException e)
				{
					throw new AuthZSecurityException(
						"Naming exception in JNDIAuthZProvider.", e);
				}
				catch (UnsupportedEncodingException e)
				{
					throw new AuthZSecurityException(
						"Naming exception in JNDIAuthZProvider.", e);
				}
			}
		}
		// Nobody appreciates us
		return false;
	}

	public MessageLevelSecurityRequirements getMinIncomingMsgLevelSecurity(
			IResource resource) throws AuthZSecurityException,
			ResourceException
	{

		JNDIResource jndiResource = (JNDIResource) resource;

		if (!jndiResource.isIdpResource())
		{
			return _gamlAclProvider.getMinIncomingMsgLevelSecurity(resource);
		}

		return _defaultMinMsgSec;
	}

	public AuthZConfig getAuthZConfig(IResource resource)
			throws AuthZSecurityException, ResourceException
	{
		if ((resource instanceof IJNDIResource) && ((IJNDIResource) resource).isIdpResource()) {
			// we are a stateless IDP resource
			return new AuthZConfig(null);
        }
		
		return _gamlAclProvider.getAuthZConfig(resource);
	}

	public AuthZConfig getAuthZConfig(IResource resource, boolean sanitize)
			throws AuthZSecurityException, ResourceException
	{
		return getAuthZConfig(resource);
	}
	
	public void setAuthZConfig(AuthZConfig config, IResource resource)
			throws AuthZSecurityException, ResourceException
	{
		if ((resource instanceof IJNDIResource) && ((IJNDIResource) resource).isIdpResource()) {
			// we are a stateless IDP resource
			return;
        }

		_gamlAclProvider.setAuthZConfig(config, resource);
	}

	public void sendAuthZConfig(AuthZConfig oldConfig, AuthZConfig newConfig,
			IResource resource)
		throws AuthZSecurityException, ResourceException
	{
		if ((resource instanceof IJNDIResource) && ((IJNDIResource) resource).isIdpResource())
			return;
		_gamlAclProvider.sendAuthZConfig(oldConfig, newConfig, resource);
	}
	
	public void receiveAuthZConfig(NotificationMessageContents message, IResource resource)
		throws ResourceException, AuthZSecurityException
	{
		if ((resource instanceof IJNDIResource) && ((IJNDIResource) resource).isIdpResource())
			return;
		_gamlAclProvider.receiveAuthZConfig(message, resource);
	}
}
