package edu.virginia.vcgr.genii.container.queue.resource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.UnsignedShort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.BESPortType;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.rns.EntryType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.queue.QueueConstants;
import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.client.queue.QueueUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.queue.QueueSecurity;
import edu.virginia.vcgr.genii.container.queue.ResourceInfoManager;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.queue.JobInformationType;
import edu.virginia.vcgr.genii.queue.JobStateEnumerationType;
import edu.virginia.vcgr.genii.queue.ReducedJobInformationType;

public class QueueDBResource extends BasicDBResource implements IQueueResource
{
	static private Log _logger = LogFactory.getLog(QueueDBResource.class);
	
	public QueueDBResource(ResourceKey parentKey, 
		DatabaseConnectionPool connectionPool)
			throws SQLException
	{
		super(parentKey, connectionPool);
	}
	
	static private final String _ADD_RESOURCE_TO_QUEUE_RESOURCE_INFO =
		"INSERT INTO queueresourceinfo (resourcename, endpoint, totalslots) " +
			"VALUES (?, ?, 1)";
	static private final String _ADD_RESOURCE_TO_QUEUE_RESOURCES =
		"INSERT INTO queueresources (queueid, resourceid) VALUES (?, IDENTITY_VAL_LOCAL())";
	static private final String _ADD_RESOURCE_TO_DYNAMIC_LIST =
		"INSERT INTO queuedynamicresourceinfo VALUES(IDENTITY_VAL_LOCAL(), ?)";
	static private final String _GET_RESOURCE_IDENTITY =
		"VALUES IDENTITY_VAL_LOCAL()";
	public void addResource(String resourceName, 
		EndpointReferenceType resourceEndpoint) throws ResourceException
	{
		PreparedStatement stmt = null;
		Statement sstmt = null;
		Connection conn = getConnection();
		ResultSet rs = null;
		
		String queueId = getKey().toString();
		
		try
		{
			stmt = conn.prepareStatement(_ADD_RESOURCE_TO_QUEUE_RESOURCE_INFO);
			stmt.setString(1, resourceName);
			stmt.setBlob(2, EPRUtils.toBlob(resourceEndpoint));
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update database.");
			stmt.close();
			stmt = null;
			stmt = conn.prepareStatement(_ADD_RESOURCE_TO_QUEUE_RESOURCES);
			stmt.setString(1, queueId);
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update database.");
			stmt.close();
			stmt = null;
			stmt = conn.prepareStatement(_ADD_RESOURCE_TO_DYNAMIC_LIST);
			stmt.setInt(1, 0);
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update database.");
			
			sstmt = conn.createStatement();
			rs = sstmt.executeQuery(_GET_RESOURCE_IDENTITY);
			if (!rs.next())
				throw new ResourceException("Unable to retrieve identity.");

			ThreadFactory factory = (ThreadFactory)NamedInstances.getRoleBasedInstances(
				).lookup("thread-factory");
			factory.newThread(new UpdateWorker(
				resourceName, 
				rs.getInt(1), 
				resourceEndpoint)).start();
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to update database.", sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			StreamUtils.close(sstmt);
		}
	}
		
	private class UpdateWorker implements Runnable
	{
		private String _resourceName;
		private int _resourceID;
		private EndpointReferenceType _resourceEndpoint;
		
		public UpdateWorker(
				String resourceName, 
				int resourceID, 
				EndpointReferenceType resourceEndpoint)
		{
			_resourceName = resourceName;
			_resourceID = resourceID;
			_resourceEndpoint = resourceEndpoint;
		}
		
		public void run()
		{
			try
			{
				ResourceInfoManager.getManager(getKey().toString()).updateResource(
					_resourceName, 
					_resourceID, 
					_resourceEndpoint, 
					null, 
					(ICallingContext) getProperty(IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME));
			}
			catch (ResourceException re)
			{
				_logger.warn("Error trying to update resource information.", re);
			}
			catch (InterruptedException ie)
			{
			}
		}
	}
	
