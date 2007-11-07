package edu.virginia.vcgr.genii.container.informationService.resource;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.axis.message.MessageElement;
import org.ggf.rns.EntryType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;


import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.rns.RNSDBResource;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class DBISResource extends RNSDBResource implements IISResource{
	
	static private final String _ADD_BES_ENTRY_STATEMENT =
		"INSERT INTO isbescontainers(resourcename, endpoint, callingcontext) VALUES(?, ?, ?)";
	static private final String _REMOVE_ENTRIES_STMT =
		"DELETE FROM isbescontainers WHERE resourcename = ?";
	

	public DBISResource (ResourceKey parentKey, DatabaseConnectionPool connectionPool)
			throws SQLException
	{
		super(parentKey, connectionPool);
	}

	
	
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		finally
		{
			close(stmt);
		}
	}



	public void addResource(String resourceName, 
			EndpointReferenceType resourceEndpoint, ICallingContext callingContext) 
		throws ResourceException {
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_ADD_BES_ENTRY_STATEMENT);
			stmt.setString(1, resourceName);
			stmt.setBlob(2, EPRUtils.toBlob(resourceEndpoint));
			stmt.setBlob(3, DBSerializer.toBlob(callingContext));
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update database.");
			stmt.close();
			stmt = null;
			
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to update database.", sqe);
		} catch (IOException e) {
			throw new ResourceException ("Couldn't update database." , e);
		}
		finally
		{
			StreamUtils.close(stmt);
		
		}			
	}


	static private final String _LIST_RESOURCES_STMT =
		"SELECT resourcename, endpoint FROM isbescontainers";
	
	public Collection<EntryType> listResources(Pattern pattern) throws ResourceException {
		
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		Collection<EntryType> ret = new ArrayList<EntryType>();
		
		try
		{
				
			stmt = _connection.prepareStatement(_LIST_RESOURCES_STMT);
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
			throw new ResourceException("Unable to query database.", sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	

	public void configureResource(String resourceName, int numSlots) throws ResourceException {
		// TODO Auto-generated method stub
		
	}


	public Collection<String> removeEntries(String name)
	throws ResourceException
	{
		//Pattern p = Pattern.compile(name);
		ArrayList<String> ret = new ArrayList<String>();
		PreparedStatement stmt = null;
		//Collection<String> entries = listEntries();
		
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
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			close(stmt);
		}
	}

	public Collection<String> remove(Pattern pattern) throws ResourceException {
		// TODO Auto-generated method stub
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
			throw new ResourceException("Unable to query database.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	
	
	}
}
