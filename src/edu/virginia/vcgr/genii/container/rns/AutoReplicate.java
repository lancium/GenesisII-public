package edu.virginia.vcgr.genii.container.rns;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.List;

import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.ogf.schemas.naming._2006._08.naming.ResolveFailedFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.resolver.GeniiResolverServiceImpl;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.container.sync.ReplicationItem;
import edu.virginia.vcgr.genii.container.sync.ResourceSyncRunner;
import edu.virginia.vcgr.genii.container.sync.SyncProperty;
import edu.virginia.vcgr.genii.container.sync.VersionedResourceUtils;
import edu.virginia.vcgr.genii.resolver.ExtResolveRequestType;
import edu.virginia.vcgr.genii.resolver.GeniiResolverPortType;
import edu.virginia.vcgr.genii.resolver.UpdateResponseType;

public class AutoReplicate
{
	static private Log _logger = LogFactory.getLog(AutoReplicate.class);

	/**
	 * If "resource" is a directory with an auto-replicate policy, and "primaryEPR" is a non-local
	 * resource that can be replicated, then quickly create and return a local replica. Otherwise,
	 * return null.
	 */
	@SuppressWarnings("unchecked")
	public static ReplicationItem autoReplicate(IResource resource, EndpointReferenceType primaryEPR) throws RemoteException
	{
		// If this resource is in this container, then don't replicate it in this container.
		GUID containerID = Container.getContainerID();
		GUID primaryContainerID = EPRUtils.extractContainerID(primaryEPR);
		if ((primaryContainerID != null) && containerID.equals(primaryContainerID))
			return null;
		// This resource must support WS-Naming in order to be replicated.
		WSName primaryName = new WSName(primaryEPR);
		URI endpointIdentifier = primaryName.getEndpointIdentifier();
		if (endpointIdentifier == null)
			return null;
		List<ResolverDescription> resolverList = primaryName.getResolvers();
		if ((resolverList == null) || (resolverList.size() == 0))
			return null;
		// If this resource is already replicated in this container,
		// then ask the resolver for the replica's EPR, including the resolver element.
		if (haveLocalInstance(endpointIdentifier)) {
			EndpointReferenceType localEPR = getLocalReplicaEPR(resolverList, containerID);
			if (localEPR != null)
				return new ReplicationItem(null, localEPR);
		}
		// Check directory's auto-replication policy.
		if (resource.getProperty(GeniiDirPolicy.REPLICATION_POLICY_PROP_NAME) == null)
			return null;

		// To support replication, a resource type must send update messages with version vectors.
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, primaryEPR);
		MessageElement element = VersionedResourceUtils.getResourceProperty(common, SyncProperty.REPLICATION_STATUS_QNAME);
		if ((element == null) || (element.getValue() == null))
			return null;

		// Find the type of local resource that best represents the remote resource.
		TypeInformation type = new TypeInformation(primaryEPR);
		String serviceName = type.getBestMatchServiceName();
		if (serviceName == null)
			return null;

		// Get the Java class to create resources of the found resource type.
		String serviceURL = Container.getServiceURL(serviceName);
		JavaServiceDesc desc = Container.findService(serviceName);
		if ((serviceURL == null) || (desc == null))
			return null;
		GenesisIIBase service;
		try {
			Class<? extends GenesisIIBase> serviceClass = (Class<? extends GenesisIIBase>) desc.getImplClass();
			service = serviceClass.newInstance();
		} catch (Exception exception) {
			if (_logger.isDebugEnabled())
				_logger.debug("Local getImplClass() and serviceClass.newInstance(): " + exception);
			return null;
		}
		ResourceSyncRunner runner = service.getClassResourceSyncRunner();
		if (runner == null)
			return null;

		// Create a new resource as an uninitialized replica.
		// Get the new EPR, which will be added to the local replica of the directory.
		MessageElement[] elementArr = new MessageElement[1];
		elementArr[0] = new MessageElement(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM, endpointIdentifier);
		EndpointReferenceType localEPR = service.CreateEPR(elementArr, serviceURL);
		AddressingParameters ap = new AddressingParameters(localEPR.getReferenceParameters());
		String rkString = ap.getResourceKey();

		// Update the resolver and get a local EPR with a resolver element.
		// Unfortunately, this step sends an RPC. It would be nice if we could avoid
		// sending any RPCs at this time, because the local directory replica is locked,
		// and the primary directory instance is still waiting for a response.
		UpdateResponseType response = VersionedResourceUtils.updateResolver(resolverList, localEPR, rkString);
		localEPR = response.getNew_EPR();
		int targetID = response.getTargetID();

		// Get the DBResource that corresponds to the new resource.
		// Store the primary EPR in the replica resource.
		ResourceKey replicaKey = ResourceManager.getTargetResource(serviceName, rkString);
		IResource replicaResource = (IResource) replicaKey.dereference();
		VersionedResourceUtils.initializeReplica(replicaResource, primaryEPR, targetID);

		return new ReplicationItem(runner, localEPR);
	}

	/**
	 * Optimization: Don't ask the resolver about this resource unless the local database indicates
	 * that we have a local instance of this resource.
	 * 
	 * This breaks abstraction barriers and it may be removed.
	 */
	public static boolean haveLocalInstance(URI endpointIdentifier)
	{
		try {
			ResourceKey rKey = ResourceManager.getCurrentResource();
			IResource resource = rKey.dereference();
			if (!(resource instanceof BasicDBResource))
				return false;
			BasicDBResource dbResource = (BasicDBResource) resource;
			Connection connection = dbResource.getConnection();
			String resourceID = BasicDBResource.getResourceID(connection, endpointIdentifier.toString());
			return (resourceID != null);
		} catch (Exception exception) {
			if (_logger.isDebugEnabled())
				_logger.debug("haveLocalInstance: " + exception);
		}
		return false;
	}

	private static EndpointReferenceType getLocalReplicaEPR(List<ResolverDescription> resolverList, GUID containerID)
		throws RemoteException
	{
		if (_logger.isDebugEnabled())
			_logger.debug("AutoReplicate.getLocalReplica");
		MessageElement param = new MessageElement(GeniiResolverServiceImpl.TARGET_CONTAINER_PARAMETER, containerID.toString());
		MessageElement[] params = new MessageElement[] { param };
		for (ResolverDescription resolver : resolverList) {
			if (resolver.getType() != ResolverDescription.ResolverType.EPI_RESOLVER)
				continue;
			URI targetEPI = resolver.getEPI();
			GeniiResolverPortType proxy = ClientUtils.createProxy(GeniiResolverPortType.class, resolver.getEPR());
			ExtResolveRequestType request = new ExtResolveRequestType(targetEPI, params);
			try {
				return proxy.extResolveEPI(request);
			} catch (Exception exception) {
				// Given resource does not exist in the given container.
				if (exception instanceof ResolveFailedFaultType)
					break;
				if (_logger.isDebugEnabled())
					_logger.debug("getLocalReplica: " + exception);
			}
		}
		return null;
	}
}
