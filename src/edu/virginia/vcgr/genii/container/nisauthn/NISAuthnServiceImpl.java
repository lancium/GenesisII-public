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
package edu.virginia.vcgr.genii.container.nisauthn;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.*;
import java.util.*;
import java.util.regex.Pattern;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.PKIPathSecurity;
import org.apache.ws.security.message.token.X509Security;

import org.ws.addressing.AttributedURIType;

import org.ggf.rns.Add;
import org.ggf.rns.AddResponse;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.Move;
import org.ggf.rns.MoveResponse;
import org.ggf.rns.Query;
import org.ggf.rns.QueryResponse;
import org.ggf.rns.RNSDirectoryNotEmptyFaultType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.ggf.rns.Remove;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.AttributedURITypeSmart;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.security.gamlauthz.TransientCredentials;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.*;
import edu.virginia.vcgr.genii.container.resolver.ISimpleResolverResource;
import edu.virginia.vcgr.genii.container.resolver.SimpleResolverEntry;
import edu.virginia.vcgr.genii.container.resolver.SimpleResolverUtils;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.container.resource.SerializerResourceKeyTranslater;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.security.authz.providers.AuthZProviders;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.client.security.*;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.axis.security.FlexibleBouncyCrypto;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.x509.CertCreationSpec;
import edu.virginia.vcgr.genii.client.security.x509.CertTool;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.*;
import edu.virginia.vcgr.genii.client.ser.AnyHelper;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.enhancedrns.*;


import edu.virginia.vcgr.genii.nisauthn.*;

import org.oasis_open.docs.ws_sx.ws_trust._200512.*;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ogf.schemas.naming._2006._08.naming.ResolveFailedFaultType;
import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.ReferenceParametersType;

import org.apache.axis.AxisFault;

import org.morgan.util.configuration.ConfigurationException;


