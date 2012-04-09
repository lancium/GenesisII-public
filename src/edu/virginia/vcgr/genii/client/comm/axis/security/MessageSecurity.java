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

package edu.virginia.vcgr.genii.client.comm.axis.security;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.security.MessageLevelSecurityRequirements;
import edu.virginia.vcgr.genii.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.security.credentials.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.assertions.BasicConstraints;
import edu.virginia.vcgr.genii.security.credentials.assertions.DelegatedAttribute;
import edu.virginia.vcgr.genii.security.credentials.assertions.SignedAssertion;
import edu.virginia.vcgr.genii.security.credentials.assertions.SignedCredentialCache;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

/**
 * Data-structure and utility methods for handling the details 
 * of GII message-level security.
 * 
 * @author dgm4d
 *
 */
public class MessageSecurity
{
	public MessageLevelSecurityRequirements _neededMsgSec;
	public X509Certificate[] _resourceCertChain;
	public URI _resourceEpi;

	public MessageSecurity(MessageLevelSecurityRequirements neededMsgSec,
			X509Certificate[] resourceCertChain, URI resourceEpi)
	{

		_resourceCertChain = resourceCertChain;
		_neededMsgSec = neededMsgSec;
		_resourceEpi = resourceEpi;

	}
	
	/**
	 * Prepares outgoing GIICredentials contained within the 
	 * calling-context's TransientCredentials, performing pre-delegation
	 * and serialization steps.   
	 */
	public static void messageSendPrepareHandler(
			ICallingContext callingContext, 
			Method method,
			MessageSecurity msgSecData)
		throws GenesisIISecurityException
	{
		// get resource CertChain
		X509Certificate[] resourceCertChain = null;
		if (msgSecData != null) 
			resourceCertChain = msgSecData._resourceCertChain;
		
		// get key and cert material used to authenticate our messages
		KeyAndCertMaterial clientKeyMaterial;
		try
		{
			clientKeyMaterial = callingContext.getActiveKeyAndCertMaterial();
		}
		catch (GeneralSecurityException e)
		{
			throw new GenesisIISecurityException(
					"Could not prepare outgoing calling context: "
							+ e.getMessage(), e);
		}

		// get a copy of the outgoing credentials from calling context and
		// add our signing client identity to it
		ArrayList<GIICredential> credentials = 
			new ArrayList<GIICredential>(
				TransientCredentials.getTransientCredentials(callingContext)._credentials);
		
		//Do not delegate transport cert, should not be necessary
		//1/2012 Michael Saravo
		//if (clientKeyMaterial != null && clientKeyMaterial._clientCertChain != null) 
		//	credentials.add(new X509Identity(clientKeyMaterial._clientCertChain));

		try
		{
			// create an arrayList of Signed Assertions that are to be placed
			// in the serializable portion of the outgoing calling context
			HashSet<Serializable> toSerialize = new HashSet<Serializable>();

			Iterator<GIICredential> itr = credentials.iterator();
			while (itr.hasNext())
			{
				GIICredential cred = itr.next();

				if (cred instanceof UsernamePasswordIdentity)
				{
					// Do nothing: do not serialize UT tokens: we will be
					// sending
					// it in its own header the old fashioned way.

				}
				else if (cred instanceof SignedAssertion)
				{
					// check if we are authorized to use this assertion in our
					// outgoing messages
					SignedAssertion signedAssertion = (SignedAssertion) cred;
					if (signedAssertion.getAuthorizedIdentity()[0].equals(
							clientKeyMaterial._clientCertChain[0]))
					{

						// If we have key material for the callee, pre-delegate 
						if (resourceCertChain != null)
						{
							// Duane Merrill: We may not always want to:
							// - insert this assertion at all
							// - authorize the remote resource to use this assertion
	
							DelegatedAttribute delegatedAttribute =
								new DelegatedAttribute(
									new BasicConstraints(
										System.currentTimeMillis() - GenesisIIConstants.CredentialGoodFromOffset,
										GenesisIIConstants.CredentialExpirationMillis, 
										GenesisIIConstants.MaxDelegationDepth),
									signedAssertion, 
									resourceCertChain);
	
							// To avoid signing-costs, use a timed-cache of 
							// delegated assertions 
							signedAssertion = SignedCredentialCache.
								getCachedDelegateAssertion(
										delegatedAttribute,
										clientKeyMaterial._clientPrivateKey);
						}
	
						// add the (possibly newly delegated) credential
						// to the outgoing set
						toSerialize.add(signedAssertion);
					}
				}
			}


			if (!toSerialize.isEmpty())
			{
				callingContext.setProperty(
						GIICredential.ENCODED_GAML_CREDENTIALS_PROPERTY,
						toSerialize);
			}

		}
		catch (GeneralSecurityException e)
		{
			throw new GenesisIISecurityException(
					"Could not prepare outgoing calling context: "
							+ e.getMessage(), e);
		}
	}
	
}