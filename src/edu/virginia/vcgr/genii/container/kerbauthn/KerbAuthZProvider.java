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

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.security.auth.module.Krb5LoginModule;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.security.authz.providers.AclAuthZProvider;
import edu.virginia.vcgr.genii.security.SAMLConstants;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

/**
 * Kerberos access control implementation, extends default ACL authz provider
 * 
 * @author dmerrill
 * 
 */
public class KerbAuthZProvider extends AclAuthZProvider
{
	static private Log _logger = LogFactory.getLog(KerbAuthZProvider.class);

	/**
	 * R/W lock for the system-wide KDC setting (shame on you Sun)
	 */
	static private ReentrantReadWriteLock kdc_lock = new ReentrantReadWriteLock();

	public KerbAuthZProvider() throws GeneralSecurityException, IOException
	{
	}

	@SuppressWarnings("unchecked")
	public boolean checkAccess(Collection<NuCredential> authenticatedCallerCredentials, IResource resource,
		Class<?> serviceClass, Method operation)
	// throws PermissionDeniedException
	// , AuthZSecurityException, ResourceException
	{
		// Try regular ACLs
		try {
			super.checkAccess(authenticatedCallerCredentials, resource, serviceClass, operation);
			return true;
		} catch (Exception AclException) {
			// we assume we will need the sequel of the function now, since regular ACLs didn't
			// work.
		}

		// Try kerb backend
		String username = "";
		String realm = "";
		String kdc = "";
		try {
			username = (String) resource.getProperty(SecurityConstants.NEW_IDP_NAME_QNAME.getLocalPart());
			realm = (String) resource.getProperty(SecurityConstants.NEW_KERB_IDP_REALM_QNAME.getLocalPart());
			kdc = (String) resource.getProperty(SecurityConstants.NEW_KERB_IDP_KDC_QNAME.getLocalPart());
		} catch (ResourceException e) {
			_logger.error("failed to retrieve kerberos properties: " + e.getMessage());
			return false;
		}

		if ((realm == null) || (kdc == null)) {
			_logger.error("Insufficient Kerberos realm/kdc configuration.");
			return false;
		}

		ICallingContext callingContext;
		try {
			callingContext = ContextManager.getExistingContext();
		} catch (IOException e) {
			_logger.error("Calling context exception in JNDIAuthZProvider.", e);
			return false;
		}

		// try each identity in the caller's credentials
		ArrayList<NuCredential> callerCredentials = (ArrayList<NuCredential>) callingContext
			.getTransientProperty(SAMLConstants.CALLER_CREDENTIALS_PROPERTY);
		for (NuCredential cred : callerCredentials) {

			if (cred instanceof UsernamePasswordIdentity) {
				// Grab password from usernametoken (but use the username that is our resource name)
				UsernamePasswordIdentity utIdentity = (UsernamePasswordIdentity) cred;
				String password = utIdentity.getPassword();

				try {
					// Acquire kdc-settings read lock
					kdc_lock.readLock().lock();

					// KDC config
					Map<String, String> state = new HashMap<String, String>();
					Map<String, String> options = new HashMap<String, String>();
					options.put("useTicketCache", "false");
					options.put("refreshKrb5Config", "true");

					if (!realm.equals(System.getProperty("java.security.krb5.realm"))
						|| !kdc.equals(System.getProperty("java.security.krb5.kdc"))) {
						// Wants different KDC/realm. Upgrade lock
						kdc_lock.readLock().unlock();
						kdc_lock.writeLock().lock();

						// The only way to set realm/KDC is through these system properties
						// (or the krb5.ini/conf global config file). Shame on you, Sun.
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

					return true;

				} catch (LoginException e) {
					_logger.error("failure due to error authenticating to Kerberos domain: " + e.getMessage());
					return false;
				} finally {
					// Release read lock
					kdc_lock.readLock().unlock();
				}
			}
			// if we made it through all of that, we are authorized.
			return true;
		}

		// Nobody appreciates us
		String assetName = resource.toString();
		try {
			String addIn = (String) resource.getProperty(SecurityConstants.NEW_IDP_NAME_QNAME.getLocalPart());
			if (addIn != null)
				assetName.concat(" -- " + addIn);
		} catch (ResourceException e) {
			// ignore.
		}
		_logger.error("failure to authorize " + operation.getName() + " for " + assetName);
		return false;
	}
}
