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
import org.apache.axis.SOAPPart;
import org.apache.ws.axis.security.WSDoAllReceiver;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.message.token.Timestamp;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.ws.security.components.crypto.AbstractCrypto;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import edu.virginia.vcgr.genii.client.comm.CommConstants;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.container.axis.ServerWSDoAllReceiver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Client-side X.509 message-level security handler for incoming 
 * (response) messages.   
 * 
 * @author dgm4d
 */
@SuppressWarnings("rawtypes")
public class ClientMessageSecurityReceiver extends WSDoAllReceiver implements
		ISecurityRecvHandler
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(ClientMessageSecurityReceiver.class);

	public static final String CRYPTO_ALIAS = "RECEIVER_CRYPTO_ALIAS";
	private static final String CRYTO_PASS = "pwd";
	public static final String RESOURCE_ALIAS = "RESOURCE_ALIAS";

	private MessageSecurity _messageSec = null;
	private ICallingContext _callContext = null;

	protected boolean checkReceiverResults(Vector wsResult, Vector actions)
	{
		// checks to see if the security operations performed are the
		// same as those that we required
		return true;
	}

	public ClientMessageSecurityReceiver()
	{
	}

	public void configure(ICallingContext callContext)
	{

		setOption(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN
				+ " " + WSHandlerConstants.TIMESTAMP + " "
				+ WSHandlerConstants.SIGNATURE + " "
				+ WSHandlerConstants.ENCRYPT);
		setOption(WSHandlerConstants.PW_CALLBACK_CLASS,
				ClientMessageSecurityReceiver.ClientPWCallback.class.getName());
		setOption(WSHandlerConstants.USER, ServerWSDoAllReceiver.CRYPTO_ALIAS);
		setOption(WSHandlerConstants.USER,
				ClientMessageSecurityReceiver.CRYPTO_ALIAS);

	}

	public void invoke(MessageContext msgContext) throws AxisFault
	{

		_messageSec =
				(MessageSecurity) msgContext
						.getProperty(CommConstants.MESSAGE_SEC_CALL_DATA);
		_callContext =
				(ICallingContext) msgContext
						.getProperty(CommConstants.CALLING_CONTEXT_PROPERTY_NAME);

		// perform a quick check to see if the message has security
		// headers... (the parent implementation throws a fault and
		// we'd rather let the insecure request happen since we don't
		// force security). Swallow any exceptions and let the
		// parent implementation re-throw them.
		try
		{
			Message sm = msgContext.getCurrentMessage();
			if (sm == null)
			{
				// We did not receive anything...Usually happens when we get a
				// HTTP 202 message (with no content)
				return;
			}
			Document doc = sm.getSOAPEnvelope().getAsDocument();
			String actor = (String) getOption(WSHandlerConstants.ACTOR);
			SOAPConstants sc =
					WSSecurityUtil.getSOAPConstants(doc.getDocumentElement());
			if (WSSecurityUtil.getSecurityHeader(doc, actor, sc) == null)
			{
				return;
			}

		}
		catch (Exception e)
		{
		}

		superinvoke(msgContext);
		// super.invoke(msgContext);
	}

	protected boolean verifyTrust(X509Certificate cert, RequestData reqData)
			throws WSSecurityException
	{

		// If there was no public key/cert material for the server
		// resource, trust its signature key as its valid
		if (_messageSec._resourceEpi == null)
		{
			Crypto crypto = reqData.getSigCrypto();
			KeyStore ks = crypto.getKeyStore();
			try
			{
				ks.setCertificateEntry(RESOURCE_ALIAS, cert);
			}
			catch (java.security.GeneralSecurityException e)
			{
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
			throws WSSecurityException
	{

		AbstractCrypto crypto = null;
		try
		{
			// create an in-memory keystore for the client and resource's key
			// material
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			// set the server resource's identity as trusted
			if ((_messageSec != null) && (_messageSec._resourceEpi != null)
					&& (_messageSec._resourceCertChain != null))
			{
				keyStore.setCertificateEntry(_messageSec._resourceEpi
						.toString(), _messageSec._resourceCertChain[0]);
			}

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
	protected Crypto loadDecryptionCrypto(RequestData reqData)
			throws WSSecurityException
	{

		AbstractCrypto crypto = null;
		try
		{

			// create an in-memory keystore for the client's key material
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			KeyAndCertMaterial keyMaterial =
					_callContext.getActiveKeyAndCertMaterial();
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
					case WSPasswordCallback.USERNAME_TOKEN:
						System.err
								.println("Hmmm, probably bad -- need to lookup a password");
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

				}
				else
				{
					throw new UnsupportedCallbackException(callbacks[i],
							"Unrecognized Callback");
				}

			}
		}
	}

	/**
	 * (Duane: Copied from org.apache.ws.axis.security.WSDoAllReciever, modified
	 * to allow processing of fault messages)
	 * 
	 * Axis calls invoke to handle a message. <p/>
	 * 
	 * @param msgContext
	 *            message context.
	 * @throws AxisFault
	 */
	@SuppressWarnings("unchecked")
	public void superinvoke(MessageContext msgContext) throws AxisFault
	{

		boolean doDebug = log.isDebugEnabled();

		if (doDebug)
		{
			log.debug("WSDoAllReceiver: enter invoke() with msg type: "
					+ msgContext.getCurrentMessage().getMessageType());
		}

		RequestData reqData = new RequestData();
		/*
		 * The overall try, just to have a finally at the end to perform some
		 * housekeeping.
		 */
		try
		{
			reqData.setMsgContext(msgContext);

			Vector<String> actions = new Vector<String>();
			String action = null;
			if ((action = (String) getOption(WSHandlerConstants.ACTION)) == null)
			{
				action =
						(String) msgContext
								.getProperty(WSHandlerConstants.ACTION);
			}
			if (action == null)
			{
				throw new AxisFault("WSDoAllReceiver: No action defined");
			}
			int doAction = WSSecurityUtil.decodeAction(action, actions);

			String actor = (String) getOption(WSHandlerConstants.ACTOR);

			Message sm = msgContext.getCurrentMessage();
			Document doc = null;

			/**
			 * We did not receive anything...Usually happens when we get a HTTP
			 * 202 message (with no content)
			 */
			if (sm == null)
			{
				return;
			}

			try
			{
				doc = sm.getSOAPEnvelope().getAsDocument();
				if (doDebug)
				{
					log.debug("Received SOAP request: ");
					log.debug(org.apache.axis.utils.XMLUtils
							.PrettyDocumentToString(doc));
				}
			}
			catch (Exception ex)
			{
				throw new AxisFault(
						"WSDoAllReceiver: cannot convert into document", ex);
			}

			/*
			 * Duane: Commented out to allow for security header processing of
			 * fault messages. String msgType = sm.getMessageType(); if (msgType !=
			 * null && msgType.equals(Message.RESPONSE)) { SOAPConstants
			 * soapConstants = WSSecurityUtil
			 * .getSOAPConstants(doc.getDocumentElement()); if
			 * (WSSecurityUtil.findElement(doc.getDocumentElement(), "Fault",
			 * soapConstants.getEnvelopeURI()) != null) { return; } }
			 */

			/*
			 * To check a UsernameToken or to decrypt an encrypted message we
			 * need a password.
			 */
			CallbackHandler cbHandler = null;
			if ((doAction & (WSConstants.ENCR | WSConstants.UT)) != 0)
			{
				cbHandler = getPasswordCB(reqData);
			}

			/*
			 * Get and check the Signature specific parameters first because
			 * they may be used for encryption too.
			 */
			doReceiverAction(doAction, reqData);

			Vector<?> wsResult = null;

			try
			{
				wsResult =
						secEngine.processSecurityHeader(doc, actor, cbHandler,
								reqData.getSigCrypto(), reqData.getDecCrypto());
			}
			catch (WSSecurityException ex)
			{
				_logger.info("exception occurred in superinvoke", ex);
				throw new AxisFault(
						"WSDoAllReceiver: security processing failed", ex);
			}

			if (wsResult == null)
			{ // no security header found
				if (doAction == WSConstants.NO_SECURITY)
				{
					return;
				}
				else
				{
					throw new AxisFault(
							"WSDoAllReceiver: Request does not contain required Security header");
				}
			}

			if (reqData.getWssConfig().isEnableSignatureConfirmation()
					&& msgContext.getPastPivot())
			{
				checkSignatureConfirmation(reqData, wsResult);
			}
			/*
			 * save the processed-header flags
			 */
			ArrayList<QName> processedHeaders = new ArrayList<QName>();
			Iterator<?> iterator = sm.getSOAPEnvelope().getHeaders().iterator();
			while (iterator.hasNext())
			{
				org.apache.axis.message.SOAPHeaderElement tempHeader =
						(org.apache.axis.message.SOAPHeaderElement) iterator
								.next();
				if (tempHeader.isProcessed())
				{
					processedHeaders.add(tempHeader.getQName());
				}
			}

			/*
			 * If we had some security processing, get the original SOAP part of
			 * Axis' message and replace it with new SOAP part. This new part
			 * may contain decrypted elements.
			 */
			SOAPPart sPart = (org.apache.axis.SOAPPart) sm.getSOAPPart();

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			XMLUtils.outputDOM(doc, os, true);
			sPart.setCurrentMessage(os.toByteArray(), SOAPPart.FORM_BYTES);
			if (doDebug)
			{
				log.debug("Processed received SOAP request");
				log.debug(org.apache.axis.utils.XMLUtils
						.PrettyDocumentToString(doc));
			}

			/*
			 * set the original processed-header flags
			 */
			iterator = processedHeaders.iterator();
			while (iterator.hasNext())
			{
				QName qname = (QName) iterator.next();
				Enumeration<?> headersByName =
						sm.getSOAPEnvelope().getHeadersByName(
								qname.getNamespaceURI(), qname.getLocalPart());
				while (headersByName.hasMoreElements())
				{
					org.apache.axis.message.SOAPHeaderElement tempHeader =
							(org.apache.axis.message.SOAPHeaderElement) headersByName
									.nextElement();
					tempHeader.setProcessed(true);
				}
			}

			/*
			 * After setting the new current message, probably modified because
			 * of decryption, we need to locate the security header. That is, we
			 * force Axis (with getSOAPEnvelope()) to parse the string, build
			 * the new header. Then we examine, look up the security header and
			 * set the header as processed.
			 * 
			 * Please note: find all header elements that contain the same actor
			 * that was given to processSecurityHeader(). Then check if there is
			 * a security header with this actor.
			 */

			SOAPHeader sHeader = null;
			try
			{
				sHeader = sm.getSOAPEnvelope().getHeader();
			}
			catch (Exception ex)
			{
				throw new AxisFault(
						"WSDoAllReceiver: cannot get SOAP header after security processing",
						ex);
			}

			Iterator<?> headers = sHeader.examineHeaderElements(actor);

			SOAPHeaderElement headerElement = null;
			while (headers.hasNext())
			{
				org.apache.axis.message.SOAPHeaderElement hE =
						(org.apache.axis.message.SOAPHeaderElement) headers
								.next();
				if (hE.getLocalName().equals(WSConstants.WSSE_LN)
						&& hE.getNamespaceURI().equals(WSConstants.WSSE_NS))
				{
					headerElement = hE;
					break;
				}
			}
			((org.apache.axis.message.SOAPHeaderElement) headerElement)
					.setProcessed(true);

			/*
			 * Now we can check the certificate used to sign the message. In the
			 * following implementation the certificate is only trusted if
			 * either it itself or the certificate of the issuer is installed in
			 * the keystore.
			 * 
			 * Note: the method verifyTrust(X509Certificate) allows custom
			 * implementations with other validation algorithms for subclasses.
			 */

			// Extract the signature action result from the action vector
			WSSecurityEngineResult actionResult =
					WSSecurityUtil
							.fetchActionResult(wsResult, WSConstants.SIGN);

			if (actionResult != null)
			{
				X509Certificate returnCert = actionResult.getCertificate();

				if (returnCert != null)
				{
					if (!verifyTrust(returnCert, reqData))
					{
						throw new AxisFault(
								"WSDoAllReceiver: The certificate used for the signature is not trusted");
					}
				}
			}

			/*
			 * Perform further checks on the timestamp that was transmitted in
			 * the header. In the following implementation the timestamp is
			 * valid if it was created after (now-ttl), where ttl is set on
			 * server side, not by the client.
			 * 
			 * Note: the method verifyTimestamp(Timestamp) allows custom
			 * implementations with other validation algorithms for subclasses.
			 */

			// Extract the timestamp action result from the action vector
			actionResult =
					WSSecurityUtil.fetchActionResult(wsResult, WSConstants.TS);

			if (actionResult != null)
			{
				Timestamp timestamp = actionResult.getTimestamp();

				if (timestamp != null)
				{
					if (!verifyTimestamp(timestamp, decodeTimeToLive(reqData)))
					{
						throw new AxisFault(
								"WSDoAllReceiver: The timestamp could not be validated");
					}
				}
			}

			/*
			 * now check the security actions: do they match, in right order?
			 */
			if (!checkReceiverResults(wsResult, actions))
			{
				throw new AxisFault(
						"WSDoAllReceiver: security processing failed (actions mismatch)");

			}
			/*
			 * All ok up to this point. Now construct and setup the security
			 * result structure. The service may fetch this and check it.
			 */
			Vector<Object> results = null;
			if ((results =
					(Vector<Object>) msgContext
							.getProperty(WSHandlerConstants.RECV_RESULTS)) == null)
			{
				results = new Vector<Object>();
				msgContext
						.setProperty(WSHandlerConstants.RECV_RESULTS, results);
			}
			WSHandlerResult rResult = new WSHandlerResult(actor, wsResult);
			results.add(0, rResult);

			if (doDebug)
			{
				log.debug("WSDoAllReceiver: exit invoke()");
			}
		}
		catch (WSSecurityException e)
		{
			throw new AxisFault(e.getMessage(), e);
		}
		finally
		{
			reqData.clear();
			reqData = null;
		}
	}

}
