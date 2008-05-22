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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.*;
import java.lang.reflect.Method;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

import edu.virginia.vcgr.genii.client.comm.axis.security.FlexibleBouncyCrypto;
import edu.virginia.vcgr.genii.client.security.MessageLevelSecurity;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.client.context.*;

import org.morgan.util.configuration.*;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;

import edu.virginia.vcgr.genii.client.security.gamlauthz.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.*;
import edu.virginia.vcgr.genii.container.security.authz.providers.*;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class ServerWSDoAllReceiver extends WSDoAllReceiver
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(ServerWSDoAllReceiver.class);

	static public final String CRYPTO_ALIAS = "CRYPTO_ALIAS";
	static private final String CRYTO_PASS = "pwd";

	private static PrivateKey _serverPrivateKey;

	public ServerWSDoAllReceiver()
	{
	}

	public void configure(PrivateKey serverPrivateKey)
	{
		_serverPrivateKey = serverPrivateKey;

		setOption(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN
				+ " " + WSHandlerConstants.TIMESTAMP + " "
				+ WSHandlerConstants.SIGNATURE + " "
				+ WSHandlerConstants.ENCRYPT);
		setOption(WSHandlerConstants.PW_CALLBACK_CLASS,
				ServerWSDoAllReceiver.ServerPWCallback.class.getName());
		setOption(WSHandlerConstants.USER, ServerWSDoAllReceiver.CRYPTO_ALIAS);
	}

	public ServerWSDoAllReceiver(PrivateKey serverPrivateKey)
	{
		_serverPrivateKey = serverPrivateKey;
	}

	public void invoke(MessageContext msgContext) throws AxisFault
	{
		try
		{
			IResource resource =
					ResourceManager.getCurrentResource().dereference();
			IAuthZProvider authZHandler =
					AuthZProviders.getProvider(resource.getParentResourceKey()
							.getServiceName());

			if ((authZHandler == null)
					|| (authZHandler.getMinIncomingMsgLevelSecurity(resource)
							.isNone()))
			{
				// We have no requirements for incoming message security. If
				// there
				// are no incoming headers, don't do any crypto processing

				Message sm = msgContext.getCurrentMessage();
				if (sm == null)
				{
					// We did not receive anything...Usually happens when we get
					// a
					// HTTP 202 message (with no content)
					return;
				}

				Document doc = sm.getSOAPEnvelope().getAsDocument();
				String actor = (String) getOption(WSHandlerConstants.ACTOR);
				SOAPConstants sc =
						WSSecurityUtil.getSOAPConstants(doc
								.getDocumentElement());
				if (WSSecurityUtil.getSecurityHeader(doc, actor, sc) == null)
					return;
			}

		}
		catch (Exception e)
		{
			_logger
					.error(
							"An error occurred while trying to handler server-side, receiver security.",
							e);
			throw new AxisFault(
					"Exception thrown while retrieving security headers.", e);
		}

		// process all incoming security headers
		super.invoke(msgContext);
	}

	@SuppressWarnings("unchecked")
	protected boolean checkReceiverResults(Vector wsResult, Vector actions)
	{

		// checks to see if the security operations performed meet the minimum
		// required (as per the resource's authZ module)

		try
		{
			// get the resource's min messsage-sec level
			MessageLevelSecurity resourceMinMsgSec;
			IResource resource =
					ResourceManager.getCurrentResource().dereference();
			IAuthZProvider authZHandler =
					AuthZProviders.getProvider(resource.getParentResourceKey()
							.getServiceName());

			if (authZHandler == null)
			{
				resourceMinMsgSec = new MessageLevelSecurity();
			}
			else
			{
				resourceMinMsgSec =
						authZHandler.getMinIncomingMsgLevelSecurity(resource);
			}

			// retrieve what we required from the actions vector
			int performed = MessageLevelSecurity.NONE;
			for (int i = 0; i < wsResult.size(); i++)
			{
				int action =
						((WSSecurityEngineResult) wsResult.get(i)).getAction();
				switch (action)
				{
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
			if ((new MessageLevelSecurity(performed))
					.superset(resourceMinMsgSec))
			{
				return true;
			}

		}
		catch (ResourceException e)
		{
			e.printStackTrace(System.err);
			return false;
		}
		catch (AuthZSecurityException e)
		{
			e.printStackTrace(System.err);
			return false;
		}
		catch (AxisFault e)
		{
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
			throws WSSecurityException
	{

		AbstractCrypto crypto = null;
		try
		{
			// create an in-memory keystore for the incoming sig key material
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			crypto = new FlexibleBouncyCrypto();
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
	protected Crypto loadDecryptionCrypto(RequestData reqData)
			throws WSSecurityException
	{

		AbstractCrypto crypto = null;
		try
		{
			// create an in-memory keystore for the server's key material
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			// place the resource's cert chain and epi in the working context --
			// necessary for
			// response message-security in case we actually delete this
			// resource
			// as part of this operation
			IResource resource =
					ResourceManager.getCurrentResource().dereference();
			Certificate[] targetCertChain =
					(Certificate[]) resource
							.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
			String epi =
					(resource
							.getProperty(IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME))
							.toString();
			WorkingContext wctxt = WorkingContext.getCurrentWorkingContext();
			wctxt.setProperty(WorkingContext.CERT_CHAIN_KEY, targetCertChain);
			wctxt.setProperty(WorkingContext.EPI_KEY, epi);

			if (targetCertChain != null)
			{
				keyStore.setKeyEntry(epi, _serverPrivateKey, CRYTO_PASS
						.toCharArray(), targetCertChain);
			}

			crypto = new FlexibleBouncyCrypto();
			crypto.setKeyStore(keyStore);

			return crypto;

		}
		catch (BaseFaultType bft)
		{
			BaseFaultTypeDescription[] desc = bft.getDescription();
			if (desc != null && desc.length >= 1)
				throw new WSSecurityException(desc[0].get_value(), bft);
			else
				throw new WSSecurityException(bft.dumpToString(), bft);
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
	 * Access control point for AuthZ handlers that require the caller's message
	 * signing certificate
	 */
	protected boolean verifyTrust(X509Certificate cert, RequestData reqData)
			throws WSSecurityException
	{

		try
		{
			// we can now extract our calling context (if we did encryption, it
			// would
			// not have been readable till now)
			WorkingContext workingContext =
					WorkingContext.getCurrentWorkingContext();
			MessageContext messageContext =
					(MessageContext) workingContext
							.getProperty(WorkingContext.MESSAGE_CONTEXT_KEY);
			ICallingContext callContext = null;
			try
			{
				callContext = ContextManager.getCurrentContext();
			}
			catch (ConfigurationException e)
			{
				throw new IOException(e.getMessage());
			}

			// Grab the operation method from the message context
			org.apache.axis.description.OperationDesc desc =
					messageContext.getOperation();
			if (desc == null)
			{
				// pretend security doesn't exist -- axis will do what it does
				// when
				// it can't figure out how to dispatch to a non-existant method
				return true;
			}
			Method operation = desc.getMethod();

			// get the resource's authz handler
			IResource resource =
					ResourceManager.getCurrentResource().dereference();
			IAuthZProvider authZHandler =
					AuthZProviders.getProvider(resource.getParentResourceKey()
							.getServiceName());

			// Let the authZ handler make the decision
			return authZHandler.checkAccess(callContext, cert, resource,
					operation);

		}
		catch (IOException e)
		{
			throw new WSSecurityException(e.getMessage(), e);
		}
	}

	static public class ServerPWCallback implements CallbackHandler
	{

		/**
		 * 
		 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
		 * 
		 */
		@SuppressWarnings("unchecked")
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
					case WSPasswordCallback.USERNAME_TOKEN:
						// broken
						/*
						 * // return password from file to make sure of match
						 * pc.setPassword("mooch"); return;
						 */
						break;

					case WSPasswordCallback.USERNAME_TOKEN_UNKNOWN:
						ICallingContext callContext = null;
						try
						{
							callContext = ContextManager.getCurrentContext();
						}
						catch (ConfigurationException e)
						{
							throw new IOException(e.getMessage());
						}

						// check to make sure the username and password match

						// add the identity to the current calling context
						UsernamePasswordIdentity identity =
								new UsernamePasswordIdentity(pc.getIdentifer(),
										pc.getPassword());
						TransientCredentials transientCredentials =
								TransientCredentials
										.getTransientCredentials(callContext);
						transientCredentials._credentials.add(identity);

						// add the identity to the caller's credential list
						ArrayList<GamlCredential> callerCredentials =
								(ArrayList<GamlCredential>) callContext
										.getTransientProperty(GamlCredential.CALLER_CREDENTIALS_PROPERTY);
						callerCredentials.add(identity);

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
