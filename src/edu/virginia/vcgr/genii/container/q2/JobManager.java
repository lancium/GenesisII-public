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
import org.ggf.bes.factory.ActivityDocumentType;
import org.ggf.bes.factory.CreateActivityResponseType;
import org.ggf.bes.factory.CreateActivityType;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.rns.EntryType;
import org.morgan.util.GUID;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

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
	private DatabaseConnectionPool _connectionPool;
	
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
		_connectionPool = connectionPool;
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
		
		if (job.getJobState().equals(QueueStates.RUNNING))
			new JobKiller(jobID).run();

		_database.modifyJobState(connection, jobID,
			attempts, newState, new Date(), null, null, null);
		connection.commit();
		
		_runningJobs.remove(new Long(jobID));
		_queuedJobs.remove(new Long(jobID));
		
		if (newState.equals(QueueStates.REQUEUED))
		{
			_logger.debug("Re-queing job " + jobID);
			_queuedJobs.put(new SortableJobKey(
				jobID, job.getPriority(), job.getSubmitTime()), job);
		} else
		{
			_logger.debug("Failing job " + jobID);
		}
		job.setJobState(newState);
		job.clearBESID();
		
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
		
		if (job.getJobState().equals(QueueStates.RUNNING))
			new JobKiller(jobID).run();
		
		job.incrementRunAttempts();
		_database.modifyJobState(connection, jobID,
			job.getRunAttempts(), QueueStates.FINISHED,
			new Date(), null, null, null);
		connection.commit();
		job.setJobState(QueueStates.FINISHED);
		
		_logger.debug("Finished job " + jobID);
		_queuedJobs.remove(new Long(jobID));
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
		_database.completeJobs(connection, jobsToComplete);
		connection.commit();
		
		for (Long jobID : jobsToComplete)
		{
			JobData data = _jobsByID.remove(jobID);
			if (data != null)
			{
				_jobsByTicket.remove(data.getJobTicket());
				_queuedJobs.remove(new SortableJobKey(
					data));
				_runningJobs.remove(jobID);
			}
		}
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
		for (JobData job : _runningJobs.values())
		{
			JobCommunicationInfo info = new JobCommunicationInfo(
				job.getJobID(), job.getBESID().longValue());
			
			IBESPortTypeResolver portTypeResolver = null;
			IJobEndpointResolver endpointResolver = null;
			Resolver resolver = new Resolver();
			
			portTypeResolver = resolver;
			endpointResolver = resolver;
			
			_outcallThreadPool.enqueue(new JobUpdateWorker(
				this, portTypeResolver, endpointResolver, connectionPool, info));
		}
	}
	
	synchronized public boolean hasQueuedJobs()
	{
		return !_queuedJobs.isEmpty();
	}
	
	synchronized public void recordUsedSlots(HashMap<Long, ResourceSlots> slots)
		throws ResourceException
	{
		for (JobData job : _runningJobs.values())
		{
			Long besID = job.getBESID();
			if (besID == null)
				throw new ResourceException(
					"A job is marked as running which isn't " +
					"assigned to a BES container.");
			
			ResourceSlots rs = slots.get(besID);
			if (rs != null)
			{
				rs.reserveSlot();
				if (rs.slotsAvailable() <= 0)
					slots.remove(besID);
			}
		}
	}
	
	synchronized public Collection<JobData> getQueuedJobs()
	{
		return _queuedJobs.values();
	}
	
	synchronized public void startJobs(Connection connection, 
		Collection<ResourceMatch> matches) 
			throws SQLException, ResourceException
	{
		_database.markStarting(connection, matches);
		connection.commit();
		
		for (ResourceMatch match : matches)
		{
			JobData data = _jobsByID.get(new Long(match.getJobID()));
			_queuedJobs.remove(new SortableJobKey(data));
			data.setBESID(match.getBESID());
			data.setJobState(QueueStates.STARTING);
			_runningJobs.put(new Long(data.getJobID()), data);
			
			_outcallThreadPool.enqueue(new JobLauncher(this, match.getJobID(), 
				match.getBESID()));
		}
	}
	
	private class Resolver implements IBESPortTypeResolver, IJobEndpointResolver
	{
		private BESPortType _portType = null;
		private EndpointReferenceType _endpoint = null;
		
		private void resolve(Connection connection, long jobID) throws Throwable
		{
			JobStatusInformation info = _database.getJobStatusInformation(
				connection, jobID);
			
			_endpoint = info.getJobEndpoint();
			_portType = ClientUtils.createProxy(
				BESPortType.class, info.getBESEndpoint(), 
				info.getCallingContext());
		}
		
		@Override
		public BESPortType createClientStub(Connection connection, long besID)
				throws Throwable
		{
			return _portType;
		}

		@Override
		public EndpointReferenceType getJobEndpoint(Connection connection,
				long jobID) throws Throwable
		{
			resolve(connection, jobID);
			return _endpoint;
		}	
	}
	
	synchronized public void killJobs(Connection connection, String []tickets)
		throws SQLException, ResourceException
	{
		if (tickets == null || tickets.length == 0)
			return;
		
		Collection<Long> jobsToKill = new LinkedList<Long>();
		HashMap<Long, PartialJobInfo> ownerMap;
		
		for (String jobTicket : tickets)
		{
			JobData data = _jobsByTicket.get(jobTicket);
			if (data == null)
				throw new ResourceException("Job \"" + jobTicket 
					+ "\" does not exist.");
			jobsToKill.add(new Long(data.getJobID()));
		}
		
		ownerMap = _database.getPartialJobInfos(connection, jobsToKill);
		for (Long jobID : ownerMap.keySet())
		{
			JobData jobData = _jobsByID.get(jobID);
			PartialJobInfo pji = ownerMap.get(jobID);
			
			try
			{
				if (!QueueSecurity.isOwner(pji.getOwners()))
					throw new GenesisIISecurityException(
						"Don't have permissino to kill job \"" + 
							jobData.getJobTicket() + "\".");
			}
			catch (GenesisIISecurityException gse)
			{
				_logger.warn("Error tryint to determine job ownership.", gse);
				continue;
			}
			
			if (jobData.getJobState().equals(QueueStates.STARTING))
				jobData.kill();
			else if (jobData.getJobState().equals(QueueStates.RUNNING))
			{
				finishJob(connection, jobID);
			} else if (!jobData.getJobState().isFinalState())
			{
				finishJob(connection, jobID);
			}
		}
	}
	
	private class JobLauncher implements Runnable
	{
		private long _jobID;
		private long _besID;
		private JobManager _manager;
		
		public JobLauncher(JobManager manager, long jobID, long besID)
		{
			_manager = manager;
			_jobID = jobID;
			_besID = besID;
		}
		
		public void run()
		{
			Connection connection = null;
			EntryType entryType;
			HashMap<Long, EntryType> entries = new HashMap<Long, EntryType>();
			
			try
			{
				connection = _connectionPool.acquire();
				JobStartInformation startInfo = _database.getStartInformation(
					connection, _jobID);
				entries.put(new Long(_besID), entryType = new EntryType());
				_database.fillInBESEPRs(connection, entries);
				
				synchronized(_manager)
				{
					JobData data = _jobsByID.get(new Long(_jobID));
					if (data == null)
					{
						_logger.warn("Job " + _jobID + 
							" dissappeared before it could be started.");
						return;
					}
					
					if (data.killed())
					{
						finishJob(connection, _jobID);
						return;
					}
				}
				
				BESPortType bes = ClientUtils.createProxy(BESPortType.class, 
					entryType.getEntry_reference(), 
					startInfo.getCallingContext());
				CreateActivityResponseType resp = bes.createActivity(
					new CreateActivityType(
						new ActivityDocumentType(startInfo.getJSDL(), null)));
				
				synchronized(_manager)
				{
					_database.markRunning(connection, _jobID, 
						resp.getActivityIdentifier());
					connection.commit();
					
					JobData data = _jobsByID.get(new Long(_jobID));
					data.setJobState(
						QueueStates.RUNNING);
					if (data.killed())
						finishJob(connection, _jobID);
				}
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to start job " + _jobID, cause);
				try 
				{
					failJob(connection, _jobID, true); 
				}
				catch (Throwable cause2)
				{
					_logger.error("Unable to fail a job.", cause2);
				}
			}
			finally
			{
				_connectionPool.release(connection);
			}
		}
	}
	
	private class JobKiller implements Runnable
	{
		private long _jobID;
		
		public JobKiller(long jobID)
		{
			_jobID = jobID;
		}
		
		public void run()
		{
			Connection connection = null;
			
			try
			{
				connection = _connectionPool.acquire();
				KillInformation killInfo = _database.getKillInfo(
					connection, _jobID);
				
				try
				{
					BESPortType bes = ClientUtils.createProxy(BESPortType.class, 
						killInfo.getBESEndpoint(), 
						killInfo.getCallingContext());
					bes.terminateActivities(
						new EndpointReferenceType[] {
							killInfo.getJobEndpoint()
						} );
				}
				catch (Throwable cause)
				{
					_logger.warn("Exception occurred while killing an activity.");
				}
			}
			catch (Throwable cause)
			{
				_logger.error("Error killing job " + _jobID);
			}
			finally
			{
				_connectionPool.release(connection);
			}
		}
	}
}