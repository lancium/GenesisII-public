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
package edu.virginia.vcgr.genii.container.kerbauthn;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
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
import org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.axis.security.GIIBouncyCrypto;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.container.commonauthn.BaseAuthenticationServiceImpl;
import edu.virginia.vcgr.genii.container.commonauthn.ReplicaSynchronizer.STSResourcePropertiesRetriever;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.rns.RNSDBResourceProvider;
import edu.virginia.vcgr.genii.container.x509authn.X509AuthnServiceImpl;
import edu.virginia.vcgr.genii.kerbauthn.KerbAuthnPortType;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.XMLCompatible;
import edu.virginia.vcgr.genii.security.axis.WSSecurityUtils;
import edu.virginia.vcgr.genii.security.axis.XMLConverter;
import edu.virginia.vcgr.genii.security.credentials.BasicConstraints;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.identity.IdentityType;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

@GeniiServiceConfiguration(resourceProvider = RNSDBResourceProvider.class, defaultAuthZProvider = KerbAuthZProvider.class)
public class KerbAuthnServiceImpl extends BaseAuthenticationServiceImpl implements KerbAuthnPortType
{
	static private Log _logger = LogFactory.getLog(KerbAuthnServiceImpl.class);

	@MInject(lazy = true)
	private IRNSResource _resource;

	public KerbAuthnServiceImpl() throws RemoteException
	{
		this(WellKnownPortTypes.KERB_AUTHN_SERVICE_PORT_TYPE().getQName().getLocalPart());
	}

