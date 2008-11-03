package edu.virginia.vcgr.genii.container.q2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

/**
 * This class is a conduit for accessing all information from the database.
 * It SHOULD be the only class that actually creates and executes SQL 
 * statements.
 * 
 * @author mmm2a
 */
public class QueueDatabase
{
	static private Log _logger = LogFactory.getLog(QueueDatabase.class);
	
	/* The queue's key in the database */
	private String _queueID;
	
	public QueueDatabase(String queueID)
	{
		_queueID = queueID;
	}
	
	/**
	 * Get a list of all BES containers registered with this queue.
	 * 
	 * @param connection The database connection to use.
	 * @return The list of BES data (in-memory data) of all bes resources
	 * registerd in this queue.
	 * 
	 * @throws SQLException
	 */
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
	
	/**
	 * Add information about a BES resource into the database.
	 * 
	 * @param connection The database connection to use.
	 * @param name The name of the resource in the queue.
	 * @param epr The EPR of the resource.
	 * 
	 * @return The database key for the newly added resource.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
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
		
	/**
	 * Modify the slots allocated to a resource in the database.
	 * 
	 * @param connection The database connection to use.
	 * @param id The resource's db key.
	 * @param totalSlots The number of slots the resource should have.
	 * 
	 * @throws SQLException
	 */
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
	
	/**
	 * Remove the indicated resources from the database.
	 * 
	 * @param connection The database connection.
	 * @param toRemove The list of resources to remove from the database.
	 * 
	 * @throws SQLException
	 */
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
	
	/**
	 * Given a list of entries (BES resources), fill in the EPRs for those
	 * resources.
	 * 
	 * @param connection The database connection.
	 * @param entries The list of entries to fill in.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
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
	
	/**
	 * Get the calling context that the queue should use to "ping" resources.
	 * 
	 * @param connection The database connection.
	 * 
	 * @return The calling context for the queue.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
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
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	/**
	 * Load all jobs from the database for the given queue.
	 * 
	 * @param connection The Database connection.
	 * 
	 * @return The list of jobs (and in-memory information) for this queue.
	 * 
	 * @throws SQLException
	 */
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
	
