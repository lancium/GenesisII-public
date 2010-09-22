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
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.components.crypto.AbstractCrypto;

import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.client.comm.*;
import edu.virginia.vcgr.genii.client.context.ICallingContext;

import java.security.GeneralSecurityException;
import java.security.KeyStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Client-side X.509 message-level security handler for outgoing 
 * (request) messages.   
 * 
 * @author dgm4d
 */
public class ClientMessageSecuritySender extends WSDoAllSender implements
		ISecuritySendHandler
{
	static final long serialVersionUID = 0L;

	public static final String CRYPTO_ALIAS = "CRYPTO_ALIAS";
	private static final String CRYTO_PASS = "pwd";

	private MessageSecurity _messageSec = null;
	private ICallingContext _callContext = null;
	private boolean _serialize = false;
	private String _securityActions = "";

	public ClientMessageSecuritySender()
	{
	}

	/**
	 * Indicates that this handler is the final handler and should serialize the
	 * message context
	 */
	public void setToSerialize()
	{
		_serialize = true;
	}

	/**
	 * Configures the Send handler. Returns whether or not this handler is to
	 * perform any actions
	 */
	public boolean configure(ICallingContext callContext,
			MessageSecurity msgSecData) throws GeneralSecurityException
	{

		_messageSec = msgSecData;

		if ((_messageSec == null) || (_messageSec._neededMsgSec.isNone()))
		{
			_securityActions =
					_securityActions + " " + WSHandlerConstants.NO_SECURITY;
			return false;
		}

		if (_messageSec._neededMsgSec.isNone())
		{
			_securityActions =
					_securityActions + " " + WSHandlerConstants.NO_SECURITY;
		}
		if (_messageSec._neededMsgSec.isSign())
		{
			_securityActions =
					_securityActions + " " + WSHandlerConstants.SIGNATURE;
		}
		if (_messageSec._neededMsgSec.isEncrypt())
		{
			_securityActions =
					_securityActions + " " + WSHandlerConstants.ENCRYPT;
		}
		setOption(WSHandlerConstants.SIG_KEY_ID, "DirectReference");
		setOption(WSHandlerConstants.USER, CRYPTO_ALIAS);
		return true;
	}

	public void invoke(MessageContext msgContext) throws AxisFault
	{

		_callContext =
				(ICallingContext) msgContext
						.getProperty(CommConstants.CALLING_CONTEXT_PROPERTY_NAME);

		if (!_serialize)
		{
			// don't let this handler serialize just yet: there may be more
			_securityActions =
					_securityActions + " "
							+ WSHandlerConstants.NO_SERIALIZATION;
		}

		_securityActions = _securityActions.trim();
		setOption(WSHandlerConstants.ACTION, _securityActions);
		setOption(WSHandlerConstants.PW_CALLBACK_CLASS,
				ClientMessageSecuritySender.ClientPWCallback.class.getName());

		super.invoke(msgContext);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void decodeSignatureParameter(RequestData reqData)
			throws WSSecurityException
	{

		Vector partVector = reqData.getSignatureParts();

		// specify that we need to sign the body
		partVector.add(new WSEncryptionPart("Body",
				"http://schemas.xmlsoap.org/soap/envelope/", "Content"));

		// specify any other parts that we need to sign
		ArrayList<WSEncryptionPart> signParts =
				(ArrayList<WSEncryptionPart>) ((MessageContext) reqData
						.getMsgContext())
						.getProperty(CommConstants.MESSAGE_SEC_SIGN_PARTS);
		if (signParts != null)
		{
			for (WSEncryptionPart part : signParts)
			{
				partVector.add(part);
			}
		}

		super.decodeSignatureParameter(reqData);
	}

	public WSPasswordCallback getPassword(String username, int doAction,
			String clsProp, String refProp, RequestData reqData)
			throws WSSecurityException
	{

		return super.getPassword(username, doAction, clsProp, refProp, reqData);
	}

	/**
	 * Hook to allow subclasses to load their Signature Crypto however they see
	 * fit.
	 */
	public Crypto loadSignatureCrypto(RequestData reqData)
			throws WSSecurityException
	{

		AbstractCrypto crypto = null;
		try
		{
			KeyAndCertMaterial keyMaterial =
					_callContext.getActiveKeyAndCertMaterial();

			// create an in-memory keystore for the client's key material
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);
			keyStore.setKeyEntry(CRYPTO_ALIAS, keyMaterial._clientPrivateKey,
					CRYTO_PASS.toCharArray(), keyMaterial._clientCertChain);

			crypto = new GIIBouncyCrypto();
			crypto.setKeyStore(keyStore);

			return crypto;

		}
		catch (IOException e)
		{
			throw new WSSecurityException(e.getMessage(), e);
		}
		catch (java.security.GeneralSecurityException e)
		{
			throw new WSSecurityException(e.getMessage(), e);
		}
		catch (org.apache.ws.security.components.crypto.CredentialException e)
		{
			throw new WSSecurityException(e.getMessage(), e);
		}
	}

	/**
	 * Hook to allow subclasses to load their Encryption Crypto however they see
	 * fit.
	 */
	protected Crypto loadEncryptionCrypto(RequestData reqData)
			throws WSSecurityException
	{
		AbstractCrypto crypto = null;
		try
		{
			// create an in-memory keystore for the server's key material
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			keyStore.setCertificateEntry(CRYPTO_ALIAS,
					_messageSec._resourceCertChain[0]);

			crypto = new GIIBouncyCrypto();
			crypto.setKeyStore(keyStore);

			return crypto;

		}
		catch (IOException e)
		{
			throw new WSSecurityException(e.getMessage(), e);
		}
		catch (java.security.GeneralSecurityException e)
		{
			throw new WSSecurityException(e.getMessage(), e);
		}
		catch (org.apache.ws.security.components.crypto.CredentialException e)
		{
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

		public void handle(Callback[] callbacks) throws IOException,
				UnsupportedCallbackException
		{
			for (int i = 0; i < callbacks.length; i++)
			{
				if (callbacks[i] instanceof WSPasswordCallback)
				{
					WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
					switch (pc.getUsage())
					{
					case WSPasswordCallback.DECRYPT:
					case WSPasswordCallback.SIGNATURE:
						pc.setPassword(CRYTO_PASS);
						break;
					default:
						throw new UnsupportedCallbackException(callbacks[i],
								"Unrecognized Callback");
					}

				}
				else
				{
					throw new UnsupportedCallbackException(callbacks[i],
							"Unrecognized Callback");
				}

			}
		}
	}

}
