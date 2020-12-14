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
package edu.virginia.vcgr.genii.container.x509authn;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.X509Security;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.MetadataMappingType;
import org.ggf.rns.NameMappingType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSEntryType;
import org.ggf.rns.WriteNotPermittedFaultType;
import org.morgan.inject.MInject;
import org.morgan.util.configuration.ConfigurationException;
import org.oasis_open.docs.ws_sx.ws_trust._200512.DelegateToType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.LifetimeType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestTypeEnum;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestTypeOpenEnum;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestedProofTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestedSecurityTokenType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.comm.axis.security.GIIBouncyCrypto;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.utils.StatsLogger;
import edu.virginia.vcgr.genii.container.commonauthn.BaseAuthenticationServiceImpl;
import edu.virginia.vcgr.genii.container.commonauthn.ReplicaSynchronizer.STSResourcePropertiesRetriever;
import edu.virginia.vcgr.genii.container.commonauthn.STSCertificationSpec;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.kerbauthn.KerbAuthnServiceImpl;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;
import edu.virginia.vcgr.genii.container.rns.RNSContainerUtilities;
import edu.virginia.vcgr.genii.container.rns.RNSDBResourceProvider;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.XMLCompatible;
import edu.virginia.vcgr.genii.security.axis.AxisCredentialWallet;
import edu.virginia.vcgr.genii.security.axis.WSSecurityUtils;
import edu.virginia.vcgr.genii.security.axis.XMLConverter;
import edu.virginia.vcgr.genii.security.credentials.BasicConstraints;
import edu.virginia.vcgr.genii.security.credentials.FullX509Identity;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.identity.IdentityType;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.x509authn.X509AuthnPortType;

@GeniiServiceConfiguration(resourceProvider = RNSDBResourceProvider.class)
public class X509AuthnServiceImpl extends BaseAuthenticationServiceImpl implements X509AuthnPortType
{
	static private Log _logger = LogFactory.getLog(X509AuthnServiceImpl.class);
	// 2020-12-1 by ASG
	// keyInEPR is intended as a replacement for instanceof(GeniiNoOutcalls) which was a bit hacky.
	// If it is "true", we will not put key material in the X.509. This will in turn prevent delegation to instances
	// of a type that returns true, and will make transporting and storing EPR's consume MUCH less space.
	public boolean keyInEPR() {
		return true;
	}

	@MInject(lazy = true)
	private IRNSResource _resource;

	public X509AuthnServiceImpl() throws RemoteException
	{
		this(WellKnownPortTypes.X509_AUTHN_SERVICE_PORT_TYPE().getQName().getLocalPart());
	}

