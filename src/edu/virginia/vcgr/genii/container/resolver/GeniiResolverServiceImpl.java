package edu.virginia.vcgr.genii.container.resolver;

import org.apache.axis.types.URI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.byteio.WriteNotPermittedFaultType;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.MetadataMappingType;
import org.ggf.rns.NameMappingType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSEntryType;
import org.morgan.inject.MInject;
import org.morgan.util.GUID;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ogf.schemas.naming._2006._08.naming.ResolveFailedFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.byteio.streamable.factory.OpenStreamResponse;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AbstractNotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.GenesisIIBaseTopics;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ResourceTerminationContents;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;

import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.byteio.SByteIOResource;
import edu.virginia.vcgr.genii.container.byteio.StreamableByteIOServiceImpl;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceLock;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.RNSContainerUtilities;
import edu.virginia.vcgr.genii.container.security.authz.providers.GamlAclTopics;
import edu.virginia.vcgr.genii.container.sync.GamlAclChangeNotificationHandler;
import edu.virginia.vcgr.genii.container.sync.MessageFlags;
import edu.virginia.vcgr.genii.container.sync.ReplicationItem;
import edu.virginia.vcgr.genii.container.sync.ReplicationThread;
import edu.virginia.vcgr.genii.container.sync.ResourceSyncRunner;
import edu.virginia.vcgr.genii.container.sync.SyncProperty;
import edu.virginia.vcgr.genii.container.sync.VersionVector;
import edu.virginia.vcgr.genii.container.sync.VersionedResourceAttributeHandlers;
import edu.virginia.vcgr.genii.container.sync.VersionedResourceUtils;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.PublisherTopic;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.TopicSet;
import edu.virginia.vcgr.genii.resolver.CountRequestType;
import edu.virginia.vcgr.genii.resolver.ExtResolveRequestType;
import edu.virginia.vcgr.genii.resolver.GeniiResolverPortType;
import edu.virginia.vcgr.genii.resolver.InvalidWSNameFaultType;
import edu.virginia.vcgr.genii.resolver.UpdateRequestType;
import edu.virginia.vcgr.genii.resolver.UpdateResponseType;
import edu.virginia.vcgr.genii.security.RWXCategory;

@GeniiServiceConfiguration(
	resourceProvider=GeniiResolverDBResourceProvider.class)
