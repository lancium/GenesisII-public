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
import org.apache.axis.MessageContext;
import org.apache.ws.axis.security.WSDoAllSender;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.*;

import java.io.IOException;
import java.util.ArrayList;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.security.GeneralSecurityException;


public class ClientUsernameTokenSender extends WSDoAllSender implements ISecuritySendHandler
{
	static final long serialVersionUID = 0L;
	
	public static final String CRYPTO_ALIAS = "CRYPTO_ALIAS";
	private static final String CRYTO_PASS = "pwd";
	
	private UsernamePasswordIdentity _utIdentity;
	private boolean _serialize = false;
	private String _securityActions = "";
	
	public ClientUsernameTokenSender() {
	}

	/**
	 * Indicates that this handler is the final handler and should 
	 * serialize the message context
	 */
	public void setToSerialize() {
		_serialize = true;
	}
	
	/**
	 * Configures the Send handler. Returns whether or not this handler is to 
	 * perform any actions
	 */
	public boolean configure(ICallingContext callContext, MessageSecurityData msgSecData) throws GeneralSecurityException {		_utIdentity = null;
		
		// get credentials from calling context
		ArrayList <GamlCredential> credentials = 
			TransientCredentials.getTransientCredentials(callContext)._credentials;

		for (GamlCredential cred : credentials) {
			if (cred instanceof UsernamePasswordIdentity) {
				if (_utIdentity != null) {
					throw new GeneralSecurityException("Cannot have more than one username-token credential");
				}
				_utIdentity = (UsernamePasswordIdentity) cred;
			}
		}

		if (_utIdentity == null) {
			_securityActions = _securityActions + " " + WSHandlerConstants.NO_SECURITY;
			return false;
		}
		
		_securityActions = _securityActions + " " + WSHandlerConstants.USERNAME_TOKEN;
//			_securityActions = _securityActions + " " + WSHandlerConstants.TIMESTAMP;

    	setOption(WSHandlerConstants.USER, _utIdentity.getUserName());
    	setOption(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
		return true;
	}

    public void invoke(MessageContext msgContext) throws AxisFault {

    	if (!_serialize) {
			// don't let this handler serialize just yet: there may be more
    		_securityActions = _securityActions + " " + WSHandlerConstants.NO_SERIALIZATION;
		}
		
    	_securityActions = _securityActions.trim();
    	setOption(WSHandlerConstants.ACTION, _securityActions);        	
    	setOption(WSHandlerConstants.PW_CALLBACK_CLASS, ClientUsernameTokenSender.ClientPWCallback.class.getName());

    	super.invoke(msgContext);
    }
    
    public WSPasswordCallback getPassword(String username,
            int doAction,
            String clsProp,
            String refProp,
            RequestData reqData) throws WSSecurityException { 
    	
    	if ((doAction == WSConstants.UT) && (_utIdentity != null)) {
    		WSPasswordCallback pwCb = new WSPasswordCallback(username, 
    				WSPasswordCallback.USERNAME_TOKEN_UNKNOWN);
    		pwCb.setPassword(_utIdentity.getPassword());
    		
    		return pwCb;
    	}
    	
    	return super.getPassword(username, doAction, clsProp, refProp, reqData);
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
