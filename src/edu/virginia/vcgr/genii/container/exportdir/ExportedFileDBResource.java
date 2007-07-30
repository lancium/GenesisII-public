package edu.virginia.vcgr.genii.container.exportdir;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.exportdir.ExportedFileUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.byteio.RByteIOResource;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class ExportedFileDBResource extends RByteIOResource
	implements IExportedFileResource
{
	static private Log _logger = LogFactory.getLog(ExportedFileDBResource.class);
	
	static private final String _CREATE_FILE_INFO = 
		"INSERT INTO exportedfile VALUES (?, ?, ?)";
	static private final String _RETRIEVE_FILE_INFO =
		"SELECT path, parentIds FROM exportedfile WHERE fileid = ?";
	static private final String _DELETE_EXPORTED_FILE_STMT =
		"DELETE FROM exportedfile WHERE fileid = ?";
	
	static private final String _RETRIEVE_ALL_FILE_IDS_FOR_PARENT_STMT =
		"SELECT fileid FROM exportedfile WHERE parentIds LIKE ?";
	static private final String _DESTROY_ALL_FILES_FOR_PARENT_STMT =
		"DELETE FROM exportedfile WHERE parentIds LIKE ?";
	
	private String _parentIds = null;
	private String _filePath = null;
	
	static void fileDestroyAllForParentDir(
		Connection connection, String parentId, boolean hardDestroy)
		throws ResourceException
	{
		String parentIdSearch = "%" + ExportedFileUtils._PARENT_ID_BEGIN_DELIMITER
			+ parentId + ExportedFileUtils._PARENT_ID_END_DELIMITER + "%";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			/* Retrieve list of ids to be deleted (used to destroy base DBResource tables)*/
			stmt = connection.prepareStatement(_RETRIEVE_ALL_FILE_IDS_FOR_PARENT_STMT);
			stmt.setString(1, parentIdSearch);
			rs = stmt.executeQuery();
			Collection<String> fileids = new ArrayList<String>();
			while (rs != null && rs.next())
				fileids.add(rs.getString(1));
			
			rs.close();
			rs = null;
			stmt.close();
			stmt = null;
			
			/* delete entries from exportedfile table */
			stmt = connection.prepareStatement(_DESTROY_ALL_FILES_FOR_PARENT_STMT);
			stmt.setString(1, parentIdSearch);
			stmt.executeUpdate();
			
			stmt.close();
			stmt = null;

			RByteIOResource.destroyAll(connection, fileids);
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
	
	public ExportedFileDBResource(ResourceKey rKey, DatabaseConnectionPool pool)
		throws SQLException
	{
		super(rKey, pool);
	}
	
	public void initialize(HashMap<QName, Object> constructionParams)
		throws ResourceException
	{
		_parentIds = (String)constructionParams.get(
			IExportedFileResource.PARENT_IDS_CONSTRUCTION_PARAM);
		_filePath = (String)constructionParams.get(
			IExportedFileResource.PATH_CONSTRUCTION_PARAM);
		Boolean isServiceResource = (Boolean) constructionParams.get(
			IResource.IS_SERVICE_CONSTRUCTION_PARAM);
		
		if ((isServiceResource == null) || !isServiceResource) {
			if (_parentIds == null)
				throw new ResourceException("\"" +
					IExportedFileResource.PARENT_IDS_CONSTRUCTION_PARAM +
					"\" construction parameter MUST be set.");
			if (_filePath == null)
				throw new ResourceException("\"" +
					IExportedFileResource.PATH_CONSTRUCTION_PARAM +
					"\" construction parameter MUST be set.");
		}
		
		super.initialize(constructionParams);
		
		if ((isServiceResource == null) || !isServiceResource)
		{
			insertFileInfo(_parentIds, _filePath);
		}
	}
	
	public void load(Object key) throws ResourceUnknownFaultType, ResourceException
	{
		super.load(key);
		
		if (key == null)
			return;
		
		loadFileInfo();
		if (!fileExists())
		{
			_logger.error("Local file does not exist for ExportedFileResource.");
			
			destroy(false);
			throw FaultManipulator.fillInFault(new ResourceUnknownFaultType());
		}
	}
	
	public File chooseFile(HashMap<QName, Object> constructionParams)
		throws ResourceException
	{
		String path = (String)constructionParams.get(
			IExportedFileResource.PATH_CONSTRUCTION_PARAM);
		if (path == null)
			throw new ResourceException("Couldn't find \"" +
				IExportedFileResource.PATH_CONSTRUCTION_PARAM +
				"\" construction parameter.");
		
		return new File(path);
	}
	
	public File getCurrentFile() throws ResourceException
	{
		String file = getFilePath();
		if (file == null)
			throw new ResourceException("No file name set for ExportedFileResource.");
		
		return new File(file);
	}
	
	public void destroy() throws ResourceException
	{
		destroy(true);
	}
	
	public void destroy(boolean hardDestroy) throws ResourceException
	{
		PreparedStatement stmt = null;
		try
		{
			stmt = _connection.prepareStatement(_DELETE_EXPORTED_FILE_STMT);
			stmt.setString(1, getId());
			stmt.executeUpdate();
			super.destroy();
			
			if (hardDestroy)
				destroyFile(getFilePath());
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			close(stmt);
		}
	}

	public String getId() throws ResourceException
	{
		return _resourceKey;
	}

	public String getParentIds() throws ResourceException
	{
		return _parentIds;
	}
	
	public String getLocalPath() throws ResourceException
	{
		return getFilePath();
	}
	
	public String getFilePath() throws ResourceException
	{
		return _filePath;
	}

	protected void insertFileInfo(String parentIDs, String filePath)
		throws ResourceException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_CREATE_FILE_INFO);
			stmt.setString(1, _resourceKey);
			stmt.setString(2, filePath);
			stmt.setString(3, parentIDs);
			if (stmt.executeUpdate() != 1)
				throw new ResourceException(
					"Unable to insert ExportedFile resource information.");
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			close(stmt);
		}
	}
	
	protected void loadFileInfo() throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = _connection.prepareStatement(_RETRIEVE_FILE_INFO);
			stmt.setString(1, _resourceKey);
			rs = stmt.executeQuery();
			
			if (rs.next())
			{
				_filePath = rs.getString(1);
				_parentIds = rs.getString(2);
			} else
			{
				_filePath = null;
				_parentIds = null;
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
	
	protected boolean fileExists() throws ResourceException
	{
		File myFile = getCurrentFile();
		if (myFile.exists() && myFile.isFile())
			return true;
		
		return false;
	}
	
	protected void destroyFile(File file)
	{
		if (file == null || !file.exists() || !file.isFile())
			return;
		try
		{
			file.delete();
		}
		catch (Exception e)
		{
			_logger.debug(
				"Exception occurred while deleting file in ExportFileResource.destroyFile("
					+ file.getAbsolutePath() + ")", e);
		}
	}
	
	protected void destroyFile(String filePath)
	{
		destroyFile(new File(filePath));
	}
}