	protected KerbAuthnServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);

		addImplementedPortType(WellKnownPortTypes.KERB_AUTHN_SERVICE_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes.STS_SERVICE_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes.RNS_PORT_TYPE());
	}

	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.KERB_AUTHN_SERVICE_PORT_TYPE();
	}

	@Override
	protected void setAttributeHandlers() throws NoSuchMethodException, ResourceException, ResourceUnknownFaultType
	{
		super.setAttributeHandlers();
		new KerbAuthnAttributesHandler(getAttributePackage());
	}

	protected Object translateConstructionParameter(MessageElement property) throws Exception
	{

		// decodes the base64-encoded delegated assertion construction param
		QName name = property.getQName();
		if (name.equals(SecurityConstants.NEW_IDP_NAME_QNAME) || name.equals(SecurityConstants.NEW_KERB_IDP_REALM_QNAME)
			|| name.equals(SecurityConstants.NEW_KERB_IDP_KDC_QNAME)) {
			return property.getValue();
		} else if (name.equals(SecurityConstants.IDP_VALID_MILLIS_QNAME)) {
			return Long.decode(property.getValue());
		} else {
			return super.translateConstructionParameter(property);
		}
	}

	protected ResourceKey createResource(HashMap<QName, Object> constructionParameters) throws ResourceException, BaseFaultType
	{
		// to go away?
		// // Specify additional CN fields for our resource's certificate
		// String[] newCNs = { (String)
		// constructionParameters.get(SecurityConstants.NEW_IDP_NAME_QNAME) };
		//
		// constructionParameters.put(IResource.ADDITIONAL_CNS_CONSTRUCTION_PARAM, newCNs);

		// Specify additional O (org) field for our resource's certificate (viz. realm)
		String realm = (String) constructionParameters.get(SecurityConstants.NEW_KERB_IDP_REALM_QNAME);
		if (realm != null) {
			String[] newOrgs = { realm };
			constructionParameters.put(IResource.ADDITIONAL_ORGS_CONSTRUCTION_PARAM, newOrgs);
		}
		return super.createResource(constructionParameters);
	}

	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR, ConstructionParameters cParams,
		HashMap<QName, Object> constructionParameters, Collection<MessageElement> resolverCreationParams)
		throws ResourceException, BaseFaultType, RemoteException
	{

		if (skipPortTypeSpecificPostProcessing(constructionParameters)) {
			super.postCreate(rKey, newEPR, cParams, constructionParameters, resolverCreationParams);
			return;
		}

		String newIdpName = addResourceInServiceResourceList(newEPR, constructionParameters);

		// get the IDP resource's db resource
		IResource resource = rKey.dereference();
		X509Certificate[] resourceCertChain = (X509Certificate[]) resource
			.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);

		// store the name in the idp resource
		resource.setProperty(SecurityConstants.NEW_IDP_NAME_QNAME.getLocalPart(), newIdpName);

		// store the Realm in the idp resource
		String realm = (String) constructionParameters.get(SecurityConstants.NEW_KERB_IDP_REALM_QNAME);
		resource.setProperty(SecurityConstants.NEW_KERB_IDP_REALM_QNAME.getLocalPart(), realm);

		// store the KDC in the idp resource
		String kdc = (String) constructionParameters.get(SecurityConstants.NEW_KERB_IDP_KDC_QNAME);
		resource.setProperty(SecurityConstants.NEW_KERB_IDP_KDC_QNAME.getLocalPart(), kdc);

		// determine the credential the idp will front
		NuCredential credential = null;
		try {
			credential = new X509Identity(resourceCertChain, IdentityType.OTHER);
			storeCallingContextAndCertificate(resource, credential);
		} catch (IOException e) {
			throw new RemoteException(e.getMessage(), e);
		}

		super.postCreate(rKey, newEPR, cParams, constructionParameters, resolverCreationParams);
	}

	@Override
	protected void preDestroy() throws RemoteException, ResourceException
	{
		super.preDestroy();
		preDestroy(_resource);
	}

	protected RequestSecurityTokenResponseType delegateCredential(X509Certificate[] delegateToChain, Date created, Date expiry)
		throws GeneralSecurityException, SOAPException, ConfigurationException, RemoteException
	{

		NuCredential credential = X509AuthnServiceImpl.loadResourceCredential(_resource);

		// Get this resource's assertion, key and cert material
		ICallingContext callingContext = null;
		KeyAndCertMaterial resourceKeyMaterial = null;
		try {
			callingContext = ContextManager.getExistingContext();
			resourceKeyMaterial = callingContext.getActiveKeyAndCertMaterial();
		} catch (IOException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		}

		XMLCompatible xup = null;
		if (delegateToChain != null) {
			// Delegate the assertion to the identity in delegateToChain.
			if (credential instanceof TrustCredential) {
				_logger.info("actually seeing a trust delegation assertion: " + credential.toString());
				TrustCredential wrapped = (TrustCredential) credential;
				TrustCredential newTC = new TrustCredential(delegateToChain, IdentityType.OTHER,
					resourceKeyMaterial._clientCertChain, wrapped.getDelegateeType(), new BasicConstraints(created.getTime(),
						expiry.getTime() - created.getTime(), SecurityConstants.MaxDelegationDepth),
					TrustCredential.FULL_ACCESS);
				newTC.signAssertion(resourceKeyMaterial._clientPrivateKey);
				xup = XMLConverter.upscaleCredential(newTC);
			} else {
				// we're not an authentication proxy, so just store our identity.
				IdentityType type = IdentityType.USER;
				// hmmm: fix the identity type here.
				/*
				 * IdentityType.UNSPECIFIED; if (typeString != null) type =
				 * IdentityType.valueOf(typeString); if (type == IdentityType.UNSPECIFIED) { type =
				 * IdentityType.OTHER; if (_logger.isDebugEnabled())
				 * _logger.debug("converting unknown type string '" + typeString + "' as OTHER."); }
				 */
				X509Identity cred = new X509Identity(resourceKeyMaterial._clientCertChain, type);
				xup = XMLConverter.upscaleCredential(cred);
			}
		} else {
			// no delegatee, so just pack this thing.
			xup = XMLConverter.upscaleCredential(credential);
		}

		// ----- assemble the response document
		// -----------------------------------------

		RequestSecurityTokenResponseType response = new RequestSecurityTokenResponseType();
		MessageElement[] elements = new MessageElement[2];
		response.set_any(elements);

		// Add TokenType element
		// XMLCompatible xup = XMLConverter.upscaleCredential(credential);
		if (xup == null) {
			String msg = "unknown type of credential; cannot upscale to XMLCompatible: " + credential.toString();
			_logger.error(msg);
			throw new GeneralSecurityException(msg);
		}
		elements[0] = new MessageElement(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "TokenType"),
			xup.getTokenType());
		elements[0].setType(new QName("http://www.w3.org/2001/XMLSchema", "anyURI"));

		MessageElement wseTokenRef = (MessageElement) xup.convertToMessageElement();

		elements[1] = new MessageElement(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
			"RequestedSecurityToken"), new RequestedSecurityTokenType(new MessageElement[] { wseTokenRef }));
		elements[1].setType(RequestedProofTokenType.getTypeDesc().getXmlType());

		return response;
	}

	protected ArrayList<RequestSecurityTokenResponseType> aggregateBaggageTokens(RequestSecurityTokenType request)
		throws java.rmi.RemoteException
	{

		ArrayList<RequestSecurityTokenResponseType> gatheredResponses = new ArrayList<RequestSecurityTokenResponseType>();

		Collection<InternalEntry> entries;

		entries = _resource.retrieveEntries(null);

		for (InternalEntry entry : entries) {

			try {
				EndpointReferenceType idpEpr = entry.getEntryReference();

				// create a proxy to the remote idp and invoke it
				KerbAuthnPortType idp = ClientUtils.createProxy(KerbAuthnPortType.class, idpEpr);
				RequestSecurityTokenResponseType[] responses = idp.requestSecurityToken2(request);

				if (responses != null) {
					for (RequestSecurityTokenResponseType response : responses) {
						gatheredResponses.add(response);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("Could not retrieve token for IDP " + entry.getName() + ": " + e.getMessage(), e);
			}
		}

		return gatheredResponses;
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public RequestSecurityTokenResponseType[] requestSecurityToken2(RequestSecurityTokenType request)
		throws java.rmi.RemoteException
	{

		// ------ Parse and perform syntactic checks (has correct form) --------

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
										if (subElement.getAttributeValue("ValueType").equals(
											edu.virginia.vcgr.genii.client.comm.CommConstants.X509_SECURITY_TYPE)) {
											X509Security bstToken = new X509Security(subElement);
											X509Certificate delegateTo = bstToken.getX509Certificate(new GIIBouncyCrypto());
											delegateToChain = new X509Certificate[] { delegateTo };
										} else {
											if (delegateToChain == null) {
												throw new AxisFault(new QName(
													"http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "BadRequest"),
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
		if ((requestType == null)
			|| !requestType.getRequestTypeEnumValue().toString().equals(RequestTypeEnum._value1.toString())) {
			throw new AxisFault(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "BadRequest"),
				"IDP cannot service a request of type "
					+ ((requestType == null) ? "null" : requestType.getRequestTypeEnumValue()), null, null);
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

		// ------ Assemble response ------------------------------------------

		ArrayList<RequestSecurityTokenResponseType> responseArray = new ArrayList<RequestSecurityTokenResponseType>();

		try {
			// add the local token
			responseArray.add(delegateCredential(delegateToChain, created, expiry));

			// add the listed tokens
			responseArray.addAll(aggregateBaggageTokens(request));
		} catch (GeneralSecurityException e) {
			throw new WSSecurityException(e.getMessage(), e);
		} catch (SOAPException se) {
			throw new AxisFault(se.getLocalizedMessage(), se);
		} catch (ConfigurationException ce) {
			throw new RemoteException(ce.getMessage(), ce);
		}

		return responseArray.toArray(new RequestSecurityTokenResponseType[responseArray.size()]);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] add(RNSEntryType[] addRequest) throws RemoteException, RNSEntryExistsFaultType,
		ResourceUnknownFaultType
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
	public RNSEntryResponseType[] setMetadata(MetadataMappingType[] setMetadataRequest) throws RemoteException,
		WriteNotPermittedFaultType
	{
		throw new RemoteException("\"setMetadata\" not applicable.");
	}

	@Override
	public STSResourcePropertiesRetriever getResourcePropertyRetriver()
	{
		return new kerberosResourcePropertiesRetriever();
	}

	@Override
	public Set<QName> getSensitivePropertyNames()
	{
		Set<QName> propertyNames = super.getSensitivePropertyNames();
		propertyNames.add(SecurityConstants.NEW_KERB_IDP_REALM_QNAME);
		propertyNames.add(SecurityConstants.NEW_KERB_IDP_KDC_QNAME);
		return propertyNames;
	}

	public static class kerberosResourcePropertiesRetriever extends CommonSTSPropertiesRetriever
	{

		@Override
		public void retrieveAndStoreResourceProperties(GeniiCommon proxyToPrimary, IRNSResource resource) throws Exception
		{

			super.retrieveAndStoreResourceProperties(proxyToPrimary, resource);

			QName[] propertyNames = new QName[2];
			propertyNames[0] = SecurityConstants.NEW_KERB_IDP_REALM_QNAME;
			propertyNames[1] = SecurityConstants.NEW_KERB_IDP_KDC_QNAME;
			GetMultipleResourcePropertiesResponse response = proxyToPrimary.getMultipleResourceProperties(propertyNames);

			MessageElement[] propertyValues = response.get_any();
			if (propertyValues == null || propertyValues.length < propertyNames.length) {
				throw new RemoteException("Could not retrieve all necessary resource properties");
			}

			for (MessageElement element : propertyValues) {
				QName name = element.getQName();
				String value = element.getValue();
				if (value == null)
					throw new RuntimeException("A required Kerberos attribute is missing.");
				if (SecurityConstants.NEW_KERB_IDP_REALM_QNAME.equals(name)) {
					resource.setProperty(SecurityConstants.NEW_KERB_IDP_REALM_QNAME.getLocalPart(), value);
				} else if (SecurityConstants.NEW_KERB_IDP_KDC_QNAME.equals(name)) {
					resource.setProperty(SecurityConstants.NEW_KERB_IDP_KDC_QNAME.getLocalPart(), value);
				}
			}
		}
	}
}