	/**
	 * MOdify the state of a job to match the new parameters given.
	 * 
	 * @param connection The database connection.
	 * @param jobID The job's db key.
	 * @param attempts The new number of attempts for the job.
	 * @param newState The new state for the job.
	 * @param finishTime THe new finish time for the job.
	 * @param jobEndpoint The new job endpoint for the job.
	 * @param besID The bes db key related to this job.
	 * @param besEndpoint The endpoint of the bes associated with this
	 * job.  The reason that we keep this endpoint here (even though we
	 * probably also have it in the resources table) is that it is
	 * technically possible for a bes resource to get removed from the
	 * queue while jobs are still running on it.  If that is true, we will
	 * need this epr so we can later call back into the bes to get the status
	 * and kill/complete that job.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
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
			if (besID != null)
				stmt.setLong(5, besID.longValue());
			else
				stmt.setNull(5, Types.BIGINT);
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
	
	/**
	 * Submit a new job into the queue's database.
	 * 
	 * @param connection The database connection.
	 * @param ticket The new job ticket
	 * @param priority The job's priority.
	 * @param jsdl The job's JSDL
	 * @param callingContext THe calling context to use when making outcalls 
	 * related to this job.
	 * @param identities The owner identities associated with this job.
	 * @param state The state of the job.
	 * @param submitTime The submit time for the job.
	 * 
	 * @return THe job id assigned by the database for this job.
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
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
	
	/**
	 * Get the partial large-memory information for a given list of jobs.
	 * 
	 * @param connection The database connection to use.
	 * @param jobIDs The list of job keys to get information for.
	 * 
	 * @return A map of all job id's requested and their information 
	 * structures.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	@SuppressWarnings("unchecked")
	public HashMap<Long, PartialJobInfo> getPartialJobInfos(
		Connection connection, Collection<Long> jobIDs)
			throws SQLException, ResourceException
	{
		HashMap<Long, PartialJobInfo> ret = 
			new HashMap<Long, PartialJobInfo>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT owners, starttime, finishtime FROM q2jobs WHERE jobid = ?");
			
			for (Long jobID : jobIDs)
			{
				stmt.setLong(1, jobID.longValue());
				rs = stmt.executeQuery();
				
				if (!rs.next())
					throw new ResourceException("Unable to find job " + jobID + " in queue.");
				
				ret.put(jobID, new PartialJobInfo(
					(Collection<Identity>)DBSerializer.fromBlob(rs.getBlob(1)),
					rs.getTimestamp(2), rs.getTimestamp(3)));
				
				rs.close();
				rs = null;
			}
			
			return ret;
		}
		catch (IOException ioe)
		{
			throw new ResourceException(
				"Unable to deserialize owners for job.", ioe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	/**
	 * Get all information from the database necessary to outcall to a job to get
	 * it's current job status.
	 * 
	 * @param connection The database connection.
	 * @param jobID The job DB key.
	 * 
	 * @return THe job status information requested.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public JobStatusInformation getJobStatusInformation(
		Connection connection, long jobID) throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT callingcontext, jobendpoint, resourceendpoint " +
				"FROM q2jobs WHERE jobid = ?");
			stmt.setLong(1, jobID);
			rs = stmt.executeQuery();
			
			if (!rs.next())
				throw new ResourceException("Unable to find job " 
					+ jobID + " in database.");
			
			return new JobStatusInformation(
				EPRUtils.fromBlob(rs.getBlob(2)),
				EPRUtils.fromBlob(rs.getBlob(3)),
				(ICallingContext)DBSerializer.fromBlob(rs.getBlob(1)));
		}
		catch (IOException ioe)
		{
			throw new ResourceException(
				"Unable to deserialize calling context form database.",
				ioe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	/**
	 * Mark a job as starting in the database.  This includes setting it's
	 * state and noting the bes container that it's starting on.
	 * 
	 * @param connection The database connection.
	 * @param matches The list of scheduling matches to note in the database.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public void markStarting(Connection connection, 
		Collection<ResourceMatch> matches)
			throws SQLException, ResourceException
	{
		PreparedStatement query = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		try
		{
			query = connection.prepareStatement("SELECT resourceendpoint " +
				"FROM q2resources WHERE resourceid = ?");
			stmt = connection.prepareStatement(
				"UPDATE q2jobs SET state = ?, starttime = ?, " +
					"resourceid = ?, resourceendpoint = ? WHERE jobid = ?");
			
			for (ResourceMatch match : matches)
			{
				query.setLong(1, match.getBESID());
				rs = query.executeQuery();
				if (!rs.next())
				{
					// BES doesn't exist any more
					_logger.warn(
						"Tried to schedule a job on a bes container " +
						"that no longer exists.");
				} else
				{
					EndpointReferenceType besEPR = EPRUtils.fromBlob(
						rs.getBlob(1));
					stmt.setString(1, QueueStates.STARTING.name());
					stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
					stmt.setLong(3, match.getBESID());
					stmt.setBlob(4, EPRUtils.toBlob(besEPR));
					stmt.setLong(5, match.getJobID());
					
					stmt.addBatch();
				}
				
				rs.close();
				rs = null;
			}
			
			stmt.executeBatch();
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(query);
			StreamUtils.close(stmt);
		}
	}
	
	/**
	 * Get the large memory information from the database necessary to start
	 * a new job on a bes container.
	 * 
	 * @param connection The database connection to use.
	 * @param jobID The DB key of the job to get information for.
	 * 
	 * @return The requested job start information.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public JobStartInformation getStartInformation(
		Connection connection, long jobID)
		throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT callingcontext, jsdl FROM q2jobs WHERE jobid = ?");
			stmt.setLong(1, jobID);
			
			rs = stmt.executeQuery();
			if (!rs.next())
				throw new ResourceException("Unable to find entry for job " + jobID);
			
			return new JobStartInformation(
				(ICallingContext)DBSerializer.fromBlob(rs.getBlob(1)), 
				DBSerializer.xmlFromBlob(
					JobDefinition_Type.class, rs.getBlob(2)));
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ResourceException("Unable to deserialize calling " +
				"context and jsdl for job " + jobID, cnfe);
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Unable to deserialize calling " +
				"context and jsdl for job " + jobID, ioe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	/**
	 * Mark in the database information about a job that is now running.  This
	 * includes things like the state and the job's activity EPR.
	 * 
	 * @param connection The database connection to use.
	 * @param jobID The database key of the job.
	 * @param jobEPR The EPR of the job
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public void markRunning(Connection connection, 
		long jobID, EndpointReferenceType jobEPR) 
			throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"UPDATE q2jobs SET state = ?, jobendpoint = ? " +
				"WHERE jobid = ?");
			stmt.setString(1, QueueStates.RUNNING.name());
			stmt.setBlob(2, EPRUtils.toBlob(jobEPR));
			stmt.setLong(3, jobID);
			
			if (stmt.executeUpdate() != 1)
				throw new ResourceException(
					"Unable to update database for running job " + jobID);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	/**
	 * Complete the listed jobs (remove them from the database).
	 * 
	 * @param connection The database connection to use.
	 * @param jobIDs The list of job DB keys.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public void completeJobs(Connection connection, Collection<Long> jobIDs)
		throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"DELETE FROM q2jobs WHERE jobid = ?");
			
			for (Long jobID : jobIDs)
			{
				stmt.setLong(1, jobID.longValue());
				stmt.addBatch();
			}
			
			stmt.executeBatch();
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	/**
	 * Get all of the information from the database necessary to call out to a
	 * BES container and kill a job (terminate it).
	 * 
	 * @param connection The database connection.
	 * @param jobID The db key of the job to kill.
	 * 
	 * @return The kill information requested.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public KillInformation getKillInfo(Connection connection, long jobID) 
		throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT callingcontext, jobendpoint, resourceendpoint " +
				"FROM q2jobs WHERE jobid = ?");
			stmt.setLong(1, jobID);
			
			rs = stmt.executeQuery();
			if (!rs.next())
				throw new ResourceException("Unable to find job " + jobID 
					+ " in database.");
			
			try
			{
				return new KillInformation(
					(ICallingContext)DBSerializer.fromBlob(rs.getBlob(1)),
					EPRUtils.fromBlob(rs.getBlob(2)),
					EPRUtils.fromBlob(rs.getBlob(3)));
			}
			catch (IOException ioe)
			{
				throw new ResourceException(
					"Couldn't deserialize blob from database.", ioe);
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	public EndpointReferenceType getQueueEPR(Connection connection) 
		throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT queueepr FROM q2eprs WHERE queueid = ?");
			stmt.setString(1, _queueID);
			rs = stmt.executeQuery();
			if (rs.next())
				return EPRUtils.fromBlob(rs.getBlob(1));
			
			return null;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
}