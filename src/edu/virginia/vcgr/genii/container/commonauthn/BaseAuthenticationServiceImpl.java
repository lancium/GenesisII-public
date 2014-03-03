package edu.virginia.vcgr.genii.container.commonauthn;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSEntryType;
import org.ggf.rns.RNSMetadataType;
import org.ggf.rns.WriteNotPermittedFaultType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocument;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;
import org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.ResourceLock;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSOperations;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.sync.SyncProperty;
import edu.virginia.vcgr.genii.client.sync.VersionVector;
import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AbstractNotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSOperationContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSTopics;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.axis.ServerWSDoAllReceiver;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.commonauthn.ReplicaSynchronizer.STSResourcePropertiesRetriever;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.AutoReplicate;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.rns.RNSContainerUtilities;
import edu.virginia.vcgr.genii.container.security.authz.providers.AclTopics;
import edu.virginia.vcgr.genii.container.sync.AclChangeNotificationHandler;
import edu.virginia.vcgr.genii.container.sync.DestroyFlags;
import edu.virginia.vcgr.genii.container.sync.MessageFlags;
import edu.virginia.vcgr.genii.container.sync.ReplicationItem;
import edu.virginia.vcgr.genii.container.sync.ReplicationThread;
import edu.virginia.vcgr.genii.container.sync.ResourceSyncRunner;
import edu.virginia.vcgr.genii.container.sync.VersionedResourceAttributeHandlers;
import edu.virginia.vcgr.genii.container.sync.VersionedResourceUtils;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.PublisherTopic;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.TopicSet;
import edu.virginia.vcgr.genii.container.x509authn.BaggageAggregatable;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.x509.CertCreationSpec;
import edu.virginia.vcgr.genii.security.x509.CertTool;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.x509authn.X509AuthnPortType;

/*
 * This class incorporates the IDP specific functionalities that are common to all IDP port-types.
 * Although technically it is a super-class of all IDP port-type implementation classes we have in
 * the system, for many operations -- for example RNS operations -- subclasses have to explicitly
 * call methods on this class instead of having the operations automatically handled here when there
 * is no override. This peculiar behavior is due to the mechanism of service class generation that
 * we have in our system. When multiple port-types are registered to implement some common methods
 * in a WSDL file, the generation procedure don't extract a common interface for the common methods;
 * rather it creates independent interface classes having the same methods. So a common
 * implementation class like this should have name conflicts for the common methods if it implements
 * all the interfaces. We avoid the problem of name conflict by asking subclasses to explicitly call
 * this class's method for conflicting methods.
 */
