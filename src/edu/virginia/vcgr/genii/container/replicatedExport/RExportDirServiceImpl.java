package edu.virginia.vcgr.genii.container.replicatedExport;

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
import org.ggf.rns.EntryType;
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
import org.morgan.util.GUID;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.notification.InvalidTopicException;
import edu.virginia.vcgr.genii.client.notification.WellknownTopics;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.common.notification.Notify;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.container.byteio.RandomByteIOAttributeHandlers;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.common.notification.TopicSpace;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.replicatedExport.RExportDirPortType;
import edu.virginia.vcgr.genii.replicatedExport.PopulateDirRequestType;

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
	
	protected void registerTopics(TopicSpace topicSpace)
	throws InvalidTopicException
	{
		super.registerTopics(topicSpace);
	}
	
	/* NotificationConsumer port type */
	@RWXMapping(RWXCategory.OPEN)
	public void notify(Notify notify) 
		throws RemoteException, ResourceUnknownFaultType
	{
		try{
			String topic = notify.getTopic().toString();
			
			//destroy replica if notified of resolver termination
			if (topic.equals(WellknownTopics.TERMINATED)){
				
				/*ensure this notification is for matching resource*/
				ResourceKey rKey = ResourceManager.getCurrentResource();
				IRExportResource resource = (IRExportResource) rKey.dereference();
				String resourcePrimaryLocalPath= resource.getLocalPath();
				
				RExportSubscriptionUserData notifyData = new RExportSubscriptionUserData(
						notify.getUserData());
				
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
		catch (Throwable t){
			_logger.warn(t.getLocalizedMessage(), t);
		}
	}

	@RWXMapping(RWXCategory.WRITE)
	public AddResponse add(Add addRequest) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("add operation not supported in RExportDir.");
	}

	@RWXMapping(RWXCategory.WRITE)
	public CreateFileResponse createFile(CreateFile createFileRequest) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("createFile operation not supported in RExportDir.");
	}

	/**
	 * Lists entries in db for this rexport dir resource.
	 */
	@RWXMapping(RWXCategory.READ)
	public ListResponse list(List listRequest) 
		throws RemoteException, ResourceUnknownFaultType, 
			RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		//get current resource
		IRExportResource resource = 
			(IRExportResource)ResourceManager.getCurrentResource().dereference();
		
		//retrieve entries associated with resource 
		Collection<RExportEntry>  entries = resource.retrieveEntries(
			listRequest.getEntryName());
		
		if (entries.size() == 0)
			_logger.info("empty RExportDir lookup results");
		
		//create EntryType list of found entries for response
		EntryType []ret = new EntryType[entries.size()];
		int listIter = 0;
		for (RExportEntry entry : entries){
			ret[listIter++] = new EntryType(
				entry.getName(), entry.getAttributes(), entry.getEntryReference());
		}
		
		return new ListResponse(ret);
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

	@RWXMapping(RWXCategory.WRITE)
	public MoveResponse move(Move moveRequest) 
		throws RemoteException, ResourceUnknownFaultType, RNSFaultType
	{
		throw new RemoteException("Move operation not supported in RExportDir.");
	}
	@RWXMapping(RWXCategory.READ)
	public QueryResponse query(Query queryRequest) 
		throws RemoteException, ResourceUnknownFaultType, RNSFaultType
	{	
		throw new RemoteException("Query operation not supported in RExportDir.");
	}
	
	@RWXMapping(RWXCategory.WRITE)
	public String[] remove(Remove removeRequest) 
		throws RemoteException, ResourceUnknownFaultType, 
			RNSDirectoryNotEmptyFaultType, RNSFaultType
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
	
}