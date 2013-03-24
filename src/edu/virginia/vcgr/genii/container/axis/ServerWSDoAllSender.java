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

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.ws.axis.security.WSDoAllSender;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.components.crypto.AbstractCrypto;
import org.apache.ws.security.WSSecurityEngineResult;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.PrivateKey;
import java.util.Vector;
import java.util.Iterator;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import edu.virginia.vcgr.genii.client.comm.axis.security.GIIBouncyCrypto;
import edu.virginia.vcgr.genii.container.context.WorkingContext;

public class ServerWSDoAllSender extends WSDoAllSender
{
	static final long serialVersionUID = 0L;

	public static final String CRYPTO_ALIAS = "SENDER_CRYPTO_ALIAS";
	private static final String CRYTO_PASS = "pwd";

	private PrivateKey _serverPrivateKey;

	public ServerWSDoAllSender()
	{
	}

	public void configure(PrivateKey serverPrivateKey)
	{
		_serverPrivateKey = serverPrivateKey;

		setOption(WSHandlerConstants.PW_CALLBACK_CLASS, ServerWSDoAllReceiver.ServerPWCallback.class.getName());
		setOption(WSHandlerConstants.USER, ServerWSDoAllSender.CRYPTO_ALIAS);
		setOption(WSHandlerConstants.ENCRYPTION_USER, WSHandlerConstants.USE_REQ_SIG_CERT);
		setOption(WSHandlerConstants.SIG_KEY_ID, "DirectReference");
	}

	public void invoke(MessageContext msgContext) throws AxisFault
	{

		// get the incoming security actions
		Vector<?> results = (Vector<?>) msgContext.getProperty(WSHandlerConstants.RECV_RESULTS);
		if (results == null) {
			// no security results
			return;
		}
		WSHandlerResult rResult = (WSHandlerResult) results.get(0);
		Vector<?> securityResults = rResult.getResults();

		// formulate the outgoing security actions based upon the incoming
		// security actions
		String action = "";
		Iterator<?> itr = securityResults.iterator();
		while (itr.hasNext()) {
			WSSecurityEngineResult r = (WSSecurityEngineResult) itr.next();
			switch (r.getAction()) {
				case WSConstants.SIGN:
					action = action + " " + WSHandlerConstants.SIGNATURE;
					break;
				case WSConstants.ENCR:
					action = action + " " + WSHandlerConstants.ENCRYPT;
					break;
				default:
					break;
			}
		}
		if (action.length() == 0) {
			action = WSHandlerConstants.NO_SECURITY;
		} else {
			// trim the leading space
			action = action.substring(1);
		}

		// set the property on the action
		msgContext.setProperty(WSHandlerConstants.ACTION, action);

		super.invoke(msgContext);
	}

	/**
	 * Hook to allow subclasses to load their Signature Crypto however they see fit.
	 */
	public Crypto loadSignatureCrypto(RequestData reqData) throws WSSecurityException
	{

		AbstractCrypto crypto = null;
		try {
			// create an in-memory keystore for the client's key material
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			// get the destination epr (and its certificate) from where we
			// initially placed it in the working context when the incoming
			// message arrived
			WorkingContext ctxt = WorkingContext.getCurrentWorkingContext();
			Certificate[] targetCertChain = (Certificate[]) ctxt.getProperty(WorkingContext.CERT_CHAIN_KEY);

			keyStore.setKeyEntry(CRYPTO_ALIAS, _serverPrivateKey, CRYTO_PASS.toCharArray(), targetCertChain);

			crypto = new GIIBouncyCrypto();
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
	 * Hook to allow subclasses to load their Encryption Crypto however they see fit.
	 */
	protected Crypto loadEncryptionCrypto(RequestData reqData) throws WSSecurityException
	{
		AbstractCrypto crypto = null;
		try {
			// create an in-memory keystore for the server's key material
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			crypto = new GIIBouncyCrypto();
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

	public static class ClientPWCallback implements CallbackHandler
	{

		/**
		 * 
		 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
		 * 
		 */

		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
		{
			for (int i = 0; i < callbacks.length; i++) {
				if (callbacks[i] instanceof WSPasswordCallback) {
					WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
					// set the password given a username
					if (CRYPTO_ALIAS.compareToIgnoreCase(pc.getIdentifer()) == 0) {
						pc.setPassword(CRYTO_PASS);
					}

				} else {
					throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
				}

			}
		}
	}

}
