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
package edu.virginia.vcgr.genii.container.jndiauthn;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.text.*;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.axis.types.URI;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
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

import org.ggf.rns.*;
import org.ggf.rns.List;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.*;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.genii.enhancedrns.IterateListResponseType;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.client.security.*;
import edu.virginia.vcgr.genii.client.comm.axis.security.FlexibleBouncyCrypto;
import edu.virginia.vcgr.genii.client.context.ContextException;
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

import edu.virginia.vcgr.genii.jndiauthn.*;

import org.oasis_open.docs.ws_sx.ws_trust._200512.*;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ogf.schemas.naming._2006._08.naming.ResolveFailedFaultType;
import org.ws.addressing.EndpointReferenceType;

import org.apache.axis.AxisFault;

import org.morgan.util.configuration.ConfigurationException;

public class JNDIAuthnServiceImpl extends GenesisIIBase implements
		JNDIAuthnPortType, EnhancedRNSPortType
{

	static private Log _logger = LogFactory.getLog(JNDIAuthnServiceImpl.class);

	public JNDIAuthnServiceImpl() throws RemoteException
	{
		this(WellKnownPortTypes.JNDI_AUTHN_SERVICE_PORT_TYPE.getQName()
				.getLocalPart());
	}

	protected JNDIAuthnServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);

	}

	/**
	 * Return different implemented port types depending on who we are
	 */
	public PortType[] getImplementedPortTypes(ResourceKey rKey)
			throws ResourceException, ResourceUnknownFaultType
	{

		if ((rKey == null) || (!(rKey.dereference() instanceof IJNDIResource)))
		{
			// JNDIAuthnPortType
			PortType[] response =
					{ RNSConstants.RNS_PORT_TYPE,
							WellKnownPortTypes.JNDI_AUTHN_SERVICE_PORT_TYPE };

			return response;
		}

		IJNDIResource serviceResource = (IJNDIResource) rKey.dereference();

		if (serviceResource.isServiceResource())
		{
			// JNDIAuthnPortType
			PortType[] response =
					{ RNSConstants.RNS_PORT_TYPE,
							WellKnownPortTypes.JNDI_AUTHN_SERVICE_PORT_TYPE };

			return response;
		}
		else if (serviceResource.isIdpResource())
		{
			// individual IDP resource
			PortType[] response = { WellKnownPortTypes.STS_SERVICE_PORT_TYPE, };

			return response;
		}

		// STS for a JNDI directory resource
		PortType[] response =
				{ WellKnownPortTypes.STS_SERVICE_PORT_TYPE,
						RNSConstants.ENHANCED_RNS_PORT_TYPE,
						RNSConstants.RNS_PORT_TYPE, };

		return response;
	}

	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.JNDI_AUTHN_SERVICE_PORT_TYPE;
	}

	/**
	 * Quick test for overriding classes to implement should they desire to
	 * disable resource creation on this endpoint
	 * 
	 * @return false.
	 */
	protected boolean allowVcgrCreate() throws ResourceException,
			ResourceUnknownFaultType
	{
		ResourceKey serviceKey = ResourceManager.getCurrentResource();
		IJNDIResource serviceResource =
				(IJNDIResource) serviceKey.dereference();

		// only allow remote creation on the JNDIAuthnPortType endpoint resource
		if (!serviceResource.isServiceResource())
		{
			return false;
		}

		return true;
	}

	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
			HashMap<QName, Object> constructionParameters,
			Collection<MessageElement> resolverCreationParams)
			throws ResourceException, BaseFaultType, RemoteException
	{

		ResourceKey myKey = ResourceManager.getCurrentResource();
		IJNDIResource myResource = (IJNDIResource) myKey.dereference();
		if (!myResource.isServiceResource())
		{
			// we're an STS resource creating directory entries
			super.postCreate(rKey, newEPR, constructionParameters,
					resolverCreationParams);
			return;
		}

		// we're the service resource creating STS entries

		// make sure the specific STS doesn't yet exist
		String newStsName =
				(String) constructionParameters
						.get(SecurityConstants.NEW_JNDI_STS_NAME_QNAME);
		Collection<String> entries = myResource.listEntries();
		if (entries.contains(newStsName))
		{
			throw FaultManipulator.fillInFault(new RNSEntryExistsFaultType(
					null, null, null, null, null, null, newStsName));
		}

		// add the entry to the service's list of STSs
		myResource.addEntry(new InternalEntry(newStsName, newEPR, null));
		myResource.commit();

		super.postCreate(rKey, newEPR, constructionParameters,
				resolverCreationParams);
	}

	protected Object translateConstructionParameter(MessageElement property)
			throws Exception
	{

		// decodes the base64-encoded delegated assertion construction param
		QName name = property.getQName();
		if (name.equals(SecurityConstants.NEW_JNDI_NISDOMAIN_QNAME))
		{
			return property.getValue();
		}
		else if (name.equals(SecurityConstants.NEW_JNDI_STS_HOST_QNAME))
		{
			return property.getValue();
		}
		else if (name.equals(SecurityConstants.NEW_JNDI_STS_NAME_QNAME))
		{
			return property.getValue();
		}
		else if (name.equals(SecurityConstants.NEW_JNDI_STS_SEARCHBASE_QNAME))
		{
			return property.getValue();
		}
		else if (name.equals(SecurityConstants.NEW_JNDI_STS_TYPE_QNAME))
		{
			return property.getValue();
		}
		else
		{
			return super.translateConstructionParameter(property);
		}
	}

	protected CertCreationSpec getChildCertSpec() throws ResourceException,
			ResourceUnknownFaultType, ConfigurationException
	{

		ResourceKey myKey = ResourceManager.getCurrentResource();
		IJNDIResource myResource = (IJNDIResource) myKey.dereference();
		if (!myResource.isServiceResource())
		{
			// Returns null because we return unbound eprs for
			// new IDP resources
			return null;
		}
		return super.getChildCertSpec();
	}

	protected RequestSecurityTokenResponseType formatResponse(
			X509Certificate[] delegateToChain, Date created, Date expiry)
			throws GeneralSecurityException, SOAPException,
			ConfigurationException, RemoteException
	{

		if (delegateToChain != null)
		{
			// do delegation if necessary
			return formatDelegateToken(delegateToChain, created, expiry);
		}
		return formatIdentity();
	}

	protected RequestSecurityTokenResponseType formatIdentity()
			throws GeneralSecurityException, SOAPException,
			ConfigurationException, RemoteException
	{

		ResourceKey rKey = ResourceManager.getCurrentResource();
		IResource resource = rKey.dereference();

		X509Certificate[] identity =
				(X509Certificate[]) resource
						.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);

		// ----- assemble the response document
		// -----------------------------------------

		RequestSecurityTokenResponseType response =
				new RequestSecurityTokenResponseType();
		MessageElement[] elements = new MessageElement[2];
		response.set_any(elements);

		// Add TokenType element
		elements[0] =
				new MessageElement(new QName(
						"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
						"TokenType"), PKIPathSecurity.getType());
		elements[0].setType(new QName("http://www.w3.org/2001/XMLSchema",
				"anyURI"));

		MessageElement wseTokenRef =
				WSSecurityUtils.makePkiPathSecTokenRef(identity);

		elements[1] =
				new MessageElement(new QName(
						"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
						"RequestedSecurityToken"),
						new RequestedSecurityTokenType(
								new MessageElement[] { wseTokenRef }));
		elements[1].setType(RequestedProofTokenType.getTypeDesc().getXmlType());

		return response;
	}

	protected RequestSecurityTokenResponseType formatDelegateToken(
			X509Certificate[] delegateToChain, Date created, Date expiry)
			throws GeneralSecurityException, SOAPException,
			ConfigurationException, RemoteException
	{

		ResourceKey rKey = ResourceManager.getCurrentResource();
		IResource resource = rKey.dereference();

		X509Certificate[] identity =
				(X509Certificate[]) resource
						.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
		SignedAssertion signedAssertion = new X509Identity(identity);

		// Get this resource's key and cert material
		ICallingContext callingContext = null;
		KeyAndCertMaterial resourceKeyMaterial = null;
		try
		{
			callingContext = ContextManager.getCurrentContext();
			resourceKeyMaterial = callingContext.getActiveKeyAndCertMaterial();
		}
		catch (IOException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}

		// Delegate the assertion to delegateTo
		DelegatedAttribute delegatedAttribute =
				new DelegatedAttribute(new BasicConstraints(created.getTime(),
						expiry.getTime() - created.getTime(), 10),
						signedAssertion, delegateToChain);
		signedAssertion =
				new DelegatedAssertion(delegatedAttribute,
						resourceKeyMaterial._clientPrivateKey);

		// ----- assemble the response document
		// -----------------------------------------

		RequestSecurityTokenResponseType response =
				new RequestSecurityTokenResponseType();
		MessageElement[] elements = new MessageElement[2];
		response.set_any(elements);

		// Add TokenType element
		elements[0] =
				new MessageElement(new QName(
						"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
						"TokenType"), signedAssertion.getTokenType());
		elements[0].setType(new QName("http://www.w3.org/2001/XMLSchema",
				"anyURI"));

		MessageElement wseTokenRef = signedAssertion.toMessageElement();

		elements[1] =
				new MessageElement(new QName(
						"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
						"RequestedSecurityToken"),
						new RequestedSecurityTokenType(
								new MessageElement[] { wseTokenRef }));
		elements[1].setType(RequestedProofTokenType.getTypeDesc().getXmlType());

		return response;
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
					dt =
							(DelegateToType) element
									.getObjectValue(DelegateToType.class);
				}
				catch (Exception e)
				{
				}
				if (dt != null)
				{
					for (MessageElement subElement : dt.get_any())
					{
						if (subElement
								.getQName()
								.equals(
										new QName(
												org.apache.ws.security.WSConstants.WSSE11_NS,
												"SecurityTokenReference")))
						{
							subElement =
									subElement
											.getChildElement(new QName(
													org.apache.ws.security.WSConstants.WSSE11_NS,
													"Embedded"));
							if (subElement != null)
							{
								subElement =
										subElement
												.getChildElement(BinarySecurity.TOKEN_BST);
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
															.getX509Certificate(new FlexibleBouncyCrypto());
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
													bstToken
															.getX509Certificates(
																	false,
																	new edu.virginia.vcgr.genii.client.comm.axis.security.FlexibleBouncyCrypto());
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
					+ requestType.getRequestTypeEnumValue(), null, null);
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
			responseArray.add(formatResponse(delegateToChain, created, expiry));
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

	@RWXMapping(RWXCategory.READ)
	public ListResponse list(List list) throws RemoteException,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType,
			RNSFaultType
	{

		_logger.debug("Entered list method.");

		ResourceKey myResourceKey = ResourceManager.getCurrentResource();
		IJNDIResource myResource = (IJNDIResource) myResourceKey.dereference();
		if (myResource.isIdpResource())
		{
			throw new RemoteException("\"list\" not applicable.");
		}

		if (myResource.isServiceResource())
		{
			// list the directory contents from state

			IRNSResource resource = null;
			Collection<InternalEntry> entries;

			ResourceKey rKey = ResourceManager.getCurrentResource();
			resource = (IRNSResource) rKey.dereference();
			entries = resource.retrieveEntries(list.getEntryName());

			EntryType[] ret = new EntryType[entries.size()];
			int lcv = 0;
			for (InternalEntry entry : entries)
			{
				ret[lcv++] =
						new EntryType(entry.getName(), entry.getAttributes(),
								entry.getEntryReference());
			}

			return new ListResponse(ret);
		}

		// STS for a JNDI directory resource

		// Note: May rethink about keeping this check... It does prevent us from
		// going OOM accidently, but it's not complete...
		if (list.getEntryName() == null)
		{
			throw new RemoteException("\"unconstrained list\" not applicable.");
		}

		EntryIterator iterator =
				new EntryIterator(myResource, list.getEntryName());

		ArrayList<EntryType> accumulator = new ArrayList<EntryType>();
		while (iterator.hasNext())
		{
			MessageElement wrappedEntryType = iterator.next();
			EntryType entry =
					ObjectDeserializer.toObject(wrappedEntryType,
							EntryType.class);
			accumulator.add(entry);
		}

		return new ListResponse(accumulator.toArray(new EntryType[0]));

	}

	@RWXMapping(RWXCategory.READ)
	public IterateListResponseType iterateList(IterateListRequestType list)
			throws RemoteException, ResourceUnknownFaultType,
			RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		_logger.debug("Entered iterateList method.");

		ResourceKey myResourceKey = ResourceManager.getCurrentResource();
		IJNDIResource myResource = (IJNDIResource) myResourceKey.dereference();
		if (myResource.isIdpResource())
		{
			throw new RemoteException("\"list\" not applicable.");
		}

		if (myResource.isServiceResource())
		{
			// list the directory contents from state

			Collection<InternalEntry> entries;
			entries = myResource.retrieveEntries(null);

			Collection<MessageElement> col = new LinkedList<MessageElement>();
			for (InternalEntry internalEntry : entries)
			{
				EntryType entry =
						new EntryType(internalEntry.getName(), internalEntry
								.getAttributes(), internalEntry
								.getEntryReference());

				col.add(AnyHelper.toAny(entry));
			}

			try
			{
				return new IterateListResponseType(super.createWSIterator(col
						.iterator(), 100));
			}
			catch (ConfigurationException ce)
			{
				throw new RemoteException("Unable to create iterator.", ce);
			}
			catch (SQLException sqe)
			{
				throw new RemoteException("Unable to create iterator.", sqe);
			}
		}

		// STS for a JNDI directory resource

		try
		{
			EntryIterator iterator = new EntryIterator(myResource, null);

			return new IterateListResponseType(super.createWSIterator(iterator,
					100));

		}
		catch (java.io.IOException e)
		{
			throw new RemoteException("Unable to create iterator.", e);
		}
		catch (ConfigurationException e)
		{
			throw new RemoteException("Unable to create iterator.", e);
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to create iterator.", sqe);
		}

	}

	protected X509Certificate[] createCertChainForListing(
			IJNDIResource idpResource, IJNDIResource stsResource)
			throws RemoteException, GeneralSecurityException
	{

		try
		{

			// the expensive part: we finally generate a certificate for this
			// guy
			X509Certificate[] containerChain =
					Container.getContainerCertChain();

			if (containerChain == null)
			{
				return null;
			}

			String epiString = (String) idpResource.getKey();
			String userName = idpResource.getIdpName();

			CertCreationSpec certSpec =
					new CertCreationSpec(containerChain[0].getPublicKey(),
							containerChain, Container.getContainerPrivateKey(),
							getResourceCertificateLifetime());

			Properties jndiEnv = new Properties();
			String providerUrl = null;
			String queryUri = null;

			switch (stsResource.getStsType())
			{
			case NIS:

				jndiEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY,
						"com.sun.jndi.nis.NISCtxFactory");
				providerUrl =
						"nis://"
								+ stsResource
										.getProperty(SecurityConstants.NEW_JNDI_STS_HOST_QNAME
												.getLocalPart())
								+ "/"
								+ stsResource
										.getProperty(SecurityConstants.NEW_JNDI_NISDOMAIN_QNAME
												.getLocalPart());
				jndiEnv.setProperty(Context.PROVIDER_URL, providerUrl);

				InitialDirContext initialContext =
						new InitialDirContext(jndiEnv);
				queryUri = providerUrl + "/system/passwd/" + userName;
				String[] attrIDs = { "gecos", "uidnumber" };
				Attributes attrs =
						initialContext.getAttributes(queryUri, attrIDs);
				initialContext.close();

				// get CNs for cert (gecos common string)
				ArrayList<String> cnList = new ArrayList<String>();
				cnList.add(idpResource.getParentResourceKey().getServiceName());
				if (attrs.get("gecos") != null)
				{
					cnList.add((String) attrs.get("gecos").get());
				}

				// get UID for cert
				String uid =
						(attrs.get("uidnumber") == null) ? null
								: (String) attrs.get("uidnumber").get();

				return CertTool.createResourceCertChain(epiString, cnList, uid,
						certSpec);

			case LDAP:
				jndiEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY,
						"com.sun.jndi.ldap.LdapCtxFactory");

				throw new RemoteException(
						"\"LDAP not implemented\" not applicable.");

			default:

				throw new RemoteException("Unknown STS type.");
			}

		}
		catch (ResourceException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
		catch (NamingException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
		catch (ConfigurationException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
	}

	/* EndpointIdentifierResolver port type. */
	@RWXMapping(RWXCategory.OPEN)
	public EndpointReferenceType resolveEPI(org.apache.axis.types.URI resolveEPI)
			throws RemoteException, ResourceUnknownFaultType,
			ResolveFailedFaultType
	{
		_logger.debug("Entered resolveEPI method.");

		EndpointReferenceType myEPR =
				(EndpointReferenceType) WorkingContext
						.getCurrentWorkingContext().getProperty(
								WorkingContext.EPR_PROPERTY_NAME);

		ResourceKey stsKey = ResourceManager.getCurrentResource();
		IJNDIResource stsResource = (IJNDIResource) stsKey.dereference();
		if (stsResource.isServiceResource() || stsResource.isIdpResource())
		{
			throw new RemoteException("\"resolveEPI\" not applicable.");
		}

		HashMap<QName, Object> creationParameters =
				new HashMap<QName, Object>();
		try
		{
			creationParameters.put(
					IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM, new URI(
							resolveEPI.toString()));

			creationParameters.put(
					IJNDIResource.IS_IDP_RESOURCE_CONSTRUCTION_PARAM,
					Boolean.TRUE);

			ResourceKey idpKey = createResource(creationParameters);
			IJNDIResource idpResource = (IJNDIResource) idpKey.dereference();

			X509Certificate[] resourceCertChain =
					createCertChainForListing(idpResource, stsResource);
			idpResource.setProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME,
					resourceCertChain);

			PortType[] implementedPortTypes =
					{ WellKnownPortTypes.JNDI_AUTHN_SERVICE_PORT_TYPE,
							WellKnownPortTypes.STS_SERVICE_PORT_TYPE };
			EndpointReferenceType retval =
					ResourceManager.createEPR(idpKey, myEPR.getAddress()
							.toString(), implementedPortTypes);

			return retval;

		}
		catch (GeneralSecurityException e)
		{
			throw new ResourceException(e.getMessage(), e);
		}
		catch (URI.MalformedURIException e)
		{
			throw new ResourceException(e.getMessage(), e);
		}

	}

	@RWXMapping(RWXCategory.WRITE)
	public String[] remove(Remove remove) throws RemoteException,
			ResourceUnknownFaultType, RNSDirectoryNotEmptyFaultType,
			RNSFaultType
	{

		ResourceKey serviceKey = ResourceManager.getCurrentResource();
		IJNDIResource serviceResource =
				(IJNDIResource) serviceKey.dereference();

		if (!serviceResource.isServiceResource())
		{
			throw new RemoteException("\"remove\" not applicable.");
		}

		String[] ret;
		IRNSResource resource = null;

		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRNSResource) rKey.dereference();
		Collection<String> removed =
				resource.removeEntries(remove.getEntryName());
		ret = new String[removed.size()];
		removed.toArray(ret);
		resource.commit();

		return ret;
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public CreateFileResponse createFile(CreateFile createFile)
			throws RemoteException, RNSEntryExistsFaultType,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType,
			RNSFaultType
	{
		throw new RemoteException("\"createFile\" not applicable.");
	}

	@RWXMapping(RWXCategory.WRITE)
	public AddResponse add(Add addRequest) throws RemoteException,
			RNSEntryExistsFaultType, ResourceUnknownFaultType,
			RNSEntryNotDirectoryFaultType, RNSFaultType
	{

		throw new RemoteException("\"add\" not applicable.");

	}

	@RWXMapping(RWXCategory.WRITE)
	public MoveResponse move(Move move) throws RemoteException,
			ResourceUnknownFaultType, RNSFaultType
	{
		throw new RemoteException("\"move\" not applicable.");
	}

	@RWXMapping(RWXCategory.READ)
	public QueryResponse query(Query q) throws RemoteException,
			ResourceUnknownFaultType, RNSFaultType
	{
		throw new RemoteException("\"query\" not applicable.");
	}

	public class EntryIterator implements Iterator<MessageElement>
	{

		protected NamingEnumeration<NameClassPair> _namingEnumerator = null;
		protected String _stsEPI = null;
		protected EndpointReferenceType _stsEPR = null;
		protected Pattern _pattern = null;
		protected IJNDIResource _stsResource = null;

		protected NameClassPair _next = null;

		public EntryIterator(IJNDIResource stsResource, String entryName)
				throws ResourceException, RemoteException
		{

			_stsResource = stsResource;

			try
			{

				// get service EPI and EPR
				_stsEPI = (String) stsResource.getKey();

				_stsEPR =
						(EndpointReferenceType) WorkingContext
								.getCurrentWorkingContext().getProperty(
										WorkingContext.EPR_PROPERTY_NAME);

				if (entryName != null)
					_pattern = Pattern.compile("^\\Q" + entryName + "\\E$");
				else
					_pattern = Pattern.compile("^.*$");

				Properties jndiEnv = new Properties();
				String providerUrl = null;
				String queryUri = null;

				switch (stsResource.getStsType())
				{
				case NIS:

					jndiEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY,
							"com.sun.jndi.nis.NISCtxFactory");
					providerUrl =
							"nis://"
									+ stsResource
											.getProperty(SecurityConstants.NEW_JNDI_STS_HOST_QNAME
													.getLocalPart())
									+ "/"
									+ stsResource
											.getProperty(SecurityConstants.NEW_JNDI_NISDOMAIN_QNAME
													.getLocalPart());
					queryUri = providerUrl + "/system/passwd";

					jndiEnv.setProperty(Context.PROVIDER_URL, providerUrl);
					InitialDirContext initialContext =
							new InitialDirContext(jndiEnv);
					_namingEnumerator = initialContext.list(queryUri);
					initialContext.close();

					break;

				case LDAP:
					jndiEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY,
							"com.sun.jndi.ldap.LdapCtxFactory");

					throw new RemoteException(
							"\"LDAP not implemented\" not applicable.");
				}

			}
			catch (NamingException e)
			{
				throw new ResourceException(e.getMessage(), e);
			}
			catch (ContextException e)
			{
				throw new ResourceException(e.getMessage(), e);
			}
		}

		public MessageElement next()
		{

			if (!hasNext())
			{
				throw new NoSuchElementException("No more name elements");
			}

			try
			{

				HashMap<QName, Object> creationParameters =
						new HashMap<QName, Object>();
				creationParameters.put(
						IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM,
						_stsResource.createChildIdpEpi(_next.getName()));
				creationParameters.put(
						IJNDIResource.IS_IDP_RESOURCE_CONSTRUCTION_PARAM,
						Boolean.TRUE);
				ResourceKey listingKey = createResource(creationParameters);

				PortType[] implementedPortTypes =
						{ WellKnownPortTypes.JNDI_AUTHN_SERVICE_PORT_TYPE,
								WellKnownPortTypes.STS_SERVICE_PORT_TYPE };
				WSName wsName =
						new WSName(ResourceManager.createEPR(listingKey,
								(new AttributedURIType(WSName.UNBOUND_ADDRESS))
										.toString(), implementedPortTypes));

				// add resolver info
				wsName.addEndpointIdentifierReferenceResolver(_stsEPR);

				EntryType newEntry =
						new EntryType(_next.getName(), null, wsName
								.getEndpoint());

				return AnyHelper.toAny(newEntry);

			}
			catch (ResourceException e)
			{
				NoSuchElementException nee =
						new NoSuchElementException(e.getMessage());
				nee.initCause(e);
				throw nee;
			}
			catch (URI.MalformedURIException e)
			{
				NoSuchElementException nee =
						new NoSuchElementException(e.getMessage());
				nee.initCause(e);
				throw nee;
			}
			catch (BaseFaultType e)
			{
				NoSuchElementException nee =
						new NoSuchElementException(e.getMessage());
				nee.initCause(e);
				throw nee;
			}
			finally
			{
				_next = null;
			}
		}

		public void remove()
		{
		}

		public boolean hasNext()
		{

			if (_next != null)
			{
				return true;
			}

			try
			{
				do
				{
					if (!_namingEnumerator.hasMoreElements())
					{
						return false;
					}
					_next = _namingEnumerator.next();
				} while (!_pattern.matcher(_next.getName()).matches());
			}
			catch (NamingException e)
			{
				return false;
			}

			return true;
		}
	}

}
