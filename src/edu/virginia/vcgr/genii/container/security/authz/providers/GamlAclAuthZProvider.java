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
import java.security.cert.CertificateFactory;
import java.security.GeneralSecurityException;

import java.util.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.Security;
import edu.virginia.vcgr.genii.client.context.*;
import edu.virginia.vcgr.genii.client.security.MessageLevelSecurityRequirements;
import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.security.authz.acl.Acl;
import edu.virginia.vcgr.genii.client.security.authz.acl.AclEntry;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXManager;
import edu.virginia.vcgr.genii.client.security.authz.*;
import edu.virginia.vcgr.genii.client.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.client.security.credentials.TransientCredentials;
import edu.virginia.vcgr.genii.client.security.credentials.assertions.*;
import edu.virginia.vcgr.genii.client.security.credentials.identity.*;
import edu.virginia.vcgr.genii.container.resource.*;
import edu.virginia.vcgr.genii.client.resource.*;
import edu.virginia.vcgr.genii.container.Container;

/**
 * AuthZ provider implementation of GII Acls: an access-control mechanism 
 * comprised of read/write/execute policy-sets.
 * 
 * NOTES: - The presence of a NULL certificate in the ACL indicates open access. -
 * A NULL ACL indicates no access
 * 
 * @author dmerrill
 * 
 */
public class GamlAclAuthZProvider implements IAuthZProvider
{

	static public final String GAML_ACL_PROPERTY_NAME =
			"genii.container.security.authz.gaml-acl";

	static public final String GAML_DEFAULT_OWNER_CERT_PATH =
			"genii.security.authz.bootstrapOwnerCertPath";

	
	static protected final MessageLevelSecurityRequirements _defaultMinMsgSec =
// dgm4d: no longer need SIGN now that we do ssl holder-of-key 
//        authn of msg-level creds
//			new MessageLevelSecurity(MessageLevelSecurity.SIGN);	
			new MessageLevelSecurityRequirements(MessageLevelSecurityRequirements.NONE);

	static protected HashMap<String, X509Certificate> _defaultCertCache =
			new HashMap<String, X509Certificate>();

	static private Log _logger = LogFactory.getLog(GamlAclAuthZProvider.class);

	X509Certificate _defaultInitialResourceOwner = null;

	public GamlAclAuthZProvider(Properties properties)
			throws GeneralSecurityException, IOException
	{
		// read in the certificate that is to serve as default owner
		String defaultOwnerCertPath =
				properties.getProperty(GAML_DEFAULT_OWNER_CERT_PATH);

		if (defaultOwnerCertPath != null)
		{
			synchronized (_defaultCertCache)
			{
				_defaultInitialResourceOwner =
						_defaultCertCache.get(defaultOwnerCertPath);
				if (_defaultInitialResourceOwner == null)
				{
					CertificateFactory cf =
						CertificateFactory.getInstance("X.509");
					_defaultInitialResourceOwner =
						(X509Certificate) cf.generateCertificate(
							new FileInputStream(Installation.getDeployment(
								new DeploymentName()
								).security().getSecurityFile(defaultOwnerCertPath)));
					_defaultCertCache.put(defaultOwnerCertPath,
							_defaultInitialResourceOwner);

				}
			}
		}
		else
		{
			_defaultInitialResourceOwner = Container.getContainerCertChain()[0];
		}
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

		HashSet<Identity> defaultOwners = new HashSet<Identity>();

		// Add all of the authorized identities from incoming GAML creds
		if (callingContext != null)
		{
			TransientCredentials transientCredentials =
					TransientCredentials
							.getTransientCredentials(callingContext);

			for (GIICredential cred : transientCredentials._credentials)
			{

				if (cred instanceof Identity)
				{

					defaultOwners.add((Identity) cred);

				}
				else if ((cred instanceof SignedAssertion)
						&& (((SignedAssertion) cred).getAttribute() instanceof IdentityAttribute))
				{

					defaultOwners
							.add(((IdentityAttribute) ((SignedAssertion) cred)
									.getAttribute()).getIdentity());
				}
			}
		}

		// if no incoming credentials, use the default owner identity indicated
		// by the container
		if (defaultOwners.isEmpty())
		{
			X509Certificate[] defaultOwner = { _defaultInitialResourceOwner };
			if (_defaultInitialResourceOwner != null)
			{
				defaultOwners.add(new X509Identity(defaultOwner));
			}
		}

		/*
		 * dgm4d: this is a security issue // Add the resoure's static creating
		 * service if (serviceCertChain != null) { defaultOwners.add(new
		 * X509Identity(serviceCertChain)); }
		 */

		// Add the resource itself
		defaultOwners.add(new X509Identity((X509Certificate[]) resource
				.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME)));

		Acl acl = new Acl();
		acl.readAcl.addAll(defaultOwners);
		acl.writeAcl.addAll(defaultOwners);
		acl.executeAcl.addAll(defaultOwners);

