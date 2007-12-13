package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import org.apache.axis.types.UnsignedShort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.BESPortType;
import org.ggf.jsdl.JobDefinition_Type;
import org.morgan.util.GUID;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.queue.JobInformationType;
import edu.virginia.vcgr.genii.queue.JobStateEnumerationType;
import edu.virginia.vcgr.genii.queue.ReducedJobInformationType;

public class JobManager implements Closeable
{
	static private Log _logger = LogFactory.getLog(BESManager.class);
	
	static final private long _STATUS_CHECK_FREQUENCY = 1000L * 30;
	static final private short _MAX_RUN_ATTEMPTS = 10;
	
	volatile private boolean _closed = false;
	
	private ThreadPool _outcallThreadPool;
	private QueueDatabase _database;
	private SchedulingEvent _schedulingEvent;
	private JobStatusChecker _statusChecker;
	
	private HashMap<Long, JobData> _jobsByID = new HashMap<Long, JobData>();
	private HashMap<String, JobData> _jobsByTicket = 
		new HashMap<String, JobData>();
	private TreeMap<SortableJobKey, JobData> _queuedJobs = 
		new TreeMap<SortableJobKey, JobData>();
	private HashMap<Long, JobData> _runningJobs =
		new HashMap<Long, JobData>();
	
	public JobManager(
		ThreadPool outcallThreadPool, QueueDatabase database, SchedulingEvent schedulingEvent,
		Connection connection, DatabaseConnectionPool connectionPool) 
		throws SQLException, ResourceException, 
			ConfigurationException, GenesisIISecurityException
	{
		_database = database;
		_schedulingEvent = schedulingEvent;
		_outcallThreadPool = outcallThreadPool;
		
		loadFromDatabase(connection);
		
		_statusChecker = new JobStatusChecker(connectionPool, this, _STATUS_CHECK_FREQUENCY);
	}
	
	protected void finalize() throws Throwable
	{
		super.finalize();
		
		close();
	}
	
	synchronized public void close() throws IOException
	{
		if (_closed)
			return;
		
		_closed = true;
		_statusChecker.close();
	}
	
	synchronized private void loadFromDatabase(Connection connection)
		throws SQLException, ResourceException
	{
		Collection<JobData> jobs = _database.loadAllJobs(connection);
		LinkedList<JobData> starting = new LinkedList<JobData>();
		
		for (JobData job : jobs)
		{
			_jobsByID.put(new Long(job.getJobID()), job);
			_jobsByTicket.put(job.getJobTicket(), job);
			
			QueueStates jobState = job.getJobState();
			if (!jobState.isFinalState())
			{
				if (jobState.equals(QueueStates.QUEUED) || 
					jobState.equals(QueueStates.REQUEUED))
				{
					_queuedJobs.put(new SortableJobKey(
						job.getJobID(), job.getPriority(), 
						job.getSubmitTime()), job);
				} else if (jobState.equals(QueueStates.RUNNING))
				{
					_runningJobs.put(new Long(job.getJobID()), job);
				} else
				{
					// If it isn't final, queued, and it isn't running, then we
					// loaded one marked as STARTING, which we can't resolve 
					// (we don't have an EPR to access the job with -- it's 
					// a leak).  We will fail the job
					starting.add(job);
				}
			}
		}
		
		for (JobData job : starting)
		{
			failJob(connection, job.getJobID(), false);
		}
	}
	
	synchronized public void failJob(Connection connection, 
		long jobID, boolean countAsAnAttempt)
		throws SQLException, ResourceException
	{
		JobData job = _jobsByID.get(new Long(jobID));
		if (job == null)
		{
			// don't know where it went, but it's no longer our responsibility.
			return;
		}
		
		if (countAsAnAttempt)
			job.incrementRunAttempts();
		short attempts = job.getRunAttempts();
		QueueStates newState;
		
		if (attempts >= _MAX_RUN_ATTEMPTS)
		{
			// We can't run this job any more.
			newState = QueueStates.ERROR;
		} else
		{
			newState = QueueStates.REQUEUED;
		}
		

		_database.modifyJobState(connection, jobID,
			attempts, newState, new Date(), null, null, null);
		connection.commit();
		
		_runningJobs.remove(new Long(jobID));
		if (newState.equals(QueueStates.REQUEUED))
		{
			_logger.debug("Re-queing job " + jobID);
			_queuedJobs.put(new SortableJobKey(
				jobID, job.getPriority(), job.getSubmitTime()), job);
		} else
		{
			_logger.debug("Failing job " + jobID);
		}
		
		_schedulingEvent.notifySchedulingEvent();
	}
	
