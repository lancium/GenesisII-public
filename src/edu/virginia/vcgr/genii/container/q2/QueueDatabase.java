package edu.virginia.vcgr.genii.container.q2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.rns.EntryType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.queue.JobStateEnumerationType;
import edu.virginia.vcgr.genii.queue.ReducedJobInformationType;

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
	
	public ICallingContext getQueueCallingContext(Connection connection)
		throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT propvalue FROM properties " +
				"WHERE resourceid = ? AND propname = ?");
			stmt.setString(1, _queueID);
			stmt.setString(2, IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME);
			
			rs = stmt.executeQuery();
			
			if (rs.next())
				return (ICallingContext)DBSerializer.fromBlob(rs.getBlob(1));
			
			return new CallingContextImpl((CallingContextImpl)null);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ResourceException(
				"Unable to read calling context from database.", cnfe);
		}
		catch (IOException ioe)
		{
			throw new ResourceException(
				"Unable to read calling context from database.", ioe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	public Collection<JobData> loadAllJobs(Connection connection)
		throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		Collection<JobData> allJobs = new LinkedList<JobData>();
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT jobid, jobticket, priority, state, submittime, " +
					"runattempts, resourceid FROM q2jobs WHERE queueid = ?");
			stmt.setString(1, _queueID);
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				allJobs.add(new JobData(
					rs.getLong(1), rs.getString(2), rs.getShort(3),
					QueueStates.valueOf(rs.getString(4)),
					new Date(rs.getTimestamp(5).getTime()),
					rs.getShort(6), (Long)rs.getObject(7)));
			}
			
			return allJobs;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	public void modifyJobState(Connection connection, long jobID,
		short attempts, QueueStates newState, Date finishTime,
		EndpointReferenceType jobEndpoint, Long besID, 
		EndpointReferenceType besEndpoint) 
		throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"UPDATE q2jobs SET runattempts = ?, state = ?, " +
					"finishtime = ?, jobendpoint = ?, resourceid = ?, " +
					"resourceendpoint = ? WHERE jobid = ?");
			stmt.setShort(1, attempts);
			stmt.setString(2, newState.name());
			stmt.setTimestamp(3, new Timestamp(finishTime.getTime()));
			stmt.setBlob(4, EPRUtils.toBlob(jobEndpoint));
			stmt.setObject(5, besID);
			stmt.setBlob(6, EPRUtils.toBlob(besEndpoint));
			stmt.setLong(7, jobID);
			
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to update job record.");
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	public long submitJob(
		Connection connection, String ticket, short priority, 
		JobDefinition_Type jsdl, ICallingContext callingContext, 
		Collection<Identity> identities, 
		QueueStates state, Date submitTime) 
		throws SQLException, IOException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"INSERT INTO q2jobs (jobticket, queueid, callingcontext, " +
					"jsdl, owners, priority, state, runattempts, submittime) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?)");
			stmt.setString(1, ticket);
			stmt.setString(2, _queueID);
			stmt.setBlob(3, DBSerializer.toBlob(callingContext));
			stmt.setBlob(4, DBSerializer.xmlToBlob(jsdl));
			stmt.setBlob(5, DBSerializer.toBlob(identities));
			stmt.setShort(6, priority);
			stmt.setString(7, state.name());
			stmt.setTimestamp(8, new Timestamp(submitTime.getTime()));
			
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to add job to the queue database.");
			
			stmt.close();
			stmt = null;
			
			stmt = connection.prepareStatement("values IDENTITY_VAL_LOCAL()");
			rs = stmt.executeQuery();
			
			if (!rs.next())
				throw new SQLException(
					"Unable to determine last added job's ID.");
			return rs.getLong(1);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	@SuppressWarnings("unchecked")
	public ReducedJobInformationType getReducedInformation(
		Connection connection, long jobID) throws SQLException, IOException,
			ClassNotFoundException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT jobticket, owners, state FROM q2jobs WHERE jobid = ?");
			stmt.setLong(1, jobID);
			rs = stmt.executeQuery();
			
			if (!rs.next())
				throw new SQLException("Couldn't find job " + jobID);
			
			return new ReducedJobInformationType(
				rs.getString(1), QueueSecurity.convert(
					(Collection<Identity>)DBSerializer.fromBlob(rs.getBlob(2))),
				JobStateEnumerationType.fromString(rs.getString(3)));
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
}