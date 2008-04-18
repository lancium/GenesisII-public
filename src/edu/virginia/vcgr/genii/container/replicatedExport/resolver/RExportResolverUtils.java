package edu.virginia.vcgr.genii.container.replicatedExport.resolver;

import java.rmi.RemoteException;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.Token;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.RNSFaultType;
import org.ggf.sbyteio.StreamableByteIOPortType;
import org.morgan.util.configuration.ConfigurationException;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.notification.WellknownTopics;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.notification.Subscribe;
import edu.virginia.vcgr.genii.common.notification.UserDataType;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.replicatedExport.resolver.InvalidWSNameFaultType;
import edu.virginia.vcgr.genii.replicatedExport.resolver.RExportResolverFactoryPortType;
import edu.virginia.vcgr.genii.resolver.simple.CreateResolverRequestType;
import edu.virginia.vcgr.genii.resolver.simple.CreateResolverResponseType;
import edu.virginia.vcgr.genii.replicatedExport.resolver.RExportResolverPortType;
import edu.virginia.vcgr.genii.replicatedExport.RExportDirPortType;
import edu.virginia.vcgr.genii.exportdir.ExportedFilePortType;

import edu.virginia.vcgr.genii.replicatedExport.resolver.CreateReplicaRequest;
import edu.virginia.vcgr.genii.replicatedExport.resolver.ResolverQueryRequest;
import edu.virginia.vcgr.genii.replicatedExport.PopulateDirRequestType;
import edu.virginia.vcgr.genii.replicatedExport.resolver.ServiceEPRRequest;

public class RExportResolverUtils
{
	static private Log _logger = LogFactory.getLog(RExportResolverUtils.class);
	
	static public String REXPORT_RESOLVER_QNAME = "http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver";
	static public String REXPORT_RESOLVER_EPI_LNAME = "EPI";
	static public String _PATH_ELEM_NAME = "path";
	
	static public QName REFERENCE_RESOLVER_EPI_QNAME = new QName(
			REXPORT_RESOLVER_QNAME, 
			REXPORT_RESOLVER_EPI_LNAME);
	
	static final private String _REXPORT_RESOLVER_SERVICE_URL_PATH = 
		"/axis/services/RExportResolverPortType";
	
	static public String _FILE_TYPE = "F";
	static public String _DIR_TYPE = "D";

	
	/**
	 * Given information in resolver entry, return epr to replica.  
	 * Currently resolves to current single replica in entry.
	 * 
	 * @param thisEntry: db entry containing all replica information
	 * 
	 * @return EPR of replica to which resolve should occur
	 */
	static public EndpointReferenceType resolveEPR(RExportResolverEntry thisEntry)
	{
		//do magic to choose best replica for resolution
		return thisEntry.getReplicaEPR();
	}
	
	/**
	 * Generates augmented EPR for primary that contains the original EPR 
	 * plus an EPR for the resolver added to end of metadata element. 
	 * 
	 * @param primaryEPR: epr of primary
	 * @param resolverEPR: epr of resolver
	 * 
	 * @return newEPR: EPR with both primary and resolver EPRs
	 */
	static public EndpointReferenceType createResolutionEPR(EndpointReferenceType primaryEPR, 
			EndpointReferenceType resolverEPR)
	{
		//disect components of primaryEPR
		org.ws.addressing.AttributedURIType origAddress = primaryEPR.getAddress();
		org.ws.addressing.ReferenceParametersType origRefParams = primaryEPR.getReferenceParameters();
		org.ws.addressing.MetadataType origMetadata = primaryEPR.getMetadata();
		org.apache.axis.message.MessageElement [] origMessageElements = primaryEPR.get_any();
		
		//create new metadata element containing resolver epr
		org.ws.addressing.MetadataType newMetadata = null;
		org.apache.axis.message.MessageElement newResolverElement = 
			new org.apache.axis.message.MessageElement(WSName.REFERENCE_RESOLVER_QNAME, resolverEPR);
		
		//preserve any existing metadata elements
		if (origMetadata == null){
			origMetadata = new org.ws.addressing.MetadataType();
		}
		int numMetadataElements = 0;
		org.apache.axis.message.MessageElement [] origMetadataElements = origMetadata.get_any();
		if (origMetadataElements != null){
			numMetadataElements = origMetadataElements.length;
		}
		
		//setup new metadata elements with old elements and new resolver addition
		org.apache.axis.message.MessageElement [] newMetadataElements = 
			new org.apache.axis.message.MessageElement[numMetadataElements+1];
		
		for (int i = 0; i < numMetadataElements; i++){
			newMetadataElements[i] = origMetadataElements[i];
		}
		newMetadataElements[numMetadataElements] = newResolverElement;
		newMetadata = new org.ws.addressing.MetadataType(newMetadataElements);
		
		//create new resolution epr that contains resolver
		EndpointReferenceType newEPR = new EndpointReferenceType(
				origAddress, 
				origRefParams, 
				newMetadata, 
				origMessageElements);
		
		return newEPR;
	}
	
