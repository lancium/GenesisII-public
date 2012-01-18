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
package edu.virginia.vcgr.genii.container.x509authn;

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
import java.util.LinkedList;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.PKIPathSecurity;
import org.apache.ws.security.message.token.X509Security;

import org.ggf.rns.LookupResponseType;
import org.ggf.rns.MetadataMappingType;
import org.ggf.rns.NameMappingType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSEntryType;
import org.ggf.rns.RNSMetadataType;
import org.ggf.rns.WriteNotPermittedFaultType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;

import org.oasis_open.docs.ws_sx.ws_trust._200512.DelegateToType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.LifetimeType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestTypeEnum;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestTypeOpenEnum;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestedProofTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestedSecurityTokenType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.rns.RNSContainerUtilities;
import edu.virginia.vcgr.genii.container.rns.RNSDBResourceProvider;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.axis.security.GIIBouncyCrypto;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.WSSecurityUtils;
import edu.virginia.vcgr.genii.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.security.credentials.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.assertions.BasicConstraints;
import edu.virginia.vcgr.genii.security.credentials.assertions.DelegatedAssertion;
import edu.virginia.vcgr.genii.security.credentials.assertions.DelegatedAttribute;
import edu.virginia.vcgr.genii.security.credentials.assertions.IdentityAttribute;
import edu.virginia.vcgr.genii.security.credentials.assertions.SignedAssertion;
import edu.virginia.vcgr.genii.security.credentials.assertions.SignedAttributeAssertion;
import edu.virginia.vcgr.genii.security.credentials.identity.IdentityType;
import edu.virginia.vcgr.genii.security.credentials.identity.X509Identity;
import edu.virginia.vcgr.genii.x509authn.X509AuthnPortType;


import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import org.apache.axis.AxisFault;

import org.morgan.inject.MInject;
import org.morgan.util.configuration.ConfigurationException;

@GeniiServiceConfiguration(
	resourceProvider=RNSDBResourceProvider.class)
