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

import javax.naming.Context;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import javax.naming.directory.InitialDirContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.context.*;
import edu.virginia.vcgr.genii.client.security.MessageLevelSecurity;
import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.*;
import edu.virginia.vcgr.genii.container.resource.*;
import edu.virginia.vcgr.genii.client.resource.*;

import edu.virginia.vcgr.genii.container.security.authz.providers.*;


/**
 * 
 * NOTES: - The presence of a NULL certificate in the ACL indicates open access. -
 * A NULL ACL indicates no access
 * 
 * @author dmerrill
 * 
 */
public class NISAuthZProvider implements IAuthZProvider {

	static private final String _NIS_HOST = 
		"edu.virginia.vcgr.genii.container.nisauthn.host";
	static private final String _NIS_DOMAIN = 
		"edu.virginia.vcgr.genii.container.nisauthn.domain";
	
	static protected final MessageLevelSecurity _defaultMinMsgSec = new MessageLevelSecurity(
			MessageLevelSecurity.SIGN | MessageLevelSecurity.ENCRYPT);

	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(NISAuthZProvider.class);
	static private GamlAclAuthZProvider _gamlAclProvider = new GamlAclAuthZProvider();
	
	private String _nisHost = null;
	private String _nisDomain = null;
	
	public NISAuthZProvider(Properties properties) {
		_nisHost = properties.getProperty(_NIS_HOST);		
		_nisDomain = properties.getProperty(_NIS_DOMAIN);		
	}
	
	public String getNisHost() {
		return _nisHost;
	}

	public String getNisDomain() {
		return _nisDomain;
	}
	
	/**
	 * Not applicable for virtualized NIS identities
	 */
	public void setDefaultAccess(
			ICallingContext callingContext,
			IResource resource,
			X509Certificate[] serviceCertChain) throws AuthZSecurityException,
			ResourceException {
		
		if (resource.isServiceResource()) {
			_gamlAclProvider.setDefaultAccess(callingContext, resource, serviceCertChain);
		}
	}

	
	@SuppressWarnings("unchecked")
	public boolean checkAccess(
			ICallingContext callingContext,
			X509Certificate callerCert, 
			IResource resource, 
			Method operation)
				throws AuthZSecurityException, ResourceException {

		if (resource.isServiceResource()) {
			return _gamlAclProvider.checkAccess(callingContext, callerCert, resource, operation);
		}
		
		// try each identity in the caller's credentials
		ArrayList<GamlCredential> callerCredentials = (ArrayList<GamlCredential>)
			callingContext.getTransientProperty(GamlCredential.CALLER_CREDENTIALS_PROPERTY);
		for (GamlCredential cred : callerCredentials) {
			
			if (cred instanceof UsernameTokenIdentity) {
				
				UsernameTokenIdentity utIdentity = (UsernameTokenIdentity) cred;

				// lookup the human name
				NISAuthZProvider authZHandler = (NISAuthZProvider) AuthZProviders.getProvider(
						resource.getParentResourceKey().getServiceName());
				String nameService = 
					"nis://" + 
					authZHandler.getNisHost() + 
					"/" + 
					authZHandler.getNisDomain();

				try {
					Properties nisEnv = new Properties();
					nisEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.nis.NISCtxFactory");
					nisEnv.setProperty(Context.PROVIDER_URL, nameService);
					InitialDirContext initialContext = new InitialDirContext(nisEnv);
					String epiString = (String) resource.getParentResourceKey().getKey();
					String userName = epiString.substring(epiString.lastIndexOf(':') + 1);
					String lookupString = nameService + "/system/passwd/" + userName;
					String[] attrIDs = {"userPassword"}; 
					Attributes attrs = initialContext.getAttributes(lookupString, attrIDs); 
					initialContext.close();

					Attribute passwordAttr = attrs.get("userPassword");
					byte[] ypPasswordBytes = (byte[]) passwordAttr.get();
					
					String ypPassword = (new String(ypPasswordBytes, "UTF8"));
					ypPassword = ypPassword.substring("{crypt}".length());
					String utPassword = utIdentity.getToken();
					
					if (org.mortbay.util.UnixCrypt.crypt(
							utPassword, 
							ypPassword).equals(ypPassword)) {
						return true;
					}
					
				} catch (NamingException e) {
		    		throw new AuthZSecurityException(e.getMessage(), e);
				} catch (UnsupportedEncodingException e) {
		    		throw new AuthZSecurityException(e.getMessage(), e);
				}			
			}
		}
		
		throw new AuthZSecurityException("Access denied for method " + operation.getName());
	}

	public MessageLevelSecurity getMinIncomingMsgLevelSecurity(
			IResource resource) throws AuthZSecurityException,
			ResourceException {

		if (resource.isServiceResource()) {
			return _gamlAclProvider.getMinIncomingMsgLevelSecurity(resource);
		}
		
		return _defaultMinMsgSec;
	}

	public AuthZConfig getAuthZConfig(IResource resource)
			throws AuthZSecurityException, ResourceException {

		if (resource.isServiceResource()) {
			return _gamlAclProvider.getAuthZConfig(resource);
		}

		return new AuthZConfig(null);
	}

	public void setAuthZConfig(AuthZConfig config, IResource resource)
			throws AuthZSecurityException, ResourceException {

		if (resource.isServiceResource()) {
			 _gamlAclProvider.setAuthZConfig(config, resource);
		}
	}

}
