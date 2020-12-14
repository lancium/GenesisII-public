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
package edu.virginia.vcgr.genii.container.jndiauthn;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.PKIPathSecurity;
import org.apache.ws.security.message.token.X509Security;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.MetadataMappingType;
import org.ggf.rns.NameMappingType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSEntryType;
import org.ggf.rns.ReadNotPermittedFaultType;
import org.ggf.rns.WriteNotPermittedFaultType;
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
import org.ogf.schemas.naming._2006._08.naming.ResolveFailedFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.comm.axis.Elementals;
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
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileRequestType;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileResponseType;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.genii.jndiauthn.JNDIAuthnPortType;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.XMLCompatible;
import edu.virginia.vcgr.genii.security.axis.AxisCredentialWallet;
import edu.virginia.vcgr.genii.security.axis.WSSecurityUtils;
import edu.virginia.vcgr.genii.security.axis.XMLConverter;
import edu.virginia.vcgr.genii.security.credentials.BasicConstraints;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.identity.IdentityType;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;
import edu.virginia.vcgr.genii.security.x509.CertCreationSpec;
import edu.virginia.vcgr.genii.security.x509.CertTool;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

@GeniiServiceConfiguration(resourceProvider = JNDIResourceProvider.class, defaultAuthZProvider = JNDIAuthZProvider.class)
public class JNDIAuthnServiceImpl extends GenesisIIBase implements JNDIAuthnPortType, EnhancedRNSPortType
{

	static private Log _logger = LogFactory.getLog(JNDIAuthnServiceImpl.class);
	
	// 2020-12-1 by ASG
	// keyInEPR is intended as a replacement for instanceof(GeniiNoOutcalls) which was a bit hacky.
	// If it is "true", we will not put key material in the X.509. This will in turn prevent delegation to instances
	// of a type that returns true, and will make transporting and storing EPR's consume MUCH less space.
	public boolean keyInEPR() {
		return true;
	}

	public JNDIAuthnServiceImpl() throws RemoteException
	{
		this(WellKnownPortTypes.JNDI_AUTHN_SERVICE_PORT_TYPE().getQName().getLocalPart());
	}