	/** 
	 * Subscribe resolver to primary's termination event.  
	 * EPI of resolver in passed as user data.
	 * 
	 * @param entry: db entry for resolver containing all relavent info
	 */
	static public void createTerminationSubscription(RExportResolverEntry entry)
		throws ResourceException
	{
		try{
			UserDataType userData = new UserDataType(new MessageElement[] { 
					new MessageElement(
							REFERENCE_RESOLVER_EPI_QNAME, 
							entry.getCommonEPI().toString())});
			GeniiCommon producer = ClientUtils.createProxy(
					GeniiCommon.class, entry.getPrimaryEPR());
			producer.subscribe(new Subscribe(
					new Token(WellknownTopics.TERMINATED), 
					null, 
					entry.getResolverEPR(), 
					userData));
		}
		catch (Exception e){ 
			throw new ResourceException(
					"Could not create subscription to resource termination.", e);
		}
	}
	
	/**
	 * Coordinates resolver/replica creation for replication of new export entry
	 * 
	 * @param oldEPR: primary epr without resolver epr
	 * @param replicaType: file or dir primary type
	 * @param primaryLocalPath: localpath of export on primary
	 * @param exportDirEPI: EPI of directory containing new export entry
	 * 			     Used to acquire epr to containing dir's replica
	 * @param replicaName: name of file/dir (last part of primaryLocalPath) 
	 * 			    for use in dir entry
	 * 
	 * @return resolvedEPR: oldEPR augmented with epr of newly created resolver on containerName
	 * 
	 */
	static public EndpointReferenceType setupRExport(
			EndpointReferenceType oldEPR, 
			String replicaType, 
			String primaryLocalPath, 
			String exportDirEPI, 
			String replicaName)
		throws CreationException, ResourceException, RemoteException, 
		ResourceException, ConfigurationException
	{
		//create proxy to resolver factory 
		//  at current (primary) container where new export entry was created
		RExportResolverFactoryPortType resolverFactoryProxy = null;
		
		try{
			resolverFactoryProxy = ClientUtils.createProxy(
					RExportResolverFactoryPortType.class,
					EPRUtils.makeEPR(
						Container.getServiceURL("RExportResolverFactoryPortType")));
		}
		catch (Exception e){
			throw new CreationException(
					"Could not create proxy to resolver factory", e);
		}
		
		//acquire EPR to resolver for exportDir containing this new export entry
		EndpointReferenceType dirResolverEPR = lookupResolverByEPI(
				exportDirEPI, null);
		
		//prepare creation params for resolver
		MessageElement []resolverCreationProperties = new MessageElement[3];
		
		//pass export path to local primary location
		resolverCreationProperties[0] = new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, _PATH_ELEM_NAME), primaryLocalPath);
		
		//set type of resolver
		//  non-root resolvers do not create replica resource with resolver creation
		resolverCreationProperties[1] = new MessageElement(
				RExportResolverServiceImpl.REXPORT_RESOLVER_TYPE, 
				"non-root");
		
		//get epr to replication service from dirResolverEPR 
		EndpointReferenceType replicationServiceEPR = 
			getResolverServiceEPR(dirResolverEPR);
		
		//pass resolver service epr to create replica at specified location
		resolverCreationProperties[2] = new MessageElement(
				RExportResolverServiceImpl.REXPORT_RESOLVER_SERVICE_EPR_NAME, 
				replicationServiceEPR);
		
		//create new resolver using resolver factory
		CreateResolverResponseType resolverCreationResponse = null;
		try{
			resolverCreationResponse = resolverFactoryProxy.createResolver(
					new CreateResolverRequestType(oldEPR, resolverCreationProperties));
		}
		catch(Throwable t){
			_logger.debug("Could not create rexport resolver.");
			throw new ResourceException(
					"Could not create rexport resolver via factory.", t);
		}
		//get resolution epr (oldEPR augmented with resolverEPR) from creation response
		EndpointReferenceType resolvedEPR = resolverCreationResponse.getResolution_EPR();
		
		//get resolver EPR from creation response
		EndpointReferenceType resolverEPR = resolverCreationResponse.getResolver_EPR();
		
		/*create replica*/
		EndpointReferenceType primaryDataStream = null;
		EndpointReferenceType replicaEPR = null;
		try{
			//if file export, get epr to stream of primary data
			if (replicaType.equals(_FILE_TYPE))
				primaryDataStream = getExportFileData(primaryLocalPath, null);
			
			
			//setup proxy to export resolver (to container where resolver was created)
			RExportResolverPortType resolverService = ClientUtils.createProxy(
					RExportResolverPortType.class, resolverEPR);
			
			replicaEPR = resolverService.createReplica(new CreateReplicaRequest(
					resolvedEPR,
					primaryDataStream,
					replicaType,
					replicaName)).getReplica_EPR();
		}
		catch(Exception e){
			throw new ResourceException(
					"Unable to connect to rexport resolver to create replica.", e);
		}
		finally{
			if (primaryDataStream != null){
				StreamableByteIOPortType streamProxy = ClientUtils.createProxy(
						StreamableByteIOPortType.class, primaryDataStream);
				streamProxy.destroy(new Destroy());
			}
		}
		
		try{
			//populate rexportentry table with new replica
			recordReplicaUnderDir(
					dirResolverEPR, 
					replicaEPR, 
					replicaName, 
					replicaType);
		}
		catch(Exception e){
			throw new ResourceException(
					"Unable to store replica info in rexportdir db resource.", e);
		}
		return resolvedEPR;
	}
	
	/** 
	 * Packages data from local harddrive of exported entry
	 * into a stream and returns EPR to it
	 * 
	 * @param primaryLocalPath: local harddrive path to data for export file entry
	 * @param exportFileServiceEPR: epr to exportedFile serivce on primaries container
	 * 
	 * @return primaryDataEPR: epr of stream containing export data
	 */
	static public EndpointReferenceType getExportFileData(String primaryLocalPath,
			EndpointReferenceType exportFileServiceEPR)
		throws ResourceException
	{
		if (exportFileServiceEPR == null)
			exportFileServiceEPR = EPRUtils.makeEPR(
					Container.getServiceURL("ExportedFilePortType"));
		
		//EPR for new replica
		EndpointReferenceType primaryDataEPR = null;
		
		/*create instance of correct replication service*/
		try {
			ExportedFilePortType exportedFileService = ClientUtils.createProxy(
					ExportedFilePortType.class, exportFileServiceEPR);
			primaryDataEPR = (exportedFileService.openStream(
					primaryLocalPath).getEndpoint());
			
		}
		catch (Exception e){
			throw new ResourceException(
					"Could not create stream for exported data.", e);
		}
		
		return primaryDataEPR;
	}
	
	/**
	 * Determines if constructionParams contain "non-default" resolver type param
	 * 
	 * @return Returns true if not, false if yes
	 * 
	 */
	static public boolean isRootResolver(MessageElement []constructionParams)
		throws Exception
	{		
		for (MessageElement elem : constructionParams){
			QName elemName = elem.getQName();
			if (elemName.equals(
					RExportResolverServiceImpl.REXPORT_RESOLVER_TYPE)){
				if (elem.getValue().equals("non-root")){
					_logger.info("Non-root resolver param found");
					return false;
				}
			}
		}
		return true;
	}
	
	
	/**
	 * Populate RExportDir resource with entry for new replica
	 * 
	 * looks up resolver on replicationURL associated with exportDirEPI
	 * connects to resolver and resolves to get replicaDirEPR
	 * connects to replica and adds new replica entry for replicaEPR with replicaname
	 * 
	 * @param dirResolverEPR: epr of resolver of exportDir containing new export entry
	 * @param replicaEPR: epr of newly created replica
	 * @param replicaName: 
	 */
	static protected void recordReplicaUnderDir(EndpointReferenceType dirResolverEPR, 
			EndpointReferenceType replicaEPR, String replicaName, 
			String replicaType)
		throws ResourceException, RemoteException, ConfigurationException
	{
		_logger.info("RExportDir resource population beings here");
		
		//acquire EPR to resolver for RExportDir containing created replica
		//EndpointReferenceType resolverEPR = lookupResolverByEPI(
		//		exportDirEPI, replicationURL);
		
		//acquire EPR from resolver of RExportDir containing created replica for new export entry
		EndpointReferenceType RExportDirEPR = null;
		
		try{
			//setup proxy to resolver
			RExportResolverPortType resolverService = ClientUtils.createProxy(
					RExportResolverPortType.class, dirResolverEPR);
			
			//get epr of rexportdir replica for this dir
			RExportDirEPR = resolverService.resolve(null);
		}
		catch(Exception e){
			throw new ResourceException(
					"Unable to connect/update/resolve rexportdir's resolver.", e);
		}
		
		/*add entry for new replica to dir it resides in*/
		
		//setup proxy to RExportDir service 
		RExportDirPortType RExportDirService = ClientUtils.createProxy(
				RExportDirPortType.class, RExportDirEPR);
		
		//call method to add newly created replica data to rexportentry table
		try{
			RExportDirService.populateDir(new PopulateDirRequestType(
				replicaEPR, replicaName, replicaType));
		}
		catch(Exception e){
			throw new ResourceException(
					"Unable to call populateDir in rexportdir db resource.", e);
		}
	}
	
	/**
	 * Returns EPR of RExportResolver associated with specified resource 
	 * 	from resolver-resource mapping table
	 * Location sensitive - 
	 * 		either (resolver) location passed in or current is used as default
	 * 
	 * @param resourcesEPI: EPI of resource whose resolver is to be looked up
	 * @param  resolverURL: URL of machine where resolver resides
	 *  	if null, use current container
	 * 
	 * @return resolverEPR: EPR of RExportResolver for specified resource
	 *  
	 */
	static public EndpointReferenceType lookupResolverByEPI(String resourcesEPI, 
			String resolverContainerURL)
		throws ResourceException
	{
		EndpointReferenceType resolverEPR = null;
		
		String resolverServiceURL = null;
		
		//create URL to appropriate resolver service
		if (resolverContainerURL != null)
			resolverServiceURL = resolverContainerURL 
				+ _REXPORT_RESOLVER_SERVICE_URL_PATH;
		else 
			resolverServiceURL = Container.getServiceURL("RExportResolverPortType");
		
		//create proxy to resolver service
		RExportResolverPortType resolverServiceProxy = null;
		try{
			resolverServiceProxy = ClientUtils.createProxy(
					RExportResolverPortType.class,
					EPRUtils.makeEPR(resolverServiceURL));
		}
		catch (Exception e){
			throw new ResourceException("Could not create proxy to resolver.", e);
		}
		
		//query resolver service for EPR of resolver for specified resource
		try{
			resolverEPR = resolverServiceProxy.resolverQuery(
					new ResolverQueryRequest(resourcesEPI)).getResolver_EPR();
		}
		catch (Exception e){
			throw new ResourceException(
					"Unable to query for resource's resolver.", e);
		}
		
		//return resolver of resource being terminated
		return resolverEPR;
	}
	
	/**
	 * Destroy resolver associated with export entry being destroyed
	 *  also destroy resolver-resource mapping
	 * 
	 * called by ExportedDirDB on export destroy (quit)
	 * 
	 * location sensitive
	 * 	(resolver) location passed in else assumed same as current container
	 */
	static public void destroyResolverByEPI(String resourcesEPI, String resolverURL)
		throws ResourceException
	{	
		EndpointReferenceType resolverEPR = lookupResolverByEPI(
				resourcesEPI, resolverURL);
		
		destroyResolverResourceInfo(resourcesEPI);
		
		destroyResolver(resolverEPR);
	}
	
	/**
	 * Destroy resolver-resource mapping in table
	 * lookup entry by EPI from resolverEPR 
	 * 
	 */
	static protected void destroyResolverResourceInfo(String resourcesEPI)
		throws ResourceException
	{	
		try{
			//get epr of rexport resolver service
			//  at current (primary) container where new export entry was created
			EndpointReferenceType rexportResolverServiceEPR = EPRUtils.makeEPR(
					Container.getServiceURL("RExportResolverPortType"));
			
			ResourceKey rKey = ResourceManager.getTargetResource(rexportResolverServiceEPR);
			IRExportResolverResource resource = (IRExportResolverResource)rKey.dereference();
			
			resource.updateResolverResourceInfo(resourcesEPI, null, null, true);
		}
		catch(Exception e){
			throw new ResourceException(
					"Could not destroy resolver resource mapping.", e);
		}
	}
	
	
	/**
	 * Destroy resolver found in given EPR
	 * 
	 * called by ExportedDirDB/ExportedFileDB on export destroy (quit)
	 */
	static public void destroyResolverByEPR(EndpointReferenceType resourceEPR)
		throws ResourceException
	{
		EndpointReferenceType resolverEPR = extractResolverFromEPR(resourceEPR);
		
		//get commonEPI
		String primaryEPI = null;
		WSName primaryWSName = new WSName(resourceEPR);
		if (!primaryWSName.isValidWSName())
			throw new ResourceException("Could not extract EPI from resource EPR.");
		primaryEPI = primaryWSName.getEndpointIdentifier().toString();
		
		destroyResolverResourceInfo(primaryEPI);
		
		destroyResolver(resolverEPR);
	}
	
	/**
	 * Notify specified resolver to terminate
	 * */
	static protected void destroyResolver(EndpointReferenceType resolverEPR)
		throws ResourceException
	{
		RExportResolverPortType resolverServiceProxy = null;
		try{
			resolverServiceProxy = ClientUtils.createProxy(
					RExportResolverPortType.class,
					resolverEPR);
			
			//send termination notification
			resolverServiceProxy.notify(null);
		}
		catch (Exception e){
			throw new ResourceException(
					"Could not notify resolver of primary termination.", e);
		}
	}
	
	/*
	 * Extract resolver from given EPR
	 * 
	 * location independent
	 * potentially pass in name of resolver if more than one resolver possible
	 */
	static public EndpointReferenceType extractResolverFromEPR(
			EndpointReferenceType myEPR){
		
		EndpointReferenceType resolverEPR = null;
		WSName exportResolverWSName = new WSName(myEPR);
		List<ResolverDescription> resolvers = exportResolverWSName.getResolvers();
		if (resolvers.size() > 1 )
			_logger.info("More than one resolver exists; using last");
		
//?how to ensure using desired resolver?	
		for (ResolverDescription nextResolver : resolvers){
			resolverEPR = nextResolver.getEPR();
		}
		return resolverEPR;
	}
	
	/*
	 * extract resolver factory EPR from resolver creation params
	 */
	static public EndpointReferenceType extractResolverServiceEPR(
			MessageElement []factoryPassedResolverCreationParams)
		throws ResourceException
	{
		
		EndpointReferenceType resolverServiceEPR = null;
		
		for (MessageElement elem : factoryPassedResolverCreationParams){
			QName elemName = elem.getQName();
			if (elemName.equals(
					RExportResolverServiceImpl.REXPORT_RESOLVER_SERVICE_EPR_NAME)){
				try{
					resolverServiceEPR = (EndpointReferenceType) elem.getObjectValue(
						EndpointReferenceType.class);
				}
				catch(Exception e){
					throw new ResourceException(
						"Uable to extract resolver service epr from" +
						" resolver creation params.", e);
				}
				return resolverServiceEPR;
			}
		}
		throw new ResourceException(
				"Cannot create resolver with NULL EPR " +
				"for resolver service in construction params.");
	}
	
	/*
	 * create creation params for creating a new RExportResolver
	 * adds new EPI and factoryPassedResolverCreationParams with to passed in params
	 * 
	 * location independent
	 */
	static public MessageElement[] createResolverCreationParams(
			MessageElement []factoryPassedResolverCreationParams,
			EndpointReferenceType targetEPR)
	{
		//get number of originally passed parameters
		int numExistingElements = 0;
		if (factoryPassedResolverCreationParams != null){
			numExistingElements = factoryPassedResolverCreationParams.length;
		}
		
		//create collection of resolver creation properties
		MessageElement []resolverCreationProperties = new MessageElement[numExistingElements+2];
		
		//set new parameters
		resolverCreationProperties[0] = new MessageElement(
				IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM);
		resolverCreationProperties[0].setValue(WSName.generateNewEPI().toString());
		resolverCreationProperties[1] = new MessageElement(
				RExportResolverServiceImpl.REXPORT_RESOLVER_TARGET_CONSTRUCTION_PARAMETER, 
				targetEPR);
		
		//add old parameters to new parameters
		for (int i = 0; i < numExistingElements; i++){
			resolverCreationProperties[i+2] = factoryPassedResolverCreationParams[i];
		}
		
		return resolverCreationProperties;
	}
	
	static public void updateResolverResourceInfo(EndpointReferenceType resolverEPR,
		EndpointReferenceType primaryEPR)
		throws InvalidWSNameFaultType, ResourceUnknownFaultType, ResourceException
	{
		//get commonEPI
		String primaryEPI = null;
		WSName primaryWSName = new WSName(primaryEPR);
		if (!primaryWSName.isValidWSName())
			throw new InvalidWSNameFaultType();
		primaryEPI = primaryWSName.getEndpointIdentifier().toString();
		
		//get resolver EPI
		String resolverEPI = null;
		WSName resolverWSName = new WSName(resolverEPR);
		if (!resolverWSName.isValidWSName())
			throw new InvalidWSNameFaultType();
		resolverEPI = resolverWSName.getEndpointIdentifier().toString();
		
		//get epr of rexport resolver service
		//  at current (primary) container where new export entry was created
		EndpointReferenceType rexportResolverServiceEPR = EPRUtils.makeEPR(
				Container.getServiceURL("RExportResolverPortType"));
		
		ResourceKey rKey = ResourceManager.getTargetResource(rexportResolverServiceEPR);
		IRExportResolverResource resource = (IRExportResolverResource)rKey.dereference();
		
		resource.updateResolverResourceInfo(
				primaryEPI, 
				resolverEPI, 
				resolverEPR, 
				false);	
	}
	
	static public EndpointReferenceType getResolverServiceEPR(
			EndpointReferenceType resolverEPR)
		throws RNSFaultType 
	{
		EndpointReferenceType resolverServiceEPR = null;
		
		//create proxy to resolver service
		try {
			RExportResolverPortType resolverProxy = ClientUtils.createProxy(
					RExportResolverPortType.class, resolverEPR);
			
//!replace with own method called "getResolverServiceEPR(void)"
			resolverServiceEPR = resolverProxy.getResolverServiceEPR(
					new ServiceEPRRequest(resolverEPR)).getResolverServiceEPR();
		}
		catch (Exception ce){
			throw FaultManipulator.fillInFault(new RNSFaultType(null, null, null, null,
				new BaseFaultTypeDescription[] {
					new BaseFaultTypeDescription(ce.getLocalizedMessage())
			}, null, null));
		}
		
		return resolverServiceEPR;	
	}

}



















