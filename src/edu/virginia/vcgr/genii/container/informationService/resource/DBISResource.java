/**
 * @author Krasi
 */
package edu.virginia.vcgr.genii.container.informationService.resource;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.EntryType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;


import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.rns.RNSDBResource;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class DBISResource extends RNSDBResource implements IISResource{
	
	static private Log _logger = LogFactory.getLog(DBISResource.class);
	
	static private final String _ADD_BES_ENTRY_STATEMENT =
		"INSERT INTO isbescontainers(servicekey, resourcename, endpoint, serviceEPR, callingcontext) VALUES(?, ?, ?, ?, ?)";
	static private final String _REMOVE_ENTRIES_STMT =
		"DELETE FROM isbescontainers WHERE resourcename = ?";
	

	public DBISResource (
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool,
			IResourceKeyTranslater translater)
		throws SQLException
	{
		super(parentKey, connectionPool, translater);
	}

	
	/*
	 * (non-Javadoc)
	 * @see edu.virginia.vcgr.genii.container.rns.RNSDBResource#addEntry(edu.virginia.vcgr.genii.container.rns.InternalEntry)
	 * this function is not used
	 */
	public void addEntry(InternalEntry entry) throws ResourceException,
	RNSEntryExistsFaultType
	{
		PreparedStatement stmt = null;
		
		
		try
		{
			stmt = _connection.prepareStatement(_ADD_BES_ENTRY_STATEMENT);
			stmt.setBlob(2, EPRUtils.toBlob(entry.getEntryReference()));
			if (stmt.executeUpdate() != 1)
				
				throw new ResourceException("Unable to update resource");
			_connection.commit();
		}
		catch (SQLException sqe)
		{
			if (sqe.getErrorCode() == -104)
			{
				// Uniqueness problem
				RNSEntryExistsFaultType fault = new RNSEntryExistsFaultType();
				fault.setPath(entry.getName());
				throw FaultManipulator.fillInFault(fault);
			} else
				throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		} 
		catch (IOException ioe) {
		_logger.warn(ioe.getLocalizedMessage(), ioe);}
		
		finally
		{
			close(stmt);
		}
	}



	public void addResource( String resourceName, 
			EndpointReferenceType resourceEndpoint, EndpointReferenceType serviceEndpoint, ICallingContext callingContext) 
		throws ResourceException {
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_ADD_BES_ENTRY_STATEMENT);
			stmt.setString(1, this._resourceKey);
			stmt.setString(2, resourceName);
			stmt.setBlob(3, EPRUtils.toBlob(resourceEndpoint));
			stmt.setBlob(4, EPRUtils.toBlob(serviceEndpoint));
			stmt.setBlob(5, DBSerializer.toBlob(callingContext));
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


	static private final String _LIST_RESOURCES_STMT =
		"SELECT resourcename, endpoint FROM isbescontainers WHERE servicekey = ?";
	
	public Collection<EntryType> listResources(String _entryName) throws ResourceException
	{
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		
		Collection<EntryType> ret = new ArrayList<EntryType>();
		
		try
		{
				
			stmt = _connection.prepareStatement(_LIST_RESOURCES_STMT);
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
	

	public void configureResource(String resourceName, int numSlots) throws ResourceException {
		_logger.warn("Method configureResource() is not implemented");
		
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

	public Collection<String> remove(String entryName) throws ResourceException {
		_logger.warn("Method remove(Pattern pattern) in not implemented");
		return null;
	}
	
	
	static private final String _GET_CONTEXT_STMT =
		"SELECT callingcontext FROM isbescontainers WHERE resourcename = ?";
	
	public ICallingContext getContextInformation(String name) throws ResourceException {
		
		ResultSet rs = null;
		ICallingContext ret = null;
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_GET_CONTEXT_STMT);
			stmt.setString(1, name);
			rs =  stmt.executeQuery();
			ret = (ICallingContext) rs.getBlob(1);
			return ret;
		}
		catch (SQLException sqe)
		{
			_logger.warn(sqe.getLocalizedMessage(), sqe);
			throw new ResourceException("Unable to query database.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	
	
	}
}
