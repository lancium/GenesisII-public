package edu.virginia.vcgr.genii.container.q2;

import java.io.IOException;
import java.sql.Blob;
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
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.JobDefinition_Type;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryEventToken;
import edu.virginia.vcgr.genii.container.q2.resource.IQueueResource;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.rns.LegacyEntryType;
import edu.virginia.vcgr.genii.security.credentials.identity.Identity;

/**
 * This class is a conduit for accessing all information from the database. It SHOULD be the only
 * class that actually creates and executes SQL statements.
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

	public String getQueueID()
	{
		return _queueID;
	}

	/**
	 * Get a list of all BES containers registered with this queue.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @return The list of BES data (in-memory data) of all bes resources registerd in this queue.
	 * 
	 * @throws SQLException
	 */
	public Collection<BESData> loadAllBESs(Connection connection) throws SQLException
	{
		Collection<BESData> ret = new LinkedList<BESData>();

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT resourceid, resourcename, totalslots "
				+ "FROM q2resources WHERE queueid = ?");
			stmt.setString(1, _queueID);
			rs = stmt.executeQuery();

			while (rs.next()) {
				ret.add(new BESData(rs.getLong(1), rs.getString(2), rs.getInt(3)));
			}

			return ret;
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	/**
	 * Add information about a BES resource into the database.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param name
	 *            The name of the resource in the queue.
	 * @param epr
	 *            The EPR of the resource.
	 * 
	 * @return The database key for the newly added resource.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public long addNewBES(Connection connection, String name, EndpointReferenceType epr) throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("INSERT INTO q2resources "
				+ "(queueid, resourcename, resourceendpoint, totalslots) " + "VALUES (?, ?, ?, 1)");
			stmt.setString(1, _queueID);
			stmt.setString(2, name);
			stmt.setBlob(3, EPRUtils.toBlob(epr, "q2resources", "resourceendpoint"));

			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to add new BES container into database.");

			stmt.close();
			stmt = null;

			stmt = connection.prepareStatement("values IDENTITY_VAL_LOCAL()");
			rs = stmt.executeQuery();

			if (!rs.next())
				throw new SQLException("Unable to determine last added BES container's ID.");
			return rs.getLong(1);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	/**
	 * Modify the slots allocated to a resource in the database.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param id
	 *            The resource's db key.
	 * @param totalSlots
	 *            The number of slots the resource should have.
	 * 
	 * @throws SQLException
	 */
	public void configureResource(Connection connection, long id, int totalSlots) throws SQLException
	{
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement("UPDATE q2resources SET totalslots = ? " + "WHERE resourceid = ?");
			stmt.setInt(1, totalSlots);
			stmt.setLong(2, id);

			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to update resource's slot count.");
		} finally {
			StreamUtils.close(stmt);
		}
	}

	/**
	 * Remove the indicated resources from the database.
	 * 
	 * @param connection
	 *            The database connection.
	 * @param toRemove
	 *            The list of resources to remove from the database.
	 * 
	 * @throws SQLException
	 */
	public void removeBESs(Connection connection, Collection<BESData> toRemove) throws SQLException
	{
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement("DELETE FROM q2resources WHERE resourceid = ?");

			for (BESData data : toRemove) {
				stmt.setLong(1, data.getID());
				stmt.addBatch();
			}

			stmt.executeBatch();
		} finally {
			StreamUtils.close(stmt);
		}
	}

	/**
	 * Given a list of entries (BES resources), fill in the EPRs for those resources.
	 * 
	 * @param connection
	 *            The database connection.
	 * @param entries
	 *            The list of entries to fill in.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public void fillInBESEPRs(Connection connection, HashMap<Long, LegacyEntryType> entries) throws SQLException,
		ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT resourceendpoint FROM q2resources " + "WHERE resourceid = ?");
			String failPlace = null;
			for (Long key : entries.keySet()) {
				LegacyEntryType entry = entries.get(key);

				stmt.setLong(1, key.longValue());
				rs = stmt.executeQuery();
				if (!rs.next()) {
					failPlace = entry.getEntry_name();
					break;
				}

				entry.setEntry_reference(EPRUtils.fromBlob(rs.getBlob(1)));

				StreamUtils.close(rs);
				rs = null;
			}
			if (failPlace != null) {
				throw new SQLException("Unable to locate BES resource \"" + failPlace + "\".");
			}
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	/**
	 * Get the calling context that the queue should use to "ping" resources.
	 * 
	 * @param connection
	 *            The database connection.
	 * 
	 * @return The calling context for the queue.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public ICallingContext getQueueCallingContext(Connection connection) throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT propvalue FROM properties " + "WHERE resourceid = ? AND propname = ?");
			stmt.setString(1, _queueID);
			stmt.setString(2, IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME);

			rs = stmt.executeQuery();

			if (rs.next())
				return (ICallingContext) DBSerializer.fromBlob(rs.getBlob(1));

			return new CallingContextImpl((CallingContextImpl) null);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	/**
	 * Load all jobs from the database for the given queue.
	 * 
	 * @param connection
	 *            The Database connection.
	 * 
	 * @return The list of jobs (and in-memory information) for this queue.
	 * 
	 * @throws SQLException
	 */
	public Collection<JobData> loadAllJobs(Connection connection) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		Collection<JobData> allJobs = new LinkedList<JobData>();

		try {
			stmt = connection.prepareStatement("SELECT a.jobid, a.jobticket, a.priority, a.state, a.submittime, "
				+ "a.runattempts, a.resourceid, b.historytoken, a.callingcontext," + "a.jsdl "
				+ "FROM q2jobs AS a LEFT OUTER JOIN q2jobhistorytokens AS b " + "ON a.jobid = b.jobid WHERE a.queueid = ?");
			stmt.setString(1, _queueID);
			rs = stmt.executeQuery();

			while (rs.next()) {
				long jobid = rs.getLong(1);
				String jobTicket = rs.getString(2);

				JobDefinition_Type jsdl = null;

				try {
					jsdl = DBSerializer.xmlFromBlob(JobDefinition_Type.class, rs.getBlob(10));
				} catch (Throwable cause) {
					_logger.warn("Error getting JSDL for job.", cause);
				}

				JobData data = new JobData(jobid, QueueUtils.getJobName(jsdl), jobTicket, rs.getShort(3),
					QueueStates.valueOf(rs.getString(4)), new Date(rs.getTimestamp(5).getTime()), rs.getShort(6),
					(Long) rs.getObject(7), HistoryContextFactory.createContext(HistoryEventCategory.Default,
						DBSerializer.fromBlob(rs.getBlob(9)), historyKey(jobTicket)));

				Blob blob = rs.getBlob(8);
				if (blob != null) {
					HistoryEventToken token = (HistoryEventToken) DBSerializer.fromBlob(blob);
					if (token != null)
						data.historyToken(token);
				}

				allJobs.add(data);
			}

			return allJobs;
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	/**
	 * MOdify the state of a job to match the new parameters given.
	 * 
	 * @param connection
	 *            The database connection.
	 * @param jobID
	 *            The job's db key.
	 * @param attempts
	 *            The new number of attempts for the job.
	 * @param newState
	 *            The new state for the job.
	 * @param finishTime
	 *            THe new finish time for the job.
	 * @param jobEndpoint
	 *            The new job endpoint for the job.
	 * @param besID
	 *            The bes db key related to this job.
	 * @param besEndpoint
	 *            The endpoint of the bes associated with this job. The reason that we keep this
	 *            endpoint here (even though we probably also have it in the resources table) is
	 *            that it is technically possible for a bes resource to get removed from the queue
	 *            while jobs are still running on it. If that is true, we will need this epr so we
	 *            can later call back into the bes to get the status and kill/complete that job.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public void modifyJobState(Connection connection, long jobID, short attempts, QueueStates newState, Date finishTime,
		EndpointReferenceType jobEndpoint, Long besID, EndpointReferenceType besEndpoint) throws SQLException,
		ResourceException
	{
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement("UPDATE q2jobs SET runattempts = ?, state = ?, "
				+ "finishtime = ?, jobendpoint = ?, resourceid = ?, " + "resourceendpoint = ? WHERE jobid = ?");
			stmt.setShort(1, attempts);
			stmt.setString(2, newState.name());
			stmt.setTimestamp(3, new Timestamp(finishTime.getTime()));
			stmt.setBlob(4, EPRUtils.toBlob(jobEndpoint, "q2jobs", "jobendpoint"));
			if (besID != null)
				stmt.setLong(5, besID.longValue());
			else
				stmt.setNull(5, Types.BIGINT);
			stmt.setBlob(6, EPRUtils.toBlob(besEndpoint, "q2jobs", "resourceendpoint"));
			stmt.setLong(7, jobID);

			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to update job record.");
		} finally {
			StreamUtils.close(stmt);
		}
	}

	public void historyToken(Connection connection, long jobID, HistoryEventToken newToken) throws SQLException
	{
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement("DELETE FROM q2jobhistorytokens WHERE jobid = ?");
			stmt.setLong(1, jobID);
			stmt.executeUpdate();

			stmt.close();
			stmt = null;

			if (newToken != null) {
				stmt = connection.prepareStatement("INSERT INTO " + "q2jobhistorytokens (jobid, queueid, historytoken) "
					+ "VALUES (?, ?, ?)");
				stmt.setLong(1, jobID);
				stmt.setString(2, _queueID);
				stmt.setBlob(3, DBSerializer.toBlob(newToken, "q2jobhistorytokens", "historytoken"));

				stmt.executeUpdate();
			}
		} finally {
			StreamUtils.close(stmt);
		}
	}

	public HistoryEventToken historyToken(Connection connection, long jobID) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT historytoken FROM q2jobhistorytokens WHERE jobid = ?");
			stmt.setLong(1, jobID);
			rs = stmt.executeQuery();

			if (!rs.next())
				return null;

			return (HistoryEventToken) DBSerializer.fromBlob(rs.getBlob(1));
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	/**
	 * Submit a new job into the queue's database.
	 * 
	 * @param connection
	 *            The database connection.
	 * @param ticket
	 *            The new job ticket
	 * @param priority
	 *            The job's priority.
	 * @param jsdl
	 *            The job's JSDL
	 * @param callingContext
	 *            THe calling context to use when making outcalls related to this job.
	 * @param identities
	 *            The owner identities associated with this job.
	 * @param state
	 *            The state of the job.
	 * @param submitTime
	 *            The submit time for the job.
	 * 
	 * @return THe job id assigned by the database for this job.
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public long submitJob(Connection connection, String ticket, short priority, JobDefinition_Type jsdl,
		ICallingContext callingContext, Collection<Identity> identities, QueueStates state, Date submitTime)
		throws SQLException, IOException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("INSERT INTO q2jobs (jobticket, queueid, callingcontext, "
				+ "jsdl, owners, priority, state, runattempts, submittime) " + "VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?)");
			stmt.setString(1, ticket);
			stmt.setString(2, _queueID);
			stmt.setBlob(3, DBSerializer.toBlob(callingContext, "q2jobs", "callingcontext"));
			stmt.setBlob(4, DBSerializer.xmlToBlob(jsdl, "q2jobs", "jsdl"));
			stmt.setBlob(5, DBSerializer.toBlob(identities, "q2jobs", "owners"));
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
				throw new SQLException("Unable to determine last added job's ID.");
			long jobid = rs.getLong(1);

			stmt.close();
			stmt = null;

			stmt = connection.prepareStatement("INSERT INTO q2jobpings (jobid, failedcommattempts) " + "VALUES (?, 0)");
			stmt.setLong(1, jobid);
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to set job communication attempts.");

			return jobid;
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	/**
	 * Get the partial large-memory information for a given list of jobs.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param jobIDs
	 *            The list of job keys to get information for.
	 * 
	 * @return A map of all job id's requested and their information structures.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	@SuppressWarnings("unchecked")
	public HashMap<Long, PartialJobInfo> getPartialJobInfos(Connection connection, Collection<Long> jobIDs)
		throws SQLException, ResourceException
	{
		HashMap<Long, PartialJobInfo> ret = new HashMap<Long, PartialJobInfo>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT owners, starttime, finishtime FROM q2jobs WHERE jobid = ?");

			Long failId = null;
			for (Long jobID : jobIDs) {
				stmt.setLong(1, jobID.longValue());
				rs = stmt.executeQuery();

				if (!rs.next()) {
					failId = jobID;
					break;
				}

				ret.put(
					jobID,
					new PartialJobInfo((Collection<Identity>) DBSerializer.fromBlob(rs.getBlob(1)), rs.getTimestamp(2), rs
						.getTimestamp(3)));

				rs.close();
				rs = null;
			}
			if (failId != null) {
				throw new ResourceException("Unable to find job " + failId + " in queue.");
			}

			return ret;
		} catch (IOException ioe) {
			throw new ResourceException("Unable to deserialize owners for job.", ioe);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	/**
	 * Get all information from the database necessary to outcall to a job to get it's current job
	 * status.
	 * 
	 * @param connection
	 *            The database connection.
	 * @param jobID
	 *            The job DB key.
	 * 
	 * @return THe job status information requested.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public JobStatusInformation getJobStatusInformation(Connection connection, long jobID) throws SQLException,
		ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT callingcontext, jobendpoint, resourceendpoint "
				+ "FROM q2jobs WHERE jobid = ?");
			stmt.setLong(1, jobID);
			rs = stmt.executeQuery();

			if (!rs.next())
				throw new ResourceException("Unable to find job " + jobID + " in database.");

			return new JobStatusInformation(EPRUtils.fromBlob(rs.getBlob(2)), EPRUtils.fromBlob(rs.getBlob(3)),
				(ICallingContext) DBSerializer.fromBlob(rs.getBlob(1)));
		} catch (IOException ioe) {
			throw new ResourceException("Unable to deserialize calling context form database.", ioe);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	/**
	 * Mark a job as starting in the database. This includes setting it's state and noting the bes
	 * container that it's starting on.
	 * 
	 * @param connection
	 *            The database connection.
	 * @param matches
	 *            The list of scheduling matches to note in the database.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public void markStarting(Connection connection, Collection<ResourceMatch> matches) throws SQLException, ResourceException
	{
		PreparedStatement query = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;

		try {
			query = connection.prepareStatement("SELECT resourceendpoint " + "FROM q2resources WHERE resourceid = ?");
			stmt = connection.prepareStatement("UPDATE q2jobs SET state = ?, starttime = ?, "
				+ "resourceid = ?, resourceendpoint = ? WHERE jobid = ?");

			for (ResourceMatch match : matches) {
				query.setLong(1, match.getBESID());
				rs = query.executeQuery();
				if (!rs.next()) {
					// BES doesn't exist any more
					_logger.warn("Tried to schedule a job on a bes container " + "that no longer exists.");
				} else {
					EndpointReferenceType besEPR = EPRUtils.fromBlob(rs.getBlob(1));
					stmt.setString(1, QueueStates.STARTING.name());
					stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
					stmt.setLong(3, match.getBESID());
					stmt.setBlob(4, EPRUtils.toBlob(besEPR, "q2jobs", "resourceendpoint"));
					stmt.setLong(5, match.getJobID());

					stmt.addBatch();
				}

				rs.close();
				rs = null;
			}

			stmt.executeBatch();
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(query);
			StreamUtils.close(stmt);
		}
	}

	public JobDefinition_Type getJSDL(Connection connection, long jobID) throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT jsdl FROM q2jobs WHERE jobid = ?");
			stmt.setLong(1, jobID);

			rs = stmt.executeQuery();
			if (!rs.next())
				throw new ResourceException("Unable to find entry for job " + jobID);

			return DBSerializer.xmlFromBlob(JobDefinition_Type.class, rs.getBlob(1));
		} catch (ClassNotFoundException cnfe) {
			throw new ResourceException("Unable to deserialize calling " + "context and jsdl for job " + jobID, cnfe);
		} catch (IOException ioe) {
			throw new ResourceException("Unable to deserialize calling " + "context and jsdl for job " + jobID, ioe);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	public EndpointReferenceType getLogEPR(Connection connection, long jobID) throws ResourceException, SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT logepr FROM q2logs WHERE jobid = ?");
			stmt.setLong(1, jobID);

			rs = stmt.executeQuery();
			if (!rs.next())
				throw new ResourceException("Unable to find log entry for job " + jobID);

			return EPRUtils.fromBlob(rs.getBlob(1));
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	/**
	 * Get the large memory information from the database necessary to start a new job on a bes
	 * container.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param jobID
	 *            The DB key of the job to get information for.
	 * 
	 * @return The requested job start information.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public JobStartInformation getStartInformation(Connection connection, long jobID) throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT callingcontext, jsdl FROM q2jobs WHERE jobid = ?");
			stmt.setLong(1, jobID);

			rs = stmt.executeQuery();
			if (!rs.next())
				throw new ResourceException("Unable to find entry for job " + jobID);

			return new JobStartInformation((ICallingContext) DBSerializer.fromBlob(rs.getBlob(1)), DBSerializer.xmlFromBlob(
				JobDefinition_Type.class, rs.getBlob(2)));
		} catch (ClassNotFoundException cnfe) {
			throw new ResourceException("Unable to deserialize calling " + "context and jsdl for job " + jobID, cnfe);
		} catch (IOException ioe) {
			throw new ResourceException("Unable to deserialize calling " + "context and jsdl for job " + jobID, ioe);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	/**
	 * Mark in the database information about a job that is now running. This includes things like
	 * the state and the job's activity EPR.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param jobID
	 *            The database key of the job.
	 * @param jobEPR
	 *            The EPR of the job
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public void markRunning(Connection connection, long jobID, EndpointReferenceType jobEPR) throws SQLException,
		ResourceException
	{
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement("UPDATE q2jobs SET state = ?, jobendpoint = ? " + "WHERE jobid = ?");
			stmt.setString(1, QueueStates.RUNNING.name());
			stmt.setBlob(2, EPRUtils.toBlob(jobEPR, "q2jobs", "jobendpoint"));
			stmt.setLong(3, jobID);

			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update database for running job " + jobID);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	/**
	 * Complete the listed jobs (remove them from the database).
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param jobIDs
	 *            The list of job DB keys.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public void completeJobs(Connection connection, Collection<Long> jobIDs) throws SQLException, ResourceException
	{
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
		PreparedStatement stmt3 = null;
		PreparedStatement stmt4 = null;
		PreparedStatement stmt5 = null;
		PreparedStatement stmt6 = null;

		try {
			stmt1 = connection.prepareStatement("DELETE FROM q2jobs WHERE jobid = ?");
			stmt2 = connection.prepareStatement("DELETE FROM q2errors WHERE jobid = ?");
			stmt3 = connection.prepareStatement("DELETE FROM q2jobpings WHERE jobid = ?");
			stmt4 = connection.prepareStatement("DELETE FROM q2logs WHERE jobid = ?");
			stmt5 = connection.prepareStatement("DELETE FROM q2joblogtargets WHERE jobid = ?");
			stmt6 = connection.prepareStatement("DELETE FROM q2jobhistorytokens WHERE jobid = ?");

			for (Long jobID : jobIDs) {
				stmt1.setLong(1, jobID.longValue());
				stmt1.addBatch();

				stmt2.setLong(1, jobID.longValue());
				stmt2.addBatch();

				stmt3.setLong(1, jobID.longValue());
				stmt3.addBatch();

				stmt4.setLong(1, jobID.longValue());
				stmt4.addBatch();

				stmt5.setLong(1, jobID.longValue());
				stmt5.addBatch();

				stmt6.setLong(1, jobID.longValue());
				stmt6.addBatch();
			}

			stmt1.executeBatch();
			stmt2.executeBatch();
			stmt3.executeBatch();
			stmt4.executeBatch();
			stmt5.executeBatch();
			stmt6.executeBatch();
		} finally {
			StreamUtils.close(stmt1);
			StreamUtils.close(stmt2);
			StreamUtils.close(stmt3);
			StreamUtils.close(stmt4);
			StreamUtils.close(stmt5);
			StreamUtils.close(stmt6);
		}
	}

	/**
	 * Get all of the information from the database necessary to call out to a BES container and
	 * kill a job (terminate it).
	 * 
	 * @param connection
	 *            The database connection.
	 * @param jobID
	 *            The db key of the job to kill.
	 * 
	 * @return The kill information requested.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public KillInformation getKillInfo(Connection connection, long jobID, Long besID) throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT callingcontext, jobendpoint, resourceendpoint "
				+ "FROM q2jobs WHERE jobid = ?");
			stmt.setLong(1, jobID);

			rs = stmt.executeQuery();
			if (!rs.next())
				throw new ResourceException("Unable to find job " + jobID + " in database.");

			try {
				EndpointReferenceType bes = EPRUtils.fromBlob(rs.getBlob(3));
				if (bes == null && besID != null) {
					HashMap<Long, LegacyEntryType> entryMap = new HashMap<Long, LegacyEntryType>();
					entryMap.put(besID, new LegacyEntryType());
					fillInBESEPRs(connection, entryMap);
					bes = entryMap.get(besID).getEntry_reference();
				}
				return new KillInformation((ICallingContext) DBSerializer.fromBlob(rs.getBlob(1)), EPRUtils.fromBlob(rs
					.getBlob(2)), bes);
			} catch (IOException ioe) {
				throw new ResourceException("Couldn't deserialize blob from database.", ioe);
			}
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	public EndpointReferenceType getQueueEPR(Connection connection) throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT queueepr FROM q2eprs WHERE queueid = ?");
			stmt.setString(1, _queueID);
			rs = stmt.executeQuery();
			if (rs.next())
				return EPRUtils.fromBlob(rs.getBlob(1));

			return null;
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	public void addError(Connection connection, long jobid, short attempt, Collection<String> errors) throws SQLException
	{
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement("INSERT INTO q2errors (queueid, jobid, attempt, errors) "
				+ "VALUES (?, ?, ?, ?)");
			stmt.setString(1, _queueID);
			stmt.setLong(2, jobid);
			stmt.setShort(3, attempt);
			stmt.setBlob(4, DBSerializer.toBlob(errors, "q2errors", "errors"));
			stmt.executeUpdate();
		} finally {
			StreamUtils.close(stmt);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Collection<String>> getAttemptErrors(Connection connection, long jobid) throws SQLException
	{
		Vector<Collection<String>> ret = new Vector<Collection<String>>(10);
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT attempt, errors FROM q2errors WHERE jobid = ?");
			stmt.setLong(1, jobid);
			rs = stmt.executeQuery();
			while (rs.next()) {
				short attempt = rs.getShort(1);
				Blob blob = rs.getBlob(2);
				Collection<String> errors = (Collection<String>) DBSerializer.fromBlob(blob);
				if (errors != null && errors.size() > 0) {
					if (attempt >= ret.size())
						ret.setSize(attempt + 1);

					Collection<String> tmp = ret.get(attempt);
					if (tmp == null)
						ret.set(attempt, errors);
					else
						tmp.addAll(errors);
				}
			}

			return ret;
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	public void setJobCommunicationAttempts(Connection connection, long jobid, int number) throws SQLException
	{
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement("UPDATE q2jobpings SET failedcommattempts = ? " + "WHERE jobid = ?");
			stmt.setInt(1, number);
			stmt.setLong(2, jobid);
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to set job communication attempt number.");
		} finally {
			StreamUtils.close(stmt);
		}
	}

	public int getJobCommunicationAttempts(Connection connection, long jobid) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT failedcommattempts FROM q2jobpings WHERE jobid = ?");
			stmt.setLong(1, jobid);
			rs = stmt.executeQuery();
			if (!rs.next())
				throw new SQLException("Unable to get job attempt number from database.");
			return rs.getInt(1);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	public void incrementFinishCount(Connection connection) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Long value = null;

		try {
			stmt = connection.prepareStatement("SELECT propvalue FROM properties " + "WHERE resourceid = ? and propname = ?");
			stmt.setString(1, _queueID);
			stmt.setString(2, IQueueResource.TOTAL_COUNT_PROPERTY_NAME);
			rs = stmt.executeQuery();
			if (rs.next())
				value = (Long) DBSerializer.fromBlob(rs.getBlob(1));
			stmt.close();
			stmt = null;
			if (value == null) {
				stmt = connection.prepareStatement("INSERT INTO properties " + "(resourceid, propname, propvalue) "
					+ "VALUES (?, ?, ?)");
				stmt.setString(1, _queueID);
				stmt.setString(2, IQueueResource.TOTAL_COUNT_PROPERTY_NAME);
				stmt.setBlob(3, DBSerializer.toBlob(new Long(1), "properties", "propvalue"));
			} else {
				stmt = connection.prepareStatement("UPDATE properties SET propvalue = ? "
					+ "WHERE resourceid = ? AND propname = ?");
				stmt.setBlob(1, DBSerializer.toBlob(new Long(value.longValue() + 1), "properties", "propvalue"));
				stmt.setString(2, _queueID);
				stmt.setString(3, IQueueResource.TOTAL_COUNT_PROPERTY_NAME);
			}

			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to update total job count.");
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	public long getTotalFinished(Connection connection) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Long value = null;

		try {
			stmt = connection.prepareStatement("SELECT propvalue FROM properties " + "WHERE resourceid = ? and propname = ?");
			stmt.setString(1, _queueID);
			stmt.setString(2, IQueueResource.TOTAL_COUNT_PROPERTY_NAME);
			rs = stmt.executeQuery();
			if (rs.next())
				value = (Long) DBSerializer.fromBlob(rs.getBlob(1));
			else
				value = new Long(0);

			return value.longValue();
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	final public String historyKey(String jobTicket)
	{
		return String.format("q2:%s:%s", _queueID, jobTicket);
	}

	public void setSecurityHeader(Connection connection, long jobId, String certificate) throws SQLException
	{
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement("INSERT INTO security_headers " + "(jobid, certificate) " + "VALUES (?, ?)");

			stmt.setLong(1, jobId);
			stmt.setBlob(2, DBSerializer.toBlob(certificate, "security_headers", "certificate"));

			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to add security headers.");

		} finally {
			StreamUtils.close(stmt);
		}
	}

	public String getSecurityHeader(Connection connection, long jobID) throws SQLException
	{

		ResultSet rs = null;
		java.sql.Statement query = null;
		String header = "";

		try {
			query = connection.createStatement();
			rs = query.executeQuery("SELECT * FROM security_headers where jobid = " + jobID);

			if (rs.next())
				header = (String) DBSerializer.fromBlob(rs.getBlob(2));

			return header;
		} finally {

			StreamUtils.close(rs);
			StreamUtils.close(query);
		}
	}
}