public class NISAuthnServiceImpl extends GenesisIIBase implements
		NISAuthnPortType, EnhancedRNSPortType {
	
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(NISAuthnServiceImpl.class);

	public NISAuthnServiceImpl() throws RemoteException {
		this(WellKnownPortTypes.NIS_AUTHN_SERVICE_PORT_TYPE.getLocalPart());
	}

	protected NISAuthnServiceImpl(String serviceName) throws RemoteException {
		super(serviceName);

		addImplementedPortType(WellKnownPortTypes.NIS_AUTHN_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.ENHANCED_RNS_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.RNS_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.STS_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.ENDPOINT_IDENTIFIER_RESOLVER_SERVICE_PORT_TYPE);
	}

	public QName getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.NIS_AUTHN_SERVICE_PORT_TYPE;
	}
	
	protected URI createChildEPI(URI serviceUri, String childName) throws URISyntaxException {
		return new URI(serviceUri.toString() + ":" + childName);
	}

	/**
	 * Quick test for overriding classes to implement should they desire
	 * to disable resource creation on this endpoint
	 * @return false.  
	 */
	protected boolean allowVcgrCreate() {
		return false;
	}
	
	// Returns null because we return unbound eprs for efficiency-sake
	protected CertCreationSpec getChildCertSpec() 
		throws ResourceException, ConfigurationException {
		
		return null;
	}	

	protected void setAttributeHandlers() throws NoSuchMethodException {
		super.setAttributeHandlers();
		new NISAuthnAttributeHandlers(getAttributePackage());
	}
	
	protected RequestSecurityTokenResponseType formatResponse (
			X509Certificate[] delegateToChain, Date created, Date expiry) 
			throws GeneralSecurityException, SOAPException, ConfigurationException, RemoteException {
		
		if (delegateToChain != null) {
			// do delegation if necessary
			return formatDelegateToken(delegateToChain, created, expiry);
		} 
		return formatIdentity();
	}

	protected RequestSecurityTokenResponseType formatIdentity () 
			throws GeneralSecurityException, SOAPException, ConfigurationException, RemoteException {
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		IResource resource = rKey.dereference();

		X509Certificate[] identity = 
			(X509Certificate[]) resource.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME); 

		//----- assemble the response document -----------------------------------------
		
		RequestSecurityTokenResponseType response = new RequestSecurityTokenResponseType();
		MessageElement[] elements = new MessageElement[2];
		response.set_any(elements);

		// Add TokenType element
		elements[0] = new MessageElement(
			new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
				"TokenType"), 
				PKIPathSecurity.getType());
		elements[0].setType(new QName("http://www.w3.org/2001/XMLSchema",
				"anyURI"));

		MessageElement wseTokenRef = 
			SecurityUtils.makePkiPathSecTokenRef(identity);
		
		elements[1] = new MessageElement(
			new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
				"RequestedSecurityToken"), 
			new RequestedSecurityTokenType(new MessageElement[] { wseTokenRef }));
		elements[1].setType(RequestedProofTokenType.getTypeDesc().getXmlType());
		
		return response;
	}
	
	
	protected RequestSecurityTokenResponseType formatDelegateToken(
			X509Certificate[] delegateToChain, Date created, Date expiry) 
			throws GeneralSecurityException, SOAPException, ConfigurationException, RemoteException {
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		IResource resource = rKey.dereference();

		X509Certificate[] identity = 
			(X509Certificate[]) resource.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME); 
		SignedAssertion signedAssertion = new X509Identity(identity);

		// Get this resource's key and cert material
		ICallingContext callingContext = null;
		KeyAndCertMaterial resourceKeyMaterial = null;
		try {
			callingContext = ContextManager.getCurrentContext();
			resourceKeyMaterial = callingContext.getActiveKeyAndCertMaterial();
		} catch (IOException e) {
	    	throw new GeneralSecurityException(e.getMessage(), e);	
		}

		// Delegate the assertion to delegateTo 
		DelegatedAttribute delegatedAttribute = new DelegatedAttribute(
			new BasicConstraints(
				created.getTime(), 
				expiry.getTime() - created.getTime(), 
				10), 
			signedAssertion, 
			delegateToChain);
		signedAssertion = new DelegatedAssertion(
			delegatedAttribute, 
			resourceKeyMaterial._clientPrivateKey);
		
		
		//----- assemble the response document -----------------------------------------
		
		
		RequestSecurityTokenResponseType response = new RequestSecurityTokenResponseType();
		MessageElement[] elements = new MessageElement[2];
		response.set_any(elements);

		// Add TokenType element
		elements[0] = new MessageElement(
			new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
				"TokenType"), 
			SecurityConstants.GAML_TOKEN_TYPE);
		elements[0].setType(new QName("http://www.w3.org/2001/XMLSchema",
				"anyURI"));

		// Add RequestedSecurityToken element
		MessageElement binaryToken = null;
		try {
			binaryToken = new MessageElement(
				BinarySecurity.TOKEN_BST, 
				SignedAssertionBaseImpl.base64encodeAssertion(signedAssertion));
			binaryToken.setAttributeNS(null, "ValueType", SecurityConstants.GAML_TOKEN_TYPE);
		} catch (IOException e) {
	    	throw new GeneralSecurityException(e.getMessage(), e);	
		}

		MessageElement embedded = new MessageElement(new QName(
				org.apache.ws.security.WSConstants.WSSE11_NS, "Embedded"));
		embedded.addChild(binaryToken);

		MessageElement wseTokenRef = new MessageElement(new QName(
				org.apache.ws.security.WSConstants.WSSE11_NS,
				"SecurityTokenReference"));
		wseTokenRef.addChild(embedded);

		elements[1] = new MessageElement(
			new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
				"RequestedSecurityToken"), 
			new RequestedSecurityTokenType(new MessageElement[] { wseTokenRef }));
		elements[1].setType(RequestedProofTokenType.getTypeDesc().getXmlType());
		
		return response;
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public RequestSecurityTokenResponseType[] requestSecurityToken2(
			RequestSecurityTokenType request) throws java.rmi.RemoteException {

		//------ Parse and perform syntactic checks (has correct form) --------		

		String tokenType = null;
		RequestTypeOpenEnum requestType = null;
		LifetimeType lifetime = null;
		X509Certificate[] delegateToChain = null;
		
		for (MessageElement element : request.get_any()) {
			if (element.getName().equals("TokenType")) {
				// process TokenType element
				tokenType = element.getValue();
				
			} else if (element.getName().equals("RequestType")) {
				// process RequestType element
				try {
					requestType = (RequestTypeOpenEnum) element.getObjectValue(RequestTypeOpenEnum.class);
				} catch (Exception e) {}
				
			} else if (element.getName().equals("Lifetime")) {
				// process LifeTime element
				try {
					lifetime = (LifetimeType) element.getObjectValue(LifetimeType.class);
				} catch (Exception e) {}
				
			} else if (element.getName().equals("DelegateTo")) {
				// process DelegateTo element
				DelegateToType dt = null;
				try {
					dt = (DelegateToType) element.getObjectValue(DelegateToType.class);
				} catch (Exception e) {}
				if (dt != null) {
					for (MessageElement subElement : dt.get_any()) {
						if (subElement.getQName().equals(new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "SecurityTokenReference"))) {
							subElement = subElement.getChildElement(
								new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "Embedded"));
							if (subElement != null) {
								subElement = subElement.getChildElement(BinarySecurity.TOKEN_BST);
								if (subElement != null) {
									try {
										if (subElement.getAttributeValue("ValueType").equals(X509Security.getType())) {
											X509Security bstToken = new X509Security(subElement);
											X509Certificate delegateTo = bstToken.getX509Certificate(new FlexibleBouncyCrypto());
											delegateToChain = new X509Certificate[] { delegateTo };
										} else if (subElement.getAttributeValue("ValueType").equals(X509Security.getType())) {
											PKIPathSecurity bstToken = new PKIPathSecurity(element);
											delegateToChain = bstToken.getX509Certificates(false, 
												new edu.virginia.vcgr.genii.client.comm.axis.security.FlexibleBouncyCrypto());											
										} else {
											if (delegateToChain == null) {
												throw new AxisFault(
														new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "BadRequest"), 
														"Missing or unsupported DelegateTo security ValueType", 
														null, 
														null);
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
		
		// check requested token type
		if ((tokenType == null) || !tokenType.equals(SecurityConstants.GAML_TOKEN_TYPE)) {
			throw new AxisFault(
					new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "BadRequest"), 
					"IDP cannot provide tokens of type " + tokenType, 
					null, 
					null);
		}
		
		// check request type
		if ((tokenType == null) || !requestType.getRequestTypeEnumValue().toString().equals(RequestTypeEnum._value1.toString())) {
			throw new AxisFault(
					new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "BadRequest"), 
					"IDP cannot service a request of type " + requestType.getRequestTypeEnumValue(), 
					null, 
					null);
		}
		
		// check lifetime element
		if (lifetime == null) {
			throw new AxisFault(
					new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "InvalidRequest"), 
					"Missing Lifetime parameter", 
					null, 
					null);
		}

	    SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date created = zulu.parse(lifetime.getCreated().get_value(), new ParsePosition(0));
		Date expiry = zulu.parse(lifetime.getExpires().get_value(), new ParsePosition(0));

		if ((created == null) || (expiry == null)) {
			throw new AxisFault(
					new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "InvalidRequest"), 
					"Could not parse lifetime dates", 
					null, 
					null);
		}
		
		//------ Assemble response ------------------------------------------		
		
		ArrayList<RequestSecurityTokenResponseType> responseArray = 
			new ArrayList<RequestSecurityTokenResponseType>();
		
		try {
			// add the local token
			responseArray.add(formatResponse(delegateToChain, created, expiry));
		} catch (GeneralSecurityException e) {
	    	throw new WSSecurityException(e.getMessage(), e);	
		} catch (SOAPException se) {
			throw new AxisFault(se.getLocalizedMessage(), se);
		} catch (ConfigurationException ce) {
			throw new RemoteException(ce.getMessage(), ce);
		}

		return responseArray.toArray(new RequestSecurityTokenResponseType[responseArray.size()]);
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public CreateFileResponse createFile(CreateFile createFile)
			throws RemoteException, RNSEntryExistsFaultType,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType,
			RNSFaultType {
		throw new RemoteException("\"createFile\" not applicable.");
	}

	@RWXMapping(RWXCategory.WRITE)
	public AddResponse add(Add addRequest) throws RemoteException,
			RNSEntryExistsFaultType, ResourceUnknownFaultType,
			RNSEntryNotDirectoryFaultType, RNSFaultType {

		throw new RemoteException("\"add\" not applicable.");

	}

	public class EntryIterator implements Iterator<MessageElement> {
		
		protected NamingEnumeration<NameClassPair> _namingEnumerator = null;
		protected URI _serviceEPI = null;
		protected EndpointReferenceType _serviceEPR = null;
		protected Pattern _pattern = null;
		
		protected NameClassPair _next = null;
		
		public EntryIterator(
				String nameService, 
				URI serviceEPI,
				EndpointReferenceType serviceEPR, 
				String regExp) throws NamingException {

			Properties nisEnv = new Properties();
			nisEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.nis.NISCtxFactory");
			nisEnv.setProperty(Context.PROVIDER_URL, nameService);
			InitialDirContext initialContext = new InitialDirContext(nisEnv);
			
			String queryURI = nameService + "/system/passwd";
			
			_namingEnumerator = initialContext.list(queryURI);
			initialContext.close();
			_serviceEPI = serviceEPI;
			_serviceEPR = serviceEPR; 
			_pattern = Pattern.compile(regExp);
		}
		
		public MessageElement next() {

			if (!hasNext()) {
				throw new NoSuchElementException("No more name elements");
			}
			
			try {
				
				HashMap<QName, Object> creationParameters = new HashMap<QName, Object>();
				creationParameters.put(
						IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM,
						createChildEPI(_serviceEPI, _next.getName()));
				ResourceKey listingKey = createResource(creationParameters);
			
				QName[] implementedPortTypes = {
						WellKnownPortTypes.NIS_AUTHN_SERVICE_PORT_TYPE, 
						WellKnownPortTypes.STS_SERVICE_PORT_TYPE}; 
				WSName wsName = new WSName(ResourceManager.createEPR(
						listingKey, 
						(new AttributedURIType(WSName.UNBOUND_ADDRESS)).toString(), 
						implementedPortTypes));
				
				// add resolver info
				wsName.addEndpointIdentifierReferenceResolver(_serviceEPR);
				
				EntryType newEntry = new EntryType(
						_next.getName(), 
						null,
						wsName.getEndpoint());
				
				return AnyHelper.toAny(newEntry);

			} catch (ResourceException e) {
				NoSuchElementException nee = new NoSuchElementException(e.getMessage());
				nee.initCause(e);
				throw nee;
			} catch (URISyntaxException e) {
				NoSuchElementException nee = new NoSuchElementException(e.getMessage());
				nee.initCause(e);
				throw nee;
			} catch (BaseFaultType e) {
				NoSuchElementException nee = new NoSuchElementException(e.getMessage());
				nee.initCause(e);
				throw nee;
			} finally {
				_next = null;
			}
		}

		public void remove() {}
		
		public boolean hasNext() {

			if (_next != null) {
				return true;
			}
			
			try {
				do {
					if (!_namingEnumerator.hasMoreElements()) {
						return false;
					}
					_next = _namingEnumerator.next();
				} while (!_pattern.matcher(_next.getName()).matches());
			} catch (NamingException e) {
				return false;
			} 

			return true;
		}
	}
	
	@RWXMapping(RWXCategory.READ)
    public IterateListResponseType iterateList(IterateListRequestType list) 
    	throws RemoteException, ResourceUnknownFaultType, 
    		RNSEntryNotDirectoryFaultType, RNSFaultType
    {
    	_logger.debug("Entered iterateList method.");

		ResourceKey serviceKey = ResourceManager.getCurrentResource();
		INISResource serviceResource = (INISResource) serviceKey.dereference();
		if (!serviceResource.isServiceResource()) {
			throw new RemoteException("\"iterateList\" not applicable.");
		}

		// get service EPI and EPR
		URI serviceEPI = (URI) serviceResource.getProperty(
				IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME);
		EndpointReferenceType serviceEPR = (EndpointReferenceType)WorkingContext.getCurrentWorkingContext().getProperty(
				WorkingContext.EPR_PROPERTY_NAME);	

		// get configured name service
		NISAuthZProvider authZHandler = (NISAuthZProvider) AuthZProviders.getProvider(
				serviceKey.getServiceName());
		String nameService = 
			"nis://" + 
			authZHandler.getNisHost() + 
			"/" + 
			authZHandler.getNisDomain();
    	
 		try {
 			EntryIterator iterator = new EntryIterator(
 					nameService, 
 					serviceEPI, 
 					serviceEPR, 
 					list.getEntry_name_regexp());

 			return new IterateListResponseType(super.createWSIterator(iterator));
		
		} catch (ConfigurationException e) {
			throw new RemoteException("Unable to create iterator.", e);
		} catch (NamingException e) {
			throw new RemoteException("Unable to create iterator.", e);
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to create iterator.", sqe);
		} 
	    
    }	
	
	protected X509Certificate[] createCertChainForListing(ResourceKey listingKey) 
			throws GeneralSecurityException {
		
		try {
			// the expensive part: we finally generate a certificate for this guy
			X509Certificate[] containerChain = Container.getContainerCertChain();
	
			if (containerChain == null) {
				return null;
			}
			
			// lookup the human name
			NISAuthZProvider authZHandler = (NISAuthZProvider) AuthZProviders.getProvider(
					listingKey.getServiceName());
			String nameService = 
				"nis://" + 
				authZHandler.getNisHost() + 
				"/" + 
				authZHandler.getNisDomain();

			Properties nisEnv = new Properties();
			nisEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.nis.NISCtxFactory");
			nisEnv.setProperty(Context.PROVIDER_URL, nameService);
			InitialDirContext initialContext = new InitialDirContext(nisEnv);
			String epiString = (String) listingKey.getKey();
			String userName = epiString.substring(epiString.lastIndexOf(':') + 1);
			String lookupString = nameService + "/system/passwd/" + userName;
			String[] attrIDs = {"gecos", "uidnumber"}; 
			Attributes attrs = initialContext.getAttributes(lookupString, attrIDs); 
			initialContext.close();

			// get CNs for cert (gecos common string)
			ArrayList<String> cnList = new ArrayList<String>();
			cnList.add(listingKey.getServiceName());
			if (attrs.get("gecos") != null) {
				cnList.add((String) attrs.get("gecos").get());
			}
			
			// get UID for cert
			String uid = (attrs.get("uidnumber") == null) ? null : (String) attrs.get("uidnumber").get();
			
			CertCreationSpec certSpec = new CertCreationSpec(
					containerChain[0].getPublicKey(),
					containerChain,
					Container.getContainerPrivateKey(),
					getResourceCertificateLifetime());		
			
			return CertTool.createResourceCertChain(
					epiString, 
					cnList,
					uid,
					certSpec);
		} catch (ResourceException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		} catch (NamingException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		} catch (ConfigurationException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		}
	}
	
	/* EndpointIdentifierResolver port type. */
	@RWXMapping(RWXCategory.OPEN)
	public EndpointReferenceType resolveEPI(org.apache.axis.types.URI resolveEPI) 
		throws RemoteException,
			ResourceUnknownFaultType, 
			ResolveFailedFaultType
	{
		_logger.debug("Entered resolveEPI method.");
		
		EndpointReferenceType myEPR = 
			(EndpointReferenceType)WorkingContext.getCurrentWorkingContext().getProperty(
					WorkingContext.EPR_PROPERTY_NAME);
		
		ResourceKey serviceKey = ResourceManager.getCurrentResource();
		IResource serviceResource = serviceKey.dereference();
		if (!serviceResource.isServiceResource()) {
			throw new RemoteException("\"resolveEPI\" not applicable.");
		}
		
		HashMap<QName, Object> creationParameters = new HashMap<QName, Object>();
		try {
			creationParameters.put(
					IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM,
					new URI(resolveEPI.toString()));
			
			ResourceKey listingKey = createResource(creationParameters);
			IResource listingResource = listingKey.dereference();

			X509Certificate[] resourceCertChain = 
				createCertChainForListing(listingKey);
			listingResource.setProperty(
					IResource.CERTIFICATE_CHAIN_PROPERTY_NAME, 
					resourceCertChain);
			
			QName[] implementedPortTypes = {
					WellKnownPortTypes.NIS_AUTHN_SERVICE_PORT_TYPE, 
					WellKnownPortTypes.STS_SERVICE_PORT_TYPE}; 
			EndpointReferenceType retval = ResourceManager.createEPR(
					listingKey, 
					myEPR.getAddress().toString(), 
					implementedPortTypes);
			
			return retval;

		} catch (GeneralSecurityException e) {
			throw new ResourceException(e.getMessage(), e);
		} catch (URISyntaxException e) {
			throw new ResourceException(e.getMessage(), e);
		}
			
	}	
	
	@RWXMapping(RWXCategory.READ)
	public ListResponse list(List list) throws RemoteException,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType,
			RNSFaultType {

    	_logger.debug("Entered list method.");

		ResourceKey serviceKey = ResourceManager.getCurrentResource();
		INISResource serviceResource = (INISResource) serviceKey.dereference();
		if (!serviceResource.isServiceResource()) {
			throw new RemoteException("\"list\" not applicable.");
		}

		// Note: May rethink about keeping this check... It does prevent us from
    	// going OOM accidently, but it's not complete...
    	if (list.getEntry_name_regexp().equals("*.")) {
    		throw new RemoteException("\"unconstrained list\" not applicable.");
    	}

		// get service EPI and EPR
		URI serviceEPI = (URI) serviceResource.getProperty(
				IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME);
		EndpointReferenceType serviceEPR = (EndpointReferenceType)WorkingContext.getCurrentWorkingContext().getProperty(
				WorkingContext.EPR_PROPERTY_NAME);	

		// get configured name service
		NISAuthZProvider authZHandler = (NISAuthZProvider) AuthZProviders.getProvider(
				serviceKey.getServiceName());
		String nameService = 
			"nis://" + 
			authZHandler.getNisHost() + 
			"/" + 
			authZHandler.getNisDomain();
    	
 		try {
 			EntryIterator iterator = new EntryIterator(
 					nameService, 
 					serviceEPI, 
 					serviceEPR, 
 					list.getEntry_name_regexp());

 			ArrayList<EntryType> accumulator = new ArrayList<EntryType>();
 			while (iterator.hasNext()) {
 				MessageElement wrappedEntryType = iterator.next();
 				EntryType entry = ObjectDeserializer.toObject(wrappedEntryType, EntryType.class);
 				accumulator.add(entry);
 			}
 			
 			return new ListResponse(accumulator.toArray(new EntryType[0]));
		
		} catch (NamingException e) {
			throw new RemoteException("Unable to create iterator.", e);
		} 
	}

	@RWXMapping(RWXCategory.WRITE)
	public MoveResponse move(Move move) throws RemoteException,
			ResourceUnknownFaultType, RNSFaultType {
		throw new RemoteException("\"move\" not applicable.");
	}

	@RWXMapping(RWXCategory.READ)
	public QueryResponse query(Query q) throws RemoteException,
			ResourceUnknownFaultType, RNSFaultType {
		throw new RemoteException("\"query\" not applicable.");
	}

	@RWXMapping(RWXCategory.WRITE)
	public String[] remove(Remove remove) throws RemoteException,
			ResourceUnknownFaultType, RNSDirectoryNotEmptyFaultType,
			RNSFaultType {
    	
		String entry_name = remove.getEntry_name();
    	String []ret;
    	IRNSResource resource = null;
    	
    	ResourceKey rKey = ResourceManager.getCurrentResource();
    	resource = (IRNSResource)rKey.dereference();
	    Collection<String> removed = resource.removeEntries(entry_name);
	    ret = new String[removed.size()];
	    removed.toArray(ret);
	    resource.commit();
    
	    return ret;	
	}



	static public class NISAuthnAttributeHandlers extends
			AbstractAttributeHandler {
		public NISAuthnAttributeHandlers(AttributePackage pkg)
				throws NoSuchMethodException {
			super(pkg);
		}

		public Collection<MessageElement> getTransferMechsAttr() {
			ArrayList<MessageElement> ret = new ArrayList<MessageElement>();

			ret.add(new MessageElement(new QName(
					ByteIOConstants.RANDOM_BYTEIO_NS,
					ByteIOConstants.XFER_MECHS_ATTR_NAME),
					ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
			ret.add(new MessageElement(new QName(
					ByteIOConstants.RANDOM_BYTEIO_NS,
					ByteIOConstants.XFER_MECHS_ATTR_NAME),
					ByteIOConstants.TRANSFER_TYPE_DIME_URI));
			ret.add(new MessageElement(new QName(
					ByteIOConstants.RANDOM_BYTEIO_NS,
					ByteIOConstants.XFER_MECHS_ATTR_NAME),
					ByteIOConstants.TRANSFER_TYPE_MTOM_URI));

			return ret;
		}

		@Override
		protected void registerHandlers() throws NoSuchMethodException {
			addHandler(new QName(ByteIOConstants.RANDOM_BYTEIO_NS,
					ByteIOConstants.XFER_MECHS_ATTR_NAME),
					"getTransferMechsAttr");
		}
	}

}
