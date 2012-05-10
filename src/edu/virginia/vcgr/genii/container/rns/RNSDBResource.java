package edu.virginia.vcgr.genii.container.rns;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.ggf.rns.RNSEntryExistsFaultType;
import org.morgan.util.GUID;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorEntry;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class RNSDBResource extends BasicDBResource implements IRNSResource
{
	static private final String _ADD_ENTRY_STATEMENT =
		"INSERT INTO entries VALUES(?, ?, ?, ?, ?)";
	static private final String _SELECT_ENTRIES_STMT =
		"SELECT name FROM entries WHERE resourceid = ?";
	static private final String _SELECT_SINGLETON_ENTRY_STMT =
		"SELECT name FROM entries WHERE resourceid = ? AND name = ?";
	static private final String _RETRIEVE_ONE_STMT =
		"SELECT name, endpoint, id, attrs FROM entries WHERE resourceid = ? AND name = ?";
	static private final String _RETRIEVE_ALL_STMT =
		"SELECT name, endpoint, id, attrs FROM entries WHERE resourceid = ?";
	static private final String _REMOVE_ENTRIES_STMT =
		"DELETE FROM entries WHERE resourceid = ? AND name = ?";
	static private String _RETRIEVE_COUNT_STMT =
		"SELECT COUNT(*) FROM entries WHERE resourceid = ?";
	static private String _RETRIEVE_PART_ENTRY_ONE_STMT =
		"SELECT name, id FROM entries WHERE resourceid = ? AND name = ?";	
	static private String _RETRIEVE_PART_ENTRY_ALL_STMT =
		"SELECT name, id FROM entries WHERE resourceid = ? ";
	static private String _RETRIEVE_ENTRY_FROM_ID =
		"SELECT name, endpoint, id, attrs FROM entries WHERE id = ?";
	
	public RNSDBResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(parentKey, connectionPool);
	}
	
	public void addEntry(InternalEntry entry) throws ResourceException,
			RNSEntryExistsFaultType
	{
		PreparedStatement stmt = null;
		String attrKey = (new GUID()).toString();
		
		try
		{
			stmt = _connection.prepareStatement(_ADD_ENTRY_STATEMENT);
			stmt.setString(1, this._resourceKey);
			stmt.setString(2, entry.getName());
			stmt.setBlob(3, EPRUtils.toBlob(entry.getEntryReference(),
				"entries", "endpoint"));
			stmt.setString(4, attrKey);
			stmt.setBytes(5, ObjectSerializer.anyToBytes(entry.getAttributes()));
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
				fault.setEntryName(entry.getName());
				throw FaultManipulator.fillInFault(fault);
			} else
				throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}

	public Collection<String> listEntries(String name) throws ResourceException
	{
		ArrayList<String> ret = new ArrayList<String>();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			if (name == null)
				stmt = _connection.prepareStatement(_SELECT_ENTRIES_STMT);
			else
			{
				stmt = _connection.prepareStatement(_SELECT_SINGLETON_ENTRY_STMT);
				stmt.setString(2, name);
			}
			stmt.setString(1, _resourceKey);
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				ret.add(rs.getString(1));
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

	public Collection<String> removeEntries(String entryName)
		throws ResourceException
	{
		ArrayList<String> ret = new ArrayList<String>();
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_REMOVE_ENTRIES_STMT);
			stmt.setString(1, _resourceKey);
			stmt.setString(2, entryName);
			
			stmt.executeUpdate();
			ret.add(entryName);
			
			return ret;
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}

	public Collection<InternalEntry> retrieveEntries(String entryName)
			throws ResourceException
	{
		ArrayList<InternalEntry> ret = new ArrayList<InternalEntry>();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		boolean isBatch = true; //batch denotes entryName is null
		
		try
		{
			if (entryName == null)
				stmt = _connection.prepareStatement(_RETRIEVE_ALL_STMT);
			else
			{
				stmt = _connection.prepareStatement(_RETRIEVE_ONE_STMT);
				stmt.setString(2, entryName);
				isBatch = false;
			}
			
			stmt.setString(1, _resourceKey);
			rs = stmt.executeQuery();
			
			if(isBatch)
			{
				while (rs.next())
				{
					InternalEntry entry = new InternalEntry(
						rs.getString(1), EPRUtils.fromBlob(rs.getBlob(2)),
						ObjectDeserializer.anyFromBytes(rs.getBytes(4)), true);
					ret.add(entry);
				}
				
			}
			
			else
			{
				if(rs.next())
				{
					InternalEntry entry = new InternalEntry(
							rs.getString(1), EPRUtils.fromBlob(rs.getBlob(2)),
							ObjectDeserializer.anyFromBytes(rs.getBytes(4)), true);
						ret.add(entry);
				}
				
				else
				{
					InternalEntry entry = new InternalEntry(entryName, null, null, false); //signifying that the entry name does not exist
					ret.add(entry);
				}
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

	//This method gives the number of entries within this RNS resource
	@Override
	public int retrieveOccurrenceCount() throws ResourceException 
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int count = 0;
		
		try
		{
			stmt = _connection.prepareStatement(_RETRIEVE_COUNT_STMT );
			stmt.setString(1, _resourceKey);
			rs = stmt.executeQuery();
			
			if(rs.next())
				count = rs.getInt(1);
			
			return count;
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

	@Override
	public Collection<InMemoryIteratorEntry> retrieveIdOfEntry(String request)
			throws ResourceException 
	{
		ArrayList<InMemoryIteratorEntry> ret = new ArrayList<InMemoryIteratorEntry>();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		boolean isBatch = false;	//isBatch means : null is passed as argument
		
		try
		{
			if(request == null)
			{
				stmt = _connection.prepareStatement(_RETRIEVE_PART_ENTRY_ALL_STMT);
				isBatch = true;
			}
			
			else
			{
				stmt = _connection.prepareStatement(_RETRIEVE_PART_ENTRY_ONE_STMT);
				stmt.setString(2, request);
			}
			
			stmt.setString(1, _resourceKey);
			rs = stmt.executeQuery();
			
			if(isBatch)
			{
				while(rs.next())
				{
					InMemoryIteratorEntry imie = new InMemoryIteratorEntry(rs.getString(1), rs.getString(2), true);	//entry exists
					ret.add(imie);
				}
			}
			
			else
			{
				if(rs.next())
				{
					InMemoryIteratorEntry imie = new InMemoryIteratorEntry(rs.getString(1), rs.getString(2), true);	//entry exists
					ret.add(imie);
				}
				
				else
				{
					InMemoryIteratorEntry imie = new InMemoryIteratorEntry(rs.getString(1), new String(""), false);	//entry does not exist
					ret.add(imie);
				}
				
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

	@Override
	public InternalEntry retrieveInternalEntryFromID(String id)
			throws ResourceException 
	{
		//returns null if the id doesn't exist
		return retrieveByIndex(_connection, id);
	}

	public static InternalEntry retrieveByIndex(Connection connection, String id) throws ResourceException
	{
		//returns null if the id doesn't exist
		InternalEntry ie = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(_RETRIEVE_ENTRY_FROM_ID );
		//	stmt.setString(1, _resourceKey);
			stmt.setString(1,id);
			rs = stmt.executeQuery();
			
			if(rs.next())
				ie = new InternalEntry(rs.getString(1), EPRUtils.fromBlob(rs.getBlob(2)),
						ObjectDeserializer.anyFromBytes(rs.getBytes(4)), true);
			
			return ie;
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
	
	@Override
	public Object getProperty(String propertyName) throws ResourceException {
		if (ELEMENT_COUNT_PROPERTY.equalsIgnoreCase(propertyName)) {
			return retrieveOccurrenceCount();
		}
		return super.getProperty(propertyName);
	}	
}