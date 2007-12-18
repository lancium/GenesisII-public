package edu.virginia.vcgr.genii.client.security.gamlauthz.axis;

import java.io.*;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlCredential;
import edu.virginia.vcgr.genii.client.security.gamlauthz.TransientCredentials;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.*;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;

public class GamlMessageSendHandler {

	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(GamlMessageSendHandler.class);
	
	/**
	 * Intended to manipulate the calling context as necessary before 
	 * it is sent in an outgoing message
	 */
	public static void messageSendPrepareHandler(
			ICallingContext callingContext, 
			Method method, 
			X509Certificate[] resourceCertChain) throws GenesisIISecurityException {

		// get credentials from calling context
		ArrayList <GamlCredential> credentials = 
			TransientCredentials.getTransientCredentials(callingContext)._credentials;

		// get key and cert material used to authenticate our messages
		KeyAndCertMaterial clientKeyMaterial;
		try {
			clientKeyMaterial = callingContext.getActiveKeyAndCertMaterial();
		} catch (GeneralSecurityException e) {
			throw new GenesisIISecurityException("Could not prepare outgoing calling context: " + e.getMessage(), e);
		}

		try {
			// create an arrayList of Signed Assertions that are to be placed 
			// in the serializable portion of the outgoing calling context
			ArrayList<Serializable> toSerialize = new ArrayList<Serializable>();
			
			Iterator<GamlCredential> itr = credentials.iterator();
			while (itr.hasNext()) {
				GamlCredential cred = itr.next();
		
				if (cred instanceof UsernameTokenIdentity) {
					// Do nothing: do not serialize UT tokens: we will be sending
					// it in its own header the old fashioned way.
					
				} else if (cred instanceof SignedAssertion) {
					SignedAssertion signedAssertion = (SignedAssertion) cred;
					
					// Check if we are authorized to use this assertion in our 
					// outgoing messages
					if ((clientKeyMaterial != null) && 
						(signedAssertion.getAuthorizedIdentity()[0].equals(clientKeyMaterial._clientCertChain[0]))) {
						
						// TODO: We may not always want to:
						// - insert this assertion at all
						// - authorize the remote resource to use this assertion
						if (resourceCertChain != null) {
							// Because we know an identity for the remote resource, delegate the 
							// current credential assertion to it
							DelegatedAttribute delegatedAttribute = new DelegatedAttribute(
									null,
									signedAssertion, 
									resourceCertChain);
							
							signedAssertion = SignedCredentialCache.getCachedDelegateAssertion(
									delegatedAttribute, 
									clientKeyMaterial._clientPrivateKey);
						}
						
						toSerialize.add(signedAssertion);
					}
				}
			}

			
			if (clientKeyMaterial._clientCertChain.length > 1) {
				// We're not a temporary self-signed cert: create an identity 
				// assertion about myself
				IdentityAttribute myIdentityAttr = new IdentityAttribute (
					new BasicConstraints(
						System.currentTimeMillis() - (1000L * 60 * 15), // 15 minutes ago
						GenesisIIConstants.CredentialExpirationMillis,	// valid 24 hours
						10),											// 10 delegations
					new X509Identity(clientKeyMaterial._clientCertChain));
				
				// unlike incoming credentials that we have no control over, we don't cache 
				// these (yet) because we can give ourselves a full expiration window
				SignedAssertion signedAssertion = new SignedAttributeAssertion( 
						myIdentityAttr, 
						clientKeyMaterial._clientPrivateKey);
				
				if (resourceCertChain != null) {
					// Because we know an identity for the remote resource, delegate the 
					// current credential assertion to it
					DelegatedAttribute delegatedAttribute = new DelegatedAttribute(
							null,
							signedAssertion, 
							resourceCertChain);
					signedAssertion = new DelegatedAssertion(
						delegatedAttribute, 
						clientKeyMaterial._clientPrivateKey);
				}
				
				toSerialize.add(signedAssertion);
			}

			if (!toSerialize.isEmpty()) {
				callingContext.setProperty(
						GamlCredential.ENCODED_GAML_CREDENTIALS_PROPERTY,
						toSerialize);
			}

		} catch (GeneralSecurityException e) {
			throw new GenesisIISecurityException("Could not prepare outgoing calling context: " + e.getMessage(), e);
		}	
	}
	
	
	
}
