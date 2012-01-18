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

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.GeneralSecurityException;

import java.util.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.Security;
import edu.virginia.vcgr.genii.client.context.*;
import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.security.authz.acl.Acl;
import edu.virginia.vcgr.genii.client.security.authz.acl.AclEntry;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXManager;
import edu.virginia.vcgr.genii.client.security.authz.*;
import edu.virginia.vcgr.genii.container.resource.*;
import edu.virginia.vcgr.genii.client.resource.*;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.security.MessageLevelSecurityRequirements;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.security.credentials.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.assertions.*;
import edu.virginia.vcgr.genii.security.credentials.identity.*;

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

	X509Certificate []_defaultInitialResourceOwners = null;

	public GamlAclAuthZProvider()
		throws GeneralSecurityException, IOException
	{
		Collection<File> ownerFiles = Installation.getDeployment(
			new DeploymentName()).security().getDefaultOwnerFiles();
		
		// read in the certificates that are to serve as default owner
		if (ownerFiles != null && ownerFiles.size() > 0)
		{
			synchronized (_defaultCertCache)
			{
				Collection<X509Certificate> ownerCerts = 
					new ArrayList<X509Certificate>(ownerFiles.size());
				
				for (File ownerFile : ownerFiles)
				{
					X509Certificate ownerCert = _defaultCertCache.get(
						ownerFile.getName());
					if (ownerCert == null)
					{
						CertificateFactory cf =
							CertificateFactory.getInstance("X.509");
						FileInputStream fin = null;
						try
						{
							ownerCert = (X509Certificate)cf.generateCertificate(
								fin = new FileInputStream(ownerFile));
							_defaultCertCache.put(ownerFile.getName(), ownerCert);
						}
						finally
						{
							StreamUtils.close(fin);
						}
					}
					ownerCerts.add(ownerCert);
				}
				

				
				_defaultInitialResourceOwners = ownerCerts.toArray(
					new X509Certificate[ownerCerts.size()]);
			}
		}
		else
		{
			_defaultInitialResourceOwners = new X509Certificate[] {
					Container.getContainerCertChain()[0]
			};
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

		// Add desired authorized identities from incoming GAML cred
		if (callingContext != null)
		{
			TransientCredentials transientCredentials =
					TransientCredentials
							.getTransientCredentials(callingContext);

			for (GIICredential cred : transientCredentials._credentials)
			{

				if (cred instanceof Identity)
				{
					if (((Identity)cred).placeInUMask())
						defaultOwners.add((Identity)cred);
					
				}
				
				else if ((cred instanceof SignedAssertion)
						&& (((SignedAssertion) cred).getAttribute() instanceof IdentityAttribute))
				{

					Identity assertedIdentity = 
						((IdentityAttribute) ((SignedAssertion) cred).getAttribute()).getIdentity();
					
					if (assertedIdentity.placeInUMask())
						defaultOwners.add(assertedIdentity);
					
				}
			}
		}

		// if no incoming credentials, use the default owner identity indicated
		// by the container
		if (defaultOwners.isEmpty())
		{
			if (_defaultInitialResourceOwners != null)
			{
				for (X509Certificate cert : _defaultInitialResourceOwners)
				{
					defaultOwners.add(new X509Identity(
						new X509Certificate[] { cert }));
				}
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
			case CLOSED:
				return false;
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

			
			RWXCategory category = RWXManager.lookup(serviceClass, operation);
			
			
			// try each identity in the caller's credentials
			for (GIICredential cred : authenticatedCallerCredentials)
			{
				if (cred instanceof Identity)
				{
					
					if (cred instanceof SignedAssertion){
						if (((SignedAssertion)cred).checkAccess(category)){
							if(checkAclAccess((Identity) cred, serviceClass, operation, acl))
								return;
						}
					}
					// straight-up identity (username/password)
					else if(checkAclAccess((Identity) cred, serviceClass, operation, acl))
					{
						return;
					}
				}
				else if (cred instanceof SignedAssertion) 
				{
					
					SignedAssertion sa = (SignedAssertion) cred;
					if(sa.checkAccess(category)){
						// possibly unwrap an identity from an IdentityAttribute
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
		} catch (GeneralSecurityException e) {
			throw new AuthZSecurityException(
					"Credential not authorized for" +
					" this type of operation.", e);
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
		return getAuthZConfig(resource, true);
	}
	
	public AuthZConfig getAuthZConfig(IResource resource, boolean sanitize)
			throws AuthZSecurityException, ResourceException
	{

		try
		{
			// get ACL
			Acl acl =
					(Acl) resource.getProperty(GAML_ACL_PROPERTY_NAME);

			if (acl != null)
			{
				return Acl.encodeAcl(acl, true);
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
