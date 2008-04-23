package edu.virginia.vcgr.genii.container.replicatedExport;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.io.File;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.morgan.util.GUID;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.ReferenceParametersType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.MessageElementUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.container.byteio.IRByteIOResource;
import edu.virginia.vcgr.genii.container.byteio.RandomByteIOAttributeHandlers;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.replicatedExport.resolver.RExportResolverUtils;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.client.replicatedExport.RExportUtils;

public class RExportDBResource extends BasicDBResource implements IRExportResource
{
	static private Log _logger = LogFactory.getLog(RExportDBResource.class);
	
	private String _myLocalPath = null;
	private String _myParentIds = null;
	
	protected static EndpointReferenceType _fileServiceEPR;
	protected static EndpointReferenceType _dirServiceEPR;
	
	static private final String _ADD_ENTRY=
		"INSERT INTO rexport VALUES(?, ?, ?)";
	
	static private final String _RETRIEVE_DIR_INFO =
		"SELECT path, parentIds FROM rexport WHERE dirid = ?";
	
	static private final String _UPDATE_PATH = 
		"UPDATE rexport SET path = ? WHERE dirid = ?";
	
	static private final String _RETRIEVE_REXPORT_ENTRIES_STMT =
		"SELECT dirid, name, endpoint, entryid, type " +
		"FROM rexportentry WHERE dirid = ?";
	
	static private final String _DELETE_REXPORT_ENTRY_ATTRS_STMT =
		"DELETE FROM rexportentryattr WHERE entryid = ?";
	
	static private final String _DELETE_REXPORT_ENTRY_STMT =
		"DELETE FROM rexportentry WHERE entryid = ?";
	
	static private final String _ADD_ENTRY_STMT =
		"INSERT INTO rexportentry VALUES(?, ?, ?, ?, ?)";
	
	static private final String _ADD_DIR_ATTR_STMT =
		"INSERT INTO rexportentryattr VALUES(?, ?)";
	
	//delete all entry attr entries that match dirid
	static private final String _DELETE_EXPORT_ENTRY_ATTR =
		"DELETE FROM rexportentryattr WHERE entryid in " +
		"(SELECT entryid FROM rexportentry WHERE dirid = ?)";
	
	//delete all rexport entry entries that match dirid
	static private final String _DELETE_EXPORT_ENTRIES_STMT =
		"DELETE FROM rexportentry WHERE dirid = ?";

	/*clear rexport*/
	//delete all rexport entries that match dirid
	static private final String _DELETE_REXPORT_STMT =
		"DELETE FROM rexport WHERE dirid = ?";
	
	//get all dirids that match parentId
	static private final String _RETRIEVE_ALL_DIR_IDS_FOR_PARENT_STMT = 
		"SELECT dirid FROM rexport WHERE parentIds LIKE ?";
	
	/*clear attrs*/
	//get all dirids that match parentId
	//then get all entryids of these 
	//delete all attr entries that match these entryids
	static private final String _DESTROY_ALL_ATTRS_FOR_PARENT_STMT =
		"DELETE FROM rexportentryattr " +
		"WHERE entryid in (SELECT entryid FROM rexport WHERE dirid in " +
		"(SELECT dirid FROM rexport WHERE parentIds LIKE ?))";
	
	/*clear rexportentry entries*/
	//get all dirids that match parentId
	//destroy all rexport entries that match these dirids
	static private final String _DESTROY_ALL_ENTRIES_FOR_PARENT_STMT =
		"DELETE FROM rexportentry " +
		"WHERE dirid in " +
		"(SELECT dirid FROM rexport WHERE parentIds LIKE ?)";
	
	/*clear rexport given parentIds*/
	static private final String _DESTROY_ALL_DIRS_FOR_PARENT_STMT = 
		"DELETE FROM rexport WHERE parentIds LIKE ?";
	