	static private final String _LIST_RESOURCES_STMT =
		"SELECT qri.resourcename, qri.endpoint, qri.totalslots FROM " +
			"(SELECT resourceid FROM queueresources WHERE queueid = ?) AS qr" +
				" INNER JOIN " +
			"queueresourceinfo AS qri ON qr.resourceid = qri.resourceid";
	public Collection<EntryType> listResources(Pattern pattern) 
		throws ResourceException
	{
		ResultSet rs = null;
		PreparedStatement stmt = null;
		Connection conn = getConnection();
		
		String queueId = getKey().toString();
		Collection<EntryType> ret = new ArrayList<EntryType>();
		
		try
		{
			stmt = conn.prepareStatement(_LIST_RESOURCES_STMT);
			stmt.setString(1, queueId);
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				String entryName = rs.getString(1);
				EndpointReferenceType resourceEndpoint =
					EPRUtils.fromBlob(rs.getBlob(2));
				int totalSlots = rs.getInt(3);
				Matcher matcher = pattern.matcher(entryName);
				if (matcher.matches())
				{
					ret.add(new EntryType(entryName,
						new MessageElement[] {
							new MessageElement(
								QueueConstants.RESOURCE_SLOTS_QNAME, totalSlots)
						}, resourceEndpoint));
				}
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
	
	static final private String _LIST_RESOURCE_IDS =
		"SELECT qri.resourcename, qri.resourceid FROM " +
			"(SELECT resourceid FROM queueresources WHERE queueid = ?) AS qr" +
				" INNER JOIN " +
			"queueresourceinfo AS qri ON qr.resourceid = qri.resourceid";
	public Collection<String> remove(Pattern pattern) throws ResourceException
	{
		Collection<String> ret = new ArrayList<String>();
		Collection<Integer> resourceIds = new ArrayList<Integer>();
		
		Connection conn = getConnection();
		PreparedStatement stmt = null;
		Statement stmt2 = null;
		ResultSet rs = null;
		
		String queueId = getKey().toString();
		
		try
		{
			stmt = conn.prepareStatement(_LIST_RESOURCE_IDS);
			stmt.setString(1, queueId);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				String resourceName = rs.getString(1);
				int resourceId = rs.getInt(2);
				Matcher matcher = pattern.matcher(resourceName);
				if (matcher.matches())
				{
					resourceIds.add(new Integer(resourceId));
					ret.add(resourceName);
				}
			}
			
			stmt2 = conn.createStatement();
			for (Integer resourceId : resourceIds)
			{
				stmt2.addBatch("DELETE FROM queueresources WHERE resourceid = "
					+ resourceId);
				stmt2.addBatch("DELETE FROM queueresourceinfo WHERE resourceid = "
					+ resourceId);
				stmt2.addBatch("DELETE FROM queuedynamicresourceinfo WHERE resourceid = "
					+ resourceId);
			}
			
			stmt2.executeBatch();
			return ret;
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to update database.", sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt2);
			StreamUtils.close(stmt);
		}
	}
	
	static final private String _GET_RESOURCE_STMT =
		"SELECT qri.resourceid FROM " +
		"(SELECT resourceid FROM queueresources WHERE queueid = ?) AS qr" +
			" INNER JOIN " +
		"(SELECT resourceid, totalslots FROM queueresourceinfo WHERE resourcename = ?) " +
			"AS qri ON qr.resourceid = qri.resourceid";
	static final private String _CONFIGURE_RESOURCE_STMT =
		"UPDATE queueresourceinfo SET totalslots = ? WHERE resourceid = ?";
	public void configureResource(String resourceName, int numSlots) throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection conn = getConnection();
		
		String queueId = getKey().toString();
		int resourceId;
		
		try
		{
			stmt = conn.prepareStatement(_GET_RESOURCE_STMT);
			stmt.setString(1, queueId);
			stmt.setString(2, resourceName);

			rs = stmt.executeQuery();
			if (!rs.next())
				throw new ResourceException(
					"Couldn't find resource \"" + resourceName + "\".");
			resourceId = rs.getInt(1);
			stmt.close();
			stmt = null;
			stmt = conn.prepareStatement(_CONFIGURE_RESOURCE_STMT);
			stmt.setInt(1, numSlots);
			stmt.setInt(2, resourceId);
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update database.");
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to update database.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	static final private String _ADD_JOB_INFO_STMT =
		"INSERT INTO queuejobinfo " +
			"(callingcontext, jobticket, jsdl, state, priority, " +
			"submittime, starttime, finishtime, failedattempts) VALUES " +
			"(?, ?, ?, 'QUEUED', ?, CURRENT_TIMESTAMP, NULL, NULL, 0)";
	static final private String _ADD_JOB_STMT =
		"INSERT INTO queuejobs " +
			"(queueid, jobid) VALUES (?, IDENTITY_VAL_LOCAL())";
	static final private String _GET_JOB_ID = "VALUES IDENTITY_VAL_LOCAL()";
	static final private String _ADD_OWNER_STMT =
		"INSERT INTO queuejobowners (jobid, owner) VALUES (?, ?)";
	public void submitJob(ICallingContext callingContext, 
		String jobTicket, JobDefinition_Type jsdl, int priority, 
		Collection<Identity> owners) throws ResourceException
	{
		Connection conn = getConnection();
		PreparedStatement stmt = null;
		Statement sstmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.prepareStatement(_ADD_JOB_INFO_STMT);
			stmt.setBlob(1, DBSerializer.toBlob(callingContext));
			stmt.setString(2, jobTicket);
			byte []data = DBSerializer.xmlSerialize(jsdl);
			if (data.length > 8192)
			{
				_logger.error("Attempting to write " + data.length + 
					" bytes to the database in a 8192 space slot.");
			}
			stmt.setBytes(3, data);
			stmt.setInt(4, priority);
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update database.");
			stmt.close();
			stmt = null;
			
			stmt = conn.prepareStatement(_ADD_JOB_STMT);
			stmt.setString(1, _resourceKey);
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update database.");
			stmt.close();
			stmt = null;
			
			sstmt = conn.createStatement();
			rs = sstmt.executeQuery(_GET_JOB_ID);
			if (!rs.next())
				throw new ResourceException("Unable to get job id from database.");
			int jobID = rs.getInt(1);
			
			stmt = conn.prepareStatement(_ADD_OWNER_STMT);
			for (Identity id : owners)
			{
				stmt.setInt(1, jobID);
				stmt.setBytes(2, DBSerializer.serialize(id));
				stmt.addBatch();
			}
			stmt.executeBatch();
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to update database.", sqe);
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Couldn't update database.", ioe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(sstmt);
			StreamUtils.close(stmt);
		}
	}
	
	static final private String _GET_JOB_INFO_STMT =
		"SELECT a.jobid, a.state, a.priority, " +
			"a.submittime, a.starttime, a.finishtime, a.failedattempts FROM " +
			"(SELECT jobid FROM queuejobs WHERE queueid = ?) AS x INNER JOIN " +
			"(SELECT jobid, state, priority, " +
				"submittime, starttime, finishtime, failedattempts " +
				"FROM queuejobinfo WHERE " +
				"jobticket = ?) AS a ON x.jobid = a.jobid";
	static final private String _GET_OWNERS_STMT =
		"SELECT owner FROM queuejobowners WHERE jobid = ?";
	
	private JobInformationType[] getStatus() throws ResourceException
	{
		ArrayList<JobInformationType> ret = new ArrayList<JobInformationType>();
		PreparedStatement getJobInfoStatement = null;
		PreparedStatement stmt = null;
		PreparedStatement getJobOwners = null;
		ResultSet rs = null;
		Connection conn = getConnection();
		
		try
		{
			stmt = conn.prepareStatement(_LIST_JOBS_STMT);
			getJobOwners = conn.prepareStatement(_GET_OWNERS_STMT);
			getJobInfoStatement = conn.prepareStatement(_GET_JOB_INFO_STMT);
			
			
			stmt.setString(1, _resourceKey);
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				int jobID = rs.getInt(1);
				String ticket = rs.getString(3);
				if (QueueSecurity.isOwner(getJobOwners(getJobOwners, jobID)))
				{
					ret.add(getStatus(
							getJobInfoStatement, getJobOwners, ticket));
				}
			}
			
			return ret.toArray(new JobInformationType[0]);
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Unable to serialize/deserialize owners.", ioe);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ResourceException("Unable to serialize/deserialize owners.", cnfe);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Error querying the database.", sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(getJobInfoStatement);
			StreamUtils.close(getJobOwners);
			StreamUtils.close(stmt);
		}
	}
	
