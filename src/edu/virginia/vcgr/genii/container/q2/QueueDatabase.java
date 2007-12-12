package edu.virginia.vcgr.genii.container.q2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.ggf.rns.EntryType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class QueueDatabase
{
	private String _queueID;
	
	public QueueDatabase(String queueID)
	{
		_queueID = queueID;
	}
	
	public Collection<BESData> loadAllBESs(Connection connection)
		throws SQLException
	{
		Collection<BESData> ret = new LinkedList<BESData>();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT resourceid, resourcename, totalslots " +
				"FROM q2resources WHERE queueid = ?");
			stmt.setString(1, _queueID);
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				ret.add(new BESData(
					rs.getLong(1), rs.getString(2), rs.getInt(3)));
			}
			
			return ret;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	public long addNewBES(Connection connection, String name,
		EndpointReferenceType epr) throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"INSERT INTO q2resources " +
					"(queueid, resourcename, resourceendpoint, totalslots) " +
				"VALUES (?, ?, ?, 1)");
			stmt.setString(1, _queueID);
			stmt.setString(2, name);
			stmt.setBlob(3, EPRUtils.toBlob(epr));
			
			if (stmt.executeUpdate() != 1)
				throw new SQLException(
					"Unable to add new BES container into database.");
			
			stmt.close();
			stmt = null;
			
			stmt = connection.prepareStatement("values IDENTITY_VAL_LOCAL()");
			rs = stmt.executeQuery();
			
			if (!rs.next())
				throw new SQLException(
					"Unable to determine last added BES container's ID.");
			return rs.getLong(1);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
		
	public void configureResource(Connection connection, long id, int totalSlots)
		throws SQLException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = connection.prepareStatement("UPDATE q2resources SET totalslots = ? " +
				"WHERE resourceid = ?");
			stmt.setInt(1, totalSlots);
			stmt.setLong(2, id);
			
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to update resource's slot count.");
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	public void removeBESs(Connection connection, 
		Collection<BESData> toRemove) throws SQLException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"DELETE FROM q2resources WHERE resourceid = ?");
			
			for (BESData data : toRemove)
			{
				stmt.setLong(1, data.getID());
				stmt.addBatch();
			}
			
			stmt.executeBatch();
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	public void fillInBESEPRs(Connection connection, 
		HashMap<Long, EntryType> entries) 
		throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT resourceendpoint FROM q2resources " +
				"WHERE resourceid = ?");
			
			for (Long key : entries.keySet())
			{
				EntryType entry = entries.get(key);
				
				stmt.setLong(1, key.longValue());
				rs = stmt.executeQuery();
				if (!rs.next())
				{
					throw new SQLException("Unable to locate BES resource \"" +
						entry.getEntry_name() + "\".");
				}
				
				entry.setEntry_reference(EPRUtils.fromBlob(rs.getBlob(1)));
				
				StreamUtils.close(rs);
				rs = null;
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
}