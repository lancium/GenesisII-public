/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package edu.virginia.vcgr.genii.client.comm.axis;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPHeader;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSEncryptionPart;
import org.morgan.util.GUID;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.ReferenceParametersType;

import edu.virginia.vcgr.appmgr.launcher.ApplicationLauncher;
import edu.virginia.vcgr.appmgr.launcher.ApplicationLauncherConsole;
import edu.virginia.vcgr.appmgr.version.Version;
import edu.virginia.vcgr.genii.client.ClientIdGenerator;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.CommConstants;
import edu.virginia.vcgr.genii.client.comm.GeniiSOAPHeaderConstants;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.comm.axis.security.MessageSecurity;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.ContainerConfiguration;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.invoke.handlers.MyProxyCertificate;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.axis.AxisCredentialWallet;
import edu.virginia.vcgr.genii.security.credentials.BasicConstraints;
import edu.virginia.vcgr.genii.security.credentials.CredentialCache;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.identity.IdentityType;
import edu.virginia.vcgr.genii.security.x509.CertEntry;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

public class AxisClientHeaderHandler extends BasicHandler
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(AxisClientHeaderHandler.class);

	private void setGenesisIIHeaders(MessageContext msgContext) throws AxisFault
	{
		SOAPHeader header = null;

		try {
			header = (SOAPHeader) msgContext.getMessage().getSOAPHeader();

			Version currentVersion;
			ApplicationLauncherConsole console = ApplicationLauncher.getConsole();
			if (console != null) {
				currentVersion = console.currentVersion();

				if (currentVersion != null && !currentVersion.equals(Version.EMPTY_VERSION)) {
					SOAPHeaderElement geniiVersion =
						new SOAPHeaderElement(GeniiSOAPHeaderConstants.GENII_ENDPOINT_VERSION_QNAME, currentVersion.toString());
					geniiVersion.setActor(null);
					geniiVersion.setMustUnderstand(false);
					header.addChildElement(geniiVersion);
				}
			}

			// add our endpoint qname header, which is also used to signify that this is a genesis 2 endpoint.
			SOAPHeaderElement isGenesisII = new SOAPHeaderElement(GeniiSOAPHeaderConstants.GENII_ENDPOINT_QNAME, Boolean.TRUE);
			isGenesisII.setActor(null);
			isGenesisII.setMustUnderstand(false);
			header.addChildElement(isGenesisII);

			/*
			 * add in the new header for credential streamlining notation, signifying that the server knows how to reassemble partial
			 * credentials from a client when the client has previously presented the whole credential chains.
			 */
			SOAPHeaderElement supportsStreamlining =
				new SOAPHeaderElement(GeniiSOAPHeaderConstants.GENII_SUPPORTS_CREDENTIAL_STREAMLINING_QNAME,
					CredentialCache.SERVER_CREDENTIAL_STREAMLINING_ENABLED ? "true" : "false");
			supportsStreamlining.setActor(null);
			supportsStreamlining.setMustUnderstand(false);
			header.addChildElement(supportsStreamlining);

		} catch (SOAPException se) {
			_logger.error("saw failure during soap header creation", se);

			throw new AxisFault(se.getLocalizedMessage());
		}
	}

	private void setMessageID(MessageContext msgContext) throws AxisFault
	{
		SOAPHeaderElement messageid = new SOAPHeaderElement(
			new QName(EndpointReferenceType.getTypeDesc().getXmlType().getNamespaceURI(), "MessageID"), "urn:uuid:" + new GUID());
		messageid.setActor(null);
		messageid.setMustUnderstand(false);
		try {
			msgContext.getMessage().getSOAPHeader().addChildElement(messageid);
		} catch (SOAPException se) {
			throw new AxisFault(se.getLocalizedMessage());
		}
	}

	private void setSOAPAction(MessageContext msgContext) throws AxisFault
	{
		String uri = msgContext.getSOAPActionURI();

		if ((uri != null) && (uri.length() > 0)) {
			SOAPHeaderElement action =
				new SOAPHeaderElement(new QName(EndpointReferenceType.getTypeDesc().getXmlType().getNamespaceURI(), "Action"), uri);
			action.setActor(null);
			action.setMustUnderstand(false);
			try {
				msgContext.getMessage().getSOAPHeader().addChildElement(action);
			} catch (SOAPException se) {
				throw new AxisFault(se.getLocalizedMessage());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void setWSAddressingHeaders(MessageContext msgContext) throws AxisFault
	{
		if (!msgContext.containsProperty(CommConstants.TARGET_EPR_PROPERTY_NAME))
			return;

		EndpointReferenceType target = (EndpointReferenceType) msgContext.getProperty(CommConstants.TARGET_EPR_PROPERTY_NAME);
		if (target == null)
			return;

		try {
			String WSA_NS = EndpointReferenceType.getTypeDesc().getXmlType().getNamespaceURI();
			SOAPHeader header = (SOAPHeader) msgContext.getMessage().getSOAPHeader();

			SOAPHeaderElement to = new SOAPHeaderElement(new QName(WSA_NS, "To"), target.getAddress().get_value().toString());
			header.addChildElement(to);

			// specify that we need to sign the To header
			ArrayList<WSEncryptionPart> signParts =
				(ArrayList<WSEncryptionPart>) msgContext.getProperty(CommConstants.MESSAGE_SEC_SIGN_PARTS);
			if (signParts == null) {
				signParts = new ArrayList<WSEncryptionPart>();
				msgContext.setProperty(CommConstants.MESSAGE_SEC_SIGN_PARTS, signParts);
			}
			signParts.add(new WSEncryptionPart("To", "http://www.w3.org/2005/08/addressing", "Element"));

			ReferenceParametersType rpt = target.getReferenceParameters();
			if (rpt != null) {
				MessageElement[] any = rpt.get_any();
				if (any != null) {
					Collection<QName> referenceParameters = new ArrayList<QName>(any.length);

					for (MessageElement elem : any) {
						SOAPHeaderElement she = new SOAPHeaderElement(elem);

						/*
						 * dgm4d: Haxx for problem where resource keys go missing: Basically we have resource keys occasionally set as
						 * MessageElement.objectValue, which isn't deep-copied from "elem" during the SOAPHeaderElement construction.
						 */
						if ((elem.getObjectValue() != null) && ((she.getChildren() == null) || (she.getChildren().isEmpty()))) {
							she.setObjectValue(elem.getObjectValue());
						}

						she.removeAttributeNS(EndpointReferenceType.getTypeDesc().getXmlType().getNamespaceURI(), "IsReferenceParameter");
						she.addAttribute(EndpointReferenceType.getTypeDesc().getXmlType().getNamespaceURI(), "IsReferenceParameter", "true");
						header.addChildElement(she);
						referenceParameters.add(elem.getQName());
					}

					for (QName refParamName : referenceParameters) {
						// specify that we need to sign the reference params
						signParts.add(new WSEncryptionPart(refParamName.getLocalPart(), refParamName.getNamespaceURI(), "Element"));
					}
				}
			}
		} catch (SOAPException se) {
			throw new AxisFault(se.getLocalizedMessage(), se);
		}
	}
	
	static private boolean delegateTo(EndpointReferenceType epr) 
	{
		// =======================================================
		// 2020-11-18 ASG. Part of eliminating delegation to RNS, ByteIO, and Lightweight export.
		// Don't delegate if the endpoint is a LightweightExport, and RNS or a byteIO
		boolean delegate=true;
		if (epr == null) {
			_logger.warn("could not find EPR target in message context; cannot check whether to delegate.");
			return true;
		} else {
			String serviceName=null;
			try {
				serviceName=EPRUtils.extractServiceName(epr);
			} catch (AxisFault e) {
				return true;
			}
			if (serviceName!=null) 
				if ((serviceName.indexOf("EnhancedRNSPortType")>=0)||
						(serviceName.indexOf("RandomByteIOPortType")>=0) ||
						(serviceName.indexOf("LightWeightExportPortType")>=0)) delegate=false;									
		}
		return delegate;
	}

	public static void delegateCredentials(CredentialWallet wallet, ICallingContext callingContext, MessageContext messageContext,
		MessageSecurity msgSecData) throws Exception
	{
		if ((wallet == null) || (msgSecData == null)) {
			_logger.error("failure in calling delegate credentials; null object passed");
			return;
		}

		X509Certificate[] resourceCertChain = msgSecData._resourceCertChain;
		KeyAndCertMaterial clientKeyAndCertificate = callingContext.getActiveKeyAndCertMaterial();
		if (clientKeyAndCertificate == null) {
			_logger.error("no active key and cert material available: not performing delegation");
			return;
		}
		
		EndpointReferenceType target = (EndpointReferenceType) messageContext.getProperty(CommConstants.TARGET_EPR_PROPERTY_NAME);
		GUID containerGUID = null;
		// =======================================================
		// 2020-11-18 ASG. Part of eliminating delegation to RNS, ByteIO, and Lightweight export
		boolean delegate = true;
		if (target == null) {
			_logger.warn("could not find EPR target in message context; cannot use this for container id.");
		} else {
			containerGUID = EPRUtils.getGeniiContainerID(target);
			delegate=delegateTo(target);
		}

		long beginTime = System.currentTimeMillis() - SecurityConstants.CredentialGoodFromOffset;
		long endTime = System.currentTimeMillis() + SecurityConstants.CredentialExpirationMillis;

		BasicConstraints restrictions = new BasicConstraints(beginTime, endTime, SecurityConstants.MaxDelegationDepth);

		EnumSet<RWXCategory> accessCategories = EnumSet.of(RWXCategory.READ, RWXCategory.WRITE, RWXCategory.EXECUTE);

		/*
		 * A new credentials wallet is needed for the resource. Otherwise, the operation of delegation will corrupt client's own credentials
		 * wallet.
		 */
		AxisCredentialWallet walletForResource = new AxisCredentialWallet();

		boolean foundAny = false;
		for (TrustCredential trustDelegation : wallet.getCredentials()) {
			walletForResource.getRealCreds().addCredential(trustDelegation);
			foundAny = true;

			boolean handledThisAlready = false;

			if (resourceCertChain == null) {
				if (_logger.isTraceEnabled())
					_logger.trace("no resource cert chain; using bare credentials.");

				if (ConfigurationManager.getCurrentConfiguration().isServerRole()) {
					/*
					 * in the server role, we still want to delegate to the TLS certificate so that any credentials will mention the true
					 * sender (at the TLS level) of the credentials.
					 */
					CertEntry tlsKey = ContainerConfiguration.getContainerTLSCert();
					if (tlsKey != null) {
						// delegate from the credential's resource to our tls cert.
						TrustCredential newCred = walletForResource.getRealCreds().delegateTrust(tlsKey._certChain, IdentityType.CONNECTION,
							clientKeyAndCertificate._clientCertChain, clientKeyAndCertificate._clientPrivateKey, restrictions,
							accessCategories, trustDelegation);
						if (newCred == null) {
							if (_logger.isTraceEnabled()) {
								_logger.debug("failure in trust delegation to tls cert.  dropping this credential on floor:\n"
									+ trustDelegation + "\nbecause we received a null delegated assertion for our tls cert.");
							}
							continue;
						}
					}
				}

			} else {

				try {

					if (ConfigurationManager.getCurrentConfiguration().isServerRole()) {
						/*
						 * in the server role, we first delegate from the source resource to our container tls cert and thence to the target
						 * resource.
						 */
						CertEntry tlsKey = ContainerConfiguration.getContainerTLSCert();
						if (tlsKey != null) {
							// first delegate from the credential's resource to our tls cert.
							TrustCredential newCred = walletForResource.getRealCreds().delegateTrust(tlsKey._certChain,
								IdentityType.CONNECTION, clientKeyAndCertificate._clientCertChain, clientKeyAndCertificate._clientPrivateKey,
								restrictions, accessCategories, trustDelegation);
							if (newCred == null) {
								if (_logger.isTraceEnabled()) {
									_logger.debug(
										"failure in first level of trust delegation, to tls cert.  dropping this credential on floor:\n"
											+ trustDelegation + "\nbecause we received a null delegated assertion for our tls cert.");
								}
								continue;
							}

							// then delegate from the tls cert to the remote resource.
							// =======================================================
							// 2020-11-18 ASG. Part of eliminating delegation to RNS, ByteIO, and Lightweight export
							// if (delegate) added
							if (delegate) 
								walletForResource.getRealCreds().delegateTrust(resourceCertChain, IdentityType.OTHER, tlsKey._certChain,
										tlsKey._privateKey, restrictions, accessCategories, newCred);

							handledThisAlready = true;
						} else {
							_logger.error("failed to find a tls certificate for delegating in the outcall");
						}

					}

					// if no delegation step performed at some point above, do it here.
					// =======================================================
					// 2020-11-18 ASG. Part of eliminating delegation to RNS, ByteIO, and Lightweight export
					// if && delegate added 
					if (!handledThisAlready&&delegate) {
						if (_logger.isTraceEnabled())
							_logger
								.debug("outcall, normal trust delegation by: " + clientKeyAndCertificate._clientCertChain[0].getSubjectDN());

						// in the client role here, so just delegate to the resource.
						TrustCredential newTC = walletForResource.getRealCreds().delegateTrust(resourceCertChain, IdentityType.OTHER,
							clientKeyAndCertificate._clientCertChain, clientKeyAndCertificate._clientPrivateKey, restrictions,
							accessCategories, trustDelegation);

						if (newTC != null) {
							if (_logger.isTraceEnabled())
								_logger.debug("after delegation, cred for outcall is: " + newTC.describe(VerbosityLevel.HIGH));
						}
					}

				} catch (Throwable e) {
					_logger.error("failed to delegate trust", e);
				}
			}
		}

		// server additions after all the credentials (if any) have been handled above.
		if (ConfigurationManager.getCurrentConfiguration().isServerRole()) {
			CertEntry tlsKey = ContainerConfiguration.getContainerTLSCert();
			if (tlsKey == null) {
				_logger.error("failed to find a TLS certificate to delegate to for outcall");
			} else {
				/*
				 * extra credential 1: the idea here is that we need assurance that the source resource trusts this tls certificate and
				 * therefore the recipient should also.
				 */
				// this credential says that the resource trusts the tls connection cert.
				TrustCredential newTC =
					CredentialCache.generateCredential(tlsKey._certChain, IdentityType.CONNECTION, clientKeyAndCertificate._clientCertChain,
						clientKeyAndCertificate._clientPrivateKey, restrictions, RWXCategory.FULL_ACCESS);
				walletForResource.getRealCreds().addCredential(newTC);
				if (_logger.isTraceEnabled())
					_logger.debug("made extra credential for connection: " + newTC);
				foundAny = true;

				/*
				 * possible extra credential 2: this credential says that the tls connection cert trusts a pass-through tls identity, if any.
				 * this identity is used for matching the original tls session cert who requested the identity against possible patterns in
				 * the ACL; otherwise we can't join groups designated by myproxy CA or other CA certs.
				 */
				X509Certificate passThrough =
					(X509Certificate) callingContext.getSingleValueProperty(GenesisIIConstants.PASS_THROUGH_IDENTITY);
				if (passThrough != null) {
					/*
					 * verify that this cert matches the last TLS we saw from the client, or we won't propagate it.
					 */
					X509Certificate lastTLS =
						(X509Certificate) callingContext.getSingleValueProperty(GenesisIIConstants.LAST_TLS_CERT_FROM_CLIENT);
					if (!passThrough.equals(lastTLS)) {
						_logger.warn("ignoring pass-through credential that doesn't match client's last TLS certificate.");
					} else {
						X509Certificate passOn[] = new X509Certificate[1];
						passOn[0] = passThrough;

						TrustCredential newerTC = CredentialCache.generateCredential(passOn, IdentityType.CONNECTION, tlsKey._certChain,
							tlsKey._privateKey, restrictions, RWXCategory.FULL_ACCESS);
						if (newerTC == null) {
							_logger.error("failed to create credential for pass-through connection for: " + passOn[0].getSubjectDN());
						} else {
							walletForResource.getRealCreds().addCredential(newerTC);
							foundAny = true;
							if (_logger.isDebugEnabled())
								_logger.debug("made credential for pass-through connection: " + newerTC);
						}
					}
				}
			}
		}

		if (!foundAny) {
			_logger.debug("Found zero credentials to delegate for soap header.");
		}
		SOAPMessage msg = messageContext.getMessage();
		final javax.xml.soap.SOAPHeader soapHeader = msg.getSOAPHeader();
		ArrayList<String> credRefs = new ArrayList<>();
		soapHeader
			.addChildElement(walletForResource.convertToSOAPElement((containerGUID != null) ? containerGUID.toString(true) : null, credRefs));
		SOAPHeaderElement refsElem = walletForResource.emitReferencesAsSoap(credRefs);
		if (refsElem != null)
			soapHeader.addChildElement(refsElem);
	}

	@SuppressWarnings("unchecked")
	private void setCallingContextHeaders(MessageContext msgContext) throws AxisFault
	{
		ICallingContext callContext = null;
		if (msgContext.containsProperty(CommConstants.CALLING_CONTEXT_PROPERTY_NAME)) {
			callContext = (ICallingContext) msgContext.getProperty(CommConstants.CALLING_CONTEXT_PROPERTY_NAME);
		}

		if (callContext == null) {
			// should always have a calling context by here.
			throw new AxisFault("failure to find a calling context in the message context");
		} else {
			// update any stale creds
			try {
				ClientUtils.checkAndRenewCredentials(callContext, BaseGridTool.credsValidUntil(), new SecurityUpdateResults());
			} catch (AuthZSecurityException e) {
				throw new GenesisIISecurityException("Could not prepare outgoing calling context: " + e.getMessage(), e);
			}
			// create a new derived calling context transient for the life of this call.
			callContext = callContext.deriveNewContext();
		}

		// load the credentials up that we will be sending out.
		CredentialWallet wallet = new CredentialWallet();
		TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(callContext);
		if (transientCredentials != null) {
			for (NuCredential cred : transientCredentials.getCredentials()) {
				if (cred instanceof TrustCredential) {
					wallet.addCredential((TrustCredential) cred);

					if (_logger.isTraceEnabled())
						_logger.debug("loading cred for outgoing wallet: " + cred.describe(VerbosityLevel.HIGH));
				}
			}
		}

		/*
		 * now zero out any transient credentials that are stored in the copied context still, since those cannot be deserialized using the
		 * old scheme with unicore6 security lib. plus these are just baggage and are not used for any authorization decisions.
		 */
		TransientCredentials.setTransientCredentials(callContext, null);

		/*
		 * process the transient credentials to prepare the serializable portion of the calling context for them.
		 */
		MessageSecurity msgSecData = (MessageSecurity) msgContext.getProperty(CommConstants.MESSAGE_SEC_CALL_DATA);
		try {
			// =======================================================
			// 2020-11-18 ASG. Part of eliminating delegation to RNS, ByteIO, and Lightweight export
			// EndpointReferenceType target = (EndpointReferenceType) msgContext.getProperty(CommConstants.TARGET_EPR_PROPERTY_NAME);
			// if (delegateTo(target))
				delegateCredentials(wallet, callContext, msgContext, msgSecData);
		} catch (Exception ex) {
			_logger.warn("ERROR: Failed to delegate SAML credentials.", ex);
		}

		try {
			if (_logger.isTraceEnabled()) {
				_logger.trace(String.format("Calling Context:\n%s", callContext.describe()));
			}
		} catch (Throwable cause) {
			_logger.warn("Unable to log calling context information.", cause);
		}

		try {
			SOAPHeader header = (SOAPHeader) msgContext.getMessage().getSOAPHeader();
			SOAPHeaderElement context =
				new SOAPHeaderElement(ObjectSerializer.toElement(callContext.getSerialized(), GenesisIIConstants.CONTEXT_INFORMATION_QNAME));
			header.addChildElement(context);

			// specify that we need to sign the calling context
			ArrayList<WSEncryptionPart> signParts =
				(ArrayList<WSEncryptionPart>) msgContext.getProperty(CommConstants.MESSAGE_SEC_SIGN_PARTS);
			if (signParts == null) {
				signParts = new ArrayList<WSEncryptionPart>();
				msgContext.setProperty(CommConstants.MESSAGE_SEC_SIGN_PARTS, signParts);
			}
			signParts.add(new WSEncryptionPart("calling-context", "http://vcgr.cs.virginia.edu/Genesis-II", "Element"));

		} catch (IOException e) {
			throw new AxisFault(e.getLocalizedMessage(), e);
		} catch (SOAPException se) {
			throw new AxisFault(se.getLocalizedMessage(), se);
		}

		if (_logger.isTraceEnabled())
			_logger.trace("context after setting headers:\n" + callContext.dumpContext());
	}

	private void setClientID(MessageContext msgContext) throws AxisFault
	{
		SOAPHeaderElement clientId = new SOAPHeaderElement(GenesisIIConstants.CLIENT_ID_QNAME, ClientIdGenerator.getClientId());
		clientId.setActor(null);
		clientId.setMustUnderstand(false);
		try {
			msgContext.getMessage().getSOAPHeader().addChildElement(clientId);
		} catch (SOAPException se) {
			throw new AxisFault(se.getLocalizedMessage());
		}
	}

	private void setMyProxyCertificateHeaders(MessageContext msgContext) throws AxisFault
	{
		if (_logger.isTraceEnabled())
			_logger.trace("the myproxy header is: " + MyProxyCertificate.getPEMString());
		SOAPHeaderElement pemKey = new SOAPHeaderElement(GenesisIIConstants.MYPROXY_QNAME, MyProxyCertificate.getPEMString());
		try {
			msgContext.getMessage().getSOAPHeader().addChildElement(pemKey);
		} catch (SOAPException se) {
			throw new AxisFault(se.getLocalizedMessage());
		}
	}

	public void invoke(MessageContext msgContext) throws AxisFault
	{
		setMessageID(msgContext);
		setSOAPAction(msgContext);
		setWSAddressingHeaders(msgContext);
		setCallingContextHeaders(msgContext);
		setGenesisIIHeaders(msgContext);
		setClientID(msgContext);
		if (MyProxyCertificate.isAvailable())
			setMyProxyCertificateHeaders(msgContext);
	}
}