	public JobInformationType[] getStatus(String []jobTickets) throws ResourceException
	{
		if (jobTickets == null || jobTickets.length == 0)
			return getStatus();
		
		JobInformationType []ret = new JobInformationType[jobTickets.length];
		PreparedStatement getJobInfoStatement = null;
		PreparedStatement getOwnersStmt = null;
		Connection conn = getConnection();
		
		try
		{
			getJobInfoStatement = conn.prepareStatement(_GET_JOB_INFO_STMT);
			getOwnersStmt = conn.prepareStatement(_GET_OWNERS_STMT);
			for (int lcv = 0; lcv < jobTickets.length; lcv++)
			{
				ret[lcv] = getStatus(
					getJobInfoStatement, getOwnersStmt, jobTickets[lcv]);
			}
			
			return ret;
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to query the database.");
		}
		finally
		{
			StreamUtils.close(getOwnersStmt);
			StreamUtils.close(getJobInfoStatement);
		}
	}
	
	private JobInformationType getStatus(PreparedStatement getJobInfoStatement,
		PreparedStatement getOwnersStmt, String jobTicket)
			throws SQLException, ResourceException
	{
		int failedAttempts = 0;
		Collection<Identity> owners = new ArrayList<Identity>();
		JobStateEnumerationType jobStatus;
		int priority;
		Calendar submitTime;
		Calendar startTime;
		Calendar finishTime;
		
		ResultSet rs = null;
		
		try
		{
			getJobInfoStatement.setString(1, _resourceKey);
			getJobInfoStatement.setString(2, jobTicket);
			rs = getJobInfoStatement.executeQuery();
			if (!rs.next())
				throw new ResourceException("Unable to find job " + jobTicket);
			
			int jobID = rs.getInt(1);
			jobStatus = JobStateEnumerationType.fromString(rs.getString(2));
			priority = rs.getInt(3);
			submitTime = fromTimestamp(rs.getTimestamp(4));
			startTime = fromTimestamp(rs.getTimestamp(5));
			finishTime = fromTimestamp(rs.getTimestamp(6));
			failedAttempts = rs.getInt(7);

			owners = getJobOwners(getOwnersStmt, jobID);
			if (!QueueSecurity.isOwner(owners))
				throw new ResourceException(
					"Caller doesn't have permission to get status for job " + jobTicket);
			
			return new JobInformationType(jobTicket, 
				QueueUtils.serializeIdentities(owners),
				jobStatus, (byte)priority, submitTime, startTime, finishTime,
				new UnsignedShort(failedAttempts));
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ResourceException("Unable to deserialize owner identity.");
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Unable to serialize owner identities.", ioe);
		}
		finally
		{
			StreamUtils.close(rs);
		}
	}
	