	public RExportDBResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool,
			IResourceKeyTranslater translater)
		throws SQLException
	{
		super(parentKey, connectionPool, translater);
	}
	
	public void initialize(HashMap<QName, Object> constructionParams)
	throws ResourceException
	{
		_myParentIds= (String)constructionParams.get(
			IRExportResource.PARENT_IDS_CONSTRUCTION_PARAM);
		_myLocalPath = (String)constructionParams.get(
			IRExportResource.LOCALPATH_CONSTRUCTION_PARAM);
		
		super.initialize(constructionParams);
		
		Boolean isService = (Boolean)constructionParams.get(
			IResource.IS_SERVICE_CONSTRUCTION_PARAM);
	//?when does this occur?
		if (isService == null || !isService.booleanValue())
			insertDirInfo();
	}
	
	public void load(ReferenceParametersType refParams) 
		throws ResourceUnknownFaultType, ResourceException
	{
		super.load(refParams);
		
		if (isServiceResource())
			return;
		
		loadDirInfo();
		/*do we care if dir does not exist? this should be handled by other means
		if (!dirExists())
		{
			_logger.error("Local file does not exist for RExportDir resource.");
			
			destroy(_connection, false);
			throw FaultManipulator.fillInFault(new ResourceUnknownFaultType());
		}
		*/
	}
	
	protected void loadDirInfo() throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = _connection.prepareStatement(_RETRIEVE_DIR_INFO);
			stmt.setString(1, getId());
			rs = stmt.executeQuery();
			
			if (rs.next()){
				_myLocalPath = rs.getString(1);
				_myParentIds = rs.getString(2);
			} 
			else{
				_myLocalPath = null;
				_myParentIds = null;
			}
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			close(rs);
			close(stmt);
		}
	}
	
	public void setLocalPath(String localPath) 
		throws ResourceException
	{
		_myLocalPath = localPath;
		updatePathInfo();
	}
	
	protected void insertDirInfo() throws ResourceException
	{
		/*
		if (_myLocalPath == null)
			//|| _myParentIds == null)
			throw new ResourceException(
				"Cannot add RExport without valid parent IDs or path.");
		*/
		
		//until set properly
		if ( _myLocalPath == null)
			_myLocalPath = (String)"";
	
		if ( _myParentIds == null)
			_myParentIds = (String)"";
		
		_logger.info("RExport table populated with replica for: " + _myLocalPath);
		
		PreparedStatement stmt = null;
		
		try{
			stmt = _connection.prepareStatement(_ADD_ENTRY);
			stmt.setString(1, _resourceKey);
			stmt.setString(2, _myLocalPath);
			stmt.setString(3, _myParentIds);
			if (stmt.executeUpdate() != 1)
				throw new ResourceException(
					"Unable to insert RExport resource information.");
		}
		catch (SQLException sqe){
			throw new ResourceException(
					"Could not add rexport resource entry", sqe);
		}
		finally{
			close(stmt);
		}
	}
	
	protected void updatePathInfo() throws ResourceException
	{
		_logger.info("Updating Dir path in rexport table with: " + _myLocalPath);
		PreparedStatement stmt = null;
		
		try{
			stmt = _connection.prepareStatement(_UPDATE_PATH);
			stmt.setString(1, _myLocalPath);
			stmt.setString(2, _resourceKey);
			stmt.executeUpdate();
		}
		catch (SQLException sqe){
			throw new ResourceException(
					"Could not update path for rexport resource entry", sqe);
		}
		finally{
			close(stmt);
		}
	}
	
	protected RExportEntry createEntryForRealFile(String nextRealName,
				EndpointReferenceType serviceEPR,
				String entryType, String localPath, String parentIds)
		throws ResourceException, RemoteException
	{
		try
		{
			_logger.info("Creating new rexport entries");
			
			MessageElement []creationProperties = RExportUtils.createCreationProperties(
					localPath, parentIds, null);
			
			/* create new RExport resource */
			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, serviceEPR);
			VcgrCreateResponse resp = common.vcgrCreate(
				new VcgrCreate(creationProperties));
			
			EndpointReferenceType entryReference = resp.getEndpoint();
		
			//update entryref to include resolver epr
			try{
				entryReference = RExportResolverUtils.setupRExport(
						entryReference, 
						entryType,
						creationProperties[0].getValue(),
						null,
						nextRealName);
			}
			catch (Exception e){
				_logger.error("Unable to create rexport resolver: " + e.getLocalizedMessage());
				throw new ResourceException("Unable to create rexport resolver.", e);
			}
		
			//create entry for new export resource in export DB
			String newId = (new GUID()).toString();
			RExportEntry newEntry = new RExportEntry(
					getId(), nextRealName, entryReference, newId, 
					entryType, null);
				addEntry(newEntry, false);
				
			return newEntry;
		}
		catch (ConfigurationException ce){
			throw new ResourceException("Could not create new expot entry.", ce);
		}
	}
	
	/*
	 * Initiate appropriate additions into db to store information about entry
	 * 
	 * in: entry - entry to be added into DB
	 * in: createOnDisk - if true, entry is to be created for real as well
	 * 
	 * */
	public void addEntry(RExportEntry entry, boolean createOnDisk)
			throws ResourceException, RNSEntryExistsFaultType
	{
		/* if createFile is true, create underlying file */
		if (createOnDisk)
			createOnDisk(entry.getName(), entry.getType());
		
		addEntry(entry.getName(), entry.getEntryReference(), entry.getId(), entry.getType());
		addAttributes(entry.getName(), entry.getAttributes(), entry.getId());
	}
	
	/*
	 * Add entry info into db
	 */
	protected void addEntry(String entryName, EndpointReferenceType entryReference,
			String entryID, String entryType)
			throws ResourceException, RNSEntryExistsFaultType
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_ADD_ENTRY_STMT);
			stmt.setString(1, getId());
			stmt.setString(2, entryName);
			stmt.setBlob(3, EPRUtils.toBlob(entryReference));
			stmt.setString(4, entryID);
			stmt.setString(5, entryType);
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update resource.");
		}
		catch (SQLException sqe)
		{
			if (sqe.getErrorCode() == -104)
			{
				// Uniqueness problem
				RNSEntryExistsFaultType fault = new RNSEntryExistsFaultType();
				fault.setPath(entryName);
				throw FaultManipulator.fillInFault(fault);
			}
			else
				throw new ResourceException(
						"Could not add new rexport entry to table.", sqe);
		}
		finally{
			close(stmt);
		}
	}
	
	/*
	 * Add entry attr info into db
	 */
	protected void addAttributes(String entryName, MessageElement[] attrs, String entryID)
		throws ResourceException
	{
		PreparedStatement stmt = null;
		
		try
		{
			if (attrs != null && attrs.length > 0)
			{
				stmt = _connection.prepareStatement(_ADD_DIR_ATTR_STMT);
				
				for (MessageElement nextAttr : attrs)
				{
					stmt.setString(1, entryID);
					stmt.setBytes(2, MessageElementUtils.toBytes(nextAttr));
					if (stmt.executeUpdate() != 1)
						throw new ResourceException(
							"Unable to update attributes for RNS resource.");
				}
			}
		}
		catch (SQLException sqe){
			throw new ResourceException(
					"Problem with storing attrs in rexport table", sqe);
		}
		finally{
			close(stmt);
		}
	}

	protected void createOnDisk(String entryName, String entryType)
		throws ResourceException
	{
		String fullPath = RExportUtils.createFullPath(getLocalPath(), entryName);
		
		_logger.debug("Export Dir asked to create \"[" + entryType + "] " + fullPath + "\" on disk");
		
		try
		{
			//create real file if file
			if (entryType.equals(RExportEntry._FILE_TYPE)){
				if (!RExportUtils.createLocalFile(fullPath))
					throw FaultManipulator.fillInFault(
						new RNSEntryExistsFaultType());
			} 
			//create real dir if dir
			else if (entryType.equals(RExportEntry._DIR_TYPE)){
				if (!RExportUtils.createLocalDir(fullPath))
					throw FaultManipulator.fillInFault(
						new RNSEntryExistsFaultType());
			} 
			else{
				throw new ResourceException("Improper type for exported dir entry.");
			}
		}
		catch (IOException ioe){
			_logger.error("Unable to create file/directory at path " + fullPath, ioe);
			throw new ResourceException("Unable to create file/directory at path " +
				fullPath, ioe);
		}
	}
	
	
	protected void removeEntry(RExportEntry entry, boolean hardDestroy)
		throws ResourceException, ResourceUnknownFaultType
	{
		try{
			//get EPR of entry to be removed
			ResourceKey rKey = ResourceManager.getTargetResource(entry.getEntryReference());
			
			//get this resource
			IRExportEntryResource resource = (IRExportEntryResource)rKey.dereference();
			
			resource.destroy(_connection, hardDestroy);
		}
		catch (ResourceException ruft){
			// Ignore so we can keep cleaning up.
			_logger.error("Unable to destroy RExport resource.", ruft);
		}
			
		/* remove entry information from db*/
		PreparedStatement stmt = null;
		
		try{
			//delete attributes associated with entry
			stmt = _connection.prepareStatement(_DELETE_REXPORT_ENTRY_ATTRS_STMT);
			stmt.setString(1, entry.getId());
			stmt.executeUpdate();
			
			stmt.close();
			stmt = null;
			
			//delete entry
			stmt = _connection.prepareStatement(_DELETE_REXPORT_ENTRY_STMT);
			stmt.setString(1, entry.getId());
			stmt.executeUpdate();
			
			stmt.close();
			stmt = null;
		}
		catch (SQLException sqe){
			throw new ResourceException(
					"Could not remove entry from rexport table", sqe);
		}
		finally{
			close(stmt);
		}
	}
	
	/*
	 * call destroy on this resource
	 * if hardDestroy, destroy primary's local resources as well
	 */
	public void destroy(boolean hardDestroy) throws ResourceException, ResourceUnknownFaultType
	{
		destroy(_connection, hardDestroy);
	}
	
	/*
	 * destroy all entries under directory
	 * destroy info related to directory
	 * if hardDestroy, destroy localFiles
	 */
	public void destroy(Connection connection, boolean hardDestroy) 
		throws ResourceException, ResourceUnknownFaultType
	{
		dirDestroyAllForParentDir(connection, getId(), false);
		//not needed as files and dirs saved together in one table
		//ExportedFileDBResource.fileDestroyAllForParentDir(_connection, getId(), false);
		
		/* Delete information related directly to parent exported dir */
		PreparedStatement stmt = null;
		try
		{
			stmt = connection.prepareStatement(_DELETE_EXPORT_ENTRY_ATTR);
			stmt.setString(1, getId());
			stmt.executeUpdate();
			
			close(stmt);
			stmt = null;
			
			stmt = connection.prepareStatement(_DELETE_EXPORT_ENTRIES_STMT);
			stmt.setString(1, getId());
			stmt.executeUpdate();
			
			close(stmt);
			stmt = null;
			
			stmt = connection.prepareStatement(_DELETE_REXPORT_STMT);
			stmt.setString(1, getId());
			stmt.executeUpdate();
			
			super.destroy();
			
			/* Delete underlying file system resources if necessary 
			 * In the semantics where replica is read only, hardDestroy is always false
			 */
			if (hardDestroy)
				destroyDirectory(getLocalPath());
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Could not destory entry", sqe);
		}
		finally
		{
			close(stmt);
		}
	}
	
	static void dirDestroyAllForParentDir(Connection connection, 
			String parentId, boolean hardDestroy) 
		throws ResourceException
	{
		String parentIdSearch = "%" + RExportUtils._PARENT_ID_BEGIN_DELIMITER +
		parentId + RExportUtils._PARENT_ID_END_DELIMITER
		+ "%";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(_RETRIEVE_ALL_DIR_IDS_FOR_PARENT_STMT);
			stmt.setString(1, parentIdSearch);
			rs = stmt.executeQuery();
			Collection<String> dirids = new ArrayList<String>();
			while (rs.next())
				dirids.add(rs.getString(1));
			
			rs.close();
			rs = null;
			stmt.close();
			stmt = null;
			
			stmt = connection.prepareStatement(_DESTROY_ALL_ATTRS_FOR_PARENT_STMT);
			stmt.setString(1, parentIdSearch);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
			
			stmt = connection.prepareStatement(_DESTROY_ALL_ENTRIES_FOR_PARENT_STMT);
			stmt.setString(1, parentIdSearch);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
			
			stmt = connection.prepareStatement(_DESTROY_ALL_DIRS_FOR_PARENT_STMT);
			stmt.setString(1, parentIdSearch);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
			
			BasicDBResource.destroyAll(connection, dirids);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Could not destroy related entries.", sqe);
		}
		finally
		{
			close(rs);
			close(stmt);
		}
	}
			
			
	protected void destroyDirectory(String path)
	{
		destroyDirectory(new File(path));
	}
	
	protected void destroyDirectory(File rootDir)
	{
		if (!rootDir.exists() || !rootDir.isDirectory())
			return;
		
		File []children = rootDir.listFiles();
		for (File child : children)
		{
			if (child.isDirectory())
				destroyDirectory(child);
			else
				child.delete();
		}
		
		rootDir.delete();
	}

	protected Collection<File> listLocalEntrieAsFiles() 
		throws ResourceException
	{
		// make sure localdir exists
		String dirPath = getLocalPath();
		if (dirPath == null)
			throw new ResourceException(
				"RExportDir has no local path.  Cannot sync entries.");
		
		//makes sure localdir is an existing dir
		File dir = new File(dirPath);
		if (!dir.exists())
			throw new ResourceException(
					"Local path for RExportDir does not exist.");
		if (!dir.isDirectory())
			throw new ResourceException("" +
					"Local path for RExportDir is not a directory.");
		
		//get dir listing 
		File []localEntries = dir.listFiles();
		
		//convert listing to file collection
		ArrayList<File> fileList = new ArrayList<File>();

		for (File nextEntry : localEntries)
			fileList.add(nextEntry);
		
		return fileList;
	}
	
	public Collection<RExportEntry> retrieveEntries(String regex)
		throws ResourceException
	{
		//assume synced already
		//retrieve what entries exist currently in db
		Collection<RExportEntry> allKnownEntries = retrieveKnownEntries();
		
		Pattern p = Pattern.compile(regex);
		
		//collection of RExport entries whose names match regex
		Collection<RExportEntry> ret = new ArrayList<RExportEntry>();
		for (RExportEntry nextEntry : allKnownEntries)
		{
			if (p.matcher(nextEntry.getName()).matches()){
				// pre-fill in attributes document for this entry
				// to send it back for pre-fetching.
				fillInAttributes(nextEntry);
				ret.add(nextEntry);
			}
		}
		
		return ret;
	}
	
	protected Collection<RExportEntry> retrieveKnownEntries()
		throws ResourceException
	{
		try{
			Collection<RExportEntry> ret = retrieveBareEntries(getId());
			
	//?		if (ret.size() > 0)
	//?			addAttributes(ret, getId());
			
			return ret;
		}
		catch (SQLException sqe){
			throw new ResourceException("Could not retrieve rexport resource entries", sqe);
		}
	}
	
	/*
	 * retrieve entries currently in db
	 */
	protected Collection<RExportEntry> retrieveBareEntries(String id)
		throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<RExportEntry> ret = new ArrayList<RExportEntry>();
		
		try{
			stmt = _connection.prepareStatement(_RETRIEVE_REXPORT_ENTRIES_STMT);
			stmt.setString(1, getId());
			rs = stmt.executeQuery();
			
			while (rs.next()){
				RExportEntry entry = new RExportEntry(
					rs.getString(1), rs.getString(2),
					EPRUtils.fromBlob(rs.getBlob(3)),
					rs.getString(4), rs.getString(5), null);
				ret.add(entry);
			}
	
			return ret;
		}
		finally{
			close(rs);
			close(stmt);
		}
	}
	
	public String getId() throws ResourceException
	{
		return _resourceKey;
	}

	public String getLocalPath() throws ResourceException
	{
		return _myLocalPath;
	}

	public String getParentIds() throws ResourceException
	{
		return _myParentIds;
	}
	
	/*
	 * Extract resolver from given EPR
	 */
	static public EndpointReferenceType getResolver(EndpointReferenceType myEPR){
		
		EndpointReferenceType resolverEPR = null;
		WSName exportResolverWSName = new WSName(myEPR);
		List<ResolverDescription> resolvers = exportResolverWSName.getResolvers();
		if (resolvers.size() > 1 )
			_logger.debug("More than one resolver exists; using last");
		
		//?how to ensure using desired resolver?	
		for (ResolverDescription nextResolver : resolvers){
			resolverEPR = nextResolver.getEPR();
		}
		return resolverEPR;
	}
	
	private void fillInAttributes(RExportEntry entry)
		throws ResourceException
	{
		File entryFile = new File(getLocalPath(), entry.getName());
		if (!entryFile.exists())
			return;
		if (!entryFile.isFile())
			return;
		
		ArrayList<MessageElement> attrs = new ArrayList<MessageElement>();
		MessageElement []attrsA = entry.getAttributes();
		if (attrsA != null)
		{
			for (MessageElement attr : attrsA)
				attrs.add(attr);
		}
		
		QName transMechName = new QName(RandomByteIOAttributeHandlers.RANDOM_BYTEIO_NS,
			"TransferMechanism");
		attrs.add(new MessageElement(
			new QName(ByteIOConstants.RANDOM_BYTEIO_NS, 
				ByteIOConstants.SIZE_ATTR_NAME), entryFile.length()));
		attrs.add(new MessageElement(transMechName,
			ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
		attrs.add(new MessageElement(transMechName,
			ByteIOConstants.TRANSFER_TYPE_DIME_URI));
		attrs.add(new MessageElement(transMechName,
			ByteIOConstants.TRANSFER_TYPE_MTOM_URI));
		
		try
		{
			IRByteIOResource resource = (IRByteIOResource)(ResourceManager.getTargetResource(
				entry.getEntryReference()).dereference());
			attrs.add(new MessageElement(new QName(
				ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.ACCESSTIME_ATTR_NAME),
				resource.getAccessTime()));
			attrs.add(new MessageElement(new QName(
				ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.MODTIME_ATTR_NAME),
				resource.getModTime()));
			attrs.add(new MessageElement(new QName(
				ByteIOConstants.RANDOM_BYTEIO_NS, ByteIOConstants.CREATTIME_ATTR_NAME),
				resource.getCreateTime()));
		}
		catch (ResourceUnknownFaultType ruft)
		{
			// We couldn't find the resource, so we just skip it for now.
		}
		
		entry.setAttributes(attrs.toArray(new MessageElement[0]));
	}
}