package edu.virginia.vcgr.genii.container.exportdir;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.sql.SQLException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.Add;
import org.ggf.rns.AddResponse;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
import org.ggf.rns.EntryPropertiesType;
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
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.exportdir.ExportedDirUtils;
import edu.virginia.vcgr.genii.client.exportdir.ExportedFileUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.ser.AnyHelper;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.exportdir.ExportedDirPortType;
import edu.virginia.vcgr.genii.enhancedrns.IterateListRequestType;
import edu.virginia.vcgr.genii.enhancedrns.IterateListResponseType;

public class ExportedDirServiceImpl extends GenesisIIBase implements
		ExportedDirPortType
{
	static private Log _logger = LogFactory.getLog(ExportedDirServiceImpl.class);
	
	public ExportedDirServiceImpl() throws RemoteException
	{
		this("ExportedDirPortType");
	}
	
	protected ExportedDirServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);
		
		addImplementedPortType(WellKnownPortTypes.EXPORTED_DIR_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.RNS_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.ENHANCED_RNS_SERVICE_PORT_TYPE);
	}
	
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.EXPORTED_DIR_SERVICE_PORT_TYPE;
	}
	
	protected ResourceKey createResource(HashMap<QName, Object> constructionParameters)
		throws ResourceException, BaseFaultType
	{
		_logger.debug("Creating new ExportedDir Resource.");
		
		if (constructionParameters == null)
		{
			ResourceCreationFaultType rcft =
				new ResourceCreationFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription(
							"Could not create ExportedDir resource without cerationProperties")
						}, null);
			throw FaultManipulator.fillInFault(rcft);
		}
		
		ExportedDirUtils.ExportedDirInitInfo initInfo = null;
		initInfo = ExportedDirUtils.extractCreationProperties(constructionParameters);
	
		constructionParameters.put(
			IExportedDirResource.PATH_CONSTRUCTION_PARAM, initInfo.getPath());
		constructionParameters.put(
			IExportedDirResource.PARENT_IDS_CONSTRUCTION_PARAM, initInfo.getParentIds());
		constructionParameters.put(
			IExportedDirResource.REPLICATION_INDICATOR, initInfo.getReplicationState());
		constructionParameters.put(
			IExportedDirResource.LAST_MODIFIED_TIME, initInfo.getLastModifiedTime());
		
		return super.createResource(constructionParameters);
	}
	
	protected void fillIn(ResourceKey rKey, EndpointReferenceType newEPR,
			HashMap<QName, Object> creationParameters,
			Collection<MessageElement> resolverCreationParams) 
		throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(rKey, newEPR, creationParameters, resolverCreationParams);
		
		Date d = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		
		IExportedDirResource resource = (IExportedDirResource)rKey.dereference();
		resource.setCreateTime(c);
		resource.setModTime(c);
		resource.setAccessTime(c);
	}
	
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateFileResponse createFile(CreateFile createFileRequest)
			throws RemoteException, RNSEntryExistsFaultType,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType,
			RNSFaultType
	{
		EndpointReferenceType entryReference;
		String filename = createFileRequest.getFilename();
		IExportedDirResource resource;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IExportedDirResource)rKey.dereference();
		
		Collection<ExportedDirEntry> entries = resource.retrieveEntries(".*");
		for (ExportedDirEntry entry : entries)
		{
			if (filename.equals(entry.getName()))
				throw FaultManipulator.fillInFault(new RNSEntryExistsFaultType(
					null, null, null, null, null, null, filename));
		}
		
		String fullPath = ExportedFileUtils.createFullPath(
			resource.getLocalPath(), filename);
		String parentIds = ExportedDirUtils.createParentIdsString(
			resource.getParentIds(), resource.getId());
		try
		{
			/*
			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class,
					EPRUtils.makeEPR(Container.getServiceURL("ExportedFilePortType")));
			entryReference = 
				common.vcgrCreate(new VcgrCreate(
					ExportedFileUtils.createCreationProperties(
					fullPath, parentIds))).getEndpoint();
			*/
			WorkingContext.temporarilyAssumeNewIdentity(
				EPRUtils.makeEPR(Container.getServiceURL("ExportedFilePortType")));
			entryReference = new ExportedFileServiceImpl().vcgrCreate(new VcgrCreate(
				ExportedFileUtils.createCreationProperties(
						fullPath, parentIds, resource.getReplicationState()))).getEndpoint();
		}
		finally
		{
			WorkingContext.releaseAssumedIdentity();
		}
		/*
		catch (ConfigurationException ce)
		{
			throw new ResourceException(ce.getLocalizedMessage(), ce);
		}
		*/
		
		String newEntryId = (new GUID()).toString();
		ExportedDirEntry newEntry = new ExportedDirEntry(
			resource.getId(), filename, entryReference,
			newEntryId, ExportedDirEntry._FILE_TYPE, null);
		resource.addEntry(newEntry, true);
		resource.commit();
		
		return new CreateFileResponse(entryReference);
	}

	@RWXMapping(RWXCategory.WRITE)
	public AddResponse add(Add addRequest) throws RemoteException,
		RNSEntryExistsFaultType, ResourceUnknownFaultType,
		RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		//add request missing
		if (addRequest == null)
		{
			// Pure factory operation
			throw FaultManipulator.fillInFault(new RNSFaultType(
				null, null, null, null, new BaseFaultTypeDescription[] {
					new BaseFaultTypeDescription(
						"Pure factory version of add not allowed in export dir.")
				}, null, null));
		}
		
		//decipher add request
		//get name of file
		String name = addRequest.getEntry_name();
		EndpointReferenceType entryReference = addRequest.getEntry_reference();
		MessageElement []attrs = addRequest.get_any();
		
		if (entryReference != null)
		{
			throw FaultManipulator.fillInFault(new RNSFaultType(
				null, null, null, null, new BaseFaultTypeDescription[] {
					new BaseFaultTypeDescription(
						"Add not allowed in ExportDirs (unless you are creating " +
						"a new directory.")
				}, null, null));
		}
		
		_logger.debug("ExportDir asked to add \"" + name + "\".");
		
		IExportedDirResource resource;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		
		resource = (IExportedDirResource)rKey.dereference();
		String fullPath = ExportedFileUtils.createFullPath(
			resource.getLocalPath(), name);
		String parentIds = ExportedDirUtils.createParentIdsString(
			resource.getParentIds(), resource.getId());
		String isReplicated = resource.getReplicationState();
		EndpointReferenceType newRef =
			vcgrCreate(new VcgrCreate(ExportedDirUtils.createCreationProperties(
				fullPath, 
				parentIds, 
				isReplicated))).getEndpoint();
		
		String newEntryId = (new GUID()).toString();
		ExportedDirEntry newEntry = new ExportedDirEntry(
			resource.getId(), name, newRef, newEntryId, ExportedDirEntry._DIR_TYPE, attrs);
		resource.addEntry(newEntry, true);
		resource.commit();
		
		return new AddResponse(newRef);
	}

	@RWXMapping(RWXCategory.READ)
    public IterateListResponseType iterateList(IterateListRequestType list) 
    	throws RemoteException, ResourceUnknownFaultType, 
    		RNSEntryNotDirectoryFaultType, RNSFaultType
    {
		String entry_name_regexp = list.getEntry_name_regexp();
		
		_logger.debug("ExportDir asked to lookup iter for \"" + entry_name_regexp + "\".");
		
		IExportedDirResource resource = 
			(IExportedDirResource)ResourceManager.getCurrentResource().dereference();
		Collection<ExportedDirEntry> entries = null;
		
		entries = resource.retrieveEntries(entry_name_regexp);
		
		//create collection of MessageElement entries
		Collection<MessageElement> entryCollection = new LinkedList<MessageElement>();
    	for (ExportedDirEntry exportDirEntry : entries){
    		EntryType entry = new EntryType(
    				exportDirEntry.getName(), 
    				exportDirEntry.getAttributes(), 
    				exportDirEntry.getEntryReference());

    		entryCollection.add(AnyHelper.toAny(entry));
    	}
		
		try{
			return new IterateListResponseType(super.createWSIterator(
					entryCollection.iterator(), 25));
		}
		catch (ConfigurationException ce){
			throw new RemoteException("Unable to create iterator for exportDir lookup.", ce);
		}
		catch (SQLException sqe){
			throw new RemoteException("Unable to create iterator for exportDir lookup.", sqe);
		} 
    }	
	
	@RWXMapping(RWXCategory.READ)
	public ListResponse list(List listRequest) throws RemoteException,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType,
			RNSFaultType
	{
		String entry_name_regexp = listRequest.getEntry_name_regexp();
		
		_logger.debug("ExportDir asked to lookup \"" + entry_name_regexp + "\".");
		
		IExportedDirResource resource = 
			(IExportedDirResource)ResourceManager.getCurrentResource().dereference();
		Collection<ExportedDirEntry> entries;
		
		entries = resource.retrieveEntries(entry_name_regexp);
		EntryType []ret = new EntryType[entries.size()];
		int lcv = 0;
		for (ExportedDirEntry entry : entries)
		{
			ret[lcv++] = new EntryType(
				entry.getName(), entry.getAttributes(), entry.getEntryReference());
		}
		
		return new ListResponse(ret);
	}

	@RWXMapping(RWXCategory.WRITE)
	public MoveResponse move(Move moveRequest) throws RemoteException,
			ResourceUnknownFaultType, RNSFaultType
	{
		throw FaultManipulator.fillInFault(new RNSFaultType(
			null, null, null, null, new BaseFaultTypeDescription[] {
				new BaseFaultTypeDescription("RNS.move not supported in export dir.")
			}, null, null));
	}

	@RWXMapping(RWXCategory.READ)
	public QueryResponse query(Query queryRequest) throws RemoteException,
			ResourceUnknownFaultType, RNSFaultType
	{
		String entryPattern = queryRequest.getEntryPattern();
		EntryType []tmp = list(new List(entryPattern)).getEntryList();
		EntryPropertiesType []ret = new EntryPropertiesType[tmp.length];
		EndpointReferenceType myEPR = 
			(EndpointReferenceType)WorkingContext.getCurrentWorkingContext().getProperty(
				WorkingContext.EPR_PROPERTY_NAME);
		
		for (int lcv = 0; lcv < tmp.length; lcv++)
		{
			ret[lcv] = new EntryPropertiesType(myEPR,
				tmp[lcv].getEntry_name(), tmp[lcv].get_any(),
				tmp[lcv].getEntry_reference());
		}
		
		return new QueryResponse(ret[0]);
	}

	@RWXMapping(RWXCategory.WRITE)
	public String[] remove(Remove removeRequest) throws RemoteException,
			ResourceUnknownFaultType, RNSDirectoryNotEmptyFaultType,
			RNSFaultType
	{
		String entry_name = removeRequest.getEntry_name();
		String []ret;
		IExportedDirResource resource = 
			(IExportedDirResource)ResourceManager.getCurrentResource().dereference();
		Collection<String> removed = resource.removeEntries(entry_name, true);
		ret = new String[removed.size()];
		removed.toArray(ret);
		resource.commit();
		
		return ret;
	}
}