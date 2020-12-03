package edu.virginia.vcgr.genii.container.resource;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.morgan.util.configuration.ConfigurationException;
import org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.EmptyType;
import org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.IncludeTokenOpenType;
import org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.IncludeTokenType;
import org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.NestedPolicyType;
import org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.SePartsType;
import org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.TokenAssertionType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ClaimsType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.w3.www.ns.ws_policy.AppliesTo;
import org.w3.www.ns.ws_policy.Policy;
import org.w3.www.ns.ws_policy.PolicyAttachment;
import org.w3.www.ns.ws_policy.PolicyReference;
import org.w3.www.ns.ws_policy.URI;
import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;
import org.ws.addressing.ReferenceParametersType;

import edu.virginia.vcgr.genii.client.ContainerProperties;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.axis.Elementals;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.container.ContainerConstants;
import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSAddressingConstants;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.ogsa.OGSAWSRFBPConstants;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.AttributedURITypeSmart;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.common.security.RequiredMessageSecurityType;
import edu.virginia.vcgr.genii.common.security.RequiredMessageSecurityTypeMin;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.security.authz.providers.AuthZProviders;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.axis.MessageLevelSecurityRequirements;
import edu.virginia.vcgr.genii.security.axis.WSSecurityUtils;

public class ResourceManager
{
	static private Log _logger = LogFactory.getLog(ResourceManager.class);

	// used to limit the reporting of the EPR construction properties setting to one time.
	static Boolean reportedAlready = false;

	static public ResourceKey getTargetResource(String serviceName, String resourceKey) throws ResourceException, ResourceUnknownFaultType
	{
		try {
			WorkingContext ctxt = WorkingContext.getCurrentWorkingContext();
			ResourceKey rKey = new ResourceKey(serviceName, new AddressingParameters(resourceKey, null, null));
			ctxt.setProperty(new GUID().toString(), rKey);
			return rKey;
		} catch (AxisFault af) {
			throw new ResourceException(af.getLocalizedMessage(), af);
		}
	}

	static public ResourceKey getTargetResource(EndpointReferenceType epr) throws ResourceException, ResourceUnknownFaultType
	{
		try {
			WorkingContext ctxt = WorkingContext.getCurrentWorkingContext();
			String serviceName = EPRUtils.extractServiceName(epr);
			ResourceKey rKey = new ResourceKey(serviceName, new AddressingParameters(epr.getReferenceParameters()));
			ctxt.setProperty(new GUID().toString(), rKey);
			return rKey;
		} catch (AxisFault af) {
			throw new ResourceException(af.getLocalizedMessage(), af);
		}
	}

	static public ResourceKey getCurrentResource() throws ResourceUnknownFaultType, ResourceException
	{
		WorkingContext ctxt = WorkingContext.getCurrentWorkingContext();
		ResourceKey key = null;

		if (ctxt != null) {
			key = (ResourceKey) ctxt.getProperty(WorkingContext.CURRENT_RESOURCE_KEY);
			if (_logger.isTraceEnabled()) {
				if (key != null)
					_logger.trace("found key in context, key=" + key);
				else
					_logger.trace("no key found in context for field: " + WorkingContext.CURRENT_RESOURCE_KEY);
			}
		}
		if (key == null) {
			EndpointReferenceType epr = (EndpointReferenceType) ctxt.getProperty(WorkingContext.EPR_PROPERTY_NAME);
			if (epr == null)
				throw new ResourceException("Couldn't locate target EPR in current working context for key=" + key);

			String serviceName = (String) ctxt.getProperty(WorkingContext.TARGETED_SERVICE_NAME);
			if (serviceName == null)
				throw new ResourceException("Couldn't locate target service name in current working context for key=" + key);

			if (_logger.isTraceEnabled())
				_logger.trace("about to make resource key for svc=" + serviceName + " epr=" + epr.getAddress());
			key = new ResourceKey(serviceName, new AddressingParameters(epr.getReferenceParameters()));
			ctxt.setProperty(WorkingContext.CURRENT_RESOURCE_KEY, key);
		}

		return key;
	}

	static public ResourceKey getServiceResource(String serviceName) throws ResourceException, ResourceUnknownFaultType
	{
		WorkingContext ctxt = WorkingContext.getCurrentWorkingContext();
		ResourceKey rKey = new ResourceKey(serviceName, new AddressingParameters(null));
		ctxt.setProperty(new GUID().toString(), rKey);
		return rKey;
	}