	protected X509AuthnServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);

		addImplementedPortType(WellKnownPortTypes.X509_AUTHN_SERVICE_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes.STS_SERVICE_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes.RNS_PORT_TYPE());
	}

	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.X509_AUTHN_SERVICE_PORT_TYPE();
	}

	@Override
	protected Object translateConstructionParameter(MessageElement property) throws Exception
	{
		// decodes the base64-encoded delegated assertion construction param
		QName name = property.getQName();
		if (name.equals(SecurityConstants.NEW_IDP_NAME_QNAME)) {
			return property.getValue();
		} else if (name.equals(SecurityConstants.IDP_VALID_MILLIS_QNAME)) {
			return Long.decode(property.getValue());
		} else if (name.equals(SecurityConstants.NEW_IDP_TYPE_QNAME)) {
			if (_logger.isDebugEnabled())
				_logger.debug("for name " + name + " got " + property.getValue());
			return property.getValue();
		} else {
			return super.translateConstructionParameter(property);
		}
	}

	@Override
	protected void preDestroy() throws RemoteException, ResourceException
	{
		super.preDestroy();
		preDestroy(_resource);
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public RequestSecurityTokenResponseType[] requestSecurityToken2(RequestSecurityTokenType request) throws java.rmi.RemoteException
	{
		return sharedSecurityTokenResponder(this, _resource, request);
	}

	/**
	 * preconditions:
	 * 
	 * 1) the client has passed us an x509 session certificate that we will delegate to. this is the delegatee of the credentials we'll build.
	 * 
	 * 2) the client has given us sufficient information to authenticate against the STS here; this may be a password or the mere fact that
	 * they have a particular TLS cert.
	 * 
	 */
	public static RequestSecurityTokenResponseType[] sharedSecurityTokenResponder(BaseAuthenticationServiceImpl theThis,
		IRNSResource resource, RequestSecurityTokenType request) throws java.rmi.RemoteException
	{
		// Parse and perform syntactic checks (look for correct form).
		RequestTypeOpenEnum requestType = null;
		LifetimeType lifetime = null;
		X509Certificate[] delegateToChain = null;

		for (MessageElement element : request.get_any()) {
			if (element.getName().equals("TokenType")) {
				// process TokenType element
				// String tokenType = element.getValue();
			} else if (element.getName().equals("RequestType")) {
				// process RequestType element
				try {
					requestType = (RequestTypeOpenEnum) element.getObjectValue(RequestTypeOpenEnum.class);
				} catch (Exception e) {
				}
			} else if (element.getName().equals("Lifetime")) {
				// process LifeTime element
				try {
					lifetime = (LifetimeType) element.getObjectValue(LifetimeType.class);
				} catch (Exception e) {
				}
			} else if (element.getName().equals("DelegateTo")) {
				// process DelegateTo element
				DelegateToType dt = null;
				try {
					dt = (DelegateToType) element.getObjectValue(DelegateToType.class);
				} catch (Exception e) {
				}
				if (dt != null) {
					for (MessageElement subElement : dt.get_any()) {
						if (WSSecurityUtils.matchesSecurityToken(subElement)) {
							subElement = WSSecurityUtils.acquireChildSecurityElement(subElement, "Embedded");
							if (subElement != null) {
								subElement = subElement.getChildElement(BinarySecurity.TOKEN_BST);
								if (subElement != null) {
									try {
										if (subElement.getAttributeValue("ValueType")
											.equals(edu.virginia.vcgr.genii.client.comm.CommConstants.X509_SECURITY_TYPE)) {
											X509Security bstToken = new X509Security(subElement);
											X509Certificate delegateTo = bstToken.getX509Certificate(new GIIBouncyCrypto());
											delegateToChain = new X509Certificate[] { delegateTo };
										} else {
											if (delegateToChain == null) {
												throw new AxisFault(
													new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "BadRequest"),
													"Missing or unsupported DelegateTo security ValueType", null, null);
											}
										}
									} catch (GenesisIISecurityException e) {
										throw new WSSecurityException(e.getMessage(), e);
									} catch (WSSecurityException e) {
										throw new WSSecurityException(e.getMessage(), e);
									} catch (IOException e) {
										throw new WSSecurityException(e.getMessage(), e);
									} catch (CredentialException e) {
										throw new WSSecurityException(e.getMessage(), e);
									}
								}
							}
						}
					}
				}
			}
		}

		// check request type
		if ((requestType == null) || !requestType.getRequestTypeEnumValue().toString().equals(RequestTypeEnum._value1.toString())) {
			throw new AxisFault(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "BadRequest"),
				"IDP cannot service a request of type " + ((requestType == null) ? "null" : requestType.getRequestTypeEnumValue()), null,
				null);
		}

		// check lifetime element
		if (lifetime == null) {
			throw new AxisFault(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "InvalidRequest"),
				"Missing Lifetime parameter", null, null);
		}

		SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		zulu.setTimeZone(TimeZone.getTimeZone("ZULU"));
		Date created = zulu.parse(lifetime.getCreated().get_value(), new ParsePosition(0));
		Date expiry = zulu.parse(lifetime.getExpires().get_value(), new ParsePosition(0));

		if ((created == null) || (expiry == null)) {
			throw new AxisFault(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "InvalidRequest"),
				"Could not parse lifetime dates", null, null);
		}

		// Assemble response.
		ArrayList<RequestSecurityTokenResponseType> responseArray = new ArrayList<RequestSecurityTokenResponseType>();

		try {
			/*
			 * do the first authentication against this resource, which should be an STS, and decide if the caller has the right to assume
			 * that identity.
			 */
			TrustCredential tc = delegateCredential(theThis, resource, delegateToChain, created, expiry);
			RequestSecurityTokenResponseType response = createResponse(theThis, tc);
			responseArray.add(response);

			/*
			 * aggregate the group identities or other things we find under the STS by attempting to authenticate against each item found.
			 */
			if (theThis instanceof BaggageAggregatable) {
				BaggageAggregatable bagger = (BaggageAggregatable) theThis;
				responseArray.addAll(bagger.aggregateBaggageTokens(resource, request));
			} else {
				_logger.error("unknown type to aggregate baggage for: " + theThis.getClass().getName());
			}
		} catch (AuthZSecurityException e) {
			throw new WSSecurityException(e.getMessage(), e);
		} catch (SOAPException se) {
			throw new AxisFault(se.getLocalizedMessage(), se);
		} catch (ConfigurationException ce) {
			throw new RemoteException(ce.getMessage(), ce);
			// } catch (FileNotFoundException e) {
			// throw new AxisFault(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			throw new AxisFault(e.getLocalizedMessage(), e);
		}

		return responseArray.toArray(new RequestSecurityTokenResponseType[responseArray.size()]);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] add(RNSEntryType[] addRequest) throws RemoteException, RNSEntryExistsFaultType, ResourceUnknownFaultType
	{
		return addRNSEntries(addRequest, _resource);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public LookupResponseType lookup(String[] lookupRequest) throws RemoteException, ResourceUnknownFaultType
	{
		return lookup(lookupRequest, _resource);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] remove(String[] removeRequest) throws RemoteException, WriteNotPermittedFaultType
	{
		return remove(removeRequest, _resource);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] rename(NameMappingType[] renameRequest) throws RemoteException, WriteNotPermittedFaultType
	{
		throw new RemoteException("\"rename\" not applicable.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] setMetadata(MetadataMappingType[] setMetadataRequest) throws RemoteException, WriteNotPermittedFaultType
	{
		throw new RemoteException("\"setMetadata\" not applicable.");
	}

	@Override
	public STSResourcePropertiesRetriever getResourcePropertyRetriver()
	{
		return new CommonSTSPropertiesRetriever();
	}

	/**
	 * used by both x509 and kerberos authorization.
	 */
	public static void sharedPostCreate(BaseAuthenticationServiceImpl theThis, ResourceKey rKey, EndpointReferenceType newEPR,
		ConstructionParameters cParams, GenesisHashMap constructionParameters, Collection<MessageElement> resolverCreationParams)
		throws ResourceException, BaseFaultType, RemoteException
	{
		// determine the credential the idp will front.
		NuCredential credential = null;
		MessageElement encodedCredential = constructionParameters.getMessageElement(SecurityConstants.IDP_STORED_CREDENTIAL_QNAME);

		// get the IDP resource's db resource
		IResource resource = rKey.dereference();
		X509Certificate[] resourceCertChain = (X509Certificate[]) resource.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);

		String newIdpName = theThis.addResourceInServiceResourceList(newEPR, constructionParameters);

		// store the name in the idp resource
		resource.setProperty(SecurityConstants.NEW_IDP_NAME_QNAME.getLocalPart(), newIdpName);

		KeyAndCertMaterial resourceKeyMaterial = null;
		try {
			resourceKeyMaterial = ContextManager.getExistingContext().getActiveKeyAndCertMaterial();
		} catch (IOException e) {
			throw new RemoteException(e.getMessage(), e);

		}

		STSCertificationSpec stsCertificationSpec = null;
		stsCertificationSpec = (STSCertificationSpec) constructionParameters.get(IResource.CERTIFICATE_CREATION_SPEC_CONSTRUCTION_PARAM);
		PrivateKey privateKey = stsCertificationSpec.getSubjectPrivateKey();
		if ((stsCertificationSpec == null) || (stsCertificationSpec.issuerPrivateKey == null)) {
			_logger.error("there is no certificate stored for the authorization; using container keys instead.");
			privateKey = resourceKeyMaterial._clientPrivateKey;
		}

		try {
			if (encodedCredential != null) {
				if (_logger.isDebugEnabled())
					_logger.debug("building credential wallet from element.");

				AxisCredentialWallet wallet = null;
				try {
					wallet = new AxisCredentialWallet(encodedCredential, null, null, null);
				} catch (Throwable t) {
					_logger.error("caught unexpected problem when decoding response from auth request", t);
					throw new RemoteException(t.getLocalizedMessage(), t);
				}

				if (wallet.getRealCreds().isEmpty()) {
					_logger.error("found no credentials in encoded chunk.");
					return;
				}
				credential = wallet.getRealCreds().getCredentials().get(0);

				if (_logger.isDebugEnabled())
					_logger.debug("our wallet retrieved from soap is:\n" + wallet.getRealCreds().describe(VerbosityLevel.HIGH));

				if (credential instanceof TrustCredential) {
					if (_logger.isDebugEnabled())
						_logger.debug("seeing a trust credential to process: " + credential.toString());

					TrustCredential wrapped = (TrustCredential) credential;
					/*
					 * Delegate the assertion to delegateTo. note that we are using a ridiculously long time limit here rather than not create
					 * a basic constraints object.
					 */
					TrustCredential newTC = new TrustCredential(resourceCertChain, IdentityType.OTHER, resourceKeyMaterial._clientCertChain,
						wrapped.getDelegateeType(),
						new BasicConstraints(System.currentTimeMillis() - SecurityConstants.CredentialGoodFromOffset, Long.MAX_VALUE,
							SecurityConstants.MaxDelegationDepth),
						RWXCategory.FULL_ACCESS);
					newTC.extendTrustChain(wrapped);
					newTC.signAssertion(privateKey);
					credential = newTC;

					boolean paranoidChecking = false;
					if (paranoidChecking) {
						boolean worked = TrustCredential.paranoidSerializationCheck(newTC);
						if (!worked) {
							_logger.error("failed paranoid serialization check!  see logging in prior lines.");
						}
					}

				} else if (credential instanceof X509Identity) {
					if (_logger.isDebugEnabled())
						_logger.debug("failure: seeing x509 identity to process from wire: " + credential.toString());
				}

			} else {
				// we're not an authentication proxy, so just store our identity.
				IdentityType type = IdentityType.OTHER;
				// Get Identity type from type name if we can.
				String typeString = (String) constructionParameters.get(SecurityConstants.NEW_IDP_TYPE_QNAME);
				if (typeString != null)
					type = IdentityType.valueOf(typeString);
				if (theThis instanceof KerbAuthnServiceImpl) {
					type = IdentityType.USER;
				}
				if (type == IdentityType.UNSPECIFIED) {
					type = IdentityType.OTHER;
					if (_logger.isDebugEnabled())
						_logger.debug("converting unknown type string '" + typeString + "' as OTHER.");
				}
				credential = new FullX509Identity(resourceCertChain, type, privateKey);
			}

			theThis.storeCallingContextAndCertificate(resource, credential);

		} catch (IOException e) {
			throw new RemoteException(e.getMessage(), e);
		}
	}

	@Override
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR, ConstructionParameters cParams,
		GenesisHashMap constructionParameters, Collection<MessageElement> resolverCreationParams)
		throws ResourceException, BaseFaultType, RemoteException
	{
		if (skipPortTypeSpecificPostProcessing(constructionParameters)) {
			super.postCreate(rKey, newEPR, cParams, constructionParameters, resolverCreationParams);
			return;
		}
		sharedPostCreate(this, rKey, newEPR, cParams, constructionParameters, resolverCreationParams);
		super.postCreate(rKey, newEPR, cParams, constructionParameters, resolverCreationParams);
	}

	/**
	 * builds the initial credential for the client that states that the STS trusts the client's TLS cert.
	 */
	public static TrustCredential delegateCredential(BaseAuthenticationServiceImpl theThis, IRNSResource resource,
		X509Certificate[] delegateToChain, Date created, Date expiry)
		throws AuthZSecurityException, SOAPException, ConfigurationException, RemoteException
	{
		TrustCredential newTC = null;

		if (delegateToChain != null)
			_logger.info("delegating to " + delegateToChain[0].getSubjectDN());

		NuCredential resourceCred = RNSContainerUtilities.loadRNSResourceCredential(resource);
		_logger.debug("resource's credential is: " + resourceCred.toString());
		// 2014-11-05 ASG - adding logging
		String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);
		StatsLogger.logStats(
			"X509AuthnServiceImpl: authenticating " + resourceCred.getOriginalAsserter()[0].getSubjectDN() + " to client at " + caller);
		// End logging

		KeyAndCertMaterial resourceKeyMaterial = null;
		if (delegateToChain == null) {
			_logger.debug("delegate to chain was null...  ignoring credential.");
		} else {
			// delegate to the chain we were given...
			// Get this resource's assertion, key and cert material
			ICallingContext callingContext = null;
			try {
				callingContext = ContextManager.getExistingContext();
				resourceKeyMaterial = callingContext.getActiveKeyAndCertMaterial();
				if (resourceKeyMaterial == null)
					throw new IOException("failed to load resource's key material!");
			} catch (IOException e) {
				throw new AuthZSecurityException(e.getMessage(), e);
			}

			// to be filled with a valid delegated credential, if possible.
			if (resourceCred instanceof TrustCredential) {
				if (_logger.isDebugEnabled())
					_logger.debug("wrapping this trust credential: " + resourceCred.toString());
				TrustCredential wrapped = (TrustCredential) resourceCred;
				// Delegate the assertion to delegateTo.
				newTC =
					new TrustCredential(delegateToChain, IdentityType.OTHER, resourceKeyMaterial._clientCertChain, wrapped.getDelegateeType(),
						new BasicConstraints(created.getTime(), expiry.getTime() - created.getTime(), SecurityConstants.MaxDelegationDepth),
						RWXCategory.FULL_ACCESS);
				newTC.signAssertion(resourceKeyMaterial._clientPrivateKey);
			} else if (resourceCred instanceof FullX509Identity) {
				if (_logger.isDebugEnabled())
					_logger.debug("creating trust credential from full x509 with key: " + resourceCred.toString());
				FullX509Identity realId = (FullX509Identity) resourceCred;
				// Delegate the assertion to delegateTo.
				newTC = new TrustCredential(delegateToChain, IdentityType.CONNECTION, realId.getOriginalAsserter(), realId.getType(),
					new BasicConstraints(created.getTime(), expiry.getTime() - created.getTime(), SecurityConstants.MaxDelegationDepth),
					RWXCategory.FULL_ACCESS);
				newTC.signAssertion(realId.getKey());
			} else if (resourceCred instanceof X509Identity) {
				if (_logger.isDebugEnabled())
					_logger.debug("creating trust credential from x509: " + resourceCred.toString());
				X509Identity realId = (X509Identity) resourceCred;
				// Delegate the assertion to delegateTo.
				newTC = new TrustCredential(delegateToChain, IdentityType.CONNECTION, realId.getOriginalAsserter(), realId.getType(),
					new BasicConstraints(created.getTime(), expiry.getTime() - created.getTime(), SecurityConstants.MaxDelegationDepth),
					RWXCategory.FULL_ACCESS);
				newTC.signAssertion(resourceKeyMaterial._clientPrivateKey);
			} else {
				_logger.error("failure, unknown type of assertion found.");
			}
		}
		return newTC;
	}

	public static RequestSecurityTokenResponseType createResponse(BaseAuthenticationServiceImpl theThis, TrustCredential newTC)
		throws AuthZSecurityException
	{
		AxisCredentialWallet creds = new AxisCredentialWallet();
		// the wallet will come back empty unless the newTC is non-null.
		if (newTC != null) {
			// add the newly minted credential into the list to send back.
			creds.getRealCreds().addCredential(newTC);
		}

		// assemble the response document
		RequestSecurityTokenResponseType response = new RequestSecurityTokenResponseType();
		MessageElement[] elements = new MessageElement[2];

		// Add TokenType element
		XMLCompatible xup = XMLConverter.upscaleCredential(newTC);
		if (xup == null) {
			String msg = "unknown type of credential; cannot upscale to XMLCompatible: " + newTC.toString();
			_logger.error(msg);
			throw new AuthZSecurityException(msg);
		}
		elements[0] = new MessageElement(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "TokenType"), xup.getTokenType());
		elements[0].setType(new QName("http://www.w3.org/2001/XMLSchema", "anyURI"));

		SOAPHeaderElement elemConvert = creds.convertToSOAPElement(null, null);

		/*
		 * CAK: this is where the second problem dives into problematic conversion process (landing in xerces with DOM error). it turns out we
		 * were making an extra copy of the message element that was already in axis form. that was an undetected waste of cycles previously,
		 * but now is a deadly namespace error. great fun.
		 */
		MessageElement[] delegations = new MessageElement[] { elemConvert };

		elements[1] = new MessageElement(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "RequestedSecurityToken"),
			new RequestedSecurityTokenType(delegations));
		elements[1].setType(RequestedProofTokenType.getTypeDesc().getXmlType());

		response.set_any(elements);

		return response;
	}

}
