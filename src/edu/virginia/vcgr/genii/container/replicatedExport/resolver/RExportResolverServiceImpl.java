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
package edu.virginia.vcgr.genii.container.replicatedExport.resolver;

import org.apache.axis.types.URI;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.Add;
import org.ggf.rns.AddResponse;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
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
import org.ogf.schemas.naming._2006._08.naming.ResolveFailedFaultType;
import org.ws.addressing.EndpointReferenceType;

import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.byteio.ByteIOOperations;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AbstractNotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ByteIOContentsChangedContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ByteIOTopics;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.GenesisIIBaseTopics;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ResourceTerminationContents;

import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

import edu.virginia.vcgr.genii.replicatedExport.resolver.RExportResolverPortType;
import edu.virginia.vcgr.genii.replicatedExport.resolver.InvalidWSNameFaultType;
import edu.virginia.vcgr.genii.replicatedExport.resolver.UpdateRequestType;
import edu.virginia.vcgr.genii.replicatedExport.resolver.UpdateResponseType;
import edu.virginia.vcgr.genii.container.replicatedExport.RExportUtils;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.replicatedExport.resolver.CreateReplicaRequest;
import edu.virginia.vcgr.genii.replicatedExport.resolver.CreateReplicaResponse;
import edu.virginia.vcgr.genii.replicatedExport.resolver.ResolverQueryRequest;
import edu.virginia.vcgr.genii.replicatedExport.resolver.ResolverQueryResponse;
import edu.virginia.vcgr.genii.replicatedExport.resolver.CreateRootReplicaRequest;
import edu.virginia.vcgr.genii.replicatedExport.resolver.CreateRootReplicaResponse;
import edu.virginia.vcgr.genii.replicatedExport.resolver.EPRRequestResponse;
import edu.virginia.vcgr.genii.replicatedExport.resolver.ServiceEPRRequest;