	static public ResourceKey createServiceResource(String serviceName, GenesisHashMap constructionParameters) throws ResourceException
	{
		WorkingContext ctxt = WorkingContext.getCurrentWorkingContext();

		constructionParameters.put(IResource.IS_SERVICE_CONSTRUCTION_PARAM, Boolean.TRUE);

		ResourceKey rKey = new ResourceKey(serviceName, constructionParameters);
		ctxt.setProperty(new GUID().toString(), rKey);
		return rKey;
	}

	static public ResourceKey createNewResource(String serviceName, GenesisHashMap constructionParameters) throws ResourceException
	{
		WorkingContext ctxt = WorkingContext.getCurrentWorkingContext();

		ResourceKey rKey = new ResourceKey(serviceName, constructionParameters);
		ctxt.setProperty(new GUID().toString(), rKey);

		return rKey;
	}

	static public EndpointReferenceType createEPR(ResourceKey resource, String targetServiceURL, PortType[] implementedPortTypes,
		String masterType) throws ResourceException
	{
		ReferenceParametersType refParams = null;
		AttributedURIType address = new AttributedURITypeSmart(targetServiceURL);
		if (resource != null) {
			AddressingParameters addrParams = resource.getAddressingParameters();
			if (addrParams != null)
				refParams = addrParams.toReferenceParameters();
		}

		return new EndpointReferenceType(address, refParams, createMetadata(implementedPortTypes, resource, masterType), null);
	}

