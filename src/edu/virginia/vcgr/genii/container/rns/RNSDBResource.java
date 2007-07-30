package edu.virginia.vcgr.genii.container.rns;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import org.ggf.rns.RNSEntryExistsFaultType;
import org.morgan.util.GUID;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class RNSDBResource extends BasicDBResource implements IRNSResource
{
	static private final String _ADD_ENTRY_STATEMENT =
		"INSERT INTO entries VALUES(?, ?, ?, ?, ?)";
	static private final String _SELECT_ENTRIES_STMT =
		"SELECT name FROM entries WHERE resourceid = ?";
	static private final String _RETRIEVE_STMT =
		"SELECT name, endpoint, id, attrs FROM entries WHERE resourceid = ?";
	static private final String _REMOVE_ENTRIES_STMT =
		"DELETE FROM entries WHERE resourceid = ? AND name = ?";
	
	public RNSDBResource(ResourceKey parentKey, DatabaseConnectionPool connectionPool)
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
			stmt.setBytes(3, EPRUtils.toBytes(entry.getEntryReference()));
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
				fault.setPath(entry.getName());
				throw FaultManipulator.fillInFault(fault);
			} else
				throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			close(stmt);
		}
	}

	public Collection<String> listEntries() throws ResourceException
	{
		ArrayList<String> ret = new ArrayList<String>();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = _connection.prepareStatement(_SELECT_ENTRIES_STMT);
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
			close(rs);
			close(stmt);
		}
	}

	public Collection<String> removeEntries(String regex)
			throws ResourceException
	{
		Pattern p = Pattern.compile(regex);
		ArrayList<String> ret = new ArrayList<String>();
		PreparedStatement stmt = null;
		Collection<String> entries = listEntries();
		
		try
		{
			stmt = _connection.prepareStatement(_REMOVE_ENTRIES_STMT);
			stmt.setString(1, _resourceKey);
			
			for (String entry : entries)
			{
				if (p.matcher(entry).matches())
				{
					stmt.setString(2, entry);
					stmt.executeUpdate();
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
			close(stmt);
		}
	}

	public Collection<InternalEntry> retrieveEntries(String regex)
			throws ResourceException
	{
		Pattern p = Pattern.compile(regex);
		ArrayList<InternalEntry> ret = new ArrayList<InternalEntry>();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = _connection.prepareStatement(_RETRIEVE_STMT);
			stmt.setString(1, _resourceKey);
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				InternalEntry entry = new InternalEntry(
					rs.getString(1), EPRUtils.fromBytes(rs.getBytes(2)),
					ObjectDeserializer.anyFromBytes(rs.getBytes(4)));
				if (p.matcher(entry.getName()).matches())
					ret.add(entry);
			}
			
			return ret;
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
}