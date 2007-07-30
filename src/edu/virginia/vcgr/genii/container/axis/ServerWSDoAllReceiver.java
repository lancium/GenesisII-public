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

package edu.virginia.vcgr.genii.container.axis;

import org.apache.ws.axis.security.WSDoAllReceiver;
import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.message.MessageElement;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.AbstractCrypto;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import java.io.*;
import java.lang.reflect.Method;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Vector;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.axis.security.FlexibleBouncyCrypto;
import edu.virginia.vcgr.genii.client.security.MessageLevelSecurity;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.client.context.*;

import org.morgan.util.configuration.*;

import edu.virginia.vcgr.genii.client.security.gamlauthz.*;
import edu.virginia.vcgr.genii.container.security.authz.handlers.*;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.security.x509.*;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;


public class ServerWSDoAllReceiver extends WSDoAllReceiver 
{
	static final long serialVersionUID = 0L;
	
	public static final String CRYPTO_ALIAS = "CRYPTO_ALIAS";
	
	private static final String CRYTO_PASS = "pwd";

	private PrivateKey _serverPrivateKey;
	
	public ServerWSDoAllReceiver() {
	}

	public void configure(PrivateKey serverPrivateKey) {
		_serverPrivateKey = serverPrivateKey;

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
    			ServerWSDoAllReceiver.ServerPWCallback.class.getName());
    	setOption(WSHandlerConstants.USER, ServerWSDoAllReceiver.CRYPTO_ALIAS);
	}	
	
	public ServerWSDoAllReceiver(PrivateKey serverPrivateKey) {
		_serverPrivateKey = serverPrivateKey;
	}
	
	public void invoke(MessageContext msgContext) throws AxisFault {

		try {
			IResource resource = ResourceManager.getCurrentResource().dereference();
			AuthZHandler authZHandler = AuthZHandler.getAuthZHandler(resource);
			
			if ((authZHandler == null) || (authZHandler.getMinIncomingMsgLevelSecurity(resource).isNone())) {

				// We have no requirements for incoming message security.  If there 
				// are no incoming headers, don't do any crypto processing
				
				Message sm = msgContext.getCurrentMessage();
		        if (sm == null) {
					// We did not receive anything...Usually happens when we get a 
					// HTTP 202 message (with no content)

		        	// since we're not doing any crypto, we can go ahead
		        	// and access our calling context parts now
        			WorkingContext workingContext = WorkingContext.getCurrentWorkingContext();
        			extractCallingContextPostDecrytpion(msgContext, workingContext);		        	
		        	return;
		        }
		       	Document doc = sm.getSOAPEnvelope().getAsDocument();
		        String actor = (String) getOption(WSHandlerConstants.ACTOR);
		        SOAPConstants sc = WSSecurityUtil.getSOAPConstants(doc.getDocumentElement());
		        if (WSSecurityUtil.getSecurityHeader(doc, actor, sc) == null) {

		        	// since we're not doing any crypto, we can go ahead
		        	// and access our calling context parts now
        			WorkingContext workingContext = WorkingContext.getCurrentWorkingContext();
        			extractCallingContextPostDecrytpion(msgContext, workingContext);		        	
		        	return;
		        }
			}
		        
		} catch (Exception e) {
		}

		// process all incoming security headers
        super.invoke(msgContext);
	}
	
    protected boolean checkReceiverResults(Vector wsResult, Vector actions) {

    	// checks to see if the security operations performed meet the minimum
    	// required (as per the resource's authZ module)
    	
    	try {
    		// get the resource's min messsage-sec level
    		MessageLevelSecurity resourceMinMsgSec;
    		IResource resource = ResourceManager.getCurrentResource().dereference();
    		AuthZHandler authZHandler = AuthZHandler.getAuthZHandler(resource);
    		if (authZHandler == null) {
    			resourceMinMsgSec = new MessageLevelSecurity();
    		} else {
    			resourceMinMsgSec = authZHandler.getMinIncomingMsgLevelSecurity(resource);
    		}

        	// retrieve what we required from the actions vector
        	int performed = MessageLevelSecurity.NONE;
        	for (int i = 0; i < wsResult.size(); i++) {
        		int action = ((WSSecurityEngineResult) wsResult.get(i)).getAction();
        		switch (action) {
        		case WSConstants.SIGN:
        			performed |= MessageLevelSecurity.SIGN;
        			break;
        		case WSConstants.ENCR:
        			performed |= MessageLevelSecurity.ENCRYPT;
        			break;
        		case WSConstants.UT:
        			break;
        		}
        	}
        	
        	// check to make sure we met our min level
        	if ((new MessageLevelSecurity(performed)).superset(resourceMinMsgSec)) {
        		return true;
        	}
        	
    	} catch (ResourceException e) {
    		e.printStackTrace(System.err);
    		return false;
    	} catch (AuthZSecurityException e) {
    		e.printStackTrace(System.err);
    		return false;
    	} catch (AxisFault e) {
    		e.printStackTrace(System.err);
    		return false;
    	}
    	
    	return false;    	
    }
	
	
    /**
     * Hook to allow subclasses to load their Signature Crypto however they see
     * fit.
     */
    public Crypto loadSignatureCrypto(RequestData reqData) 
			throws WSSecurityException {
    	
    	AbstractCrypto crypto = null;
        try {
        	// create an in-memory keystore for the incoming sig key material
            KeyStore keyStore = KeyStore.getInstance("JKS");
        	keyStore.load(null, null);

    		crypto = new FlexibleBouncyCrypto();
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
        	// create an in-memory keystore for the server's key material
            KeyStore keyStore = KeyStore.getInstance("JKS");
        	keyStore.load(null, null);
        	
    		// place the resource's cert chain and epi in the working context --
    		// necessary for
    		// response message-security in case we actually delete this resource
    		// as part of this operation
    		IResource resource = ResourceManager.getCurrentResource().dereference();
    		Certificate[] targetCertChain = (Certificate[]) resource
    				.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
    		String epi = (resource
    				.getProperty(IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME))
    				.toString();
    		WorkingContext wctxt = WorkingContext.getCurrentWorkingContext();
    		wctxt.setProperty(WorkingContext.CERT_CHAIN_KEY, targetCertChain);
    		wctxt.setProperty(WorkingContext.EPI_KEY, epi);        	
        	
        	if (targetCertChain != null) {
	        	keyStore.setKeyEntry(
	        			epi, 
	        			_serverPrivateKey, 
	        			CRYTO_PASS.toCharArray(), 
	        			targetCertChain);
        	}

    		crypto = new FlexibleBouncyCrypto();
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
     * Access control point for AuthZ handlers that require the caller's 
     * message signing certificate 
     */
    protected boolean verifyTrust(X509Certificate cert, RequestData reqData) throws WSSecurityException {
    	
    	try {
			// we can now extract our calling context (if we did encryption, it would
    		// not have been readable till now)
			WorkingContext workingContext = WorkingContext.getCurrentWorkingContext();
			MessageContext messageContext = (MessageContext) 
				workingContext.getProperty(WorkingContext.MESSAGE_CONTEXT_KEY);
			ICallingContext callingContext = extractCallingContextPostDecrytpion(messageContext, workingContext);

			// Grab the operation method from the message context 
    		Method operation = 
    			((MessageContext) reqData.getMsgContext()).getOperation().getMethod();
    		
    		// put the caller cert in the working context
    		callingContext.setTransientProperty(AuthZHandler.CALLING_CONTEXT_CALLER_CERT, cert);
    		
	    	// get the resource's authz handler
	    	IResource resource = ResourceManager.getCurrentResource().dereference();
    		AuthZHandler authZHandler = AuthZHandler.getAuthZHandler(resource);
    		
    		// Let the authZ handler make the decision
    		return authZHandler.checkAccess(
    				callingContext, 
    				resource, 
    				operation);

    	} catch (IOException e) {
    		throw new WSSecurityException(e.getMessage(), e);
    	}
    }
    
    
    /**
     * Returns the *calling* context, as opposed to the *current* context.  (The latter
     * may contain stateful resource context information.) 
     * 
     */
	protected ICallingContext extractCallingContextPostDecrytpion(MessageContext msgContext,
			WorkingContext workingContext) throws AxisFault,
			AuthZSecurityException {

		try {
			ICallingContext retval = null;
			IResource resource = ResourceManager.getCurrentResource().dereference();
			
			SOAPMessage m = msgContext.getMessage();
			SOAPHeader header = m.getSOAPHeader();

			Iterator iter = header.examineAllHeaderElements();
			while (iter.hasNext()) {
				SOAPHeaderElement he = (SOAPHeaderElement) iter.next();
				QName heName = new QName(he.getNamespaceURI(), he.getLocalName());
				if (heName.equals(GenesisIIConstants.CONTEXT_INFORMATION_QNAME)) {
					Element em = ((MessageElement) he).getRealElement();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintStream ps = new PrintStream(baos);
					ps.println(em);
					ps.close();
					ByteArrayInputStream bais = new ByteArrayInputStream(baos
							.toByteArray());
					ContextType ct = (ContextType) ObjectDeserializer.deserialize(
							new InputSource(bais), ContextType.class);
					
					retval = new CallingContextImpl(ct);
					
					// get the AuthZ handler and instruct it to prepare the calling context
					// (e.g., it may prepare delegation credentials, remove 
					// non-delgatable credentials, etc)
					AuthZHandler authZHandler = AuthZHandler.getAuthZHandler(resource);
					if (authZHandler != null) {
						authZHandler.prepareContexts(retval);
					}
					
					workingContext.setProperty(WorkingContext.CALLING_CONTEXT_KEY,
							retval);
				}
			}
	
			// place the resource's key material in the transient calling context
			// so that it may be properly used for outgoing messages
			Certificate[] targetCertChain = (Certificate[]) resource
					.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
			if ((targetCertChain != null) && (targetCertChain.length > 0)) {
				ClientUtils.setClientKeyAndCertMaterial(ContextManager
						.getCurrentContext(), new KeyAndCertMaterial(
						(X509Certificate[]) targetCertChain, _serverPrivateKey));
			}
		
			
			return retval;

		} catch (SOAPException se) {
			throw new AxisFault(se.getLocalizedMessage(), se);
		} catch (ConfigurationException e) {
			throw new AuthZSecurityException(e.getMessage(), e);
		} catch (IOException e) {
			throw new AuthZSecurityException(e.getMessage(), e);
		} catch (GeneralSecurityException e) {
			throw new AuthZSecurityException(e.getMessage(), e);
		}

	}    
    
    
    
    static public class ServerPWCallback implements CallbackHandler {

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
    				
    					// return password from file to make sure of match
    					BufferedReader in = new BufferedReader(new InputStreamReader( 
    						ClassLoader.getSystemClassLoader().getResourceAsStream("username-token.txt")));
        					
    					String line;
    					while ((line = in.readLine()) != null) {
    						String[] parts = line.split(":");
    						if (parts[0].equals(pc.getIdentifer())) {
    	    					pc.setPassword(parts[1]);
    							return;
    						}
    					}

    					break;
    					
    				case WSPasswordCallback.USERNAME_TOKEN_UNKNOWN: 
    					// check to make sure the username and password match
    					
    					BufferedReader in2 = new BufferedReader(new InputStreamReader( 
    						ClassLoader.getSystemClassLoader().getResourceAsStream("username-token.txt")));
    					
    					String line2;
    					while ((line2 = in2.readLine()) != null) {
    						String[] parts = line2.split(":");
    						if (parts[0].equals(pc.getIdentifer()) && parts[1].equals(pc.getPassword())) {
    							return;
    						}
    					}

    					throw new IOException("Invalid username or password");
    					
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