		resource.setProperty(GAML_ACL_PROPERTY_NAME, acl);
	}

	protected boolean checkAclAccess(Identity identity, Class<?> serviceClass,
		Method operation, Acl acl) throws AuthZSecurityException
	{

		RWXCategory category = RWXManager.lookup(serviceClass, operation);
		Collection<AclEntry> trustList = null;
		switch (category)
		{
		case READ:
			trustList = acl.readAcl;
			break;
		case WRITE:
			trustList = acl.writeAcl;
			break;
		case EXECUTE:
			trustList = acl.executeAcl;
			break;
		case OPEN:
			return true;
		}

		if (trustList == null)
		{
			// Empty ACL
			return false;
		}
		else if (trustList.contains(null))
		{
			// ACL contains null (the wildcard certificate)
			return true;
		}
		else
		{
			// go through the AclEntries
			for (AclEntry entry : trustList) 
			{
				try {
					if (entry.isPermitted(identity)) {
						return true;
					}
				} catch (Exception e) {
				}
			}
		}

		return false;
	}

	

	
	
	public void checkAccess(
		Collection<GIICredential> authenticatedCallerCredentials,
		IResource resource, 
		Class<?> serviceClass, 
		Method operation)
			throws PermissionDeniedException, AuthZSecurityException, ResourceException
	{

		try
		{
			ICallingContext callContext = ContextManager.getCurrentContext(false);

			// get ACL
			Acl acl = (Acl) resource.getProperty(GAML_ACL_PROPERTY_NAME);

			// pre-emptive check of the wildcard access
			if ((acl == null) || checkAclAccess(null, serviceClass, operation, acl))
			{
				return;
			}

			// try each identity in the caller's credentials
			for (GIICredential cred : authenticatedCallerCredentials)
			{
				if (cred instanceof Identity)
				{
					// straight-up identity
					if (checkAclAccess((Identity) cred, serviceClass, operation, acl))
					{
						return;
					}
				}
				else if (cred instanceof SignedAssertion) 
				{
					// possibly unwrap an identity from an IdentityAttribute
					SignedAssertion sa = (SignedAssertion) cred;
					if (sa.getAttribute() instanceof IdentityAttribute) 
					{
						IdentityAttribute ia = (IdentityAttribute) sa.getAttribute();
						if (checkAclAccess(ia.getIdentity(), serviceClass, operation, acl))
						{
							return;
						}
					}
				}
			}

			// Check Administrator Access
			if (Security.isAdministrator(callContext))
			{
				_logger.info("Method call made as admin.");
				return;
			}			

			// Nobody appreciates us
			throw new PermissionDeniedException(operation.getName());
		}
		catch (AuthZSecurityException ase)
		{
			// Re-throw (it's a subclass of IOException, which we do want
			// to trap)
			throw ase;
		}
		catch (IOException e)
		{
			throw new AuthZSecurityException(
					"Error processing GAML credential.", e);
		}
		catch (ConfigurationException e)
		{
			throw new AuthZSecurityException(
					"Error processing GAML credential.", e);
		}
	}

	public MessageLevelSecurityRequirements getMinIncomingMsgLevelSecurity(
			IResource resource) throws AuthZSecurityException,
			ResourceException
	{

		try
		{
			// get ACL
			Acl acl =
					(Acl) resource.getProperty(GAML_ACL_PROPERTY_NAME);

			if (acl == null)
			{

				// return no security requirements if null ACL
				return new MessageLevelSecurityRequirements(MessageLevelSecurityRequirements.NONE);

			}
			else if (acl.requireEncryption)
			{

				// add in encryption
				return _defaultMinMsgSec.computeUnion(new MessageLevelSecurityRequirements(
						MessageLevelSecurityRequirements.ENCRYPT));
			}

			return _defaultMinMsgSec;
		}
		catch (ResourceException e)
		{
			throw new AuthZSecurityException(
				"Could not retrieve minimum incoming message level security.", e);
		}
	}

	public AuthZConfig getAuthZConfig(IResource resource)
			throws AuthZSecurityException, ResourceException
	{

		try
		{
			// get ACL
			Acl acl =
					(Acl) resource.getProperty(GAML_ACL_PROPERTY_NAME);

			if (acl != null)
			{
				return Acl.encodeAcl(acl);
			}

			return new AuthZConfig(null);

		}
		catch (ResourceException e)
		{
			throw new AuthZSecurityException(
					"Unable to load GAML AuthZ config.", e);
		}
	}

	public void setAuthZConfig(AuthZConfig config, IResource resource)
			throws AuthZSecurityException, ResourceException
	{

		Acl acl = Acl.decodeAcl(config);
		resource.setProperty(GAML_ACL_PROPERTY_NAME, acl);
		resource.commit();
	}

}
