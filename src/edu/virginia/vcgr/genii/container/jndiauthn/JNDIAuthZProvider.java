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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.security.UnixCrypt;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.security.authz.providers.AclAuthZProvider;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SAMLConstants;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.axis.MessageLevelSecurityRequirements;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

/**
 * 
 * NOTES: - The presence of a NULL certificate in the ACL indicates open access. - A NULL ACL
 * indicates no access
 * 
 * @author dmerrill
 * 
 */
public class JNDIAuthZProvider implements IAuthZProvider
{

	static protected final MessageLevelSecurityRequirements _defaultMinMsgSec = new MessageLevelSecurityRequirements(
		MessageLevelSecurityRequirements.SIGN | MessageLevelSecurityRequirements.ENCRYPT);

	static private Log _logger = LogFactory.getLog(JNDIAuthZProvider.class);
	static private AclAuthZProvider _aclProvider = null;

	public JNDIAuthZProvider() throws GeneralSecurityException, IOException
	{
		_aclProvider = new AclAuthZProvider();
	}

	/**
	 * Not applicable for virtualized NIS identities
	 */
	public void setDefaultAccess(ICallingContext callingContext, IResource resource, X509Certificate[] serviceCertChain)
		throws AuthZSecurityException, ResourceException
	{

		JNDIResource jndiResource = (JNDIResource) resource;

		if (!jndiResource.isIdpResource()) {
			_aclProvider.setDefaultAccess(callingContext, resource, serviceCertChain);
		}
	}

	/**
	 * Check that the caller has access to the given operation.
	 */
	public boolean checkAccess(Collection<NuCredential> authenticatedCallerCredentials, IResource resource,
		Class<?> serviceClass, Method operation)
	{
		JNDIResource jndiResource = (JNDIResource) resource;
		if (!jndiResource.isIdpResource()) {
			return _aclProvider.checkAccess(authenticatedCallerCredentials, resource, serviceClass, operation);
		}
		if (!checkJndiAccess(jndiResource)) {
			String idpName = "";
			try {
				idpName = jndiResource.getIdpName();
			} catch (Throwable e) {
				// ignore since just for diagnostics.
			}
			_logger.error("failure: permission denied on " + operation.getName() + " -- " + idpName);
			return false;
		}
		return true;
	}

	/**
	 * Check that the caller has a type of access to the given resource.
	 */
	public boolean checkAccess(Collection<NuCredential> authenticatedCallerCredentials, IResource resource, RWXCategory category)
	{
		JNDIResource jndiResource = (JNDIResource) resource;
		if (!jndiResource.isIdpResource()) {
			_aclProvider.checkAccess(authenticatedCallerCredentials, resource, category);
			return true;
		}
		return checkJndiAccess(jndiResource);
	}

	@SuppressWarnings("unchecked")
	private boolean checkJndiAccess(JNDIResource jndiResource)
	{
		ICallingContext callingContext;
		try {
			callingContext = ContextManager.getExistingContext();
		} catch (IOException e) {
			_logger.error("failure in getting calling context in JNDIAuthZProvider: " + e.getMessage());
			return false;
		}

		// try each identity in the caller's credentials
		ArrayList<NuCredential> callerCredentials = (ArrayList<NuCredential>) callingContext
			.getTransientProperty(SAMLConstants.CALLER_CREDENTIALS_PROPERTY);
		for (NuCredential cred : callerCredentials) {
			if (cred instanceof UsernamePasswordIdentity) {
				try {
					UsernamePasswordIdentity utIdentity = (UsernamePasswordIdentity) cred;
					String userName = jndiResource.getIdpName();

					Properties jndiEnv = new Properties();
					String providerUrl = null;
					String queryUri = null;

					switch (jndiResource.getStsType()) {
						case NIS:

							jndiEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.nis.NISCtxFactory");
							providerUrl = "nis://"
								+ jndiResource.getProperty(SecurityConstants.NEW_JNDI_STS_HOST_QNAME.getLocalPart()) + "/"
								+ jndiResource.getProperty(SecurityConstants.NEW_JNDI_NISDOMAIN_QNAME.getLocalPart());
							jndiEnv.setProperty(Context.PROVIDER_URL, providerUrl);

							InitialDirContext initialContext = new InitialDirContext(jndiEnv);
							queryUri = providerUrl + "/system/passwd/" + userName;
							String[] attrIDs = { "userPassword" };
							Attributes attrs = initialContext.getAttributes(queryUri, attrIDs);
							initialContext.close();

							Attribute passwordAttr = attrs.get("userPassword");
							byte[] ypPasswordBytes = (byte[]) passwordAttr.get();

							String ypPassword = (new String(ypPasswordBytes, "UTF8"));
							ypPassword = ypPassword.substring("{crypt}".length());
							String utPassword = utIdentity.getPassword();

							if (UnixCrypt.crypt(utPassword, ypPassword).equals(ypPassword)) {
								return true;
							}
							break;

						case LDAP:
							jndiEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

							_logger.error("failure: \"LDAP not implemented\" not applicable.");
							return false;

						default:
							_logger.error("Unknown STS type.");
							return false;
					}

				} catch (NamingException e) {
					_logger.error("failure due to naming exception in JNDIAuthZProvider: " + e.getMessage());
					return false;
				} catch (UnsupportedEncodingException e) {
					_logger.error("failure due to unsupported encoding: " + e.getMessage());
					return false;
				} catch (ResourceException e) {
					_logger.error("failure due to resource exception: " + e.getMessage());
					return false;
				}
			}
		}
		// Nobody appreciates us
		return false;
	}

	public MessageLevelSecurityRequirements getMinIncomingMsgLevelSecurity(IResource resource) throws AuthZSecurityException,
		ResourceException
	{

		JNDIResource jndiResource = (JNDIResource) resource;

		if (!jndiResource.isIdpResource()) {
			return _aclProvider.getMinIncomingMsgLevelSecurity(resource);
		}

		return _defaultMinMsgSec;
	}

	public AuthZConfig getAuthZConfig(IResource resource) throws AuthZSecurityException, ResourceException
	{
		if ((resource instanceof IJNDIResource) && ((IJNDIResource) resource).isIdpResource()) {
			// we are a stateless IDP resource
			return new AuthZConfig(null);
		}

		return _aclProvider.getAuthZConfig(resource);
	}

	public AuthZConfig getAuthZConfig(IResource resource, boolean sanitize) throws AuthZSecurityException, ResourceException
	{
		return getAuthZConfig(resource);
	}

	public void setAuthZConfig(AuthZConfig config, IResource resource) throws AuthZSecurityException, ResourceException
	{
		if ((resource instanceof IJNDIResource) && ((IJNDIResource) resource).isIdpResource()) {
			// we are a stateless IDP resource
			return;
		}

		_aclProvider.setAuthZConfig(config, resource);
	}

	public void sendAuthZConfig(AuthZConfig oldConfig, AuthZConfig newConfig, IResource resource)
		throws AuthZSecurityException, ResourceException
	{
		if ((resource instanceof IJNDIResource) && ((IJNDIResource) resource).isIdpResource())
			return;
		_aclProvider.sendAuthZConfig(oldConfig, newConfig, resource);
	}

	public void receiveAuthZConfig(NotificationMessageContents message, IResource resource) throws ResourceException,
		AuthZSecurityException
	{
		if ((resource instanceof IJNDIResource) && ((IJNDIResource) resource).isIdpResource())
			return;
		_aclProvider.receiveAuthZConfig(message, resource);
	}
}