	synchronized public void finishJob(Connection connection, long jobID)
		throws SQLException, ResourceException
	{
		JobData job = _jobsByID.get(new Long(jobID));
		if (job == null)
		{
			// don't know where it went, but it's no longer our responsibility.
			return;
		}
		
		job.incrementRunAttempts();
		_database.modifyJobState(connection, jobID,
			job.getRunAttempts(), QueueStates.FINISHED,
			new Date(), null, null, null);
		connection.commit();
		
		_logger.debug("Finished job " + jobID);
		_runningJobs.remove(new Long(jobID));
		_schedulingEvent.notifySchedulingEvent();
	}
	
	synchronized public String submitJob(Connection connection,
		JobDefinition_Type jsdl, short priority) 
		throws SQLException, ConfigurationException, ResourceException
	{
		try
		{
			String ticket = new GUID().toString();
			ICallingContext callingContext = ContextManager.getCurrentContext(false);
			Collection<Identity> identities = QueueSecurity.getCallerIdentities();
			QueueStates state = QueueStates.QUEUED;
			Date submitTime = new Date();
			
			long jobID = _database.submitJob(
				connection, ticket, priority, jsdl, callingContext, identities, 
				state, submitTime);
			connection.commit();
			
			_logger.debug("Submitted job \"" + ticket + "\" as job number " + jobID);
			
			JobData job = new JobData(
				jobID, ticket, priority, state, submitTime, (short)0);
			_jobsByID.put(new Long(jobID), job);
			_jobsByTicket.put(ticket, job);
			_queuedJobs.put(new SortableJobKey(jobID, priority, submitTime),
				job);
			_schedulingEvent.notifySchedulingEvent();
			
			return ticket;
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Unable to submit job.", ioe);
		}
	}
	
	synchronized public Collection<ReducedJobInformationType> listJobs(
		Connection connection) throws SQLException, ResourceException
	{
		Collection<ReducedJobInformationType> ret = 
			new LinkedList<ReducedJobInformationType>();
		
		HashMap<Long, PartialJobInfo> ownerMap =
			_database.getPartialJobInfos(connection, _jobsByID.keySet());
		
		try
		{
			for (Long jobID : ownerMap.keySet())
			{
				JobData jobData = _jobsByID.get(jobID.longValue());
				PartialJobInfo pji = ownerMap.get(jobID);
				ret.add(new ReducedJobInformationType(
					jobData.getJobTicket(), 
					QueueSecurity.convert(pji.getOwners()),
					JobStateEnumerationType.fromString(
						jobData.getJobState().name())));
			}
			
			return ret;
		}
		catch (IOException ioe)
		{
			throw new ResourceException(
				"Unable to serialize owner information.", ioe);
		}
	}
	
	synchronized public Collection<JobInformationType> getJobStatus(
		Connection connection) throws SQLException, ResourceException
	{
		Collection<JobInformationType> ret = new LinkedList<JobInformationType>();
		HashMap<Long, PartialJobInfo> ownerMap;
		
		ownerMap = _database.getPartialJobInfos(connection, _jobsByID.keySet());
		for (Long jobID : ownerMap.keySet())
		{
			JobData jobData = _jobsByID.get(jobID);
			
			try
			{
				PartialJobInfo pji = ownerMap.get(jobID);
				if (QueueSecurity.isOwner(pji.getOwners()))
				{
					ret.add(new JobInformationType(
						jobData.getJobTicket(),
						QueueSecurity.convert(pji.getOwners()),
						JobStateEnumerationType.fromString(
							jobData.getJobState().name()),
						(byte)jobData.getPriority(),
						QueueUtils.convert(jobData.getSubmitTime()),
						QueueUtils.convert(pji.getStartTime()),
						QueueUtils.convert(pji.getFinishTime()),
						new UnsignedShort(jobData.getRunAttempts())));
				}
			}
			catch (IOException ioe)
			{
				throw new ResourceException(
					"Unable to get job status for job \"" +
					jobData.getJobTicket() + "\".", ioe);
					
			}
		}
		
		return ret;
	}
	
