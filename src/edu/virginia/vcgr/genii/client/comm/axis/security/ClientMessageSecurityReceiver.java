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

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.ws.axis.security.WSDoAllReceiver;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.ws.security.components.crypto.AbstractCrypto;
import org.w3c.dom.Document;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Vector;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.CommConstants;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.container.axis.ServerWSDoAllReceiver;

public class ClientMessageSecurityReceiver extends WSDoAllReceiver implements ISecurityHandler
{
	static final long serialVersionUID = 0L;
	
	public static final String CRYPTO_ALIAS = "RECEIVER_CRYPTO_ALIAS";
	private static final String CRYTO_PASS = "pwd";
	public static final String RESOURCE_ALIAS = "RESOURCE_ALIAS";
	
	private MessageSecurityData _messageSec = null;
	private ICallingContext _callContext = null;
	
	@SuppressWarnings("unchecked")
	protected boolean checkReceiverResults(Vector wsResult, Vector actions) 
	{
    	// checks to see if the security operations performed are the 
    	// same as those that we required
		return true;    	
    }
	
	public ClientMessageSecurityReceiver() {}
	
	public void configure(ICallingContext callContext, boolean serialize) { 
		configure(callContext);
	}
	
	public void configure(ICallingContext callContext) {

		setOption(WSHandlerConstants.ACTION, 
			WSHandlerConstants.USERNAME_TOKEN
			+ " " + 
			WSHandlerConstants.TIMESTAMP        			
			+ " " + 
			WSHandlerConstants.SIGNATURE
			+ " " + 
			WSHandlerConstants.ENCRYPT
    	);
    	setOption(WSHandlerConstants.PW_CALLBACK_CLASS, 
			ClientMessageSecurityReceiver.ClientPWCallback.class.getName());
    	setOption(WSHandlerConstants.USER, ServerWSDoAllReceiver.CRYPTO_ALIAS);		
    	setOption(WSHandlerConstants.USER, ClientMessageSecurityReceiver.CRYPTO_ALIAS);
		
	}

    public void invoke(MessageContext msgContext) throws AxisFault {

		_messageSec = (MessageSecurityData) msgContext.getProperty(
				CommConstants.MESSAGE_SEC_CALL_DATA);
		_callContext = (ICallingContext) msgContext.getProperty(
				CommConstants.CALLING_CONTEXT_PROPERTY_NAME);
    	
    	// perform a quick check to see if the message has security 
		// headers... (the parent implementation throws a fault and 
		// we'd rather let the insecure request happen since we don't 
		// force security).  Swallow any exceptions and let the 
		// parent implementation re-throw them.
		try {
			Message sm = msgContext.getCurrentMessage();
	        if (sm == null) {
				// We did not receive anything...Usually happens when we get a 
				// HTTP 202 message (with no content)
	        	return;
	        }
	       	Document doc = sm.getSOAPEnvelope().getAsDocument();
	        String actor = (String) getOption(WSHandlerConstants.ACTOR);
	        SOAPConstants sc = WSSecurityUtil.getSOAPConstants(doc.getDocumentElement());
	        if (WSSecurityUtil.getSecurityHeader(doc, actor, sc) == null) {
	        	return;
	        }
	        
		} catch (Exception e) {
		}
		
		super.invoke(msgContext);
    }
	
    protected boolean verifyTrust(X509Certificate cert, RequestData reqData) throws WSSecurityException {
    	
    	// If there was no public key/cert material for the server 
    	// resource, trust its signature key as its valid
        if (_messageSec._resourceEpi == null) {
	    	Crypto crypto = reqData.getSigCrypto();
	    	KeyStore ks = crypto.getKeyStore();
	    	try {
	    		ks.setCertificateEntry(RESOURCE_ALIAS, cert);
	    	} catch (java.security.GeneralSecurityException e) {
				throw new WSSecurityException(e.getMessage(), e);
	    	}
        }
    	
    	return super.verifyTrust(cert, reqData);
    }    
    