public class X509AuthnServiceImpl extends GenesisIIBase
	implements X509AuthnPortType
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(X509AuthnServiceImpl.class);

	@MInject(lazy = true)
	private IRNSResource _resource;
	
	public X509AuthnServiceImpl() throws RemoteException
	{
		this(WellKnownPortTypes.X509_AUTHN_SERVICE_PORT_TYPE.getQName()
				.getLocalPart());
	}

	protected X509AuthnServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);

		addImplementedPortType(WellKnownPortTypes.X509_AUTHN_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.STS_SERVICE_PORT_TYPE);
		addImplementedPortType(RNSConstants.RNS_PORT_TYPE);
	}

	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.X509_AUTHN_SERVICE_PORT_TYPE;
	}

	protected Object translateConstructionParameter(MessageElement property)
			throws Exception
	{

		// decodes the base64-encoded delegated assertion construction param
		QName name = property.getQName();
		if (name.equals(SecurityConstants.NEW_IDP_NAME_QNAME))
		{
			return property.getValue();
		}
		else if (name.equals(SecurityConstants.IDP_VALID_MILLIS_QNAME))
		{
			return Long.decode(property.getValue());
		}
		else if (name.equals(SecurityConstants.NEW_IDP_TYPE_QNAME))
		{
			return property.getValue();
		}
		else
		{
			return super.translateConstructionParameter(property);
		}
	}

	protected ResourceKey createResource(
			HashMap<QName, Object> constructionParameters)
			throws ResourceException, BaseFaultType
	{

		String[] newCNs =
				{ (String) constructionParameters
						.get(SecurityConstants.NEW_IDP_NAME_QNAME) };

		constructionParameters.put(IResource.ADDITIONAL_CNS_CONSTRUCTION_PARAM,
				newCNs);

		return super.createResource(constructionParameters);
	}

	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
		ConstructionParameters cParams, HashMap<QName, Object> constructionParameters,
		Collection<MessageElement> resolverCreationParams)
			throws ResourceException, BaseFaultType, RemoteException
	{

		// make sure the specific IDP doesn't yet exist
		String newIdpName =
				(String) constructionParameters
						.get(SecurityConstants.NEW_IDP_NAME_QNAME);
		ResourceKey serviceKey = ResourceManager.getCurrentResource();
		IRNSResource serviceResource = (IRNSResource) serviceKey.dereference();
		Collection<String> entries = serviceResource.listEntries(null);
		if (entries.contains(newIdpName))
		{
			throw FaultManipulator.fillInFault(new RNSEntryExistsFaultType(
					null, null, null, null, null, null, newIdpName));
		}

		// add the delegated identity to the service's list of IDPs
		serviceResource.addEntry(new InternalEntry(newIdpName, newEPR, null));
		serviceResource.commit();

		// get the IDP resource's db resource
		IResource resource = rKey.dereference();
		X509Certificate[] resourceCertChain =
				(X509Certificate[]) resource
						.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);

		// store the name in the idp resource
		resource.setProperty(SecurityConstants.NEW_IDP_NAME_QNAME
				.getLocalPart(), newIdpName);
		
		//Get Identity type
		String type = 
				(String)constructionParameters
				.get(SecurityConstants.NEW_IDP_TYPE_QNAME);

		// determine the credential the idp will front
		GIICredential credential = null;
		MessageElement encodedCredential =
				(MessageElement) constructionParameters
						.get(SecurityConstants.IDP_DELEGATED_CREDENTIAL_QNAME);
		try
		{

			if (encodedCredential != null)
			{
				// we're an authentication proxy for some delegated credentials
				// that we're being supplied

				credential =
						WSSecurityUtils
								.decodeTokenElement((MessageElement) encodedCredential
										.getChildElements().next());

				if (credential instanceof SignedAssertion)
				{
					// Delegate from the service to the resource
					//Type not set in assertion, must have been set in original idenity
					DelegatedAttribute delegatedAttribute =
							new DelegatedAttribute(null,
									(SignedAssertion) credential,
									resourceCertChain);

					credential =
							new DelegatedAssertion(delegatedAttribute,
									Container.getContainerPrivateKey());
				}

			}
			else
			{

				// we're not an authentication proxy, create our own signed
				// assertion
				// to give out

				X509Identity identity = new X509Identity(resourceCertChain);
				
				//Set type
				identity.setType(IdentityType.valueOf(type));

				Long validMillis =
						(Long) constructionParameters
								.get(SecurityConstants.IDP_VALID_MILLIS_QNAME);

				credential =
						new SignedAttributeAssertion(new IdentityAttribute(
								new BasicConstraints(System.currentTimeMillis()
										- (1000L * 60 * 15), // 15 minutes
																// ago
										validMillis, 10), identity), Container
								.getContainerPrivateKey());
			}

			// add the identity to the resource's saved calling context
			ICallingContext resourceContext =
					(ICallingContext) resource
							.getProperty(IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME);
			TransientCredentials transientCredentials =
					TransientCredentials
							.getTransientCredentials(resourceContext);
			transientCredentials._credentials.add(credential);
			resource.setProperty(
					IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME,
					resourceContext);

			// add the identity to our resource state
			resource.setProperty(
					SecurityConstants.IDP_DELEGATED_CREDENTIAL_QNAME
							.getLocalPart(), credential);
			
			
			

		}
		catch (IOException e)
		{
			throw new RemoteException(e.getMessage(), e);
		}
		catch (GeneralSecurityException e)
		{
			throw new RemoteException(e.getMessage(), e);
		}

		super.postCreate(rKey, newEPR, cParams, constructionParameters,
				resolverCreationParams);
	}

	protected void setAttributeHandlers()
		throws NoSuchMethodException, ResourceException, 
			ResourceUnknownFaultType
	{
		super.setAttributeHandlers();
		new X509AuthnAttributeHandlers(getAttributePackage());
	}

	protected RequestSecurityTokenResponseType delegateCredential(
		X509Certificate[] delegateToChain, Date created, Date expiry)
			throws GeneralSecurityException, SOAPException,
			ConfigurationException, RemoteException
	{

		GIICredential credential = (GIICredential)_resource.getProperty(
			SecurityConstants.IDP_DELEGATED_CREDENTIAL_QNAME.getLocalPart());

		if ((credential instanceof SignedAssertion)
			&& (delegateToChain != null))
		{
			// do delegation if necessary

			// Get this resource's assertion, key and cert material
			ICallingContext callingContext = null;
			KeyAndCertMaterial resourceKeyMaterial = null;
			try
			{
				callingContext = ContextManager.getCurrentContext();
				resourceKeyMaterial =
						callingContext.getActiveKeyAndCertMaterial();
			}
			catch (IOException e)
			{
				throw new GeneralSecurityException(e.getMessage(), e);
			}

			// Delegate the assertion to delegateTo
			DelegatedAttribute delegatedAttribute =
					new DelegatedAttribute(new BasicConstraints(created
							.getTime(), expiry.getTime() - created.getTime(),
							10), (SignedAssertion) credential, delegateToChain);
			credential =
					new DelegatedAssertion(delegatedAttribute,
							resourceKeyMaterial._clientPrivateKey);
		}

		// ----- assemble the response document
		// -----------------------------------------

		RequestSecurityTokenResponseType response =
				new RequestSecurityTokenResponseType();
		MessageElement[] elements = new MessageElement[2];
		response.set_any(elements);

		// Add TokenType element
		elements[0] = new MessageElement(new QName(
			"http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "TokenType"),
			credential.getTokenType());
		elements[0].setType(new QName("http://www.w3.org/2001/XMLSchema",
			"anyURI"));

		MessageElement wseTokenRef = credential.toMessageElement();

		elements[1] =
			new MessageElement(new QName(
				"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
				"RequestedSecurityToken"), new RequestedSecurityTokenType(
					new MessageElement[] { wseTokenRef }));
		elements[1].setType(RequestedProofTokenType.getTypeDesc().getXmlType());

		return response;
	}

	protected ArrayList<RequestSecurityTokenResponseType> aggregateBaggageTokens(
		RequestSecurityTokenType request) throws java.rmi.RemoteException
	{

		ArrayList<RequestSecurityTokenResponseType> gatheredResponses =
				new ArrayList<RequestSecurityTokenResponseType>();

		Collection<InternalEntry> entries;

		entries = _resource.retrieveEntries(null);

		for (InternalEntry entry : entries)
		{

			try
			{
				EndpointReferenceType idpEpr = entry.getEntryReference();

				// create a proxy to the remote idp and invoke it
				X509AuthnPortType idp =
						ClientUtils
								.createProxy(X509AuthnPortType.class, idpEpr);
				RequestSecurityTokenResponseType[] responses =
						idp.requestSecurityToken2(request);

				if (responses != null)
				{
					for (RequestSecurityTokenResponseType response : responses)
					{
						gatheredResponses.add(response);
					}
				}
			}
			catch (Exception e)
			{
				throw new RuntimeException("Could not retrieve token for IDP"
						+ entry.getName() + ": " + e.getMessage(), e);
			}
		}

		return gatheredResponses;
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public RequestSecurityTokenResponseType[] requestSecurityToken2(
			RequestSecurityTokenType request) throws java.rmi.RemoteException
	{

		// ------ Parse and perform syntactic checks (has correct form) --------

		RequestTypeOpenEnum requestType = null;
		LifetimeType lifetime = null;
		X509Certificate[] delegateToChain = null;

		for (MessageElement element : request.get_any())
		{
			if (element.getName().equals("TokenType"))
			{
				// process TokenType element
				// String tokenType = element.getValue();

			}
			else if (element.getName().equals("RequestType"))
			{
				// process RequestType element
				try
				{
					requestType =
							(RequestTypeOpenEnum) element
									.getObjectValue(RequestTypeOpenEnum.class);
				}
				catch (Exception e)
				{
				}

			}
			else if (element.getName().equals("Lifetime"))
			{
				// process LifeTime element
				try
				{
					lifetime =
							(LifetimeType) element
									.getObjectValue(LifetimeType.class);
				}
				catch (Exception e)
				{
				}

			}
			else if (element.getName().equals("DelegateTo"))
			{
				// process DelegateTo element
				DelegateToType dt = null;
				try
				{
					dt = (DelegateToType)element.getObjectValue(
						DelegateToType.class);
				}
				catch (Exception e)
				{
				}
				if (dt != null)
				{
					for (MessageElement subElement : dt.get_any())
					{
						if (subElement.getQName().equals(new QName(
							org.apache.ws.security.WSConstants.WSSE11_NS,
							"SecurityTokenReference")))
						{
							subElement = subElement.getChildElement(new QName(
								org.apache.ws.security.WSConstants.WSSE11_NS,
								"Embedded"));
							if (subElement != null)
							{
								subElement = subElement.getChildElement(
									BinarySecurity.TOKEN_BST);
								if (subElement != null)
								{
									try
									{
										if (subElement.getAttributeValue(
											"ValueType").equals(
											X509Security.getType()))
										{
											X509Security bstToken =
													new X509Security(subElement);
											X509Certificate delegateTo =
													bstToken
															.getX509Certificate(new GIIBouncyCrypto());
											delegateToChain =
													new X509Certificate[] { delegateTo };
										}
										else if (subElement.getAttributeValue(
												"ValueType").equals(
												X509Security.getType()))
										{
											PKIPathSecurity bstToken =
													new PKIPathSecurity(element);
											delegateToChain =
												bstToken.getX509Certificates(
													false, 
													new GIIBouncyCrypto());
										}
										else
										{
											if (delegateToChain == null)
											{
												throw new AxisFault(
													new QName(
															"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
															"BadRequest"),
													"Missing or unsupported DelegateTo security ValueType",
													null, null);
											}
										}
									}
									catch (GenesisIISecurityException e)
									{
										throw new WSSecurityException(e
												.getMessage(), e);
									}
									catch (WSSecurityException e)
									{
										throw new WSSecurityException(e
												.getMessage(), e);
									}
									catch (IOException e)
									{
										throw new WSSecurityException(e
												.getMessage(), e);
									}
									catch (CredentialException e)
									{
										throw new WSSecurityException(e
												.getMessage(), e);
									}
								}
							}
						}
					}
				}
			}
		}

		/*
		 * Don't care at the moment: they get what they get // check requested
		 * token type if ((tokenType == null) ||
		 * !tokenType.equals(WSSecurityUtils.GAML_TOKEN_TYPE)) { throw new
		 * AxisFault( new
		 * QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
		 * "BadRequest"), "IDP cannot provide tokens of type " + tokenType,
		 * null, null); }
		 */

		// check request type
		if ((requestType == null)
			|| !requestType.getRequestTypeEnumValue().toString().equals(
				RequestTypeEnum._value1.toString()))
		{
			throw new AxisFault(new QName(
					"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
					"BadRequest"), "IDP cannot service a request of type "
					+ ((requestType == null) ? "null" : requestType
							.getRequestTypeEnumValue()), null, null);
		}

		// check lifetime element
		if (lifetime == null)
		{
			throw new AxisFault(new QName(
					"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
					"InvalidRequest"), "Missing Lifetime parameter", null, null);
		}

		SimpleDateFormat zulu =
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		zulu.setTimeZone(TimeZone.getTimeZone("ZULU"));
		Date created =
				zulu.parse(lifetime.getCreated().get_value(),
						new ParsePosition(0));
		Date expiry =
				zulu.parse(lifetime.getExpires().get_value(),
						new ParsePosition(0));

		if ((created == null) || (expiry == null))
		{
			throw new AxisFault(new QName(
					"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
					"InvalidRequest"), "Could not parse lifetime dates", null,
					null);
		}

		// ------ Assemble response ------------------------------------------

		ArrayList<RequestSecurityTokenResponseType> responseArray =
				new ArrayList<RequestSecurityTokenResponseType>();

		try
		{
			// add the local token
			responseArray.add(delegateCredential(delegateToChain, created,
					expiry));

			// add the listed tokens
			responseArray.addAll(aggregateBaggageTokens(request));
		}
		catch (GeneralSecurityException e)
		{
			throw new WSSecurityException(e.getMessage(), e);
		}
		catch (SOAPException se)
		{
			throw new AxisFault(se.getLocalizedMessage(), se);
		}
		catch (ConfigurationException ce)
		{
			throw new RemoteException(ce.getMessage(), ce);
		}

		return responseArray
				.toArray(new RequestSecurityTokenResponseType[responseArray
						.size()]);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] add(RNSEntryType []addRequest) throws RemoteException,
			RNSEntryExistsFaultType, ResourceUnknownFaultType
	{
		if (addRequest == null || addRequest.length == 0)
			addRequest = new RNSEntryType[] { null };
		
		RNSEntryResponseType []ret = new RNSEntryResponseType[addRequest.length];
		for (int lcv = 0; lcv < ret.length; lcv++)
		{
			try
			{
				ret[lcv] = add(addRequest[lcv]);
			}
			catch (BaseFaultType bft)
			{
				ret[lcv] = new RNSEntryResponseType(null, null,
					bft, addRequest[lcv].getEntryName());
			}
			catch (Throwable cause)
			{
				ret[lcv] = new RNSEntryResponseType(null, null,
					FaultManipulator.fillInFault(
						new BaseFaultType(null, null, null, null, 
							new BaseFaultTypeDescription[] { 
								new BaseFaultTypeDescription("Unable to add entry!") 
							}, null)), addRequest[lcv].getEntryName());
			}
		}
		
		return ret;
	}
	
	protected RNSEntryResponseType add(RNSEntryType addRequest) 
		throws RemoteException, ResourceException, RNSEntryExistsFaultType
	{
		EndpointReferenceType entryReference;

		if (addRequest == null)
			throw new RemoteException("Incomplete add request.");

		String name = addRequest.getEntryName();
		entryReference = addRequest.getEndpoint();
		RNSMetadataType mdt = addRequest.getMetadata();
		MessageElement[] attrs = (mdt == null) ? null : mdt.get_any();

		if (entryReference == null)
			throw new RemoteException("Incomplete add request.");

		TypeInformation type = new TypeInformation(entryReference);
		if (!type.isIDP())
			throw new RemoteException("Entry is not an IDP.");

		if (_resource.isServiceResource())
			throw new RemoteException("Cannot add entries to this service.");

		_resource.addEntry(new InternalEntry(name, entryReference, attrs));
		_resource.commit();

		return new RNSEntryResponseType(entryReference, mdt, null, name);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public LookupResponseType lookup(String []lookupRequest) throws RemoteException,
		ResourceUnknownFaultType
	{
		Collection<InternalEntry> entries = new LinkedList<InternalEntry>();

		if (lookupRequest == null || lookupRequest.length == 0)
			lookupRequest = new String[] { null };
		
		for (String request : lookupRequest)
		{
			entries.addAll(_resource.retrieveEntries(request));
		}

		Collection<RNSEntryResponseType> ret = new LinkedList<RNSEntryResponseType>();
		for (InternalEntry entry : entries)
			ret.add(new RNSEntryResponseType(
				entry.getEntryReference(), RNSUtilities.createMetadata(entry.getEntryReference(),
					entry.getAttributes()), null, entry.getName()));
		
		return RNSContainerUtilities.translate(ret, iteratorBuilder(
			RNSEntryResponseType.getTypeDesc().getXmlType()));
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] remove(String[] removeRequest)
		throws RemoteException, WriteNotPermittedFaultType
	{
		RNSEntryResponseType []ret = 
			new RNSEntryResponseType[removeRequest.length];
		
		for (int lcv = 0; lcv < removeRequest.length; lcv++)
		{
			_resource.removeEntries(removeRequest[lcv]);
			ret[lcv] = new RNSEntryResponseType(
				null, null, null, removeRequest[lcv]);
		}

		return ret;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] rename(NameMappingType[] renameRequest)
		throws RemoteException, WriteNotPermittedFaultType
	{
		throw new RemoteException("\"rename\" not applicable.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] setMetadata(
		MetadataMappingType[] setMetadataRequest) throws RemoteException,
			WriteNotPermittedFaultType
	{
		throw new RemoteException("\"setMetadata\" not applicable.");
	}

	static public class X509AuthnAttributeHandlers extends
			AbstractAttributeHandler
	{
		public X509AuthnAttributeHandlers(AttributePackage pkg)
				throws NoSuchMethodException
		{
			super(pkg);
		}

		public Collection<MessageElement> getTransferMechsAttr()
		{
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
		protected void registerHandlers() throws NoSuchMethodException
		{
			addHandler(new QName(ByteIOConstants.RANDOM_BYTEIO_NS,
					ByteIOConstants.XFER_MECHS_ATTR_NAME),
					"getTransferMechsAttr");
		}
	}
}