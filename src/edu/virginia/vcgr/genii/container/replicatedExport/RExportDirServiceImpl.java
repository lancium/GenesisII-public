package edu.virginia.vcgr.genii.container.replicatedExport;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

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
import org.morgan.util.GUID;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AbstractNotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.GenesisIIBaseTopics;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ResourceTerminationContents;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.container.byteio.RandomByteIOAttributeHandlers;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.RNSContainerUtilities;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileRequestType;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileResponseType;
import edu.virginia.vcgr.genii.replicatedExport.RExportDirPortType;
import edu.virginia.vcgr.genii.replicatedExport.PopulateDirRequestType;
import edu.virginia.vcgr.genii.security.RWXCategory;

@GeniiServiceConfiguration(
	resourceProvider=RExportDBResourceProvider.class)
public class RExportDirServiceImpl extends GenesisIIBase 
	implements RExportDirPortType
{
	static private Log _logger = LogFactory.getLog(RExportDirServiceImpl.class);
	
	protected void setAttributeHandlers()
		throws NoSuchMethodException, ResourceException, 
			ResourceUnknownFaultType
	{
		super.setAttributeHandlers();
		
		new RandomByteIOAttributeHandlers(getAttributePackage());
	}
	
	public RExportDirServiceImpl() throws RemoteException
	{
		this("RExportDirPortType");
		
		addImplementedPortType(
				WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE);

		addImplementedPortType(
			RNSConstants.RNS_PORT_TYPE);
		
	}
	
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.REXPORT_DIR_PORT_TYPE;
	}
	
	protected RExportDirServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);
		
		addImplementedPortType(
				WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE);
		addImplementedPortType(
				WellKnownPortTypes.GENII_NOTIFICATION_CONSUMER_PORT_TYPE);
	}
	
	private class LegacyResourceTerminatedNotificationHandler
		extends AbstractNotificationHandler<ResourceTerminationContents>
	{
		private LegacyResourceTerminatedNotificationHandler()
		{
			super(ResourceTerminationContents.class);
		}

		@Override
		public void handleNotification(TopicPath topic,
			EndpointReferenceType producerReference,
			EndpointReferenceType subscriptionReference,
			ResourceTerminationContents contents) throws Exception
		{
			/*ensure this notification is for matching resource*/
			ResourceKey rKey = ResourceManager.getCurrentResource();
			IRExportResource resource = (IRExportResource) rKey.dereference();
			String resourcePrimaryLocalPath= resource.getLocalPath();
			
			RExportSubscriptionUserData notifyData = 
				contents.additionalUserData(
					RExportSubscriptionUserData.class);
			
			String exportPath = notifyData.getPrimaryLocalPath();
			if(resourcePrimaryLocalPath.equals(exportPath)){
				//destroy resource DB info
				//this will not send termination notification
				resource.destroy(false);
				
				_logger.info("RExportDir replica " + exportPath + " terminated.");
			}
			else{
				_logger.error("Termination notification user" +
						" data does not match RExportDir resource.");
			}
		}
	}
	
	@Override
	protected void registerNotificationHandlers(
		NotificationMultiplexer multiplexer)
	{
		super.registerNotificationHandlers(multiplexer);
		
		multiplexer.registerNotificationHandler(
			GenesisIIBaseTopics.RESOURCE_TERMINATION_TOPIC.asConcreteQueryExpression(),
			new LegacyResourceTerminatedNotificationHandler());
	}
	
	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] add(RNSEntryType[] addRequest)
			throws RemoteException, WriteNotPermittedFaultType
	{
		throw new RemoteException("add operation not supported in RExportDir.");
	}

	@RWXMapping(RWXCategory.WRITE)
	public CreateFileResponseType createFile(CreateFileRequestType createFileRequest) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType
	{
		throw new RemoteException("createFile operation not supported in RExportDir.");
	}

	/**
	 * Lists entries in db for this rexport dir resource.
	 */
	@Override
	@RWXMapping(RWXCategory.READ)
	public LookupResponseType lookup(String []lookupRequest) 
		throws RemoteException, ResourceUnknownFaultType
	{
		Collection<RExportEntry> entries = new LinkedList<RExportEntry>();
		
		//get current resource
		IRExportResource resource = 
			(IRExportResource)ResourceManager.getCurrentResource().dereference();
		
		//retrieve entries associated with resource
		if (lookupRequest == null || lookupRequest.length == 0)
			lookupRequest = new String[] { null };
		
		for (String entryName : lookupRequest)
			entries.addAll(resource.retrieveEntries(entryName));
		
		if (entries.size() == 0)
			_logger.info("empty RExportDir lookup results");
		
		//create EntryType list of found entries for response
		Collection<RNSEntryResponseType> result = 
			new ArrayList<RNSEntryResponseType>(entries.size());
		for (RExportEntry entry : entries)
		{
			result.add(new RNSEntryResponseType(
				entry.getEntryReference(), 
				RNSUtilities.createMetadata(entry.getEntryReference(), entry.getAttributes()), null, 
				entry.getName()));
		}
		
		return RNSContainerUtilities.translate(
    		result, iteratorBuilder(
    			RNSEntryResponseType.getTypeDesc().getXmlType()));
	}
	
	/*
	 * creates entry for new replica of new export entry in rexportentry table 
	 * called on RExportDir containing replica resource
	 */
	@RWXMapping(RWXCategory.WRITE)
	public void populateDir(PopulateDirRequestType request) 
		throws ResourceException, ResourceUnknownFaultType, RNSEntryExistsFaultType
	{
		//extract replica epr from request
		EndpointReferenceType replicaEPR = request.getReplica_EPR();
		
		//extract replica name from request 
		String replicaName = request.getReplica_name();
		
		//extract replica type from request
		String replicaType = request.getReplica_type();
		
		//get db entry associated with current resource
		IRExportResource dirResource = null;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		dirResource = (IRExportResource)rKey.dereference();
		
		//store new entry and commit changes
		RExportEntry newReplicaEntry = new RExportEntry(
				dirResource.getId(),   	//resourceKey of current dir 
				replicaName, 			//passed in name of replica
				replicaEPR, 			//passed in epr of replica
				(new GUID()).toString(),//newly created GUID as entryID
				replicaType, 			//passed in type of replica
				null);					//null attrs
		
		dirResource.addEntry(newReplicaEntry, false);
		dirResource.commit();
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] rename(NameMappingType[] renameRequest)
		throws RemoteException, WriteNotPermittedFaultType
	{
		throw new RemoteException("Rename operation not supported in RExportDir.");
	}
	
	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] remove(String[] removeRequest)
		throws RemoteException, WriteNotPermittedFaultType
	{
		throw new RemoteException("Remove operation not supported in RExportDir.");
	}
	
	protected Object translateConstructionParameter(MessageElement parameter)
	throws Exception
	{
		QName messageName = parameter.getQName();
		if (messageName.equals(IRExportResource.LOCALPATH_CONSTRUCTION_PARAM))
			return parameter.getValue();
		else
			return super.translateConstructionParameter(parameter);
	}
	
	protected ResourceKey createResource(HashMap<QName, Object> constructionParameters)
		throws ResourceException, BaseFaultType
	{
		_logger.info("Creating new RExportDir instance.");
		
		return super.createResource(constructionParameters);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] setMetadata(
		MetadataMappingType[] setMetadataRequest) throws RemoteException,
			WriteNotPermittedFaultType
	{
		throw new RemoteException(
			"SetMetadata operation not supported in RExportDir!");
	}
}