	static private Collection<Identity> getJobOwners(
		PreparedStatement getOwners, int jobID) 
			throws IOException, ResourceException, SQLException, ClassNotFoundException
	{
		Collection<Identity> owners = new ArrayList<Identity>();
		ResultSet rs = null;
		
		try
		{
			getOwners.setInt(1, jobID);
			rs = getOwners.executeQuery();
			
			while (rs.next())
			{
				owners.add((Identity)DBSerializer.deserialize(rs.getBytes(1)));
			}
			
			return owners;
		}
		finally
		{
			StreamUtils.close(rs);
		}
	}
	
	static final private Calendar fromTimestamp(Timestamp ts)
	{
		if (ts == null)
			return null;
		
		Calendar ret = Calendar.getInstance();
		ret.setTime(ts);
		return ret;
	}

	static final private String _LIST_JOBS_STMT =
		"SELECT a.jobid, a.state, a.jobticket  FROM " +
		"(SELECT jobid FROM queuejobs WHERE queueid = ?) AS x INNER JOIN " +
		"queuejobinfo AS a ON x.jobid = a.jobid";
	public ReducedJobInformationType[] listJobs() throws ResourceException
	{
		Collection<ReducedJobInformationType> ret = 
			new ArrayList<ReducedJobInformationType>();
		PreparedStatement stmt = null;
		PreparedStatement getJobOwners = null;
		ResultSet rs = null;
		Connection conn = getConnection();
		
		try
		{
			stmt = conn.prepareStatement(_LIST_JOBS_STMT);
			getJobOwners = conn.prepareStatement(_GET_OWNERS_STMT);
			
			stmt.setString(1, _resourceKey);
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				int jobID = rs.getInt(1);
				JobStateEnumerationType status =
					JobStateEnumerationType.fromString(rs.getString(2));
				String ticket = rs.getString(3);
				
				ret.add(new ReducedJobInformationType(ticket,
					QueueUtils.serializeIdentities(getJobOwners(getJobOwners, jobID)),
					status));
			}
			
			return ret.toArray(new ReducedJobInformationType[0]);
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Unable to serialize/deserialize owners.", ioe);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ResourceException("Unable to serialize/deserialize owners.", cnfe);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Error querying the database.", sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(getJobOwners);
			StreamUtils.close(stmt);
		}
	}

	/**
	 * This is an internal function that makes a bunch of assumptions 
	 * about the pre-conditions.  First of all, it is assumed that the stmt has
	 * already been created.  Further, the job has already been "managed" for
	 * deletion.  All this function does is remove it from the database tables.
	 * 
	 * @param stmt
	 * @param jobID
	 * @throws SQLException
	 */
	static private void complete(Statement stmt, int jobID)
		throws SQLException
	{
		stmt.addBatch("DELETE FROM queuejobs WHERE jobid = " + jobID);
		stmt.addBatch("DELETE FROM queuejobinfo WHERE jobid = " + jobID);
		stmt.addBatch("DELETE FROM queuejobowners WHERE jobid = " + jobID);
	}
	
	static final private String _GET_COMPLETED_JOB_ID_STMT =
		"SELECT a.jobid, b.state FROM " +
			"(SELECT jobid FROM queuejobs WHERE queueid = ?) AS a " +
			"INNER JOIN " +
			"(SELECT jobid, state FROM queuejobinfo " +
				"WHERE jobticket = ?) AS b " +
			"ON a.jobid = b.jobid";
	public void complete(String[] tickets) throws ResourceException
	{
		Connection conn = getConnection();
		PreparedStatement stmt = null;
		PreparedStatement ownersStmt = null;
		ResultSet rs = null;
		Statement deleteStmt = null;
		
		try
		{
			deleteStmt = conn.createStatement();
			stmt = conn.prepareStatement(_GET_COMPLETED_JOB_ID_STMT);
			ownersStmt = conn.prepareStatement(_GET_OWNERS_STMT);
			
			for (String ticket : tickets)
			{
				stmt.setString(1, _resourceKey);
				stmt.setString(2, ticket);
				rs = stmt.executeQuery();
				
				if (!rs.next())
					throw new ResourceException("Job " + ticket + " does not exist.");
			
				int jobID = rs.getInt(1);
				QueueStates state = QueueStates.valueOf(rs.getString(2));
				
				if (!QueueSecurity.isOwner(getJobOwners(ownersStmt, jobID)))
					throw new ResourceException(
						"Caller does not have permission to complete job " + ticket);
				
				if (!state.isFinalState())
					throw new ResourceException(
						"Job " + ticket + " is not in a final state.");
				
				complete(deleteStmt, jobID);
			}
			
			deleteStmt.executeBatch();
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Unable to serialize/deserialize owners.", ioe);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ResourceException("Unable to serialize/deserialize owners.", cnfe);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to query database.", sqe);
		}
		finally
		{
			StreamUtils.close(deleteStmt);
			StreamUtils.close(rs);
			StreamUtils.close(ownersStmt);
			StreamUtils.close(stmt);
		}
	}

	static final private String _GET_ALL_COMPLETED_JOB_IDS_STMT =
		"SELECT a.jobid FROM (SELECT jobid FROM queuejobs WHERE queueid = ?) AS a " +
			"INNER JOIN " +
			"(SELECT jobid FROM queuejobinfo " +
				"WHERE state IN ('FINISHED', 'ERROR')) AS b " +
			"ON a.jobid = b.jobid";
	public void completeAll() throws ResourceException
	{
		Statement deleteStmt = null;
		Connection conn = getConnection();
		PreparedStatement stmt = null;
		PreparedStatement ownersStmt = null;
		ResultSet rs = null;
		
		try
		{
			deleteStmt = conn.createStatement();
			stmt = conn.prepareStatement(_GET_ALL_COMPLETED_JOB_IDS_STMT);
			ownersStmt = conn.prepareStatement(_GET_OWNERS_STMT);
			stmt.setString(1, _resourceKey);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				int jobid = rs.getInt(1);
				Collection<Identity> owners = getJobOwners(ownersStmt, jobid);
				if (QueueSecurity.isOwner(owners))
					complete(deleteStmt, jobid);
			}
			
			deleteStmt.executeBatch();
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Unable to serialize/deserialize owners.", ioe);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ResourceException("Unable to serialize/deserialize owners.", cnfe);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to query database.", sqe);
		}
		finally
		{
			StreamUtils.close(deleteStmt);
			StreamUtils.close(rs);
			StreamUtils.close(ownersStmt);
			StreamUtils.close(stmt);
		}
	}

	static private final String _GET_JOB_ID_STMT =
		"SELECT a.jobid, b.callingcontext FROM " +
			"(SELECT jobid FROM queuejobs WHERE queueid = ?) AS a " +
				"INNER JOIN " +
			"(SELECT jobid, callingcontext " +
				"FROM queuejobinfo WHERE jobticket = ?) AS b " +
			"ON a.jobid = b.jobid";
	static private final String _GET_ACTIVE_JOB_INFO_STMT =
		"SELECT a.jobendpoint, b.endpoint FROM " +
			"(SELECT resourceid, jobendpoint FROM queueactivejobs WHERE jobid = ?) AS a " +
				"INNER JOIN " +
			"queueresourceinfo AS b ON a.resourceid = b.resourceid";
	public void killJobs(String[] tickets) throws ResourceException
	{
		HashMap<Integer, ICallingContext> jobs =
			new HashMap<Integer, ICallingContext>();
		
		PreparedStatement stmt = null;
		PreparedStatement ownersStmt = null;
		Statement sstmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = getConnection().prepareStatement(_GET_JOB_ID_STMT);
			ownersStmt = getConnection().prepareStatement(_GET_OWNERS_STMT);
			
			for (String ticket : tickets)
			{
				stmt.setString(1, _resourceKey);
				stmt.setString(2, ticket);
				rs = stmt.executeQuery();
				
				if (rs.next())
				{
					int jobid = rs.getInt(1);
					
					Collection<Identity> owners = getJobOwners(ownersStmt, jobid);
					if (!QueueSecurity.isOwner(owners))
					{
						throw new ResourceException(
							"Caller does not appear to own job " + jobid);
					}
					
					jobs.put(new Integer(jobid),
						(ICallingContext)DBSerializer.fromBlob(rs.getBlob(2)));
				}
				
				rs.close();
				rs = null;
			}
			
			stmt.close();
			stmt = null;
			stmt = getConnection().prepareStatement(_GET_ACTIVE_JOB_INFO_STMT);
			
			sstmt = getConnection().createStatement();
			
			for (Integer jobid : jobs.keySet())
			{
				stmt.setInt(1, jobid.intValue());
				rs = stmt.executeQuery();
				
				if (rs.next())
				{
					killJob(jobs.get(jobid),
						EPRUtils.fromBlob(rs.getBlob(1)),
						EPRUtils.fromBlob(rs.getBlob(2)));
				}
				
				rs.close();
				rs = null;
				
				sstmt.addBatch("DELETE FROM queuejobs WHERE jobid = " 
					+ jobid.intValue());
				sstmt.addBatch("DELETE FROM queuejobinfo WHERE jobid = "
					+ jobid.intValue());
				sstmt.addBatch("DELETE FROM queuejobowners WHERE jobid = "
					+ jobid.intValue());
				sstmt.addBatch("DELETE FROM queueactivejobs WHERE jobid = "
					+ jobid.intValue());
			}
			
			sstmt.executeBatch();
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Unable to deserialize calling context.", ioe);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ResourceException("Unable to deserialize calling context.", cnfe);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to manipulate database.", sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			StreamUtils.close(sstmt);
			StreamUtils.close(ownersStmt);
		}
	}
	
	static private void killJob(ICallingContext callingContext,
		EndpointReferenceType jobEndpoint,
		EndpointReferenceType besEndpoint)
	{
		if (jobEndpoint == null)
			return;
		
		try
		{
			BESPortType bes = ClientUtils.createProxy(
				BESPortType.class, besEndpoint, callingContext);
			bes.terminateActivities(new EndpointReferenceType[] { jobEndpoint });
		}
		catch (Throwable t)
		{
			_logger.warn("Problem killing supposedly running job.", t);
		}
	}
}