public class RExportResolverServiceImpl extends GenesisIIBase 
	implements RExportResolverPortType
{
	static private Log _logger = LogFactory.getLog(RExportResolverServiceImpl.class);
	static public QName REXPORT_RESOLVER_TARGET_CONSTRUCTION_PARAMETER =
		new QName(GenesisIIConstants.GENESISII_NS, "rexport-resolver-target-epr");
	static public QName REXPORT_RESOLVER_RESOLVER_EPI_CONSTRUCTION_PARAMETER =
		new QName(GenesisIIConstants.GENESISII_NS, "rexport-resolver-resolver-epi");
	static public QName REXPORT_PATH_ELEM_NAME = 
		new QName(GenesisIIConstants.GENESISII_NS, "path");
	static public QName REXPORT_RESOLVER_SERVICE_EPR_NAME = 
		new QName(GenesisIIConstants.GENESISII_NS, "rexport-resolver-service-epr");
	static public QName REXPORT_RESOLVER_TYPE =
		new QName(GenesisIIConstants.GENESISII_NS, "rexport-resolver-type");
	
	public RExportResolverServiceImpl() throws RemoteException
	{
		this("RExportResolverPortType");
	}
	
	protected RExportResolverServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);

		addImplementedPortType(WellKnownPortTypes.ENDPOINT_IDENTIFIER_RESOLVER_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.REFERENCE_RESOLVER_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.GENII_SIMPLE_RESOLVER_PORT_TYPE);
		addImplementedPortType(RNSConstants.RNS_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.GENII_NOTIFICATION_CONSUMER_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.REXPORT_RESOLVER_PORT_TYPE);
	}
	
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.REXPORT_RESOLVER_PORT_TYPE;
	}
	
	public void postCreate(ResourceKey rKey, EndpointReferenceType myEPR,
		ConstructionParameters cParams, HashMap<QName, Object> constructionParameters, 
		Collection<MessageElement> resolverCreationParams)
			throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(rKey, myEPR, cParams, constructionParameters, resolverCreationParams);
		
		//grab targetEPR construction parameter
		EndpointReferenceType primaryEPR = (EndpointReferenceType) constructionParameters.get(
				REXPORT_RESOLVER_TARGET_CONSTRUCTION_PARAMETER);
		if (primaryEPR == null)
			throw new ResourceException(
					"Invalid construction parameters for RExportResolverDBResource.initialize()");
		
		//grab resolverServiceEPR construction parameter
		EndpointReferenceType resolverServiceEPR = (EndpointReferenceType) constructionParameters.get(
				REXPORT_RESOLVER_SERVICE_EPR_NAME);
		if (resolverServiceEPR == null)
			throw new ResourceException(
					"Invalid construction parameters for RExportResolverDBResource.initialize()");
		
		
		String primaryLocalPath = (String)constructionParameters.get(REXPORT_PATH_ELEM_NAME);
		
		//get EPI of resolver
		URI myEPI = (URI)(rKey.dereference().getProperty(
				IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME));
		
		//create resolver entry with gathered info
		RExportResolverEntry entry = new RExportResolverEntry(
				primaryEPR, 
				myEPI, 
				myEPR, 
				primaryLocalPath,
				resolverServiceEPR);
		
		//update resolver DB with new entry
		((RExportResolverDBResource) rKey.dereference()).update(entry);
		((RExportResolverDBResource) rKey.dereference()).commit();
		
		//create termination subscription for resolver
		RExportResolverUtils.createTerminationSubscription(entry);
	}
	
	/**
	 * This function gets called when primary EPR faults
	 * @return EPR of replica 
	 */
	@RWXMapping(RWXCategory.OPEN)
	public EndpointReferenceType resolve(Object resolveRequest) 
		throws RemoteException,
			ResourceUnknownFaultType, 
			ResolveFailedFaultType
	{
		_logger.debug("Entered resolve method in RExportResolver.");
		
		IRExportResolverResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRExportResolverResource)rKey.dereference();
		
		//get entry for this resolver resource
		RExportResolverEntry myEntry = resource.getEntry();
	
		//choose which replica to resolve to
		return RExportResolverUtils.resolveEPR(myEntry);
	}
	
	@RWXMapping(RWXCategory.OPEN)
	public EndpointReferenceType resolveEPI(org.apache.axis.types.URI resolveEPI) 
		throws RemoteException,
			ResourceUnknownFaultType, 
			ResolveFailedFaultType
	{
		_logger.debug("Entered resolveEPI method in RExportResolver.");
		
		IRExportResolverResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRExportResolverResource)rKey.dereference();
		
		//get entry for this resolver resource
		RExportResolverEntry thisEntry = resource.getEntry();

		//ensure resolveEPI matches this resolver entry's EPI
		URI epi = null;
		try{
			epi = new URI(resolveEPI.toString());
		}
		catch(Throwable t){
			_logger.warn("Bad URI type passed into RExport's resolveEPI", t);
			throw new ResolveFailedFaultType();
		}
		
		if (epi == null || !epi.equals(thisEntry.getCommonEPI()))
			throw new ResolveFailedFaultType();
			
		//return new resolution EPR
		return RExportResolverUtils.resolveEPR(thisEntry);
	}
	
	/**
	 * Update target EPR associated with entry and return new resolution epr
	 * If null target EPR, just return resolution epr as is currently.
	 * 
	 * Currently only called from ResolverFactory createResolver to get resolution epr
	 */
	@RWXMapping(RWXCategory.WRITE)
	public UpdateResponseType update(UpdateRequestType updateRequest)
		throws RemoteException,
			ResourceUnknownFaultType,
			InvalidWSNameFaultType
	{
		IRExportResolverResource resource = null;
		EndpointReferenceType newTargetEPR = null;
		URI newTargetEPI = null;
		
		//get current resource and db entry
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRExportResolverResource)rKey.dereference();
		RExportResolverEntry thisEntry = resource.getEntry();
		
		//get new targetEPR
		newTargetEPR = updateRequest.getNew_EPR();

		//get EPI of new targetEPR
		WSName newWSName = new WSName(newTargetEPR);
		if (!newWSName.isValidWSName())
			throw new InvalidWSNameFaultType();
		newTargetEPI = newWSName.getEndpointIdentifier();
		
		//no update needed if primary EPIs match
		if (!thisEntry.getCommonEPI().equals(newTargetEPI)) {
			//set new targetEPR and EPI 
			thisEntry.setCommonEPI(newTargetEPI);
			thisEntry.setPrimaryEPR(newTargetEPR);
	
			//commit changes
			resource.update(thisEntry);
			resource.commit();
		}

		//return new epr with updated targetEPR and corresponding resolver
		return new UpdateResponseType(RExportResolverUtils.createResolutionEPR(
				thisEntry.getPrimaryEPR(), thisEntry.getResolverEPR()));
	}
	
	/**
	 * Initiate replica creation (EPR, data, and subscriptions)
	 * Store replica epr info in resolver db entry
	 * First ensure this is correct resolver for passed primary
	 * 
	 * @param primaryEPR: epr of new export entry on primary
	 * @param replicaName: name of replica
	 * @param isDir: true if new export entry is dir; false if file
	 * 
	 */
	private EndpointReferenceType updateReplica(
			EndpointReferenceType primaryEPR,
			EndpointReferenceType dataStreamEPR,
			String replicaName,
			String entryType)
		throws RemoteException,
			ResourceException,
			InvalidWSNameFaultType
	{		
		/*get this resolver's entry and compare that info matches what was passed*/
		
		//extract EPI of primary
		WSName primaryWSName = new WSName(primaryEPR);
		//ensure EPI exists
		if (!primaryWSName.isValidWSName())
			throw new InvalidWSNameFaultType();
		URI primaryEPI = primaryWSName.getEndpointIdentifier();
		
		//get DB entry associated with this resolver
		IRExportResolverResource resource = null;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRExportResolverResource)rKey.dereference();
		RExportResolverEntry myEntry = resource.getEntry();

		//extract resolver's EPI
		URI resolverEPI = (URI)(rKey.dereference().getProperty(
				IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME));
		
		//ensure primary and resolver EPIs match this resolver DB entry
		if (!( myEntry.getCommonEPI().equals(primaryEPI) &&
				myEntry.getResolverEPI().equals(resolverEPI))) {
			_logger.debug("DB entry mismatch for primary and resolver");
			throw new ResourceException("DB entry mismatch for primary and resolver.");
		}
		
		//create replica epr and check for validity
		EndpointReferenceType replicaEPR = RExportUtils.createReplica(
				myEntry.getPrimaryEPR(),
				myEntry.getCommonEPI().toString(),
				myEntry.getResolverEPR(), 
				myEntry.getLocalPath(),
				replicaName,
				entryType,
				dataStreamEPR);
		
		//save replica epr in db entry and commit changes
		myEntry.setReplicaEPR(replicaEPR);
		resource.update(myEntry);
		resource.commit();
		
		return replicaEPR;
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
			ByteIOTopics.BYTEIO_CONTENTS_CHANGED_TOPIC.asConcreteQueryExpression(),
			new LegacyByteIOContentsChangedNotificationHandler());
	}

	private class LegacyResourceTerminationNotificationHandler
		extends AbstractNotificationHandler<ResourceTerminationContents>
	{
		private LegacyResourceTerminationNotificationHandler()
		{
			super(ResourceTerminationContents.class);
		}

		@Override
		public void handleNotification(TopicPath topic,
			EndpointReferenceType producerReference,
			EndpointReferenceType subscriptionReference,
			ResourceTerminationContents contents) throws Exception
		{
			//get db entry associated with current resource
			ResourceKey rKey = ResourceManager.getCurrentResource();
			IRExportResolverResource resource = (IRExportResolverResource) rKey.dereference();
			RExportResolverEntry thisEntry = resource.getEntry();
			
			RExportResolverTerminateUserData userData = 
				contents.additionalUserData(
					RExportResolverTerminateUserData.class);
			
			/* check if EPI matches */
			if (thisEntry.getCommonEPI().equals(userData.getEPI()))
			{
				/* kill this resolver */
				_logger.info("Terminating RExport resolver for : "
						+ thisEntry.getLocalPath());
				destroy(new Destroy());
			}
		}
	}
	
	private class LegacyByteIOContentsChangedNotificationHandler
		extends AbstractNotificationHandler<ByteIOContentsChangedContents>
	{
		private LegacyByteIOContentsChangedNotificationHandler()
		{
			super(ByteIOContentsChangedContents.class);
		}

		@Override
		public void handleNotification(TopicPath topic,
			EndpointReferenceType producerReference,
			EndpointReferenceType subscriptionReference,
			ByteIOContentsChangedContents contents) throws Exception
		{
			ByteIOOperations specificOp = null;
			
			_logger.info(
					"RandomByteIO notification detected by rexport resolver");
			
			//get db entry associated with current resource
			ResourceKey rKey = ResourceManager.getCurrentResource();
			IRExportResolverResource resource = (IRExportResolverResource) rKey.dereference();
			RExportResolverEntry thisEntry = resource.getEntry();
			
			specificOp = contents.responsibleOperation();
			
			if (specificOp == ByteIOOperations.Write)
			{	
				//get local path of export on primary 
				String primaryLocalPath = thisEntry.getLocalPath();
				
				_logger.info("RNS write detected for exportedFile: " 
						+ primaryLocalPath);
				
				//get epr of primary exportedFile export
				EndpointReferenceType primaryEPR = thisEntry.getPrimaryEPR();
				
				//get stream of new exportedFile data
				EndpointReferenceType newDataStream = 
					RExportResolverUtils.getExportFileData(
						primaryLocalPath, primaryEPR);
				
				//update replica with data stream
				RExportUtils.unpackDataStream(thisEntry.getReplicaEPR(),
						newDataStream);
			}
		}
	}
	
	static public String _FILE_TYPE = "F";
	static public String _DIR_TYPE = "D";
	static public QName REPLICA_NAME = new QName(
			GenesisIIConstants.GENESISII_NS, "replicaName");
	
	@RWXMapping(RWXCategory.WRITE)
	public AddResponse add(Add addRequest) 
		throws RemoteException, RNSEntryExistsFaultType,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("add operation not supported in rexport resolver.");
	}
	

	/**
	 * Instantiate replica 
	 */
	@RWXMapping(RWXCategory.WRITE)
	public CreateReplicaResponse createReplica(CreateReplicaRequest creationRequest)
		throws RemoteException
	{
		//extract epr of new primary entry from request
		EndpointReferenceType primaryEPR = creationRequest.getPrimary_EPR();
		
		//extract epr of stream containing export data from request
		EndpointReferenceType dataStreamEPR = creationRequest.getDataStream_EPR();
		
		//extract entry type of export entry from request
		String entryType = creationRequest.getExport_type();
		
		//extract replica name from request
		String replicaName = creationRequest.getReplica_name();
		
		EndpointReferenceType replicaEPR = null;
		
		//instantiate replica of correct type
		try{
			replicaEPR = updateReplica(
				primaryEPR, 
				dataStreamEPR,
				replicaName, 
				entryType);
		}
		catch (Exception e){
			throw new RemoteException("Cannot create replica", e);
		}
		
		return new CreateReplicaResponse(replicaEPR);
	}
	
	/**
	 * Query for resolver associated with current resource epi in table
	 */
	@RWXMapping(RWXCategory.WRITE)
	public ResolverQueryResponse resolverQuery(ResolverQueryRequest resolverQuery) 
		throws ResourceException, ResourceUnknownFaultType
	{		
		//extract EPI of resource from request
		String resourceEPI = resolverQuery.getResource_EPI();
		
		//get db entry associated with current resource
		IRExportResolverResource resource = null;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRExportResolverResource)rKey.dereference();
		
		//return resolverEPR associated with resource via query
		return new ResolverQueryResponse(resource.queryForResourceResolver(resourceEPI));
	}

	@RWXMapping(RWXCategory.READ)
	public ListResponse list(List listRequest) 
		throws RemoteException, ResourceUnknownFaultType, 
			RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("list operation not supported in rexport resolver.");
	}

	/**
	 * Create root replica of type RExportDir.
	 * Called by CreateResolver in RExportResolverFactory
	 * 
	 */
	@RWXMapping(RWXCategory.WRITE)
	public CreateRootReplicaResponse createRootReplica(CreateRootReplicaRequest request)
		throws ResourceUnknownFaultType, ResourceException, InvalidWSNameFaultType,
			RemoteException
	{
		//extract EPR of primary from request
		// checked to match against resolver's DB entry
		EndpointReferenceType primaryEPR = request.getPrimary_EPR();
		
		/*get primary localpath for replica and save as replica name*/
		
		//get db entry associated with current resource
		IRExportResolverResource resource = null;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRExportResolverResource)rKey.dereference();
		RExportResolverEntry myEntry = resource.getEntry();
		String replicaName = myEntry.getLocalPath();
		
		//instantiate replica
		return new CreateRootReplicaResponse(updateReplica(
				primaryEPR, null, replicaName, RExportResolverUtils._DIR_TYPE));		
	}
	
	@RWXMapping(RWXCategory.WRITE)
	public CreateFileResponse createFile(CreateFile createFileRequest) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("createFile operation not supported in rexport resolver.");
	}
	
	/**
	 * Return resolverServiceEPR stored in current resolver entry 
	 */
	@RWXMapping(RWXCategory.WRITE)
	public EPRRequestResponse getResolverServiceEPR(ServiceEPRRequest request) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		//get db entry associated with current resource
		IRExportResolverResource resource = null;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRExportResolverResource)rKey.dereference();
		RExportResolverEntry myEntry = resource.getEntry();
		
		return new EPRRequestResponse(myEntry.getResolverServiceEPR());
	}
	
	@RWXMapping(RWXCategory.WRITE)
	public MoveResponse move(Move moveRequest) 
		throws RemoteException, ResourceUnknownFaultType, RNSFaultType
	{
		//throw new RemoteException("move operation not supported in rexport resolver.");
		//extract EPI of resource from request
		String resourceEPI = moveRequest.getEntry_name();
		
		//get db entry associated with current resource
		IRExportResolverResource resource = null;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRExportResolverResource)rKey.dereference();
		
		//return resolverEPR associated with resource via query
		return new MoveResponse(resource.queryForResourceResolver(resourceEPI));
	
	}
	
	@RWXMapping(RWXCategory.READ)
	public QueryResponse query(Query queryRequest) 
		throws RemoteException, ResourceUnknownFaultType, RNSFaultType
	{
		throw new RemoteException("query operation not supported in rexport resolver.");
	}
	
	@RWXMapping(RWXCategory.WRITE)
	public String[] remove(Remove removeRequest) 
		throws RemoteException, ResourceUnknownFaultType, 
			RNSDirectoryNotEmptyFaultType, RNSFaultType
	{
		
		throw new RemoteException("remove operation not supported in rexport resolver.");
	}
	
	protected Object translateConstructionParameter(MessageElement parameter)
	throws Exception
{
	QName messageName = parameter.getQName();
	if (messageName.equals(REXPORT_RESOLVER_TARGET_CONSTRUCTION_PARAMETER))
		return parameter.getObjectValue(EndpointReferenceType.class);
	else if (messageName.equals(REXPORT_PATH_ELEM_NAME))
		return parameter.getObjectValue(String.class);
	else if (messageName.equals(REXPORT_RESOLVER_TYPE))
		return parameter.getObjectValue(String.class);
	if (messageName.equals(REXPORT_RESOLVER_SERVICE_EPR_NAME))
		return parameter.getObjectValue(EndpointReferenceType.class);
	else
		return super.translateConstructionParameter(parameter);
}
	
	
}