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

package edu.virginia.vcgr.genii.container.kerbauthn;

import com.sun.security.auth.module.Krb5LoginModule;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.io.*;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.context.*;
import edu.virginia.vcgr.genii.client.security.authz.*;
import edu.virginia.vcgr.genii.container.resource.*;
import edu.virginia.vcgr.genii.client.resource.*;

import edu.virginia.vcgr.genii.container.security.authz.providers.*;
import edu.virginia.vcgr.genii.container.kerbauthn.LoginCallbackHandler;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;


/**
 * Kerberos access control implementation, extends default GAML ACL authz provider
 * 
 * @author dmerrill
 * 
 */
public class KerbAuthZProvider extends GamlAclAuthZProvider
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(KerbAuthZProvider.class);

	/**
	 * R/W lock for the system-wide KDC setting (shame on you Sun)
	 */
	static private ReentrantReadWriteLock kdc_lock = new ReentrantReadWriteLock();
	
	public KerbAuthZProvider()
		throws GeneralSecurityException, IOException
	{
	}

	@SuppressWarnings("unchecked")
	public void checkAccess(
			Collection<GIICredential> authenticatedCallerCredentials,
			IResource resource, 
			Class<?> serviceClass, 
			Method operation)
		throws PermissionDeniedException, AuthZSecurityException, ResourceException
	{
		// Try regular ACLs
		try {
			super.checkAccess(authenticatedCallerCredentials,
				resource, serviceClass, operation);

		} catch (Exception AclException) {
			
			// Try kerb backend
			String username = (String) resource.getProperty(
					SecurityConstants.NEW_IDP_NAME_QNAME.getLocalPart());
			String realm = (String) resource.getProperty(
					SecurityConstants.NEW_KERB_IDP_REALM_QNAME.getLocalPart());
			String kdc = (String) resource.getProperty(
					SecurityConstants.NEW_KERB_IDP_KDC_QNAME.getLocalPart());

			if ((realm == null) || (kdc == null)) {
				throw new AuthZSecurityException("Insufficient Kerberos realm/kdc configuration.");
			}
			
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
					// Grab password from usernametoken (but use the username that is our resource name)
					UsernamePasswordIdentity utIdentity =
							(UsernamePasswordIdentity) cred;
					String password = utIdentity.getPassword();
				
					try {
						// Acquire kdc-settings read lock
						kdc_lock.readLock().lock();
						
						// KDC config
						Map<String, String> state = new HashMap<String, String>();
						Map<String, String> options = new HashMap<String, String>();
						options.put("useTicketCache", "false");
						options.put("refreshKrb5Config", "true");

						if (!realm.equals(System.getProperty("java.security.krb5.realm")) || 
							!kdc.equals(System.getProperty("java.security.krb5.kdc"))) 
						{
							// Wants different KDC/realm.  Upgrade lock
							kdc_lock.readLock().unlock();
							kdc_lock.writeLock().lock();
							
							// The only way to set realm/KDC is through these system properties 
							// (or the krb5.ini/conf global config file).  Shame on you, Sun.  
							System.setProperty("java.security.krb5.realm", realm);
							System.setProperty("java.security.krb5.kdc", kdc);
							
							// Downgrade lock, acquiring read before giving up write
							kdc_lock.readLock().lock();
							kdc_lock.writeLock().unlock();
						}
						
						Krb5LoginModule loginCtx = new Krb5LoginModule();

						Subject subject = new Subject();
						loginCtx.initialize(subject, new LoginCallbackHandler(username, password), state, options);
						loginCtx.login();
						
						return;
						
					} catch (LoginException e) {
						throw new AuthZSecurityException("Error authenticating to Kerberos domain.", e);
					} finally {
						// Release read lock
						kdc_lock.readLock().unlock();
					}
				}
			}
	
			// Nobody appreciates us
			throw new PermissionDeniedException(operation.getName(), (String)resource.getProperty(SecurityConstants.NEW_IDP_NAME_QNAME.getLocalPart()));
		}
	}
}