	/**
	 * Added 2014-01-09 by ASG to allow us to selectively not put X.509 SecurityTokens in EPR's. It would be great if we could use
	 * "if ((this instanceof GeniiNoOutCalls))" instead of the kludge with class names, but we don't have a reference to the class For now we
	 * must explicitly list every type we want to avoid. The list should come out of a configuration file.
	 */
	static private void MetaDataSecurityToken(ArrayList<MessageElement> metaDataAny, IResource resource) throws ResourceException
	{
		String serviceName = ((ResourceKey) resource.getParentResourceKey()).getServiceName();

		// ASG 2014-01-11
		// This is where the list of classes that do not get their own X.509 goes
		Boolean matches = false;

		// Get the list of classes that should NOT have an X.509 in their EPR
		String noX509List = ContainerProperties.getContainerProperties().getEPRConstructionProperties();
		synchronized (reportedAlready) {
			if (noX509List == null) {
				noX509List = "LightWeightExportPortType";
				if (!reportedAlready)
					_logger.warn("Could not find EPRConstruction property: " + ContainerProperties.NO_X509_CLASS_LIST
						+ "; so using default value: " + noX509List);
			} else {
				if (!reportedAlready) {
					_logger.info("EPRConstruction property says no X509 for these services: " + noX509List);
				}
			}
			// don't bother saying anything about the properties again.
			reportedAlready = true;
		}

		// ASG: 2014-01-21 Now check the serviceName against the list of classes in which we do put
		// X.509 certs, e.g. LightWeightExportPortType
		StringTokenizer tokenCollector = new StringTokenizer(noX509List, ":");
		while (tokenCollector.hasMoreTokens()) {
			String className = tokenCollector.nextToken();
			if (serviceName.equalsIgnoreCase(className))
				matches = true;
		}
		
		if (!matches) { // this EPR should get an X.509 in it
			try {
				// Go ahead and put in the security stuff
				// add Security Token Reference
				X509Certificate[] certChain = (X509Certificate[]) resource.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);

				if (certChain != null) {
					// ASG: This is where the X.509 embedded security token gets added
					MessageElement wseTokenRef = WSSecurityUtils.makePkiPathSecTokenRef(certChain, "RecipientMessageIdentity");
					metaDataAny.add(wseTokenRef);
				}
			} catch (AuthZSecurityException e) {
				throw new ResourceException(e.getMessage(), e);
			} catch (IOException e) {
				throw new ResourceException(e.getMessage(), e);
			}
		} else {
			if (_logger.isTraceEnabled()) {
				_logger.debug("not putting an x509 into EPR for class: " + serviceName);
			}
		}
	}

	static private void addSecureAddressingElements(ArrayList<MessageElement> metaDataAny, IResource resource) throws ResourceException
	{
		MetaDataSecurityToken(metaDataAny, resource);

		try {
			// get authz/enc requirements
			IAuthZProvider handler = AuthZProviders.getProvider(((ResourceKey) resource.getParentResourceKey()).getServiceName());
			MessageLevelSecurityRequirements minMsgSec = new MessageLevelSecurityRequirements(MessageLevelSecurityRequirements.NONE);
			if ((handler != null) && (handler.getMinIncomingMsgLevelSecurity(resource) != null)) {
				minMsgSec = handler.getMinIncomingMsgLevelSecurity(resource);
			}

			boolean includeServerTls = Container.getContainerConfiguration().isSSL();

			// add security policy
			if (!minMsgSec.isNone() || includeServerTls) {

				// construct policy components
				ArrayList<MessageElement> policyComponents = new ArrayList<MessageElement>();

				// add ServerTLS if necessary
				if (includeServerTls) {
					org.apache.axis.types.URI serverTlsUri = new org.apache.axis.types.URI(SecurityConstants.SERVER_TLS_URI);
					PolicyReference serverTlsReference = new PolicyReference();
					serverTlsReference.setURI(serverTlsUri);
					MessageElement tlsMel = new MessageElement(PolicyReference.getTypeDesc().getXmlType().getNamespaceURI(),
						"PolicyReference", serverTlsReference);
					policyComponents.add(tlsMel);
				}

				// add X.509 message-level signing if necessary
				if (minMsgSec.isSign()) {

					// add MutualX509 Ref
					org.apache.axis.types.URI mutualX509Uri = new org.apache.axis.types.URI(SecurityConstants.MUTUAL_X509_URI);
					PolicyReference mutualX509Reference = new PolicyReference();
					mutualX509Reference.setURI(mutualX509Uri);
					MessageElement x509Mel = new MessageElement(PolicyReference.getTypeDesc().getXmlType().getNamespaceURI(),
						"PolicyReference", mutualX509Reference);
					policyComponents.add(x509Mel);

					// add our optional blend of Credentials

					// create saml claim type
					ClaimsType claims = new ClaimsType();
					claims.setDialect(new org.apache.axis.types.URI(SecurityConstants.SAML_CLAIMS_URI));

					// create include attribute
					IncludeTokenOpenType includeToken = new IncludeTokenOpenType(IncludeTokenType._value3);

					// create saml token
					TokenAssertionType samlToken = new TokenAssertionType();
					samlToken.setIncludeToken(includeToken);
					MessageElement[] samlSubEls =
						{ new MessageElement(ClaimsType.getTypeDesc().getXmlType().getNamespaceURI(), "Claims", claims) };
					samlToken.set_any(samlSubEls);

					// create policy
					Policy samlPolicy = new Policy();
					MessageElement[] policySubEls =
						{ new MessageElement(TokenAssertionType.getTypeDesc().getXmlType().getNamespaceURI(), "SamlToken", samlToken) };
					samlPolicy.set_any(policySubEls);

					// create SignedSupporting Tokens
					MessageElement signedSupportingTokensMel = new MessageElement(
						new QName(NestedPolicyType.getTypeDesc().getXmlType().getNamespaceURI(), "SignedSupportingTokens"));
					signedSupportingTokensMel.setAttribute(Policy.getTypeDesc().getXmlType().getNamespaceURI(), "Optional", "true");
					signedSupportingTokensMel
						.addChild(new MessageElement(Policy.getTypeDesc().getXmlType().getNamespaceURI(), "Policy", samlPolicy));

					policyComponents.add(signedSupportingTokensMel);
				}

				// add encryption if necessary
				if (minMsgSec.isEncrypt()) {
					SePartsType encryptedParts = new SePartsType();
					encryptedParts.setBody(new EmptyType());
					MessageElement encryptMel =
						new MessageElement(SePartsType.getTypeDesc().getXmlType().getNamespaceURI(), "EncryptedParts", encryptedParts);
					policyComponents.add(encryptMel);
				}

				// add optional username/token
				org.apache.axis.types.URI usernameTokenUri = new org.apache.axis.types.URI(SecurityConstants.USERNAME_TOKEN_URI);
				PolicyReference usernameTokenReference = new PolicyReference();
				usernameTokenReference.setURI(usernameTokenUri);
				MessageElement usernameTokenMel = new MessageElement(PolicyReference.getTypeDesc().getXmlType().getNamespaceURI(),
					"PolicyReference", usernameTokenReference);
				usernameTokenMel.setAttribute(Policy.getTypeDesc().getXmlType().getNamespaceURI(), "Optional", "true");
				policyComponents.add(usernameTokenMel);

				// construct Meta Policy
				Policy metaPolicy = new Policy();
				metaPolicy.set_any(Elementals.toArray(policyComponents));

				// construct AppliesTo
				URI appliesToUri = new URI("urn:wsaaction:*");
				MessageElement[] appliesToAny = { new MessageElement(URI.getTypeDesc().getXmlType(), appliesToUri) };
				AppliesTo appliesTo = new AppliesTo(appliesToAny);

				// construct Policy Attachment
				PolicyAttachment policyAttachment = new PolicyAttachment();
				policyAttachment.setAppliesTo(appliesTo);
				policyAttachment.setPolicy(metaPolicy);
				MessageElement policyAttachmentMel = new MessageElement(PolicyAttachment.getTypeDesc().getXmlType(), policyAttachment);

				metaDataAny.add(policyAttachmentMel);
			}
		} catch (GenesisIISecurityException e) {
			throw new ResourceException(e.getMessage(), e);
		} catch (SOAPException e) {
			throw new ResourceException(e.getMessage(), e);
		} catch (IOException e) {
			throw new ResourceException(e.getMessage(), e);
		}
	}

	static private void addOriginalSecurityElements(ArrayList<MessageElement> metaDataAny, IResource resource) throws ResourceException
	{

		MetaDataSecurityToken(metaDataAny, resource);

		try {

			// add minimum level of message level security
			IAuthZProvider handler = AuthZProviders.getProvider(((ResourceKey) resource.getParentResourceKey()).getServiceName());
			if (handler != null) {
				MessageLevelSecurityRequirements minMsgSec = handler.getMinIncomingMsgLevelSecurity(resource);

				RequiredMessageSecurityTypeMin minAttr = RequiredMessageSecurityTypeMin.fromString(minMsgSec.toString());
				RequiredMessageSecurityType min = new RequiredMessageSecurityType(handler.getClass().getName(), minAttr);

				MessageElement mel = new MessageElement(RequiredMessageSecurityType.getTypeDesc().getXmlType(), min);

				metaDataAny.add(mel);
			}

		} catch (GenesisIISecurityException e) {
			throw new ResourceException(e.getMessage(), e);
		} catch (IOException e) {
			throw new ResourceException(e.getMessage(), e);
		}

	}

	static public MetadataType createMetadata(PortType[] portTypes, ResourceKey resourceKey, String masterType) throws ResourceException
	{
		if (portTypes == null)
			portTypes = new PortType[0];

		ArrayList<MessageElement> any = new ArrayList<MessageElement>();

		any.add(new MessageElement(OGSAWSRFBPConstants.WS_RESOURCE_INTERFACES_ATTR_QNAME, PortType.portTypeFactory().translate(portTypes)));
		if (resourceKey != null) {
			String porttypeString = "unknown"; // ASG Added

			if (portTypes.length == 0) {
				any.add(new MessageElement(new QName(WSAddressingConstants.WSA_NS, "PortType"),
					new QName(GenesisIIConstants.GENESISII_NS, porttypeString = "NullPortType")));
			} else {
				if (masterType == null) // handles the case for RootRNSForks !
				{
					if (resourceKey.getServiceName() == null) {
						porttypeString = "NullPortType";
						throw new ResourceException("Couldn't locate target service name in the resource key");
					} else {
						any.add(new MessageElement(new QName(WSAddressingConstants.WSA_NS, "PortType"),
							new QName(GenesisIIConstants.GENESISII_NS, porttypeString = resourceKey.getServiceName())));
					}
				}

				else {
					any.add(new MessageElement(new QName(WSAddressingConstants.WSA_NS, "PortType"),
						new QName(GenesisIIConstants.GENESISII_NS, porttypeString = masterType)));
				}
			}
			if (_logger.isTraceEnabled()) {
				_logger.trace("Portname = " + porttypeString);
			}
			IResource resource = resourceKey.dereference();

			Object prop = resource.getProperty(IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME);
			if (prop == null) {
				String msg = "received empty property for EPI name";
				_logger.error(msg);
				throw new ResourceException(msg);
			}

			// add epi
			any.add(new MessageElement(WSName.ENDPOINT_IDENTIFIER_QNAME, prop.toString()));

			/*
			 * add security metadata (Use OGSA Secure Addressing depending on config setting)
			 */
			try {
				String useEap = Installation.getDeployment(new DeploymentName()).security().getProperty(
					edu.virginia.vcgr.genii.client.configuration.KeystoreSecurityConstants.Container.RESOURCE_IDENTITY_USE_OGSA_EAP_PROP);
				if (useEap.equalsIgnoreCase("true")) {
					addSecureAddressingElements(any, resource);
				} else {
					addOriginalSecurityElements(any, resource);
				}
			} catch (ConfigurationException ce) {
				addOriginalSecurityElements(any, resource);
			}

		}

		any.add(new MessageElement(ContainerConstants.CONTAINER_ID_METADATA_ELEMENT, Container.getContainerID().toString()));

		// Add container key.
		return new MetadataType(Elementals.toArray(any));
	}

	/**
	 * returns a textual name for the resource specified. this is very limited in capabilities, and currently only returns the key name for
	 * the resource. there seems to be no general and simple way to map a resource to its RNS path.
	 */
	static public String getResourceName(IResource resource)
	{
		String toReturn = null;
		try {
			toReturn = resource.getKey();
		} catch (Throwable e) {
			// ignore, will just miss part of print-out.
		}

		return toReturn;
	}

}
