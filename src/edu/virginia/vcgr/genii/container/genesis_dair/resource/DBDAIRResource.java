package edu.virginia.vcgr.genii.container.genesis_dair.resource;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;


import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.EntryType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rns.RNSDBResource;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class DBDAIRResource extends RNSDBResource implements IDAIRResource{
	
	static private Log _logger = LogFactory.getLog(DBDAIRResource.class);
	
	static private final String _ADD_DATARESOURCE_ENTRY_STATEMENT =
		"INSERT INTO dataresources(servicekey, serviceEPR, resourcename, resourceEPR, query) VALUES(?, ?, ?, ?, ? )";
	static private final String _REMOVE_ENTRIES_STMT =
		"DELETE FROM dataresources WHERE resourcename = ?";

	public DBDAIRResource(
			ResourceKey parentKey,
			DatabaseConnectionPool connectionPool,
			IResourceKeyTranslater translater) 
		throws SQLException {
		super(parentKey, connectionPool, translater);
	}

	public void addEntry( EndpointReferenceType serviceEPR, String resourceName, 
			EndpointReferenceType resourceEPR, String query) 
		throws ResourceException {
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_ADD_DATARESOURCE_ENTRY_STATEMENT);
			stmt.setString(1, this._resourceKey);
			stmt.setBlob(2, EPRUtils.toBlob(serviceEPR));
			stmt.setString(3, resourceName);
			stmt.setBlob(4, EPRUtils.toBlob(resourceEPR));
			stmt.setString(5, query);
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update database.");
			stmt.close();
			stmt = null;
			
		}
		catch (SQLException sqe)
		{
			
			if (sqe.getErrorCode() == -104)
			{
				// Uniqueness problem
				RNSEntryExistsFaultType fault = new RNSEntryExistsFaultType();
				fault.setPath(resourceName);
				try {
					throw FaultManipulator.fillInFault(fault);
				} 
				catch (RNSEntryExistsFaultType e) {
					_logger.warn(e.getLocalizedMessage(), e);}
			} 
			else
				_logger.warn(sqe.getLocalizedMessage(), sqe);
				System.out.println("The abstract names for the SQLDataResources must be unique.");
		
			throw new ResourceException("Unable to update database.", sqe);
		} 
		catch (IOException e) {
			_logger.warn(e.getLocalizedMessage(), e);
			throw new ResourceException ("Couldn't update database." , e);
		}
		finally
		{
			StreamUtils.close(stmt);
		}			
	}
	


	public Collection<String> removeEntries(String name)
	throws ResourceException
	{
		ArrayList<String> ret = new ArrayList<String>();
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_REMOVE_ENTRIES_STMT);
			stmt.setString(1, name);
			stmt.executeUpdate();
			ret.add(name);
	
			return ret;
		}
		catch (SQLException sqe)
		{
			_logger.warn(sqe.getLocalizedMessage(), sqe);
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			close(stmt);
		}
	}
	
	
	static private final String _LIST_DATARESOURCES_STMT =
		"SELECT resourcename, resourceEPR FROM dataresources WHERE servicekey = ?";
	
	public Collection<EntryType> listResources(Pattern pattern) throws ResourceException {
		
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		
		Collection<EntryType> ret = new ArrayList<EntryType>();
		
		try
		{
				
			stmt = _connection.prepareStatement(_LIST_DATARESOURCES_STMT);
			stmt.setString(1, _resourceKey);
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				String entryName = rs.getString(1);
				EndpointReferenceType resourceEndpoint =
					EPRUtils.fromBlob(rs.getBlob(2));
					ret.add(new EntryType(entryName,
						new MessageElement[0], resourceEndpoint));
			}
			
			return ret;
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	
	public Collection<String> remove(Pattern pattern) throws ResourceException {
		_logger.warn("Method remove(Pattern pattern) in not implemented");
		return null;
	}
	
	public void configureResource(String resourceName, int numSlots) throws ResourceException {
		_logger.warn("Method configureResource() is not implemented");
		
	}
	
	
	// database connection parameters
	private String DBDriverString = null;
	private String DBConnectString = null;
	private String username = null;
	private String password = null;
	
	
	public void initialize(HashMap<QName, Object> constructionParams)
	throws ResourceException
	{
		DBDriverString = (String) constructionParams.get(IDAIRResource._DB_DRIVER_NAME_PARAM);
		DBConnectString = (String) constructionParams.get(IDAIRResource._CONNECT_STRING_PARAM);
		username = (String) constructionParams.get(IDAIRResource._USERNAME_PARAM);
		password = (String) constructionParams.get(IDAIRResource._PASSWORD_PARAM);
		
		super.initialize(constructionParams);
		
	}
	
	public String getDBDriverString()
	{
		return DBDriverString;
	}
	
	public String getDBConnectString()
	{
		return DBConnectString;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public String getPassword()
	{
		return password;
	}

}