public abstract class BaseAuthenticationServiceImpl extends GenesisIIBase
		implements RNSTopics, BaggageAggregatable {
	private static Log _logger = LogFactory
			.getLog(BaseAuthenticationServiceImpl.class);

	protected BaseAuthenticationServiceImpl(String serviceName)
			throws RemoteException {
		super(serviceName);
	}

	@Override
	public ArrayList<RequestSecurityTokenResponseType> aggregateBaggageTokens(
			IRNSResource resource, RequestSecurityTokenType request)
			throws java.rmi.RemoteException {
		ArrayList<RequestSecurityTokenResponseType> gatheredResponses = new ArrayList<RequestSecurityTokenResponseType>();
		Collection<InternalEntry> entries = resource.retrieveEntries(null);

		for (InternalEntry entry : entries) {
			try {
				EndpointReferenceType idpEpr = entry.getEntryReference();
				// create a proxy to the remote idp and invoke it.
				X509AuthnPortType idp = ClientUtils.createProxy(
						X509AuthnPortType.class, idpEpr);
				RequestSecurityTokenResponseType[] responses = idp
						.requestSecurityToken2(request);
				if (responses != null) {
					for (RequestSecurityTokenResponseType response : responses) {
						gatheredResponses.add(response);
					}
				}
			} catch (Exception e) {
				_logger.error(
						"Could not retrieve token for IDP " + entry.getName()
								+ ": " + e.getMessage(), e);
			}
		}
		return gatheredResponses;
	}

	/*
	 * This override is extremely important for the proper functioning of any
	 * replicated IDP instance. This duplicates the certificate of the primary
	 * resource and use that certificate instead of a newly generated
	 * certificate during the resource creation process.
	 * 
	 * In addition, we use this override to modify the certificate's DN when
	 * creating a primary resource. Unlike other resources, a IDP resource's
	 * certificate bears its name in the subject DN field. To facilitate this we
	 * need to pass a construction parameter during the resource creation phase.
	 */
	@Override
	protected ResourceKey createResource(GenesisHashMap constructionParameters)
			throws ResourceException, BaseFaultType {
		// insert construction parameter to affect DN names of the certificate
		String CN = constructionParameters
				.getString(SecurityConstants.NEW_IDP_NAME_QNAME);
		if (CN != null) {
			constructionParameters.put(
					IResource.ADDITIONAL_CNS_CONSTRUCTION_PARAM,
					new String[] { CN });
		}

		// retrieve primary resources EPR during replica creation
		EndpointReferenceType certificateOwnerEPR = (EndpointReferenceType) constructionParameters
				.get(IResource.PRIMARY_EPR_CONSTRUCTION_PARAM);
		if (certificateOwnerEPR == null) {
			MessageElement certificateOwner = constructionParameters
					.getAxisMessageElement(STSConfigurationProperties.CERTIFICATE_OWNER_EPR);
			if (certificateOwner != null) {
				try {
					certificateOwnerEPR = (EndpointReferenceType) certificateOwner
							.getObjectValue(EndpointReferenceType.class);
				} catch (Exception e) {
					throw new ResourceException(
							"failed reconstruct primary resource's EPR", e);
				}
			}
		}

		// retrieve primary resource's certificate for duplication
		if (certificateOwnerEPR != null) {
			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class,
					certificateOwnerEPR);
			try {
				GetResourcePropertyResponse response = common
						.getResourceProperty(SecurityConstants.CERTIFICATE_CHAIN_QNAME);
				MessageElement property = new MessageElement(
						response.get_any()[0]);
				X509Certificate[] certificate = (X509Certificate[]) CommonSTSAttributesHandler
						.deserializeObjectFromString(property.getValue());
				constructionParameters.put(
						IResource.DUPLICATED_CERTIFICATE_PARAM, certificate);
			} catch (Exception e) {
				throw new ResourceException(
						"failed to load certificate from the primary", e);
			}
		}

		return super.createResource(constructionParameters);
	}

	/*
	 * By default, the system adds a newly created IDP resource as an RNS entry
	 * under the directory for the IDP service in a container. As RNS entries
	 * are distinguished by their names, two IDP instances having the same name
	 * cannot reside in a single container for a single IDP port-type. This
	 * apparent restriction simplifies tracking of IDP instances for later use.
	 */
	public String addResourceInServiceResourceList(
			EndpointReferenceType newEPR, GenesisHashMap constructionParameters)
			throws ResourceUnknownFaultType, ResourceException,
			RNSEntryExistsFaultType {
		// make sure the specific IDP doesn't yet exist
		String newIdpName = constructionParameters
				.getString(SecurityConstants.NEW_IDP_NAME_QNAME);
		ResourceKey serviceKey = ResourceManager.getCurrentResource();
		IRNSResource serviceResource = (IRNSResource) serviceKey.dereference();
		Collection<String> entries = serviceResource.listEntries(null);
		if (entries.contains(newIdpName)) {
			throw FaultManipulator.fillInFault(new RNSEntryExistsFaultType(
					null, null, null, null, null, null, newIdpName));
		}

		// add the delegated identity to the service's list of IDPs
		serviceResource.addEntry(new InternalEntry(newIdpName, newEPR, null));
		serviceResource.commit();
		return newIdpName;
	}

	public void storeCallingContextAndCertificate(IResource resource,
			NuCredential credential) throws ResourceException {
		ICallingContext resourceContext = (ICallingContext) resource
				.getProperty(IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME);
		TransientCredentials transientCredentials = TransientCredentials
				.getTransientCredentials(resourceContext);
		transientCredentials.add(credential);
		resource.setProperty(IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME,
				resourceContext);
		resource.setProperty(
				SecurityConstants.IDP_STORED_CREDENTIAL_QNAME.getLocalPart(),
				credential);
	}

	/*
	 * This override is used to initiate replication of an IDP instance. The
	 * presence of the primary-epr- construction-parameter indicates that the
	 * resource under creation is not a new IDP instance, rather a replica of
	 * some existing instance in some container. When this parameter is present,
	 * the system delegates the responsibility of retrieving necessary resource
	 * properties and linked descendant IDP instances of the primary copy to the
	 * ReplicaSynchronizer class, which asynchronously synchronizes this replica
	 * with the primary.
	 */
	@Override
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
			ConstructionParameters cParams,
			GenesisHashMap constructionParameters,
			Collection<MessageElement> resolverCreationParameters)
			throws ResourceException, BaseFaultType, RemoteException {

		super.postCreate(rKey, newEPR, cParams, constructionParameters,
				resolverCreationParameters);
		STSCertificationSpec stsCertificationSpec = (STSCertificationSpec) constructionParameters
				.get(IResource.CERTIFICATE_CREATION_SPEC_CONSTRUCTION_PARAM);

		IResource resource = rKey.dereference();
		resource.setProperty(IResource.PRIVATE_KEY_PROPERTY_NAME,
				stsCertificationSpec.getSubjectPrivateKey());

		EndpointReferenceType primaryEPR = (EndpointReferenceType) constructionParameters
				.get(IResource.PRIMARY_EPR_CONSTRUCTION_PARAM);
		if (primaryEPR != null) {

			// storeReplicaInServicesResourceList(newEPR, primaryEPR);

			VersionedResourceUtils.initializeReplica(resource, primaryEPR, 0);
			WorkingContext context = WorkingContext.getCurrentWorkingContext();
			ReplicationThread thread = new ReplicationThread(context);
			thread.add(new ReplicationItem(new ReplicaSynchronizer(
					getResourcePropertyRetriver()), newEPR));
			thread.start();
		}
	}

	/*
	 * By default, IDP resources are been added to corresponding service
	 * resource's RNS directory. For replication, it is not clear whether or not
	 * replicas should be similarly added in the RNS directory in the container
	 * holding the replicas. At this point, we are not adding replicas in
	 * service's RNS directory. However we keep the method in place in case we
	 * want to confirm to the default behavior.
	 */
	protected void storeReplicaInServicesResourceList(
			EndpointReferenceType newEPR, EndpointReferenceType primaryEPR)
			throws ResourceException, GenesisIISecurityException,
			RemoteException, InvalidResourcePropertyQNameFaultType,
			ResourceUnknownFaultType, ResourceUnavailableFaultType,
			RNSEntryExistsFaultType {
		GeniiCommon proxy = ClientUtils.createProxy(GeniiCommon.class,
				primaryEPR);
		GetResourcePropertyResponse idpNameProperty = proxy
				.getResourceProperty(SecurityConstants.NEW_IDP_NAME_QNAME);
		MessageElement[] propertyValue = idpNameProperty.get_any();
		if (propertyValue != null) {
			String idpName = propertyValue[0].getValue();
			GenesisHashMap propertyMap = new GenesisHashMap(1);
			propertyMap.put(SecurityConstants.NEW_IDP_NAME_QNAME, idpName);
			addResourceInServiceResourceList(newEPR, propertyMap);
		}
	}

	/*
	 * During replica creation we do not have enough information to perform
	 * port-type specific post processing of resources. The problem is calling
	 * postCreate methods without necessary properties throws exceptions. So
	 * this method is used by subclasses as an indicator that post processing
	 * through postCreate method should be avoided.
	 */
	protected boolean skipPortTypeSpecificPostProcessing(
			GenesisHashMap constructionParameters) {

		EndpointReferenceType primaryEPR = (EndpointReferenceType) constructionParameters
				.get(IResource.PRIMARY_EPR_CONSTRUCTION_PARAM);
		MessageElement isReplica = constructionParameters
				.getAxisMessageElement(STSConfigurationProperties.REPLICA_STS_CONSTRUCTION_PARAM);
		boolean skipPostCreateOverride = primaryEPR != null
				|| (isReplica != null && "TRUE".equalsIgnoreCase(isReplica
						.getValue()));
		return skipPostCreateOverride;
	}

	/*
	 * Note that the Unlink flag is used to indicate that the invoker is
	 * removing a replica from the container and the removal should have no
	 * cascading effect. On the other hand, destroy means all replicas,
	 * including the primary copy, should be removed from the GFFS name-space.
	 */
	protected void preDestroy(IRNSResource resource) throws RemoteException,
			ResourceException {
		DestroyFlags flags = VersionedResourceUtils.preDestroy(resource);
		if (flags != null) {
			TopicSet space = TopicSet.forPublisher(getClass());
			PublisherTopic topic = space
					.createPublisherTopic(RNS_OPERATION_TOPIC);
			EndpointReferenceType myEPR = (EndpointReferenceType) WorkingContext
					.getCurrentWorkingContext().getProperty(
							WorkingContext.EPR_PROPERTY_NAME);
			RNSOperations operation = (flags.isUnlinked ? RNSOperations.Unlink
					: RNSOperations.Destroy);
			topic.publish(new RNSOperationContents(operation, ".", myEPR,
					flags.vvr));
		}
	}

	/*
	 * IDP resources have their own public and private keys. This is a deviation
	 * from the standard protocol used in Genesis-II that assigns resources
	 * certificates but no key pairs. Here we have to override the default
	 * behavior because to make a replicated IDP resource a valid working copy
	 * of the primary, we need to replicate the keys from the primary. Otherwise
	 * the signatures created from different replicas would be different. If
	 * individual IDP resources did not have their own key pairs, the containers
	 * participating in a replication scheme had to share the same
	 * private-public keys.
	 */
	@Override
	protected CertCreationSpec getChildCertSpec() throws ResourceException,
			ResourceUnknownFaultType {
		try {
			KeyPair subjectKeypair = CertTool
					.generateKeyPair(SecurityConstants.IDP_RESOURCE_KEY_LENGTH);
			X509Certificate[] issuerCertificateChain = Container
					.getContainerCertChain();
			PrivateKey issuerPrivateKey = Container.getContainerPrivateKey();
			return new STSCertificationSpec(subjectKeypair,
					issuerCertificateChain, issuerPrivateKey,
					getServiceCertificateLifetime());
		} catch (GeneralSecurityException e) {
			throw new ResourceException(
					"could not generate certificate for IDP resource", e);
		}
	}

	@Override
	public ResourceSyncRunner getClassResourceSyncRunner() {
		return new ReplicaSynchronizer(getResourcePropertyRetriver());
	}

	@Override
	protected void setAttributeHandlers() throws NoSuchMethodException,
			ResourceException, ResourceUnknownFaultType {
		super.setAttributeHandlers();
		new CommonSTSAttributesHandler(getAttributePackage());
		new VersionedResourceAttributeHandlers(getAttributePackage());
	}

	/*
	 * This override is pivotal for ensuring proper behavior. A IDP resource has
	 * several properties that are either confidential or bulky. This properties
	 * are returned only when being asked for explicitly or after a strict
	 * authentication checking. However the process for retrieving resource
	 * property document is quite generic and intricately coupled with
	 * individual resource property retrieval process. Hence we added this
	 * override to remove the sensitive properties from a get resource property
	 * document response.
	 */
	@Override
	public GetResourcePropertyDocumentResponse getResourcePropertyDocument(
			GetResourcePropertyDocument request) throws RemoteException,
			ResourceUnknownFaultType, ResourceUnavailableFaultType {

		GetResourcePropertyDocumentResponse originalResponse = super
				.getResourcePropertyDocument(request);
		MessageElement[] properties = originalResponse.get_any();
		if (properties == null)
			return originalResponse;

		List<MessageElement> filteredPropertyList = new ArrayList<MessageElement>();
		Set<QName> sensitivePropertyNames = getSensitivePropertyNames();
		for (MessageElement property : properties) {
			if (!sensitivePropertyNames.contains(property.getQName())) {
				filteredPropertyList.add(property);
			}
		}
		return new GetResourcePropertyDocumentResponse(
				filteredPropertyList
						.toArray(new MessageElement[filteredPropertyList.size()]));
	}

	public static class CommonSTSPropertiesRetriever implements
			STSResourcePropertiesRetriever {

		@Override
		public void retrieveAndStoreResourceProperties(
				GeniiCommon proxyToPrimary, IRNSResource resource)
				throws Exception {

			QName[] propertyNames = new QName[5];
			propertyNames[0] = SecurityConstants.NEW_IDP_NAME_QNAME;
			propertyNames[1] = SecurityConstants.STORED_CALLING_CONTEXT_QNAME;
			propertyNames[2] = SecurityConstants.IDP_STORED_CREDENTIAL_QNAME;
			propertyNames[3] = SecurityConstants.CERTIFICATE_CHAIN_QNAME;
			propertyNames[4] = SecurityConstants.IDP_PRIVATE_KEY_QNAME;
			GetMultipleResourcePropertiesResponse response = proxyToPrimary
					.getMultipleResourceProperties(propertyNames);

			MessageElement[] propertyValues = response.get_any();
			if (propertyValues == null
					|| propertyValues.length < propertyNames.length) {
				throw new RemoteException(
						"Could not retrieve all necessary resource properties");
			}

			for (MessageElement element : propertyValues) {
				try {
					storeProperty(resource, element);
				} catch (Exception ex) {
					_logger.info("failed to store property: "
							+ element.getQName());
					throw ex;
				}
			}

			postProcessStoredCallingContext(resource);
		}

		private void storeProperty(IRNSResource resource,
				MessageElement property) throws Exception {

			QName propertyName = property.getQName();

			if (SecurityConstants.NEW_IDP_NAME_QNAME.equals(propertyName)) {
				resource.setProperty(
						SecurityConstants.NEW_IDP_NAME_QNAME.getLocalPart(),
						property.getValue());

			} else if (SecurityConstants.IDP_STORED_CREDENTIAL_QNAME
					.equals(propertyName)) {
				NuCredential delegatedCredential = (NuCredential) CommonSTSAttributesHandler
						.deserializeObjectFromString(property.getValue());
				if (delegatedCredential == null)
					_logger.info("Missing delegated credential");
				resource.setProperty(
						SecurityConstants.IDP_STORED_CREDENTIAL_QNAME
								.getLocalPart(), delegatedCredential);

			} else if (SecurityConstants.STORED_CALLING_CONTEXT_QNAME
					.equals(propertyName)) {
				ContextType serializedContextInfo = (ContextType) property
						.getObjectValue(ContextType.class);
				if (serializedContextInfo == null)
					_logger.info("Missing stored calling context");
				ICallingContext storedContext = new CallingContextImpl(
						serializedContextInfo);
				resource.setProperty(
						IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME,
						storedContext);

			} else if (SecurityConstants.CERTIFICATE_CHAIN_QNAME
					.equals(propertyName)) {
				X509Certificate[] certificate = (X509Certificate[]) CommonSTSAttributesHandler
						.deserializeObjectFromString(property.getValue());
				if (certificate == null)
					_logger.info("Missing resource certificate");
				resource.setProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME,
						certificate);

			} else if (SecurityConstants.IDP_PRIVATE_KEY_QNAME
					.equals(propertyName)) {
				PrivateKey privateKey = (PrivateKey) CommonSTSAttributesHandler
						.deserializeObjectFromString(property.getValue());
				if (privateKey == null)
					_logger.info("Missing private key");
				resource.setProperty(IResource.PRIVATE_KEY_PROPERTY_NAME,
						privateKey);
			}
		}

		/*
		 * Note that it is necessary to augment the retrieved certificate and
		 * delegated credentials to the stored calling context for the proper
		 * functioning of any IDP resource. This is because IDP resources make
		 * out-calls to other IDP resources nested in them. If the
		 * stored-calling-context does not have all the necessary authentication
		 * information those calls may fail.
		 */
		private void postProcessStoredCallingContext(IRNSResource resource)
				throws ResourceException, GeneralSecurityException {

			// update key and certificate materials
			ICallingContext storedContext = (ICallingContext) resource
					.getProperty(IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME);
			PrivateKey privateKey = (PrivateKey) resource
					.getProperty(IResource.PRIVATE_KEY_PROPERTY_NAME);
			X509Certificate[] certificate = (X509Certificate[]) resource
					.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
			storedContext.setActiveKeyAndCertMaterial(new KeyAndCertMaterial(
					certificate, privateKey));

			// update transient credentials list
			NuCredential delegatedCredential = (NuCredential) resource
					.getProperty(SecurityConstants.IDP_STORED_CREDENTIAL_QNAME
							.getLocalPart());
			TransientCredentials transientCredentials = TransientCredentials
					.getTransientCredentials(storedContext);
			transientCredentials.add(delegatedCredential);
			resource.setProperty(
					IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME,
					storedContext);
		}
	}

	/*
	 * The resource properties of a IDP resource pretty much dictate its
	 * behavior. For example; for a Kerberos port-type instance; its realm, kdc,
	 * and user attributes defines the mechanism for kerberos authentication.
	 * Therefore, to create a working replica of an IDP resource, the system has
	 * to copy relevant properties from the primary resource instance. As we
	 * don't know the specifics of individual IDP port-types, subclasses should
	 * implement this method to register an appropriate property retriever class
	 * that would be used during replication.
	 */
	public abstract STSResourcePropertiesRetriever getResourcePropertyRetriver();

	/*
	 * Subclasses should override this method to include any additional
	 * properties that they do not want to be visible in a get resource property
	 * document response.
	 */
	public Set<QName> getSensitivePropertyNames() {
		Set<QName> propertyNames = new HashSet<QName>();
		propertyNames.add(SecurityConstants.NEW_IDP_NAME_QNAME);
		propertyNames.add(SecurityConstants.IDP_PRIVATE_KEY_QNAME);
		propertyNames.add(SecurityConstants.IDP_STORED_CREDENTIAL_QNAME);
		propertyNames.add(SecurityConstants.STORED_CALLING_CONTEXT_QNAME);
		propertyNames.add(SecurityConstants.CERTIFICATE_CHAIN_QNAME);
		return propertyNames;
	}

	/********************************** RNS Operations ***********************************************/

	protected RNSEntryResponseType[] addRNSEntries(RNSEntryType[] addRequest,
			IRNSResource resource) throws RemoteException,
			RNSEntryExistsFaultType, ResourceUnknownFaultType {

		if (addRequest == null || addRequest.length == 0)
			addRequest = new RNSEntryType[] { null };

		RNSEntryResponseType[] response = new RNSEntryResponseType[addRequest.length];
		for (int index = 0; index < response.length; index++) {
			try {
				response[index] = addAnEntry(addRequest[index], resource);
			} catch (BaseFaultType fault) {
				response[index] = new RNSEntryResponseType(null, null, fault,
						addRequest[index].getEntryName());
			} catch (Throwable cause) {
				_logger.error("failure during add request", cause);
				response[index] = new RNSEntryResponseType(
						null,
						null,
						FaultManipulator
								.fillInFault(new BaseFaultType(
										null,
										null,
										null,
										null,
										new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(
												"Unable to add entry: "
														+ cause.getMessage()) },
										null)),
						addRequest[index].getEntryName());
			}
		}
		return response;
	}

	protected LookupResponseType lookup(String[] lookupRequest,
			IRNSResource resource) throws RemoteException,
			ResourceUnknownFaultType {

		Collection<InternalEntry> entries = new LinkedList<InternalEntry>();

		if (lookupRequest == null || lookupRequest.length == 0)
			lookupRequest = new String[] { null };

		for (String request : lookupRequest) {
			entries.addAll(resource.retrieveEntries(request));
		}

		Collection<RNSEntryResponseType> response = new LinkedList<RNSEntryResponseType>();
		for (InternalEntry entry : entries) {
			response.add(new RNSEntryResponseType(entry.getEntryReference(),
					RNSUtilities.createMetadata(entry.getEntryReference(),
							entry.getAttributes()), null, entry.getName()));
		}

		return RNSContainerUtilities
				.translate(response, iteratorBuilder(RNSEntryResponseType
						.getTypeDesc().getXmlType()));
	}

	protected RNSEntryResponseType[] remove(String[] removeRequest,
			IRNSResource resource) throws RemoteException,
			WriteNotPermittedFaultType {

		RNSEntryResponseType[] response = new RNSEntryResponseType[removeRequest.length];
		List<String> removedEntryNames = new ArrayList<String>();
		for (int index = 0; index < removeRequest.length; index++) {
			String entryName = removeRequest[index];
			resource.removeEntries(entryName);
			response[index] = new RNSEntryResponseType(null, null, null,
					entryName);
			removedEntryNames.add(entryName);
		}
		VersionVector versionVector = VersionedResourceUtils
				.incrementResourceVersion(resource);
		resource.commit();

		for (String entryName : removedEntryNames) {
			TopicSet space = TopicSet.forPublisher(getClass());
			PublisherTopic topic = space
					.createPublisherTopic(RNS_OPERATION_TOPIC);
			topic.publish(new RNSOperationContents(RNSOperations.Remove,
					entryName, null, versionVector));
		}
		return response;
	}

	private RNSEntryResponseType addAnEntry(RNSEntryType addRequest,
			IRNSResource resource) throws RemoteException, ResourceException,
			RNSEntryExistsFaultType {

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

		if (resource.isServiceResource())
			throw new RemoteException("Cannot add entries to this service.");

		resource.addEntry(new InternalEntry(name, entryReference, attrs));
		VersionVector versionVector = VersionedResourceUtils
				.incrementResourceVersion(resource);
		resource.commit();

		TopicSet space = TopicSet.forPublisher(getClass());
		PublisherTopic topic = space.createPublisherTopic(RNS_OPERATION_TOPIC);
		topic.publish(new RNSOperationContents(RNSOperations.Add, name,
				entryReference, versionVector));

		return new RNSEntryResponseType(entryReference, mdt, null, name);
	}

	/******************************** Notification Handling ****************************************/

	/*
	 * Synchronization of replicas is done through asynchronous notifications.
	 * There are two kind of updates that may occur in a IDP resource:
	 * add/remove of descendant entries and changes in access control. We have
	 * two different notification handlers to deal with these two different kind
	 * of updates.
	 */
	@Override
	protected void registerNotificationHandlers(
			NotificationMultiplexer multiplexer) {
		super.registerNotificationHandlers(multiplexer);
		multiplexer.registerNotificationHandler(
				RNSTopics.RNS_OPERATION_TOPIC.asConcreteQueryExpression(),
				new RNSOperationNotificationHandler());
		multiplexer.registerNotificationHandler(
				AclTopics.GENII_ACL_CHANGE_TOPIC.asConcreteQueryExpression(),
				new AclChangeNotificationHandler());
	}

	private class RNSOperationNotificationHandler extends
			AbstractNotificationHandler<RNSOperationContents> {

		public RNSOperationNotificationHandler() {
			super(RNSOperationContents.class);
		}

		/*
		 * This handler method is nearly an identical copy of the notification
		 * handler that works for Enhanced-RNS resources. We have to make few
		 * changes regarding how some attributes are retrieved due to the
		 * difference in IDP and Enhanced-RNS port-types' implementations.
		 */
		@Override
		public String handleNotification(TopicPath topicPath,
				EndpointReferenceType producerReference,
				EndpointReferenceType subscriptionReference,
				RNSOperationContents contents) throws Exception {

			_logger.info("processing an IDP update notification for replica synchronization");
			RNSOperations operation = contents.operation();
			String entryName = contents.entryName();
			EndpointReferenceType entryReference = contents.entryReference();

			if ((operation == null)
					|| !((operation.equals(RNSOperations.Add)
							&& entryName != null && entryReference != null)
							|| (operation.equals(RNSOperations.Remove) && entryName != null)
							|| operation.equals(RNSOperations.Destroy) || operation
								.equals(RNSOperations.Unlink))) {

				_logger.info("invalid notification message");
				return NotificationConstants.FAIL;
			}

			// retrieve the resource and associated lock to do necessary
			// modification
			ResourceKey resourceKey = ResourceManager.getCurrentResource();
			IRNSResource resource = (IRNSResource) resourceKey.dereference();
			ResourceLock resourceLock = resourceKey.getResourceLock();

			/*
			 * An RNS notification indicates some update in the IDPS linked
			 * under the current resource that is made by some replica. To keep
			 * replicas in sync, the same operation should be replayed in
			 * receiver of this notification. Here, however, we have to ensure
			 * that the notification is due to some valid operation, not due to
			 * some impostor attack in the system. The current way of ensuring
			 * that is to validate the authentication information that accompany
			 * the notification. The notification contains the identity of the
			 * user who modified the resource. The identity is delegated to the
			 * resource that sent the notification. Finally, the notification is
			 * signed by the resource.
			 */
			if (!ServerWSDoAllReceiver.checkAccess(resource, RWXCategory.WRITE)) {
				_logger.info("Permission denied while trying to process an RNS notification from a IDP.");
				return NotificationConstants.FAIL;
			}

			ReplicationItem item = null;
			boolean replay = false;
			try {
				// secure a lock on the resource before doing any modification
				resourceLock.lock();

				// destroy this replica if the notification indicate a destroy
				// operation is been
				// invoked
				// to some other replica.
				if (operation.equals(RNSOperations.Destroy)) {
					resource.setProperty(SyncProperty.IS_DESTROYED_PROP_NAME,
							"true");
					destroy(new Destroy());
					return NotificationConstants.OK;
				}

				VersionVector remoteVector = contents.versionVector();
				VersionVector localVector = (VersionVector) resource
						.getProperty(SyncProperty.VERSION_VECTOR_PROP_NAME);
				MessageFlags flags = VersionedResourceUtils
						.validateNotification(resource, localVector,
								remoteVector);

				// a non-null flag status indicates the modification suggested
				// by the notification
				// cannot
				// applied ever or at the present moment
				if (flags.status != null)
					return flags.status;

				if (operation.equals(RNSOperations.Add)) {

					// if the added resource is not a IDP then the current
					// notification is invalid
					TypeInformation type = new TypeInformation(entryReference);
					if (!type.isIDP())
						return NotificationConstants.FAIL;

					// get a new entry-name if there is a name conflict between
					// the local and remote
					// replicas
					entryName = processAddEntryName(entryName, flags.replay,
							resource);

					/*
					 * Note that even if the local replica update fails we do
					 * not reply with a FAIL message. This is because if a
					 * failure occurs, the local replica can still behave like
					 * the remote replica as long as it hold a reference of the
					 * EPR that points to the resource lying in the another
					 * container. The two replicas, however, are ideologically
					 * out-of-sync then. We keep this behavior to emulate
					 * Enhanced-RNS's replication logic. If it turns out to be
					 * undesirable, then return a notification failure from the
					 * catch block.
					 */
					try {
						item = AutoReplicate.autoReplicate(resource,
								entryReference);
					} catch (Exception exception) {
						_logger.warn("failed to create a local IDP replica",
								exception);
					}
					InternalEntry entry = new InternalEntry(entryName,
							(item != null ? item.localEPR : entryReference),
							null);
					resource.addEntry(entry);

				} else if (operation.equals(RNSOperations.Remove)) {
					resource.removeEntries(entryName);
				} else if (operation.equals(RNSOperations.Unlink)) {
					// remote replica stop participating in the replication
					// scheme; so stop sending
					// it notifications
					// for future updates
					VersionedResourceUtils.destroySubscription(resource,
							producerReference);
					return NotificationConstants.OK;
				}

				// update the version vector once all desired modifications are
				// done
				VersionedResourceUtils.updateVersionVector(resource,
						localVector, remoteVector);

				replay = flags.replay;

			} finally {
				resourceLock.unlock();
			}

			/*
			 * A non-null synchronizer (runner) indicates an add entry
			 * operations and a requirement for further synchronization of the
			 * added entry. So we start an asynchronous replication thread to do
			 * any remaining operations.
			 */
			if (item != null && item.runner != null) {
				ReplicationThread thread = new ReplicationThread(
						WorkingContext.getCurrentWorkingContext());
				thread.add(item);
				thread.start();
			}

			/*
			 * A replay flag indicates that the local copy has some updates that
			 * are not present in the remote copy that sent the notification
			 * message. Hence, the local copy is replaying its status to allow
			 * any out-of-sync remote copy to catch up.
			 */
			if (replay) {
				VersionVector vvr = VersionedResourceUtils
						.incrementResourceVersion(resource);
				_logger.debug("replaying notification message");
				TopicSet space = TopicSet.forPublisher(getClass());
				PublisherTopic topic = space.createPublisherTopic(topicPath);
				topic.publish(new RNSOperationContents(operation, entryName,
						entryReference, vvr));
			}

			return NotificationConstants.OK;
		}

		private String processAddEntryName(String entryName, boolean replay,
				IRNSResource resource) throws ResourceException {

			Collection<String> conflicts = resource.listEntries(entryName);
			if (conflicts.size() == 0)
				return entryName;
			if (!replay) {
				_logger.warn("overwriting an rns entry " + entryName);
				resource.removeEntries(entryName);
				return entryName;
			} else {
				_logger.warn("renaming entry from replica " + entryName);
				String prefix = entryName;
				for (int count = 2; true; count++) {
					String suffix = "." + count;
					int maxLength = 256 - suffix.length();
					if (prefix.length() > maxLength)
						prefix = prefix.substring(0, maxLength);
					entryName = prefix + suffix;
					if (resource.listEntries(entryName).size() == 0)
						return entryName;
				}
			}
		}
	}
}