public class GeniiResolverServiceImpl extends GenesisIIBase
	implements GeniiResolverPortType, ResolverTopics, GamlAclTopics
{	
	static private Log _logger = LogFactory.getLog(GeniiResolverServiceImpl.class);

	// MessageElement keys for VcgrCreate()
	static public final QName TARGET_EPR_PARAMETER =
		new QName(GenesisIIConstants.GENESISII_NS, "genii-resolver-target-epr");
	
	// Use these keys to store data in the addressing parameters additional user data.
	static public String GENII_RESOLVER_TARGET_ID_KEY =
		"edu.virginia.vcgr.genii.container.resolver.target-id";
	static public String GENII_RESOLVER_TARGET_EPI_KEY =
		"edu.virginia.vcgr.genii.container.resolver.target-epi";
	
	// MessageElement keys for extended resolve
	static public final String GENII_RESOLVER_NS =
		"http://vcgr.cs.virginia.edu/genii/genii-resolver";
	static public final QName TARGET_CONTAINER_PARAMETER =
		new QName(GENII_RESOLVER_NS, "container-id");
	static public final QName TARGET_ID_PARAMETER =
		new QName(GENII_RESOLVER_NS, "target-id");
	
	@MInject(lazy = true)
	private IGeniiResolverResource _resource;
	
	@MInject
	private ResourceLock _resourceLock;
	
	protected void setAttributeHandlers() 
		throws NoSuchMethodException, ResourceException, ResourceUnknownFaultType
	{
		super.setAttributeHandlers();
		new VersionedResourceAttributeHandlers(getAttributePackage());
	}

	public GeniiResolverServiceImpl() throws RemoteException
	{
		this("GeniiResolverPortType");
	}
	
	protected GeniiResolverServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);

		addImplementedPortType(WellKnownPortTypes.ENDPOINT_IDENTIFIER_RESOLVER_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.REFERENCE_RESOLVER_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.GENII_RESOLVER_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.RNS_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.GENII_NOTIFICATION_CONSUMER_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.SBYTEIO_FACTORY_PORT_TYPE);
	}
	
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.GENII_RESOLVER_PORT_TYPE;
	}
	
	/**
	 * Create a new simple resolver resource.
	 * Write the target EPR to the database.
	 */
	public void postCreate(ResourceKey rKey, EndpointReferenceType myEPR,
		ConstructionParameters cParams, HashMap<QName, Object> constructionParameters,
		Collection<MessageElement> resolverCreationParams)
			throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(rKey, myEPR, cParams, constructionParameters, resolverCreationParams);
		
		IGeniiResolverResource resource = (IGeniiResolverResource) rKey.dereference();
		EndpointReferenceType primaryEPR = (EndpointReferenceType)
			constructionParameters.get(IResource.PRIMARY_EPR_CONSTRUCTION_PARAM);
		EndpointReferenceType targetEPR = (EndpointReferenceType)
			constructionParameters.get(TARGET_EPR_PARAMETER);
		if (primaryEPR != null)
		{
			VersionedResourceUtils.initializeReplica(resource, primaryEPR, 0);
			WorkingContext context = WorkingContext.getCurrentWorkingContext();
			ReplicationThread thread = new ReplicationThread(context);
			thread.add(new ReplicationItem(new GeniiResolverSyncRunner(), myEPR));
			thread.start();
		}
		else if (targetEPR != null)
		{
			if (EPRUtils.isUnboundEPR(targetEPR))
				throw new ResourceException("Missing EPR Address for new GeniiResolver resource");
			WSName wsname = new WSName(targetEPR);
			URI targetEPI = wsname.getEndpointIdentifier();
			if (targetEPI == null)
				throw new ResourceException("Invalid EPR for GeniiResolver - not a WSName");
			int targetID = 0;
			resource.addTargetEPR(targetEPI, targetID, targetEPR);
			GeniiResolverUtils.createTerminateSubscription(targetID, targetEPR, myEPR, resource);
			resource.commit();
		}
	}

	protected Object translateConstructionParameter(MessageElement parameter)
		throws Exception
	{
		QName messageName = parameter.getQName();
		if (messageName.equals(TARGET_EPR_PARAMETER))
			return parameter.getObjectValue(EndpointReferenceType.class);
		return super.translateConstructionParameter(parameter);
	}
			
	/* EndpointIdentifierResolver port type. */
	@RWXMapping(RWXCategory.OPEN)
	public EndpointReferenceType resolveEPI(URI resolveEPI) 
		throws RemoteException, ResourceUnknownFaultType, ResolveFailedFaultType
	{
		return doResolve(resolveEPI);
	}

	/* ReferenceResolver port type. */
	@RWXMapping(RWXCategory.OPEN)
	public EndpointReferenceType resolve(Object resolveRequest) 
		throws RemoteException, ResourceUnknownFaultType, ResolveFailedFaultType
	{
		return doResolve(null);
	}
	
	/**
	 * Select the best EPR in the array.
	 */
	private EndpointReferenceType doResolve(URI targetEPI)
		throws RemoteException, ResolveFailedFaultType
	{
		fixMetadataInWorkingContext();
		if (targetEPI == null)
		{
			URI[] targetEPIList = _resource.getTargetEPIList();
			targetEPI = targetEPIList[0];
		}
		EndpointReferenceType myEPR = (EndpointReferenceType) WorkingContext.getCurrentWorkingContext().
			getProperty(WorkingContext.EPR_PROPERTY_NAME);
		int targetID = -1;
		try
		{
			AddressingParameters addParams = new AddressingParameters(myEPR.getReferenceParameters());
			Map<String, Serializable> infoMap = addParams.getAdditionalUserInformation();
			if (infoMap != null)
			{
				Integer targetValue = (Integer) infoMap.get(GENII_RESOLVER_TARGET_ID_KEY);
				if (targetValue != null)
				{
					targetID = targetValue.intValue();
					_logger.debug("doResolve: bound targetID=" + targetID);
				}
			}
		}
		catch (Exception exception)
		{
			_logger.error("doResolve failed to get targetID", exception);
		}
		int resolutionID = 0;
		EndpointReferenceType resolutionEPR = null;
		try
		{
			_resourceLock.lock();
			int[] targetIDList = _resource.getTargetIDList(targetEPI);
			if (targetIDList.length > 0)
			{
				resolutionID = targetIDList[0];
				for (int idx = 0; idx < targetIDList.length; idx++)
				{
					int currentID = targetIDList[idx];
					if (currentID > targetID)
					{
						resolutionID = currentID;
						break;
					}
				}
				resolutionEPR = _resource.getTargetEPR(targetEPI, resolutionID);
			}
		}
		finally
		{
			_resourceLock.unlock();
		}
		if (resolutionEPR == null)
		{
			throw FaultManipulator.fillInFault(new ResolveFailedFaultType(),
					"targetEPI not found");
		}
		return GeniiResolverUtils.createResolutionEPR(_resource, resolutionEPR, myEPR, resolutionID);
	}
	
	@RWXMapping(RWXCategory.OPEN)
	public int[] getEndpointCount(CountRequestType request)
		throws RemoteException
	{
		URI targetEPI = request.getEndpointIdentifier();
		int[] targetIDList = null;
		try
		{
			_resourceLock.lock();
			targetIDList = _resource.getTargetIDList(targetEPI);
		}
		finally
		{
			_resourceLock.unlock();
		}
		return targetIDList;
	}

	/* SimpleResolver (mgmt) port type */
	@RWXMapping(RWXCategory.WRITE)
	public UpdateResponseType update(UpdateRequestType updateRequest)
		throws RemoteException,
			ResourceUnknownFaultType,
			InvalidWSNameFaultType
	{
		fixMetadataInWorkingContext();
		EndpointReferenceType newEPR = updateRequest.getNew_EPR();
		int targetID = 0;
		try
		{
			_resourceLock.lock();
			targetID = addEPR(_resource, newEPR);
			VersionVector vvr = VersionedResourceUtils.incrementResourceVersion(_resource);
			_resource.commit();
			TopicSet space = TopicSet.forPublisher(GeniiResolverServiceImpl.class);
			PublisherTopic publisherTopic = space.createPublisherTopic(RESOLVER_UPDATE_TOPIC);
			publisherTopic.publish(new ResolverUpdateContents(targetID, newEPR, vvr));
		}
		finally
		{
			_resourceLock.unlock();
		}
		EndpointReferenceType myEPR = (EndpointReferenceType) WorkingContext.getCurrentWorkingContext().
			getProperty(WorkingContext.EPR_PROPERTY_NAME);
		newEPR = GeniiResolverUtils.createResolutionEPR(_resource, newEPR, myEPR, targetID);
		return new UpdateResponseType(newEPR, targetID);
	}
	
	/**
	 * Add an EPR to the resource's list of targets.
	 */
	private int addEPR(IGeniiResolverResource resource, EndpointReferenceType targetEPR)
		throws RemoteException, InvalidWSNameFaultType
	{
		_logger.debug("SimpleResolver addEPR");
		WSName wsname = new WSName(targetEPR);
		if (!wsname.isValidWSName())
			throw new InvalidWSNameFaultType();
		URI targetEPI = wsname.getEndpointIdentifier();
		int[] targetIDList = resource.getTargetIDList(targetEPI);
		targetIDList = GeniiResolverUtils.initializeNextTargetID(resource, targetEPI, targetIDList);
		int targetID = 0;
		if (targetIDList.length > 0)
		{
			int maxTargetID = targetIDList[targetIDList.length-1];
			targetID = maxTargetID + 1;
		}
		resource.addTargetEPR(targetEPI, targetID, targetEPR);
		GeniiResolverUtils.updateNextTargetID(resource, targetEPI, targetID);
		GeniiResolverUtils.createTerminateSubscription(targetID, targetEPR, null, resource);
		return targetID;
	}
	
	@RWXMapping(RWXCategory.OPEN)
	public EndpointReferenceType extResolveEPI(ExtResolveRequestType request)
		throws RemoteException, ResolveFailedFaultType
	{
		fixMetadataInWorkingContext();
		URI targetEPI = request.getEPI();
		EndpointReferenceType resolutionEPR;
		MessageElement[] params = request.get_any();
		String containerID = getParameter(params, TARGET_CONTAINER_PARAMETER);
		if (containerID != null)
		{
			try
			{
				_resourceLock.lock();
				resolutionEPR = resolveContainer(_resource, targetEPI, containerID);
			}
			finally
			{
				_resourceLock.unlock();
			}
			if (resolutionEPR == null)
				throw FaultManipulator.fillInFault(new ResolveFailedFaultType(),
					"targetEPI not found in container");
			return resolutionEPR;
		}
		String idParam = getParameter(params, TARGET_ID_PARAMETER);
		if (idParam != null)
		{
			int targetID = -1;
			try
			{
				targetID = Integer.parseInt(idParam);
			}
			catch (Exception exception) {}
			try
			{
				_resourceLock.lock();
				resolutionEPR = _resource.getTargetEPR(targetEPI, targetID);
			}
			finally
			{
				_resourceLock.unlock();
			}
			if (resolutionEPR == null)
				throw FaultManipulator.fillInFault(new ResolveFailedFaultType(),
					"targetEPI not found with targetID");
			return resolutionEPR;
		}
		return doResolve(request.getEPI());	
	}
	
	private static String getParameter(MessageElement[] params, QName targetKey)
	{
		if (params != null)
		{
			for (MessageElement param : params)
			{
				QName key = param.getQName();
				try
				{
					if (key.equals(targetKey))
						return (String) param.getObjectValue(String.class);
				}
				catch (Exception ignore) {}
			}
		}
		return null;
	}

	private static EndpointReferenceType resolveContainer(IGeniiResolverResource resource,
			URI targetEPI, String containerID)
		throws ResourceException
	{
		int[] targetIDList = resource.getTargetIDList(targetEPI);
		for (int targetID : targetIDList)
		{
			EndpointReferenceType targetEPR = resource.getTargetEPR(targetEPI, targetID);
			GUID guid = EPRUtils.extractContainerID(targetEPR);
			if (guid.toString().equals(containerID))
			{
				EndpointReferenceType myEPR = (EndpointReferenceType) WorkingContext.getCurrentWorkingContext().
					getProperty(WorkingContext.EPR_PROPERTY_NAME);
				return GeniiResolverUtils.createResolutionEPR(resource, targetEPR, myEPR, targetID);
			}
		}
		return null;
	}
	
	/* RNS port type */
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] add(RNSEntryType []request) throws RemoteException
	{
		throw new RemoteException("\"add\" not applicable.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public LookupResponseType lookup(String []lookupRequest)
		throws RemoteException, ResourceException
    {
		fixMetadataInWorkingContext();
		EndpointReferenceType myEPR = (EndpointReferenceType) WorkingContext.getCurrentWorkingContext().
			getProperty(WorkingContext.EPR_PROPERTY_NAME);
		Collection<RNSEntryResponseType> result = new LinkedList<RNSEntryResponseType>();
		
		// If service, list all resolved resources (Target EPI --> Resolver EPR)
		if (_resource.getKey() == null)
		{
			HashMap<URI, String> resolvers = _resource.listAllResolvers();
			if (resolvers != null && resolvers.size() > 0)
			{
				ResourceKey rKey = _resource.getParentResourceKey();
				String serviceName = rKey.getServiceName();
				String currentURL = myEPR.getAddress().toString();
				PortType[] implementedPortTypes = getImplementedPortTypes(rKey);
				Iterator<Map.Entry<URI, String>> iterator;
				iterator = resolvers.entrySet().iterator();
				while (iterator.hasNext())
				{
					Map.Entry<URI, String> mapEntry = iterator.next();
					URI targetEPI = mapEntry.getKey();
					if (requestContainsFilename(lookupRequest, targetEPI.toString()))
					{
						String entryResourceKey = mapEntry.getValue();
						ResourceKey entryKey = ResourceManager.getTargetResource(serviceName, entryResourceKey);
						EndpointReferenceType resolverEPR =
							ResourceManager.createEPR(entryKey, currentURL, implementedPortTypes, null);
						resolverEPR = GeniiResolverUtils.createUserInfoEPR(resolverEPR,
								GENII_RESOLVER_TARGET_EPI_KEY, targetEPI, true);
						result.add(new RNSEntryResponseType(resolverEPR, null, null, targetEPI.toString()));
					}
				}
			}
		}
		else
		{
			// Is the user asking to list the instances of a specific resolved resource?
			URI targetEPI = null;
			AddressingParameters addParams = new AddressingParameters(myEPR.getReferenceParameters());
			Map<String, Serializable> infoMap = addParams.getAdditionalUserInformation();
			if (infoMap != null)
				targetEPI = (URI) infoMap.get(GENII_RESOLVER_TARGET_EPI_KEY);
			if (targetEPI == null)
			{
				// List this resolver's resources (Target EPI --> Resolver EPR)
				URI[] targetEPIList = _resource.getTargetEPIList();
				for (int idx = 0; idx < targetEPIList.length; idx++)
				{
					targetEPI = targetEPIList[idx];
					if (requestContainsFilename(lookupRequest, targetEPI.toString()))
					{
						EndpointReferenceType resolverEPR = GeniiResolverUtils.createUserInfoEPR(myEPR,
								GENII_RESOLVER_TARGET_EPI_KEY, targetEPI, true);
						result.add(new RNSEntryResponseType(resolverEPR, null, null, targetEPI.toString()));
					}
				}
			}
			else
			{
				// List this resource's instances (Target ID -> Resource EPR)
				int[] targetIDList = _resource.getTargetIDList(targetEPI);
				for (int idx = 0; idx < targetIDList.length; idx++)
				{
					int targetID = targetIDList[idx];
					String name = Integer.toString(targetID);
					if (requestContainsFilename(lookupRequest, name))
					{
						EndpointReferenceType targetEPR = _resource.getTargetEPR(targetEPI, targetID);
						if (targetEPR != null)
							result.add(new RNSEntryResponseType(targetEPR, null, null, name));
					}
				}
			}
		}
		return RNSContainerUtilities.translate(
    		result, iteratorBuilder(RNSEntryResponseType.getTypeDesc().getXmlType()));
    }

	private static boolean requestContainsFilename(String[] lookupRequest, String name)
	{
		// If no filenames were specified, then all files are accepted.
		if (lookupRequest == null || lookupRequest.length == 0)
			return true;
		for (String request : lookupRequest)
		{
			if (request.equals(name))
				return true;
		}
		return false;
	}
	
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] rename(NameMappingType[] renameRequest)
		throws RemoteException, WriteNotPermittedFaultType
	{
		throw new RemoteException("\"rename\" not applicable.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] remove(String[] removeRequest)
		throws RemoteException, WriteNotPermittedFaultType
	{
		throw new RemoteException("\"remove\" not applicable.");
	}

	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] setMetadata(
		MetadataMappingType[] setMetadataRequest) throws RemoteException,
			WriteNotPermittedFaultType
	{
		throw new RemoteException("setMetadata operation not supported!");
	}
	
	public ResourceSyncRunner getClassResourceSyncRunner()
	{
		return new GeniiResolverSyncRunner();
	}
	
	@Override
	protected void registerNotificationHandlers(
			NotificationMultiplexer multiplexer)
	{
		super.registerNotificationHandlers(multiplexer);
		
		multiplexer.registerNotificationHandler(
			GenesisIIBaseTopics.RESOURCE_TERMINATION_TOPIC.asConcreteQueryExpression(),
			new LegacyResourceTerminationNotificationHandler());
		multiplexer.registerNotificationHandler(
			RESOLVER_UPDATE_TOPIC.asConcreteQueryExpression(),
			new ResolverUpdateNotificationHandler());
		multiplexer.registerNotificationHandler(
			GamlAclTopics.GAML_ACL_CHANGE_TOPIC.asConcreteQueryExpression(),
			new GamlAclChangeNotificationHandler());
	}

	private class LegacyResourceTerminationNotificationHandler
		extends AbstractNotificationHandler<ResourceTerminationContents>
	{
		private LegacyResourceTerminationNotificationHandler()
		{
			super(ResourceTerminationContents.class);
		}

		@Override
		public String handleNotification(TopicPath topic,
			EndpointReferenceType producerReference,
			EndpointReferenceType subscriptionReference,
			ResourceTerminationContents contents) throws Exception
		{
			SimpleResolverTerminateUserData notifyData = 
				contents.additionalUserData(SimpleResolverTerminateUserData.class);
			URI targetEPI = notifyData.getTargetEPI();
			int targetID = notifyData.getTargetID();
			try
			{
				_resourceLock.lock();
				_resource.removeTargetEPR(targetEPI, targetID);
				_logger.debug("resolver: destroyed targetID=" + targetID);
				_resource.commit();
			}
			finally
			{
				_resourceLock.unlock();
			}
			if (_resource.getEntryCount() == 0)
			{
				// Destroy this resolver resource.
				destroy(new Destroy());
			}
			return NotificationConstants.OK;
		}
	}

	private class ResolverUpdateNotificationHandler
		extends AbstractNotificationHandler<ResolverUpdateContents>
	{
		private ResolverUpdateNotificationHandler()
		{
			super(ResolverUpdateContents.class);
		}

		@Override
		public String handleNotification(TopicPath topicPath,
				EndpointReferenceType producerReference,
				EndpointReferenceType subscriptionReference,
				ResolverUpdateContents contents) throws Exception
		{
			int targetID = contents.targetID();
			if ((targetID < 0) || (targetID > 999))
			{
				_logger.warn("SimpleResolverServiceImpl.notify: invalid targetID " + targetID);
				return NotificationConstants.FAIL;
			}
			EndpointReferenceType targetEPR = contents.entryReference();
			if (targetEPR == null)
			{
				_logger.warn("SimpleResolverServiceImpl.notify: targetEPR is null");
				return NotificationConstants.FAIL;
			}
			VersionVector remoteVector = contents.versionVector();
			
			IGeniiResolverResource resource = _resource;
			boolean replay = false;
			try
			{
				_resourceLock.lock();
				VersionVector localVector = (VersionVector) resource.getProperty(
						SyncProperty.VERSION_VECTOR_PROP_NAME);
				MessageFlags flags = VersionedResourceUtils.validateNotification(
						resource, localVector, remoteVector);
				if (flags.status != null)
					return flags.status;
				WSName wsname = new WSName(targetEPR);
				URI targetEPI = wsname.getEndpointIdentifier();
				EndpointReferenceType currentEPR = resource.getTargetEPR(targetEPI, targetID);
				if (currentEPR != null)
				{
					if (flags.replay)
					{
						// Handle add/add conflict like a directory. The non-replayed entry
						// remains at targetID, and the replayed entry gets a new targetID.
						// BUG: This does not necessarily fix all third-parties.
						int[] targetIDList = resource.getTargetIDList(targetEPI);
						int maxTargetID = targetIDList[targetIDList.length-1];
						targetID = maxTargetID + 1;
					}
					else
					{
						// TODO: Unsubscribe from currentEPR.  Replace currentEPR with targetEPR.
						// currentEPR will be re-added by replay message.
					}
				}
				_logger.debug("notify: add targetID=" + targetID);
				resource.addTargetEPR(targetEPI, targetID, targetEPR);
				GeniiResolverUtils.updateNextTargetID(resource, targetEPI, targetID);
				GeniiResolverUtils.createTerminateSubscription(targetID, targetEPR, null, resource);
				VersionedResourceUtils.updateVersionVector(resource, localVector, remoteVector);
				replay = flags.replay;
			}
			finally
			{
				_resourceLock.unlock();
			}
			if (replay)
			{
				VersionVector vvr = VersionedResourceUtils.incrementResourceVersion(resource);
				_logger.debug("GeniiResolverServiceImpl.notify: replay message");
				TopicSet space = TopicSet.forPublisher(GeniiResolverServiceImpl.class);
				PublisherTopic publisherTopic = space.createPublisherTopic(topicPath);
				publisherTopic.publish(new ResolverUpdateContents(targetID, targetEPR, vvr));
			}
			return NotificationConstants.OK;
		}
	}

	/**
	 * Write the entire contents of the resolverentries table to an output stream
	 * so another resolver resource can become a replica of this resource.
	 */
	@RWXMapping(RWXCategory.READ)
	public OpenStreamResponse openStream(Object request)
		throws RemoteException, ResourceUnknownFaultType, ResourceCreationFaultType
	{
		ObjectOutputStream ostream = null;
		try
		{
			_resourceLock.lock();
			File superDir = Container.getConfigurationManager().getUserDirectory();
			File entryFile = File.createTempFile("resolver", ".obj", superDir);
			String filename = entryFile.getAbsolutePath();
			_logger.debug("filename=" + filename);
			ostream = new ObjectOutputStream(new FileOutputStream(entryFile));
			_resource.writeAllEntries(ostream);
			ostream.close();
			ostream = null;
			entryFile.setReadOnly();
			
			MessageElement[] params = new MessageElement[3];
			params[0] = new MessageElement(ByteIOConstants.SBYTEIO_DESTROY_ON_CLOSE_FLAG, "true");
			params[1] = new MessageElement(SByteIOResource.FILE_PATH_PROPERTY, filename);
			params[2] = new MessageElement(SByteIOResource.MUST_DESTROY_PROPERTY, "true");
			StreamableByteIOServiceImpl service = new StreamableByteIOServiceImpl();
			String serviceURL = Container.getServiceURL("StreamableByteIOPortType");
			EndpointReferenceType entryReference = service.CreateEPR(params, serviceURL);
			return new OpenStreamResponse(entryReference);
		}
		catch (Exception exception)
		{
			throw new ResourceException("Failed to create stream.", exception);
		}
		finally
		{
			StreamUtils.close(ostream);
			_resourceLock.unlock();
		}
	}
	
}