	protected JNDIAuthnServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);

	}

	@Override
	public String getMasterType(ResourceKey rKey) throws ResourceException, ResourceUnknownFaultType
	{

		if ((rKey == null) || (!(rKey.dereference() instanceof IJNDIResource))) {
			// JNDIAuthnPortType
			return new String("JNDIAuthnPortType");
		}

		IJNDIResource serviceResource = (IJNDIResource) rKey.dereference();

		if (serviceResource.isServiceResource()) {
			// JNDIAuthnPortType
			return new String("JNDIAuthnPortType");
		} else if (serviceResource.isIdpResource()) {
			// individual IDP resource
			return new String("IndividualIDPResource");
		}

		// STS for a JNDI directory resource
		return new String("STSForJNDIAuthnPortType");

	}

	/**
	 * Return different implemented port types depending on who we are
	 */
	@Override
	public PortType[] getImplementedPortTypes(ResourceKey rKey) throws ResourceException, ResourceUnknownFaultType
	{
		if ((rKey == null) || (!(rKey.dereference() instanceof IJNDIResource))) {
			// JNDIAuthnPortType
			PortType[] response = { WellKnownPortTypes.RNS_PORT_TYPE(), WellKnownPortTypes.JNDI_AUTHN_SERVICE_PORT_TYPE() };

			return response;
		}

		IJNDIResource serviceResource = (IJNDIResource) rKey.dereference();

		if (serviceResource.isServiceResource()) {
			// JNDIAuthnPortType
			PortType[] response = { WellKnownPortTypes.RNS_PORT_TYPE(), WellKnownPortTypes.JNDI_AUTHN_SERVICE_PORT_TYPE() };

			return response;
		} else if (serviceResource.isIdpResource()) {
			// individual IDP resource
			PortType[] response = { WellKnownPortTypes.STS_SERVICE_PORT_TYPE(), };

			return response;
		}

		// STS for a JNDI directory resource
		PortType[] response =
			{ WellKnownPortTypes.STS_SERVICE_PORT_TYPE(), WellKnownPortTypes.ENHANCED_RNS_PORT_TYPE(), WellKnownPortTypes.RNS_PORT_TYPE(), };

		return response;
	}

	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.JNDI_AUTHN_SERVICE_PORT_TYPE();
	}

	/**
	 * Quick test for overriding classes to implement should they desire to disable resource creation on this endpoint
	 * 
	 * @return false.
	 */
	@Override
	protected boolean allowVcgrCreate() throws ResourceException, ResourceUnknownFaultType
	{
		ResourceKey serviceKey = ResourceManager.getCurrentResource();
		IJNDIResource serviceResource = (IJNDIResource) serviceKey.dereference();

		// only allow remote creation on the JNDIAuthnPortType endpoint resource
		if (!serviceResource.isServiceResource()) {
			return false;
		}

		return true;
	}

	@Override
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR, ConstructionParameters cParams,
		GenesisHashMap constructionParameters, Collection<MessageElement> resolverCreationParams)
		throws ResourceException, BaseFaultType, RemoteException
	{

		ResourceKey myKey = ResourceManager.getCurrentResource();
		IJNDIResource myResource = (IJNDIResource) myKey.dereference();
		if (!myResource.isServiceResource()) {
			// we're an STS resource creating directory entries
			super.postCreate(rKey, newEPR, cParams, constructionParameters, resolverCreationParams);
			return;
		}

		// we're the service resource creating STS entries

		// make sure the specific STS doesn't yet exist
		String newStsName = (String) constructionParameters.get(SecurityConstants.NEW_JNDI_STS_NAME_QNAME);
		Collection<String> entries = myResource.listEntries(null);
		if (entries.contains(newStsName)) {
			throw edu.virginia.vcgr.genii.client.wsrf.FaultManipulator
				.fillInFault(new RNSEntryExistsFaultType(null, null, null, null, null, null, newStsName));
		}

		// add the entry to the service's list of STSs
		myResource.addEntry(new InternalEntry(newStsName, newEPR, null));
		myResource.commit();

		super.postCreate(rKey, newEPR, cParams, constructionParameters, resolverCreationParams);
	}

	@Override
	protected Object translateConstructionParameter(MessageElement property) throws Exception
	{

		// decodes the base64-encoded delegated assertion construction param
		QName name = property.getQName();
		if (name.equals(SecurityConstants.NEW_JNDI_NISDOMAIN_QNAME)) {
			return property.getValue();
		} else if (name.equals(SecurityConstants.NEW_JNDI_STS_HOST_QNAME)) {
			return property.getValue();
		} else if (name.equals(SecurityConstants.NEW_JNDI_STS_NAME_QNAME)) {
			return property.getValue();
		} else if (name.equals(SecurityConstants.NEW_JNDI_STS_SEARCHBASE_QNAME)) {
			return property.getValue();
		} else if (name.equals(SecurityConstants.NEW_JNDI_STS_TYPE_QNAME)) {
			return property.getValue();
		} else {
			return super.translateConstructionParameter(property);
		}
	}

	@Override
	protected CertCreationSpec getChildCertSpec() throws ResourceException, ResourceUnknownFaultType, ConfigurationException
	{

		ResourceKey myKey = ResourceManager.getCurrentResource();
		IJNDIResource myResource = (IJNDIResource) myKey.dereference();
		if (!myResource.isServiceResource()) {
			// Returns null because we return unbound eprs for
			// new IDP resources
			return null;
		}
		return super.getChildCertSpec();
	}

	protected RequestSecurityTokenResponseType formatResponse(X509Certificate[] delegateToChain, Date created, Date expiry)
		throws AuthZSecurityException, SOAPException, ConfigurationException, RemoteException
	{

		if (delegateToChain != null) {
			// do delegation if necessary
			return formatDelegateToken(delegateToChain, created, expiry);
		}
		return formatIdentity();
	}

	protected RequestSecurityTokenResponseType formatIdentity()
		throws AuthZSecurityException, SOAPException, ConfigurationException, RemoteException
	{

		ResourceKey rKey = ResourceManager.getCurrentResource();
		IResource resource = rKey.dereference();

		X509Certificate[] identity = (X509Certificate[]) resource.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);

		// ----- assemble the response document
		// -----------------------------------------

		RequestSecurityTokenResponseType response = new RequestSecurityTokenResponseType();
		MessageElement[] elements = new MessageElement[2];

		// Add TokenType element
		elements[0] =
			new MessageElement(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "TokenType"), PKIPathSecurity.getType());
		elements[0].setType(new QName("http://www.w3.org/2001/XMLSchema", "anyURI"));

		MessageElement wseTokenRef = WSSecurityUtils.makePkiPathSecTokenRef(identity);

		elements[1] = new MessageElement(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "RequestedSecurityToken"),
			new RequestedSecurityTokenType(new MessageElement[] { wseTokenRef }));
		elements[1].setType(RequestedProofTokenType.getTypeDesc().getXmlType());

		response.set_any(elements);
		return response;
	}

	protected RequestSecurityTokenResponseType formatDelegateToken(X509Certificate[] delegateToChain, Date created, Date expiry)
		throws AuthZSecurityException, SOAPException, ConfigurationException, RemoteException
	{
		if (_logger.isDebugEnabled())
			_logger.debug("hitting the formatDelegateToken in jndi authn");
		/*
		 * ResourceKey rKey = ResourceManager.getCurrentResource(); IResource resource = rKey.dereference();
		 * 
		 * X509Certificate[] identity = (X509Certificate[]) resource .getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
		 */

		// Get this resource's key and cert material
		ICallingContext callingContext = null;
		KeyAndCertMaterial resourceKeyMaterial = null;
		try {
			callingContext = ContextManager.getExistingContext();
			resourceKeyMaterial = callingContext.getActiveKeyAndCertMaterial();
		} catch (IOException e) {
			throw new AuthZSecurityException(e.getMessage(), e);
		}

		AxisCredentialWallet creds = new AxisCredentialWallet();

		// Delegate the assertion to delegateTo
		TrustCredential tc =
			new TrustCredential(delegateToChain, IdentityType.CONNECTION, resourceKeyMaterial._clientCertChain, IdentityType.USER,
				new BasicConstraints(created.getTime(), expiry.getTime() - created.getTime(), SecurityConstants.MaxDelegationDepth),
				RWXCategory.FULL_ACCESS);
		tc.signAssertion(resourceKeyMaterial._clientPrivateKey);

		creds.getRealCreds().addCredential(tc);

		// ----- assemble the response document
		// -----------------------------------------

		RequestSecurityTokenResponseType response = new RequestSecurityTokenResponseType();
		MessageElement[] elements = new MessageElement[2];

		XMLCompatible xup = XMLConverter.upscaleCredential(tc);
		if (xup == null) {
			String msg = "unknown type of credential; cannot upscale to XMLCompatible: " + tc.toString();
			_logger.error(msg);
			throw new AuthZSecurityException(msg);
		}

		// Add TokenType element
		elements[0] = new MessageElement(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "TokenType"), xup.getTokenType());
		elements[0].setType(new QName("http://www.w3.org/2001/XMLSchema", "anyURI"));

		org.apache.axis.message.MessageElement wseTokenRef = creds.convertToSOAPElement(null, null);

		elements[1] = new MessageElement(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "RequestedSecurityToken"),
			new RequestedSecurityTokenType(Elementals.unitaryArray(wseTokenRef)));
		elements[1].setType(RequestedProofTokenType.getTypeDesc().getXmlType());

		response.set_any(elements);
		return response;
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public RequestSecurityTokenResponseType[] requestSecurityToken2(RequestSecurityTokenType request) throws java.rmi.RemoteException
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
				"IDP cannot service a request of type " + requestType.getRequestTypeEnumValue(), null, null);
		}

		// check lifetime element
		if (lifetime == null) {
			throw new AxisFault(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "InvalidRequest"),
				"Missing Lifetime parameter", null, null);
		}

		SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
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
			responseArray.add(formatResponse(delegateToChain, created, expiry));
		} catch (AuthZSecurityException e) {
			throw new WSSecurityException(e.getMessage(), e);
		} catch (SOAPException se) {
			throw new AxisFault(se.getLocalizedMessage(), se);
		} catch (ConfigurationException ce) {
			throw new RemoteException(ce.getMessage(), ce);
		}

		return responseArray.toArray(new RequestSecurityTokenResponseType[responseArray.size()]);
	}

	protected X509Certificate[] createCertChainForListing(IJNDIResource idpResource, IJNDIResource stsResource)
		throws RemoteException, GeneralSecurityException
	{

		try {

			// the expensive part: we finally generate a certificate for this
			// guy
			X509Certificate[] containerChain = Container.getContainerCertChain();

			if (containerChain == null) {
				return null;
			}

			String epiString = (String) idpResource.getKey();
			String userName = idpResource.getIdpName();

			CertCreationSpec certSpec = new CertCreationSpec(containerChain[0].getPublicKey(), containerChain,
				Container.getContainerPrivateKey(), getResourceCertificateLifetime());

			Properties jndiEnv = new Properties();
			String providerUrl = null;
			String queryUri = null;

			switch (stsResource.getStsType()) {
				case NIS:

					jndiEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.nis.NISCtxFactory");
					providerUrl = "nis://" + stsResource.getProperty(SecurityConstants.NEW_JNDI_STS_HOST_QNAME.getLocalPart()) + "/"
						+ stsResource.getProperty(SecurityConstants.NEW_JNDI_NISDOMAIN_QNAME.getLocalPart());
					jndiEnv.setProperty(Context.PROVIDER_URL, providerUrl);

					InitialDirContext initialContext = new InitialDirContext(jndiEnv);
					queryUri = providerUrl + "/system/passwd/" + userName;
					String[] attrIDs = { "gecos", "uidnumber" };
					Attributes attrs = initialContext.getAttributes(queryUri, attrIDs);
					initialContext.close();

					// get CNs for cert (gecos common string)
					ArrayList<String> cnList = new ArrayList<String>();
					cnList.add(((ResourceKey) idpResource.getParentResourceKey()).getServiceName());
					if (attrs.get("gecos") != null) {
						cnList.add((String) attrs.get("gecos").get());
					}

					// get UID for cert
					String uid = (attrs.get("uidnumber") == null) ? null : (String) attrs.get("uidnumber").get();

					Map.Entry<List<DERObjectIdentifier>, List<String>> additionalFields =
						CertTool.constructCommonDnFields(epiString, null, cnList, uid);

					return CertTool.createResourceCertChain(certSpec, additionalFields);

				case LDAP:
					jndiEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

					throw new RemoteException("\"LDAP not implemented\" not applicable.");

				default:

					throw new RemoteException("Unknown STS type.");
			}

		} catch (ResourceException e) {
			throw new AuthZSecurityException(e.getMessage(), e);
		} catch (NamingException e) {
			throw new AuthZSecurityException(e.getMessage(), e);
		} catch (ConfigurationException e) {
			throw new AuthZSecurityException(e.getMessage(), e);
		}
	}

	/* EndpointIdentifierResolver port type. */
	@RWXMapping(RWXCategory.OPEN)
	public EndpointReferenceType resolveEPI(org.apache.axis.types.URI resolveEPI)
		throws RemoteException, ResourceUnknownFaultType, ResolveFailedFaultType
	{
		if (_logger.isDebugEnabled())
			_logger.debug("Entered resolveEPI method.");

		EndpointReferenceType myEPR =
			(EndpointReferenceType) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.EPR_PROPERTY_NAME);

		ResourceKey stsKey = ResourceManager.getCurrentResource();
		IJNDIResource stsResource = (IJNDIResource) stsKey.dereference();
		if (stsResource.isServiceResource() || stsResource.isIdpResource()) {
			throw new RemoteException("\"resolveEPI\" not applicable.");
		}

		GenesisHashMap creationParameters = new GenesisHashMap();
		try {
			creationParameters.put(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM, new URI(resolveEPI.toString()));

			creationParameters.put(IJNDIResource.IS_IDP_RESOURCE_CONSTRUCTION_PARAM, Boolean.TRUE);

			ResourceKey idpKey = createResource(creationParameters);
			IJNDIResource idpResource = (IJNDIResource) idpKey.dereference();

			X509Certificate[] resourceCertChain = createCertChainForListing(idpResource, stsResource);
			idpResource.setProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME, resourceCertChain);

			PortType[] implementedPortTypes =
				{ WellKnownPortTypes.JNDI_AUTHN_SERVICE_PORT_TYPE(), WellKnownPortTypes.STS_SERVICE_PORT_TYPE() };
			EndpointReferenceType retval =
				ResourceManager.createEPR(idpKey, myEPR.getAddress().toString(), implementedPortTypes, new String("JNDIWithSTSPortType"));

			return retval;

		} catch (GeneralSecurityException e) {
			throw new ResourceException(e.getMessage(), e);
		} catch (URI.MalformedURIException e) {
			throw new ResourceException(e.getMessage(), e);
		}

	}

	@Override
	public RNSEntryResponseType[] remove(String[] removeRequest) throws RemoteException, WriteNotPermittedFaultType
	{
		// In reality, this service is deprecated and needs to be re-written
		// using resource forks. In the meantime, merely to have the system
		// compile, I have included the correct function interfaces for
		// RNS 1.1, but am not upgrading this service to work with it.
		//
		// Mark Morgan, 11 April 2011
		return null;
	}

	@Override
	public RNSEntryResponseType[] rename(NameMappingType[] renameRequest) throws RemoteException, WriteNotPermittedFaultType
	{
		// In reality, this service is deprecated and needs to be re-written
		// using resource forks. In the meantime, merely to have the system
		// compile, I have included the correct function interfaces for
		// RNS 1.1, but am not upgrading this service to work with it.
		//
		// Mark Morgan, 11 April 2011
		return null;
	}

	@Override
	public CreateFileResponseType createFile(CreateFileRequestType createFileRequest) throws RemoteException
	{
		// In reality, this service is deprecated and needs to be re-written
		// using resource forks. In the meantime, merely to have the system
		// compile, I have included the correct function interfaces for
		// RNS 1.1, but am not upgrading this service to work with it.
		//
		// Mark Morgan, 11 April 2011
		return null;
	}

	@Override
	public RNSEntryResponseType[] setMetadata(MetadataMappingType[] setMetadataRequest) throws RemoteException, WriteNotPermittedFaultType
	{
		// In reality, this service is deprecated and needs to be re-written
		// using resource forks. In the meantime, merely to have the system
		// compile, I have included the correct function interfaces for
		// RNS 1.1, but am not upgrading this service to work with it.
		//
		// Mark Morgan, 11 April 2011
		return null;
	}

	@Override
	public RNSEntryResponseType[] add(RNSEntryType[] addRequest) throws RemoteException, WriteNotPermittedFaultType
	{
		// In reality, this service is deprecated and needs to be re-written
		// using resource forks. In the meantime, merely to have the system
		// compile, I have included the correct function interfaces for
		// RNS 1.1, but am not upgrading this service to work with it.
		//
		// Mark Morgan, 11 April 2011
		return null;
	}

	@Override
	public LookupResponseType lookup(String[] lookupRequest) throws RemoteException, ReadNotPermittedFaultType
	{
		// In reality, this service is deprecated and needs to be re-written
		// using resource forks. In the meantime, merely to have the system
		// compile, I have included the correct function interfaces for
		// RNS 1.1, but am not upgrading this service to work with it.
		//
		// Mark Morgan, 11 April 2011
		return null;
	}
}