    /**
     * Hook to allow subclasses to load their Signature Crypto however they see
     * fit.
     */
    public Crypto loadSignatureCrypto(RequestData reqData) 
			throws WSSecurityException {

    	AbstractCrypto crypto = null;
        try {
        	// create an in-memory keystore for the client and resource's key material
            KeyStore keyStore = KeyStore.getInstance("JKS");
        	keyStore.load(null, null);
        	
        	// set the client's key material
        	KeyAndCertMaterial keyMaterial = ClientUtils.getActiveKeyAndCertMaterial(_callContext);
            keyStore.setKeyEntry(
            		CRYPTO_ALIAS, 
            		keyMaterial._clientPrivateKey, 
            		CRYTO_PASS.toCharArray(), 
            		keyMaterial._clientCertChain);
            
            // set the server resource's identity as trusted
            if ((_messageSec._resourceEpi != null) && (_messageSec._resourceCertChain != null)) {
	        	keyStore.setCertificateEntry(
	        			_messageSec._resourceEpi.toString(), 
	        			_messageSec._resourceCertChain[0]);
            }

    		crypto = new FlexibleBouncyCrypto("sigCrypto");
    		crypto.setKeyStore(keyStore);
    		
    		return crypto;

        } catch (IOException e) {
    		throw new WSSecurityException(e.getMessage(), e);
        } catch (java.security.GeneralSecurityException e) {
    		throw new WSSecurityException(e.getMessage(), e);
		} catch (org.apache.ws.security.components.crypto.CredentialException e) {
			throw new WSSecurityException(e.getMessage(), e);
		}
    }

    /**
     * Hook to allow subclasses to load their Encryption Crypto however they
     * see fit.
     */
    protected Crypto loadDecryptionCrypto(RequestData reqData) 
			throws WSSecurityException {
    	
    	AbstractCrypto crypto = null;
        try {
        	
        	// create an in-memory keystore for the client's key material
            KeyStore keyStore = KeyStore.getInstance("JKS");
        	keyStore.load(null, null);

        	KeyAndCertMaterial keyMaterial = ClientUtils.getActiveKeyAndCertMaterial(_callContext);
            keyStore.setKeyEntry(
            		CRYPTO_ALIAS, 
            		keyMaterial._clientPrivateKey, 
            		CRYTO_PASS.toCharArray(), 
            		keyMaterial._clientCertChain);
        	
    		crypto = new FlexibleBouncyCrypto("enc&dec crypto");
    		crypto.setKeyStore(keyStore);
    		
    		return crypto;

        } catch (IOException e) {
    		throw new WSSecurityException(e.getMessage(), e);
        } catch (java.security.GeneralSecurityException e) {
    		throw new WSSecurityException(e.getMessage(), e);
		} catch (org.apache.ws.security.components.crypto.CredentialException e) {
			throw new WSSecurityException(e.getMessage(), e);
		}
    }	
	
    public static class ClientPWCallback implements CallbackHandler {

    	/**
    	 * 
    	 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
    	 * 
    	 */

    	public void handle(Callback[] callbacks) throws IOException,
    			UnsupportedCallbackException {
    		for (int i = 0; i < callbacks.length; i++) {
    			if (callbacks[i] instanceof WSPasswordCallback) {
    				WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];

    				switch (pc.getUsage()) {
    				case WSPasswordCallback.USERNAME_TOKEN:
    					System.err.println("Hmmm, probably bad -- need to lookup a password");
    				case WSPasswordCallback.USERNAME_TOKEN_UNKNOWN: 
    					// don't care
    					return;
    				case WSPasswordCallback.DECRYPT: 
    				case WSPasswordCallback.SIGNATURE: 
    					pc.setPassword(CRYTO_PASS);
        				break;
    				default:
        				throw new UnsupportedCallbackException(callbacks[i],
        					"Unrecognized Callback");
    				}    				

    			} else {
    				throw new UnsupportedCallbackException(callbacks[i],
    						"Unrecognized Callback");
    			}

    		}
    	}
    }    
	
}