	synchronized public Collection<JobInformationType> getJobStatus(
		Connection connection, String []jobs)
			throws SQLException, ResourceException
	{
		if (jobs == null || jobs.length == 0)
			return getJobStatus(connection);
		
		Collection<JobInformationType> ret = 
			new LinkedList<JobInformationType>();
		
		Collection<Long> jobIDs = new LinkedList<Long>();
		for (String ticket : jobs)
		{
			JobData data = _jobsByTicket.get(ticket);
			if (data == null)
				throw new ResourceException("Job \"" + ticket 
					+ "\" does not exist in queue.");
			
			jobIDs.add(new Long(data.getJobID()));
		}
		
		HashMap<Long, PartialJobInfo> ownerMap =
			_database.getPartialJobInfos(connection, jobIDs);
		
		try
		{
			for (Long jobID : ownerMap.keySet())
			{
				JobData jobData = _jobsByID.get(jobID.longValue());
				PartialJobInfo pji = ownerMap.get(jobID);
				
				if (QueueSecurity.isOwner(pji.getOwners()))
				{
					ret.add(new JobInformationType(
						jobData.getJobTicket(),
						QueueSecurity.convert(pji.getOwners()),
						JobStateEnumerationType.fromString(
							jobData.getJobState().name()),
						(byte)jobData.getPriority(),
						QueueUtils.convert(jobData.getSubmitTime()),
						QueueUtils.convert(pji.getStartTime()),
						QueueUtils.convert(pji.getFinishTime()),
						new UnsignedShort(jobData.getRunAttempts())));
				} else
				{
					throw new GenesisIISecurityException(
						"Not permitted to get status of job \"" 
						+ jobData.getJobTicket() + "\".");
				}
			}
			
			return ret;
		}
		catch (IOException ioe)
		{
			throw new ResourceException(
				"Unable to serialize owner information.", ioe);
		}
	}
	
	synchronized private void completeJobs(Connection connection, 
		Collection<Long> jobsToComplete)
			throws SQLException, ResourceException
	{
		
	}
	
	synchronized public void completeJobs(Connection connection)
		throws SQLException, ResourceException
	{
		// Find all jobs that caller owns AND that are in a final state
		Collection<Long> jobsToComplete = new LinkedList<Long>();
		HashMap<Long, PartialJobInfo> ownerMap;
		
		ownerMap = _database.getPartialJobInfos(connection, _jobsByID.keySet());
		for (Long jobID : ownerMap.keySet())
		{
			JobData jobData = _jobsByID.get(jobID);
			PartialJobInfo pji = ownerMap.get(jobID);
			
			try
			{
				if (jobData.getJobState().isFinalState() && 
					QueueSecurity.isOwner(pji.getOwners()))
				{
					jobsToComplete.add(jobID);
				}
			}
			catch (AuthZSecurityException azse)
			{
				_logger.warn(
					"Security exception caused us not to complete a job.", 
					azse);
			}
		}	
		
		// Go ahead and complete them
		completeJobs(connection, jobsToComplete);
	}
	
	synchronized public void completeJobs(Connection connection, String []jobs)
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		if (jobs == null || jobs.length == 0)
		{
			completeJobs(connection);
			return;
		}
		
		Collection<Long> jobsToComplete = new LinkedList<Long>();
		HashMap<Long, PartialJobInfo> ownerMap;
		
		for (String jobTicket : jobs)
		{
			JobData data = _jobsByTicket.get(jobTicket);
			if (data == null)
				throw new ResourceException("Job \"" + jobTicket 
					+ "\" does not exist.");
			jobsToComplete.add(new Long(data.getJobID()));
		}
		
		ownerMap = _database.getPartialJobInfos(connection, jobsToComplete);
		for (Long jobID : ownerMap.keySet())
		{
			JobData jobData = _jobsByID.get(jobID);
			PartialJobInfo pji = ownerMap.get(jobID);
			
			if (!jobData.getJobState().isFinalState())
				throw new ResourceException("Job \"" + jobData.getJobTicket() 
					+ "\" is not in a final state.");
			
			if (!QueueSecurity.isOwner(pji.getOwners()))
				throw new GenesisIISecurityException(
					"Don't have permissino to complete ob \"" + 
						jobData.getJobTicket() + "\".");
		}	
		
		// Go ahead and complete them
		completeJobs(connection, jobsToComplete);
	}
	
	synchronized public void checkJobStatuses(
		DatabaseConnectionPool connectionPool, Connection connection)
		throws SQLException, ResourceException, ConfigurationException,
			GenesisIISecurityException
	{
		Collection<JobCommunicationInfo> commInfo;
		
		commInfo = _database.loadJobCommunicationInfo(
			connection, _runningJobs.values());
		
		for (JobCommunicationInfo job : commInfo)
		{
			BESPortType clientStub = ClientUtils.createProxy(
				BESPortType.class, job.getBESEndpoint(), 
				job.getCallingContext());
			_outcallThreadPool.enqueue(new JobUpdateWorker(
				this, clientStub, connectionPool, job));
		}
	}
}