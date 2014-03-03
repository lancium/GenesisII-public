/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.virginia.vcgr.genii.container.kerbauthn;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.MetadataMappingType;
import org.ggf.rns.NameMappingType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSEntryType;
import org.ggf.rns.WriteNotPermittedFaultType;
import org.morgan.inject.MInject;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.container.commonauthn.BaseAuthenticationServiceImpl;
import edu.virginia.vcgr.genii.container.commonauthn.ReplicaSynchronizer.STSResourcePropertiesRetriever;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;
import edu.virginia.vcgr.genii.container.rns.RNSDBResourceProvider;
import edu.virginia.vcgr.genii.container.x509authn.X509AuthnServiceImpl;
import edu.virginia.vcgr.genii.kerbauthn.KerbAuthnPortType;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

@GeniiServiceConfiguration(resourceProvider = RNSDBResourceProvider.class, defaultAuthZProvider = KerbAuthZProvider.class)
public class KerbAuthnServiceImpl extends BaseAuthenticationServiceImpl
		implements KerbAuthnPortType
// , BaggageAggregatable
{
	static private Log _logger = LogFactory.getLog(KerbAuthnServiceImpl.class);

	@MInject(lazy = true)
	private IRNSResource _resource;

	public KerbAuthnServiceImpl() throws RemoteException {
		this(WellKnownPortTypes.KERB_AUTHN_SERVICE_PORT_TYPE().getQName()
				.getLocalPart());
	}

	protected KerbAuthnServiceImpl(String serviceName) throws RemoteException {
		super(serviceName);

		addImplementedPortType(WellKnownPortTypes
				.KERB_AUTHN_SERVICE_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes.STS_SERVICE_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes.RNS_PORT_TYPE());
	}

	public PortType getFinalWSResourceInterface() {
		return WellKnownPortTypes.KERB_AUTHN_SERVICE_PORT_TYPE();
	}

	@Override
	protected void setAttributeHandlers() throws NoSuchMethodException,
			ResourceException, ResourceUnknownFaultType {
		super.setAttributeHandlers();
		new KerbAuthnAttributesHandler(getAttributePackage());
	}

	@Override
	protected Object translateConstructionParameter(MessageElement property)
			throws Exception {
		// decodes the base64-encoded delegated assertion construction param
		QName name = property.getQName();
		if (name.equals(SecurityConstants.NEW_IDP_NAME_QNAME)
				|| name.equals(SecurityConstants.NEW_KERB_IDP_REALM_QNAME)
				|| name.equals(SecurityConstants.NEW_KERB_IDP_KDC_QNAME)) {
			return property.getValue();
		} else if (name.equals(SecurityConstants.IDP_VALID_MILLIS_QNAME)) {
			return Long.decode(property.getValue());
		} else if (name.equals(SecurityConstants.NEW_IDP_TYPE_QNAME)) {
			if (_logger.isDebugEnabled())
				_logger.debug("for name " + name + " got "
						+ property.getValue());
			return property.getValue();
		} else {
			return super.translateConstructionParameter(property);
		}
	}

	@Override
	protected ResourceKey createResource(GenesisHashMap constructionParameters)
			throws ResourceException, BaseFaultType {
		// Specify additional O (org) field for our resource's certificate (viz.
		// realm)
		String realm = (String) constructionParameters
				.get(SecurityConstants.NEW_KERB_IDP_REALM_QNAME);
		if (realm != null) {
			String[] newOrgs = { realm };
			constructionParameters.put(
					IResource.ADDITIONAL_ORGS_CONSTRUCTION_PARAM, newOrgs);
		}
		return super.createResource(constructionParameters);
	}

	@Override
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
			ConstructionParameters cParams,
			GenesisHashMap constructionParameters,
			Collection<MessageElement> resolverCreationParams)
			throws ResourceException, BaseFaultType, RemoteException {
		_logger.debug("entering postCreate");
		if (skipPortTypeSpecificPostProcessing(constructionParameters)) {
			super.postCreate(rKey, newEPR, cParams, constructionParameters,
					resolverCreationParams);
			return;
		}

		// get the IDP resource's db resource
		IResource resource = rKey.dereference();

		// store the Realm in the idp resource
		String realm = (String) constructionParameters
				.get(SecurityConstants.NEW_KERB_IDP_REALM_QNAME);
		resource.setProperty(
				SecurityConstants.NEW_KERB_IDP_REALM_QNAME.getLocalPart(),
				realm);

		// store the KDC in the idp resource
		String kdc = (String) constructionParameters
				.get(SecurityConstants.NEW_KERB_IDP_KDC_QNAME);
		resource.setProperty(
				SecurityConstants.NEW_KERB_IDP_KDC_QNAME.getLocalPart(), kdc);

		X509AuthnServiceImpl.sharedPostCreate(this, rKey, newEPR, cParams,
				constructionParameters, resolverCreationParams);

		super.postCreate(rKey, newEPR, cParams, constructionParameters,
				resolverCreationParams);
	}

	@Override
	protected void preDestroy() throws RemoteException, ResourceException {
		super.preDestroy();
		preDestroy(_resource);
	}

	/*
	 * @Override public ArrayList<RequestSecurityTokenResponseType>
	 * aggregateBaggageTokens(RequestSecurityTokenType request) throws
	 * java.rmi.RemoteException { ArrayList<RequestSecurityTokenResponseType>
	 * gatheredResponses = new ArrayList<RequestSecurityTokenResponseType>();
	 * Collection<InternalEntry> entries = _resource.retrieveEntries(null);
	 * 
	 * for (InternalEntry entry : entries) { try { EndpointReferenceType idpEpr
	 * = entry.getEntryReference();
	 * 
	 * // create a proxy to the remote idp and invoke it X509AuthnPortType idp =
	 * ClientUtils.createProxy(X509AuthnPortType.class, idpEpr);
	 * RequestSecurityTokenResponseType[] responses =
	 * idp.requestSecurityToken2(request);
	 * 
	 * if (responses != null) { for (RequestSecurityTokenResponseType response :
	 * responses) { gatheredResponses.add(response); } } } catch (Exception e) {
	 * _logger.error("Could not retrieve token for IDP " + entry.getName() +
	 * ": " + e.getMessage(), e); } }
	 * 
	 * return gatheredResponses; }
	 */

	@RWXMapping(RWXCategory.EXECUTE)
	public RequestSecurityTokenResponseType[] requestSecurityToken2(
			RequestSecurityTokenType request) throws java.rmi.RemoteException {
		return X509AuthnServiceImpl.sharedSecurityTokenResponder(this,
				_resource, request);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] add(RNSEntryType[] addRequest)
			throws RemoteException, RNSEntryExistsFaultType,
			ResourceUnknownFaultType {
		return addRNSEntries(addRequest, _resource);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public LookupResponseType lookup(String[] lookupRequest)
			throws RemoteException, ResourceUnknownFaultType {
		return lookup(lookupRequest, _resource);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] remove(String[] removeRequest)
			throws RemoteException, WriteNotPermittedFaultType {
		return remove(removeRequest, _resource);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] rename(NameMappingType[] renameRequest)
			throws RemoteException, WriteNotPermittedFaultType {
		throw new RemoteException("\"rename\" not applicable.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] setMetadata(
			MetadataMappingType[] setMetadataRequest) throws RemoteException,
			WriteNotPermittedFaultType {
		throw new RemoteException("\"setMetadata\" not applicable.");
	}

	@Override
	public STSResourcePropertiesRetriever getResourcePropertyRetriver() {
		return new kerberosResourcePropertiesRetriever();
	}

	@Override
	public Set<QName> getSensitivePropertyNames() {
		Set<QName> propertyNames = super.getSensitivePropertyNames();
		propertyNames.add(SecurityConstants.NEW_KERB_IDP_REALM_QNAME);
		propertyNames.add(SecurityConstants.NEW_KERB_IDP_KDC_QNAME);
		return propertyNames;
	}

	public static class kerberosResourcePropertiesRetriever extends
			CommonSTSPropertiesRetriever {
		@Override
		public void retrieveAndStoreResourceProperties(
				GeniiCommon proxyToPrimary, IRNSResource resource)
				throws Exception {
			super.retrieveAndStoreResourceProperties(proxyToPrimary, resource);

			QName[] propertyNames = new QName[2];
			propertyNames[0] = SecurityConstants.NEW_KERB_IDP_REALM_QNAME;
			propertyNames[1] = SecurityConstants.NEW_KERB_IDP_KDC_QNAME;
			GetMultipleResourcePropertiesResponse response = proxyToPrimary
					.getMultipleResourceProperties(propertyNames);

			MessageElement[] propertyValues = response.get_any();
			if (propertyValues == null
					|| propertyValues.length < propertyNames.length) {
				throw new RemoteException(
						"Could not retrieve all necessary resource properties");
			}

			for (MessageElement element : propertyValues) {
				QName name = element.getQName();
				String value = element.getValue();
				if (value == null) {
					String msg = "A required Kerberos attribute is missing: "
							+ name;
					_logger.error(msg);
					throw new RuntimeException(msg);
				}
				if (SecurityConstants.NEW_KERB_IDP_REALM_QNAME.equals(name)) {
					resource.setProperty(
							SecurityConstants.NEW_KERB_IDP_REALM_QNAME
									.getLocalPart(), value);
				} else if (SecurityConstants.NEW_KERB_IDP_KDC_QNAME
						.equals(name)) {
					resource.setProperty(
							SecurityConstants.NEW_KERB_IDP_KDC_QNAME
									.getLocalPart(), value);
				}
			}
		}
	}
}
