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

import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;

import java.util.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.HashSet;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.context.*;
import edu.virginia.vcgr.genii.client.security.MessageLevelSecurity;
import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXManager;
import edu.virginia.vcgr.genii.client.security.gamlauthz.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.*;
import edu.virginia.vcgr.genii.container.resource.*;
import edu.virginia.vcgr.genii.client.resource.*;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.container.Container;

import edu.virginia.vcgr.genii.client.utils.deployment.DeploymentRelativeFile;

/**
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

	static protected final MessageLevelSecurity _defaultMinMsgSec =
			new MessageLevelSecurity(MessageLevelSecurity.SIGN);

	static protected HashMap<String, X509Certificate> _defaultCertCache =
			new HashMap<String, X509Certificate>();

	@SuppressWarnings("unused")
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
							(X509Certificate) cf
									.generateCertificate(new FileInputStream(
											new DeploymentRelativeFile(
													defaultOwnerCertPath)));
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

			for (GamlCredential cred : transientCredentials._credentials)
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

		GamlAcl acl = new GamlAcl();
		acl.readAcl.addAll(defaultOwners);
		acl.writeAcl.addAll(defaultOwners);
		acl.executeAcl.addAll(defaultOwners);

		resource.setProperty(GAML_ACL_PROPERTY_NAME, acl);
	}

	protected boolean checkAclAccess(Identity identity, Method operation,
			GamlAcl acl) throws AuthZSecurityException
	{

		RWXCategory category = RWXManager.lookup(operation);
		ArrayList<Identity> trustList = null;
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

			if (trustList.contains(identity))
			{

				// all's good if we straight-up contain this specific identity
				return true;

			}
			else if (identity instanceof X509Identity)
			{

				X509Certificate[] identityCertChain =
						((X509Identity) identity)
								.getAssertingIdentityCertChain();

				// use the acl's x509 identities as a trust store
				try
				{
					// create an in-memory cert keystore for the trusted certs
					KeyStore ks = KeyStore.getInstance("JKS");
					ks.load(null, null);

					// add the trusted certs into the memory-keystore
					for (Identity trustedIdentity : trustList)
					{
						if (trustedIdentity instanceof X509Identity)
						{
							X509Certificate trustedCert =
									((X509Identity) trustedIdentity)
											.getAssertingIdentityCertChain()[0];
							ks.setCertificateEntry(trustedCert
									.getSubjectX500Principal().getName(),
									trustedCert);
						}
					}

					// create a trust manager from the key store
					PKIXBuilderParameters pkixParams =
							new PKIXBuilderParameters(ks,
									new X509CertSelector());
					pkixParams.setRevocationEnabled(false);
					ManagerFactoryParameters trustParams =
							new CertPathTrustManagerParameters(pkixParams);
					TrustManagerFactory tmf =
							TrustManagerFactory.getInstance("PKIX");
					tmf.init(trustParams);
					X509TrustManager trustManager =
							(X509TrustManager) tmf.getTrustManagers()[0];
					try
					{
						trustManager.checkClientTrusted(identityCertChain,
								identityCertChain[0].getPublicKey()
										.getAlgorithm());
					}
					catch (CertificateException e)
					{
						return false;
					}

					return true;

				}
				catch (IOException e)
				{
					throw new AuthZSecurityException(e.getMessage(), e);
				}
				catch (java.security.GeneralSecurityException e)
				{
					throw new AuthZSecurityException(e.getMessage(), e);
				}
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean checkAccess(ICallingContext callingContext,
			X509Certificate callerCert, IResource resource, Method operation)
			throws AuthZSecurityException, ResourceException
	{

		try
		{

			// get ACL
			GamlAcl acl =
					(GamlAcl) resource.getProperty(GAML_ACL_PROPERTY_NAME);

			// check the wildcard access
			if ((acl == null) || checkAclAccess(null, operation, acl))
			{
				return true;
			}

			if (callingContext == null)
			{
				throw new AuthZSecurityException(
						"Error processing GAML credential: No calling context");
			}

			// get the destination certificate from the calling context
			KeyAndCertMaterial targetKeyMaterial =
					ContextManager.getCurrentContext(false)
							.getActiveKeyAndCertMaterial();
			X509Certificate[] targetCertChain = null;
			if (targetKeyMaterial != null)
			{
				targetCertChain = targetKeyMaterial._clientCertChain;
			}

			// try each identity in the caller's credentials
			boolean allowed = false;
			ArrayList<GamlCredential> callerCredentials =
					(ArrayList<GamlCredential>) callingContext
							.getTransientProperty(GamlCredential.CALLER_CREDENTIALS_PROPERTY);
			for (GamlCredential cred : callerCredentials)
			{

				if (cred instanceof Identity)
				{

					// a simple identity
					if (checkAclAccess((Identity) cred, operation, acl))
					{
						allowed = true;
					}

				}
				else if (cred instanceof SignedAssertion)
				{

					// a signed assertion: need to check validity and
					// verify authenticity
					SignedAssertion signedAssertion = (SignedAssertion) cred;
					signedAssertion.checkValidity(new Date());
					signedAssertion.validateAssertion();

					// if the assertion is pre-authorized for us, unwrap one
					// layer
					if ((targetCertChain != null)
							&& (signedAssertion.getAuthorizedIdentity()[0]
									.equals(targetCertChain[0])))
					{
						if (!(signedAssertion instanceof DelegatedAssertion))
						{
							throw new AuthZSecurityException(
									"GAML credential \""
											+ signedAssertion
											+ "\" does not match the incoming message sender");
						}
						signedAssertion =
								((DelegatedAssertion) signedAssertion).unwrap();
					}

					// verify that the request message signer is the same as the
					// authorized user of the assertion
					if (!signedAssertion.getAuthorizedIdentity()[0]
							.equals(callerCert))
					{
						throw new AuthZSecurityException(
								"GAML credential \""
										+ signedAssertion
										+ "\" does not match the incoming message sender");
					}

					// if its an identity assertion, check it against our ACLs
					if (signedAssertion.getAttribute() instanceof IdentityAttribute)
					{
						IdentityAttribute identityAttr =
								(IdentityAttribute) signedAssertion
										.getAttribute();

						if (checkAclAccess(identityAttr.getIdentity(),
								operation, acl))
						{
							allowed = true;
						}
					}
				}
			}

			if (!allowed)
			{
				throw new AuthZSecurityException("Access denied for method "
						+ operation.getName());
			}

			return true;

		}
		catch (IOException e)
		{
			throw new AuthZSecurityException(
					"Error processing GAML credential: " + e.getMessage(), e);
		}
		catch (ConfigurationException e)
		{
			throw new AuthZSecurityException(
					"Error processing GAML credential: " + e.getMessage(), e);
		}
		catch (GeneralSecurityException e)
		{
			throw new AuthZSecurityException(
					"Error processing GAML credential: " + e.getMessage(), e);
		}
	}

	public MessageLevelSecurity getMinIncomingMsgLevelSecurity(
			IResource resource) throws AuthZSecurityException,
			ResourceException
	{

		try
		{
			// get ACL
			GamlAcl acl =
					(GamlAcl) resource.getProperty(GAML_ACL_PROPERTY_NAME);

			if (acl == null)
			{

				// return no security requirements if null ACL
				return new MessageLevelSecurity(MessageLevelSecurity.NONE);

			}
			else if (acl.requireEncryption)
			{

				// add in encryption
				return _defaultMinMsgSec.computeUnion(new MessageLevelSecurity(
						MessageLevelSecurity.ENCRYPT));
			}

			return _defaultMinMsgSec;
		}
		catch (ResourceException e)
		{
			throw new AuthZSecurityException(
					"Could not retrieve minimum incoming message level security: "
							+ e.getMessage(), e);
		}
	}

	public AuthZConfig getAuthZConfig(IResource resource)
			throws AuthZSecurityException, ResourceException
	{

		try
		{
			// get ACL
			GamlAcl acl =
					(GamlAcl) resource.getProperty(GAML_ACL_PROPERTY_NAME);

			if (acl != null)
			{
				return GamlAcl.encodeAcl(acl);
			}

			return new AuthZConfig(null);

		}
		catch (ResourceException e)
		{
			throw new AuthZSecurityException(
					"Unable to load GAML AuthZ config: " + e.getMessage(), e);
		}
	}

	public void setAuthZConfig(AuthZConfig config, IResource resource)
			throws AuthZSecurityException, ResourceException
	{

		GamlAcl acl = GamlAcl.decodeAcl(config);
		resource.setProperty(GAML_ACL_PROPERTY_NAME, acl);
		resource.commit();
	}

}
