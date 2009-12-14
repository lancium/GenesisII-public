package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.Token;
import org.apache.axis.types.UnsignedShort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityDocumentType;
import org.ggf.bes.factory.CreateActivityResponseType;
import org.ggf.bes.factory.CreateActivityType;
import org.ggf.bes.factory.InvalidRequestMessageFaultType;
import org.ggf.bes.factory.NotAcceptingNewActivitiesFaultType;
import org.ggf.bes.factory.NotAuthorizedFaultType;
import org.ggf.bes.factory.UnsupportedFeatureFaultType;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.rns.EntryType;
import org.morgan.util.GUID;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.bes.BESUtils;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gridlog.GridLogTarget;
import edu.virginia.vcgr.genii.client.gridlog.GridLogUtils;
import edu.virginia.vcgr.genii.client.notification.WellknownTopics;
import edu.virginia.vcgr.genii.client.postlog.JobEvent;
import edu.virginia.vcgr.genii.client.postlog.PostEvent;
import edu.virginia.vcgr.genii.client.postlog.PostTarget;
import edu.virginia.vcgr.genii.client.postlog.PostTargets;
import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.authz.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.credentials.identity.Identity;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.notification.Subscribe;
import edu.virginia.vcgr.genii.common.notification.UserDataType;
import edu.virginia.vcgr.genii.container.cservices.gridlogger.GridLogDevice;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.gridlog.GridLogServiceUtils;
import edu.virginia.vcgr.genii.container.gridlog.GridLogTargetBundle;
import edu.virginia.vcgr.genii.container.q2.summary.SlotSummary;
import edu.virginia.vcgr.genii.queue.JobErrorPacket;
import edu.virginia.vcgr.genii.queue.JobInformationType;
import edu.virginia.vcgr.genii.queue.JobStateEnumerationType;
import edu.virginia.vcgr.genii.queue.ReducedJobInformationType;

/**
 * The Job Manager class is the main class to handle adding/removing/managing
 * jobs in the queue.  It DOES NOT handle scheduling of jobs (as that is
 * really a matching process between information stored in this manager and
 * information stored in the BES manager) though it does help the Scheduler
 * with pieces of that function.
 * 
 * @author mmm2a
 */
public class JobManager implements Closeable
{
	static private Log _logger = LogFactory.getLog(BESManager.class);
	static private GridLogDevice _jobLogger = new GridLogDevice(JobManager.class);
	
	/**
	 * How often we poll a running job (ms) to see if it is completed/failed
	 * or not.  It would be great if we could avoid polling, but BES doesn't
	 * require notification in the spec. so we can't count on it.
	 */
	static final private long _STATUS_CHECK_FREQUENCY = 1000L * 60 * 5;
	
	/** One hour of non-communication */
	static final private long MAX_COMM_ATTEMPTS = 
		(1000L * 60 * 60 * 1) / _STATUS_CHECK_FREQUENCY;
	
	/** 
	 * The maximum number of times that we will allow a job to be started and 
	 * failed before giving up.
	 */
	static final public short MAX_RUN_ATTEMPTS = 10;
	
	volatile private boolean _closed = false;
	
	private ThreadPool _outcallThreadPool;
	private QueueDatabase _database;
	private SchedulingEvent _schedulingEvent;
	private JobStatusChecker _statusChecker;
	private DatabaseConnectionPool _connectionPool;
	private BESManager _besManager;
	
	/**
	 * A map of all jobs in the queue based off of the job's key in 
	 * the database.
	 */
	private HashMap<Long, JobData> _jobsByID = new HashMap<Long, JobData>();
	
	/**
	 * A map of all jobs in the queue based off of the job's human readable
	 * job ticket.
	 */
	private HashMap<String, JobData> _jobsByTicket = 
		new HashMap<String, JobData>();
	
	/**
	 * All jobs in the queue that are waiting to run.  This map is sorted
	 * by priority, then submit time, then job ID.
	 */
	private TreeMap<SortableJobKey, JobData> _queuedJobs = 
		new TreeMap<SortableJobKey, JobData>();
	
	/**
	 * A map of all jobs currently running (or starting) in the queue.
	 */
	private HashMap<Long, JobData> _runningJobs =
		new HashMap<Long, JobData>();
	
	public JobManager(
		ThreadPool outcallThreadPool, QueueDatabase database, SchedulingEvent schedulingEvent,
		BESManager manager, Connection connection, DatabaseConnectionPool connectionPool) 
		throws SQLException, ResourceException, 
			GenesisIISecurityException
	{
		_connectionPool = connectionPool;
		_database = database;
		_schedulingEvent = schedulingEvent;
		_outcallThreadPool = outcallThreadPool;
		_besManager = manager;
		
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
	
	/**
	 * Load all jobs stored in the database into this manager.  This operation
	 * should only be called once at the beginning of each web server start.
	 * 
	 * @param connection The database connection to use.
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized private void loadFromDatabase(Connection connection)
		throws SQLException, ResourceException
	{
		_logger.debug("Starting to reload jobs from the database.");
		
		/* Ask the database for a list of all jobs stored in the database 
		 * for this queue. */
		Collection<JobData> jobs = _database.loadAllJobs(connection);
		
		/* Loop through all jobs indicated by the queue */
		LinkedList<JobData> starting = new LinkedList<JobData>();
		for (JobData job : jobs)
		{
			/* Put the job into the two identity maps (the two that all jobs go
			 * into.
			 */
			_jobsByID.put(new Long(job.getJobID()), job);
			_jobsByTicket.put(job.getJobTicket(), job);
			
			/* Now, depending on the loaded job's state, we will perform some
			 * startup-load operations
			 */
			QueueStates jobState = job.getJobState();
			if (!jobState.isFinalState())
			{
				/**
				 * If the job was in the queue and was listed as queued, or 
				 * re-queued, then we need to put the job back into the
				 * "queued" list.
				 */
				if (jobState.equals(QueueStates.QUEUED) || 
					jobState.equals(QueueStates.REQUEUED))
				{
					_queuedJobs.put(new SortableJobKey(
						job.getJobID(), job.getPriority(), 
						job.getSubmitTime()), job);
				} else if (jobState.equals(QueueStates.RUNNING) || job.getBESID() != null)
				{
					if (!jobState.equals(QueueStates.RUNNING))
					{
						_logger.debug("Found a job not marked as running in the database, but with a bes id.");
						job.setJobState(QueueStates.RUNNING);
					}
					/* Otherwise, if the job was listed as running, we need to put
					 * it into the running list.
					 */
					_runningJobs.put(new Long(job.getJobID()), job);
				} else
				{
					// If it isn't final, queued, and it isn't running, then we
					// loaded one marked as STARTING, which we can't resolve 
					// (we don't have an EPR to access the job with -- it's 
					// a leak).  We will fail the job.  There's a good chance
					// here that the bes container just leaked a job, but there
					// is no way to undo that unfortunately.
					starting.add(job);
				}
			}
		}
		
		_logger.debug("Finished reloading jobs from the database.");
		
		/* Finally, we have to actually fail all of the jobs that were marked as
		 * starting.
		 */
		_logger.debug(String.format("Failing %d jobs that were marked as starting when the " +
			"container went down.", starting.size()));
		for (JobData job : starting)
			failJob(connection, job.getJobID(), false, false, true);
	}
	
	/**
	 * Process a job that has failed for any reason.
	 * 
	 * @param connection The database connection to use.
	 * @param jobID The ID of the job to fail.
	 * @param countAsAnAttempt Indicate whether or not this failure should
	 * cound against the job's maximum attempt count.
	 * @return True if the job was requed, false otherwise.
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized public boolean failJob(Connection connection, 
		long jobID, boolean countAsAnAttempt, boolean isPermanent,
		boolean attemptKill)
		throws SQLException, ResourceException
	{
		Collection<GridLogTarget> logTargets = null;
	
		boolean ret = false;
		/* Find the job's in-memory information */
		JobData job = _jobsByID.get(new Long(jobID));
		if (job == null)
		{
			// don't know where it went, but it's no longer our responsibility.
			return ret;
		}
		
		logTargets = job.gridLogTargets();
		_logger.debug(String.format(
			"Failing job(%s, %s, %s)",
			countAsAnAttempt ? "This failure counts against the job" :
				"This failure does NOT count against the job",
			isPermanent ? "Failure is permanent" : "Failure is NOT permanent",
			attemptKill ? "Attempting to kill the job" :
				"NOT attempting to kill the job"));
		_jobLogger.log(logTargets, String.format(
			"Failing job(%s, %s, %s)",
			countAsAnAttempt ? "This failure counts against the job" :
				"This failure does NOT count against the job",
			isPermanent ? "Failure is permanent" : "Failure is NOT permanent",
			attemptKill ? "Attempting to kill the job" :
				"NOT attempting to kill the job"));
		
		/* Increment his attempt count if this counts against him. */
		if (countAsAnAttempt)
		{
			if (isPermanent)
			{
				job.incrementRunAttempts(
					MAX_RUN_ATTEMPTS - job.getRunAttempts());
			} else
			{
				job.incrementRunAttempts();
			}
			job.setNextValidRunTime(new Date());
		}
		
		short attempts = job.getRunAttempts();
		QueueStates newState;
		
		/* If' he's already been started too many times, we fail him permanently */
		if (attempts >= MAX_RUN_ATTEMPTS)
		{
			// We can't run this job any more.
			newState = QueueStates.ERROR;
		} else
		{
			/* Otherwise, we'll just requeue him */
			newState = QueueStates.REQUEUED;
			ret = true;
		}
		
		_logger.debug(String.format("Job %s's new states is %s",
			job.getJobTicket(), newState));
		
		// This is one of the few times we are going to break our pattern and
		// modify the in memory state before the database.  The reason for this
		// is that we can't afford to forget that the BES container has a job
		// on it that it's managing.  If we do, we will eventually leak memory
		// on that container.
		_runningJobs.remove(new Long(jobID));
		_queuedJobs.remove(new SortableJobKey(job));

		/* In order to fail a job that was running, we need to make an outcall
		 * to this BES container.  This is because, unless we terminate the
		 * activity through the bes container, the container will never garbage
		 * collect the information.  This can lead to a database leak in the
		 * best case, and a memory leak in the worst.
		 * 
		 * To make the outcall, we create a new worker to enqueue onto the
		 * outcall thread queue.  The major complication here is that because
		 * we can't update the database until after the job is killed, we will
		 * need to come back and upate the database inside the outcall thread.
		 * This is why we took it out of the memory structures first.  Now
		 * another thread won't try to do anything with it.  It will be up to
		 * the outcall thread worker to put this guy back on the queued list
		 * (if that's where he's destined for) when the database has been 
		 * updated and the outcall has been made).
		 */
		_outcallThreadPool.enqueue(new JobKiller(job, newState, false, attemptKill, null));
		return ret;
	}
	
	/**
	 * Similar to failing a job, this operation moves the job into a final 
	 * state -- this one being a completed-successfully state.
	 * 
	 * @param connection The database connection to use.
	 * @param jobID The job's ID.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized public void finishJob(long jobID)
		throws SQLException, ResourceException
	{
		Collection<GridLogTarget> logTargets;
		
		/* Find the job in the in-memory maps */
		JobData job = _jobsByID.get(new Long(jobID));
		if (job == null)
		{
			// don't know where it went, but it's no longer our responsibility.
			return;
		}
		
		logTargets = job.gridLogTargets();
		_jobLogger.log(logTargets, "Finishing job.");
		
		_logger.debug("Finished job " + jobID);
		
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(false);
			_database.incrementFinishCount(connection);
			connection.commit();
		}
		catch (SQLException sqe)
		{
			_logger.warn("Unable to update total jobs finished count.", sqe);
		}
		finally
		{
			_connectionPool.release(connection);
		}
		
		// This is one of the few times we are going to break our pattern and
		// modify the in memory state before the database.  The reason for this
		// is that we can't afford to forget that the BES container has a job
		// on it that it's managing.  If we do, we will eventually leak memory
		// on that container.
		_queuedJobs.remove(new SortableJobKey(job));
		_runningJobs.remove(new Long(jobID));
		job.incrementRunAttempts();
		
		/* See failJob for a complete discussion of why we enqueue an outcall
		 * worker at this point -- the reasons are the same.
		 */
		_outcallThreadPool.enqueue(new JobKiller(job, QueueStates.FINISHED, false, true, null));
	}
	
	synchronized public void killJob(Connection connection, long jobID)
		throws SQLException, ResourceException
	{
		/* Find the job in the in-memory maps */
		JobData job = _jobsByID.get(new Long(jobID));
		if (job == null)
		{
			// don't know where it went, but it's no longer our responsibility.
			return;
		}
		
		_jobLogger.log(job.gridLogTargets(), "Killing running job.");
		_logger.debug("Killing a running job:" + jobID);
		
		// This is one of the few times we are going to break our pattern and
		// modify the in memory state before the database.  The reason for this
		// is that we can't afford to forget that the BES container has a job
		// on it that it's managing.  If we do, we will eventually leak memory
		// on that container.
		_queuedJobs.remove(new SortableJobKey(job));
		_runningJobs.remove(new Long(jobID));
		job.incrementRunAttempts();
		
		Long besID = job.getBESID();
		/* This shouldn't be necessary */
		/*
		_database.modifyJobState(connection, job.getJobID(),
			job.getRunAttempts(), QueueStates.FINISHED, new Date(), null, null, null);
		connection.commit();
		*/
		
		/* Finally, note the new state in memory and clear the 
		 * old BES information. */
		job.setJobState(QueueStates.FINISHED);
		job.clearBESID();
		
		/* Otherwise, we assume that he's already in 
		 * the right list */
		_logger.debug("Moving job \"" + job.getJobTicket()
			+ "\" to the " + QueueStates.FINISHED + " state.");
	
		_outcallThreadPool.enqueue(new JobKiller(job, QueueStates.FINISHED, true, true, besID));
		
		_schedulingEvent.notifySchedulingEvent();
	}
	
	/**
	 * Submit a new job into the queue.
	 * 
	 * @param connection The database connection to use.
	 * @param jsdl The job's job description.
	 * @param priority The job's priority.
	 * 
	 * @return The job ticket assigned to this job.
	 * 
	 * @throws SQLException
	 * @throws ConfigurationException
	 * @throws ResourceException
	 */
	synchronized public String submitJob(Connection connection,
		JobDefinition_Type jsdl, short priority) 
		throws SQLException, ResourceException
	{
		GridLogTargetBundle bundle = null;
		
		try
		{
			/* First, generate a new ticket for the job.  If we were being 
			 * paranoid, we'd check to see if the ticket already exists, but the
			 * chances of that are astronomically slim (a fact that we count on
			 * for generating EPIs)
			 */
			String ticket = new GUID().toString();
			
			/* Go ahead and get the current caller's calling context.  We store this
			 * so that we can make outcalls in the future on his/her behalf.
			 */
			ICallingContext callingContext = ContextManager.getCurrentContext(false);
			
			Collection<GridLogTarget> gridLogTargets = null;
			bundle = GridLogServiceUtils.createLog();
			if (bundle != null)
			{
				GridLogUtils.addTarget(callingContext, bundle.target());
				gridLogTargets = GridLogUtils.getTargets(callingContext);
			}
			
			if (gridLogTargets == null)
				gridLogTargets = new Vector<GridLogTarget>();
			
			/* Similarly, get the current caller's security identity so that 
			 * we can store that.  This is used to protect different users 
			 * of the queue from each other.  We use this to check against 
			 * others killing, getting the status of, or completing someone 
			 * elses jobs.
			 */
			Collection<Identity> identities = QueueSecurity.getCallerIdentities(true);
			
			if (identities.size() <= 0)
				throw new ResourceException(
					"Cannot submit a job with no non-group credentials.");
			
			/* The job starts out in the queued state and with the current 
			 * time as it's submit time. */
			QueueStates state = QueueStates.QUEUED;
			Date submitTime = new Date();
			
			/* Submit the job information into the queue (and get a new 
			 * jobID from the database for it). */
			long jobID = _database.submitJob(
				connection, ticket, priority, jsdl, callingContext, identities, 
				state, submitTime, bundle, gridLogTargets);
			connection.commit();
			
			bundle = null;
			
			PostTargets.poster().post(
				JobEvent.jobSubmitted(callingContext, Long.toString(jobID)));
			
			/* The data has been committed into the database so we can reload
			 * to this point from here on out.
			 */
			
			_logger.debug("Submitted job \"" + ticket + "\" as job number " + jobID);
			
			/* Create a new data structure for the job's in memory information and
			 * put it into the in-memory lists.
			 */
			JobData job = new JobData(
				jobID, ticket, priority, state, submitTime, (short)0, gridLogTargets);
			_jobsByID.put(new Long(jobID), job);
			_jobsByTicket.put(ticket, job);
			_queuedJobs.put(new SortableJobKey(jobID, priority, submitTime),
				job);
			
			/* We've just added a new job to the queue, so we have a new scheduling
			 * opportunity.
			 */
			_schedulingEvent.notifySchedulingEvent();
			
			return ticket;
		}
		catch (IOException ioe)
		{
			if (bundle != null)
			{
				try
				{
					ClientUtils.createProxy(GeniiCommon.class, bundle.epr()).destroy(new Destroy());
				}
				catch (Throwable cause)
				{
					_logger.error(
						"Error trying to destroy useless grid log info.", cause);
				}
			}
			
			throw new ResourceException("Unable to submit job.", ioe);
		}
	}
	
	/**
	 * List all jobs currently in the queue.  This operation is considered
	 * "safe" from a security point of view and is not subject to verifying
	 * caller's identity (beyond basic ACL on the service operation itself).
	 * 
	 * @param connection The database connection to use.
	 * 
	 * @return The list of all jobs contained in the queue.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized public Collection<ReducedJobInformationType> listJobs(
		Connection connection, String ticket) throws SQLException, ResourceException
	{
		Collection<ReducedJobInformationType> ret = 
			new LinkedList<ReducedJobInformationType>();

		/* We have to ask the database for some information about the
		 * jobs that isn't kept in memory (the owner identities for
		 * example).
		 */
		Set<Long> keySet = null;
		if (ticket != null)
		{
			keySet = new HashSet<Long>();
			JobData data = _jobsByTicket.get(ticket);
			if (data != null)
				keySet.add(new Long(data.getJobID()));
		} else
			keySet = _jobsByID.keySet();
		
		HashMap<Long, PartialJobInfo> ownerMap =
			_database.getPartialJobInfos(connection, keySet);
		
		try
		{
			/* For each job in the queue... */
			for (Long jobID : ownerMap.keySet())
			{
				/* Get the in-memory data for the job */
				JobData jobData = _jobsByID.get(jobID.longValue());
				
				/* Find the corresponding database information */
				PartialJobInfo pji = ownerMap.get(jobID);
				
				/* And add the sum of that too the return list */
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
	
	synchronized public JobDefinition_Type getJSDL(long jobID) 
		throws SQLException, ResourceException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			return _database.getJSDL(connection, jobID);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	synchronized public EndpointReferenceType getLogEPR(String jobTicket)
		throws ResourceException, SQLException
	{
		return getLogEPR(_jobsByTicket.get(jobTicket).getJobID());
	}
	
	synchronized public EndpointReferenceType getLogEPR(long jobID) 
		throws ResourceException, SQLException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			return _database.getLogEPR(connection, jobID);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	synchronized public void printLog(long jobID, PrintStream out)
		throws IOException
	{
		InputStream in = null;
		
		try
		{
			EndpointReferenceType epr = getLogEPR(jobID);
			in = ByteIOStreamFactory.createInputStream(epr);
			StreamUtils.copyStream(in, out);
		}
		catch (SQLException e)
		{
			throw new IOException("Unable to print log for job.", e);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	synchronized public JobDefinition_Type getJSDL(String ticket)
		throws ResourceException, SQLException
	{
		return getJSDL(_jobsByTicket.get(ticket).getJobID());
	}
	
	synchronized public void printLog(String ticket, PrintStream out)
		throws IOException
	{
		printLog(_jobsByTicket.get(ticket).getJobID(), out);
	}
	
	/**
	 * Get the job status for jobs in the queue.  This operation IS subject to
	 * owner verification.  This means that only the owner of a job is allowed
	 * to get the status of his or her job.  This particular operation doesn't
	 * take a list of jobs to get the status of so it's assumed that the user
	 * wants status on ALL jobs that HE/SHE owns.
	 * 
	 * @param connection The database connection to use.
	 * 
	 * @return The list of statuses for all jobs owned by the caller.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized public Collection<JobInformationType> getJobStatus(
		Connection connection) throws SQLException, ResourceException
	{
		Collection<JobInformationType> ret = new LinkedList<JobInformationType>();
		HashMap<Long, PartialJobInfo> ownerMap;
	
		/* We need to get a few pieces of information that only the database
		 * has, such as job owner.
		 */
		ownerMap = _database.getPartialJobInfos(connection, 
			_jobsByID.keySet());
		
		/* Look through all the jobs managed by this queue looking for the
		 * right ones.
		 */
		for (Long jobID : ownerMap.keySet())
		{
			/* Get the in-memory information for this job */
			String scheduledOn = null;
			JobData jobData = _jobsByID.get(jobID);
			BESData besData = _besManager.findBES(jobData.getBESID());
			if (besData != null)
				scheduledOn = besData.getName();
			
			try
			{
				/* Get the database information for this job */
				PartialJobInfo pji = ownerMap.get(jobID);
				
				/* If the caller owns this jobs, then add the status 
				 * information for the job to the result list. */
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
						new UnsignedShort(jobData.getRunAttempts()),
						scheduledOn));
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
	
	/**
	 * Get the job status for jobs in the queue.  This operation IS subject to
	 * owner verification.  This means that only the owner of a job is allowed
	 * to get the status of his or her job.  This particular operation takes a
	 * list of jobs to get status for.  If the list is empty or null, then we
	 * get the status for all jobs owned by the caller.  If the list is not
	 * empty, then we get status for all jobs listed unless one of them is not
	 * owned by the caller, in which case we'll throw an exception indicating
	 * that the caller doesn't have permission to get the status of one of the
	 * requested jobs.
	 * 
	 * @param connection The database connection to use.
	 * @param jobs The list of jobs (by ticket) to get the status for.
	 * 
	 * @return The list of job statuses requested.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized public Collection<JobInformationType> getJobStatus(
		Connection connection, String []jobs)
			throws SQLException, ResourceException, GenesisIISecurityException
	{
		/* Check to see if any tickets were passed and call the
		 * "all jobs owned by me" version if none were.
		 */
		if (jobs == null || jobs.length == 0)
			return getJobStatus(connection);
		
		Collection<JobInformationType> ret = 
			new LinkedList<JobInformationType>();
		
		/* First, get the job IDs of all jobs requested by the
		 * user.
		 */
		Collection<Long> jobIDs = new LinkedList<Long>();
		for (String ticket : jobs)
		{
			JobData data = _jobsByTicket.get(ticket);
			if (data == null)
				throw new ResourceException("Job \"" + ticket 
					+ "\" does not exist in queue.");
			
			jobIDs.add(new Long(data.getJobID()));
		}
		
		/* Now ask the database to fill in information about these
		 * jobs that we don't have in memory (like owner).
		 */
		HashMap<Long, PartialJobInfo> ownerMap =
			_database.getPartialJobInfos(connection, jobIDs);
		
		try
		{
			/* Loop through the jobs checking to make sure that they are
			 * all owned by the caller.
			 */
			for (Long jobID : ownerMap.keySet())
			{
				JobData jobData = _jobsByID.get(jobID.longValue());
				BESData besData = _besManager.findBES(jobData.getBESID());
				String scheduledOn = null;
				if (besData != null)
					scheduledOn = besData.getName();
				
				PartialJobInfo pji = ownerMap.get(jobID);
				
				/* If the job is owned by the caller, add the job's
				 * status information to the result list.
				 */
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
						new UnsignedShort(jobData.getRunAttempts()),
						scheduledOn));
				} else
				{
					/* If the caller did not own a job, then we throw a
					 * security exception.
					 */
					throw new GenesisIISecurityException(
						"Not permitted to get status of job \"" 
						+ jobData.getJobTicket() + "\".");
				}
			}
			
			return ret;
		}
		catch (GenesisIISecurityException gse)
		{
			throw gse;
		}
		catch (IOException ioe)
		{
			throw new ResourceException(
				"Unable to serialize owner information.", ioe);
		}
	}
	
	synchronized public JobErrorPacket[] queryErrorInformation(
		Connection connection, String job)
			throws SQLException, ResourceException, GenesisIISecurityException
	{
		JobData jobData = _jobsByTicket.get(job);
		if (jobData == null)
			throw new ResourceException(String.format(
				"Unable to find job %s in queue.", job));
		
		/* Now ask the database to fill in information about these
		 * jobs that we don't have in memory (like owner).
		 */
		Collection<Long> jobList = new ArrayList<Long>(1);
		jobList.add(jobData.getJobID());
		HashMap<Long, PartialJobInfo> ownerMap =
			_database.getPartialJobInfos(connection, jobList);
		
		PartialJobInfo pji = ownerMap.get(jobData.getJobID());
		
		/* If the job is owned by the caller, add the job's
		 * status information to the result list.
		 */
		if (!QueueSecurity.isOwner(pji.getOwners()))
		{
			/* If the caller did not own a job, then we throw a
			 * security exception.
			 */
			throw new GenesisIISecurityException(
				"Not permitted to get status of job \"" 
				+ jobData.getJobTicket() + "\".");
		}
		
		List<Collection<String>> errors = _database.getAttemptErrors(
			connection, jobData.getJobID());
		
		Collection<JobErrorPacket> packets =
			new LinkedList<JobErrorPacket>();
		
		for (int lcv = 0; lcv < errors.size(); lcv++)
		{
			Collection<String> strings = errors.get(lcv);
			if (strings != null)
			{
				packets.add(new JobErrorPacket(new UnsignedShort(lcv),
					strings.toArray(new String[0])));
			}
		}
		
		return packets.toArray(new JobErrorPacket[0]);
	}
	
	/**
	 * This method completes (or removes from the queue) all jobs which are
	 * owned by the caller and which are in a final state.  Only jobs in a
	 * final state can be completed.  To try and complete a job which is not
	 * in a final state is considered an error and an exception will be thrown.
	 * This operation is only called internally and it is assumed that the
	 * calling method has already checked the ownership and the final state.
	 * 
	 * @param connection The database connection to use.
	 * @param jobsToComplete The list of jobs to complete.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized private void completeJobs(Connection connection, 
		Collection<Long> jobsToComplete)
			throws SQLException, ResourceException
	{
		/* First, remove the job from the database and commit the changes. */
		_database.completeJobs(connection, jobsToComplete);
		connection.commit();
		
		PostTarget pt = PostTargets.poster();
		
		/* Now that it's committed to the database, go through the in memory
		 * data structures and remove the job from all of those.
		 */
		for (Long jobID : jobsToComplete)
		{
			/* Get and remove the job information 
			 * (which has the ticket needed to remove the job from the 
			 * "byTickets" map).
			 */
			JobData data = _jobsByID.remove(jobID);
			if (data != null)
			{
				_jobLogger.log(data.gridLogTargets(),
					"Completing job.");
				/* Remove the job from all lists */
				_jobsByTicket.remove(data.getJobTicket());
				_queuedJobs.remove(new SortableJobKey(
					data));
				_runningJobs.remove(jobID);
				
				pt.post(JobEvent.jobCompleted(null, Long.toString(jobID)));
			}
		}
	}
	
	/**
	 * Complete all jobs owned by the current caller which are already
	 * in a final state.
	 * 
	 * @param connection The database connection.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized public void completeJobs(Connection connection)
		throws SQLException, ResourceException
	{
		Collection<Long> jobsToComplete = new LinkedList<Long>();
		HashMap<Long, PartialJobInfo> ownerMap;
		
		/* Get information needed from the database (like owner id) */
		ownerMap = _database.getPartialJobInfos(connection, _jobsByID.keySet());
		
		/* Find all jobs that are owend by the caller and that are in a
		 * final state.
		 */
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
	
	/**
	 * This method completes (or removes from the queue) all jobs which are
	 * owned by the caller and which are in a final state.  Only jobs in a
	 * final state can be completed.  To try and complete a job which is not
	 * in a final state is considered an error and an exception will be thrown.
	 * 
	 * @param connection The database connection to use.
	 * @param jobs The list of jobs to complete.  If this is null or empty, 
	 * then all jobs owned by the caller, and already in a final state, will
	 * be completed.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 * @throws GenesisIISecurityException
	 */
	synchronized public void completeJobs(Connection connection, String []jobs)
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		/* If no jobs were passed to us, then the caller wants us to complete
		 * all jobs that are in a final state and that are owned by him/her.
		 */
		if (jobs == null || jobs.length == 0)
		{
			completeJobs(connection);
			return;
		}
		
		Collection<Long> jobsToComplete = new LinkedList<Long>();
		HashMap<Long, PartialJobInfo> ownerMap;
		
		/* Find the job IDs for all job tickets passed in */
		for (String jobTicket : jobs)
		{
			JobData data = _jobsByTicket.get(jobTicket);
			if (data == null)
				throw new ResourceException("Job \"" + jobTicket 
					+ "\" does not exist.");
			jobsToComplete.add(new Long(data.getJobID()));
		}
		
		/* Now, get the database information for all of the jobs
		 * passed in.
		 */
		ownerMap = _database.getPartialJobInfos(connection, jobsToComplete);
		
		/* Check that every job indicated is owned by the caller and is
		 * in a final state.
		 */
		for (Long jobID : ownerMap.keySet())
		{
			JobData jobData = _jobsByID.get(jobID);
			PartialJobInfo pji = ownerMap.get(jobID);
			
			/* If the job isn't in a final state, throw an exception. */ 
			if (!jobData.getJobState().isFinalState())
				throw new ResourceException("Job \"" + jobData.getJobTicket() 
					+ "\" is not in a final state.");
			
			/* If the job isn't owned by the caller, throw an exception. */
			if (!QueueSecurity.isOwner(pji.getOwners()))
				throw new GenesisIISecurityException(
					"Don't have permission to complete job \"" + 
						jobData.getJobTicket() + "\".");
		}	
		
		// Go ahead and complete them
		completeJobs(connection, jobsToComplete);
	}
	
	synchronized public void checkJobStatus(long jobID)
	{
		int originalCount = _outcallThreadPool.size();
		
		JobData job = _jobsByID.get(jobID);
		if (job == null || job.getBESID() == null)
			return;
		
		/* For convenience, we bundle together the id's of the
		 * job to check, and the bes container on which it is
		 * running.
		 */
		JobCommunicationInfo info = new JobCommunicationInfo(
			job.getJobID(), job.getBESID().longValue());
		
		/* As in other places, we use a callback mechanism to
		 * allow outcall threads to late bind "large" information
		 * at the last minute.  This keeps us from putting too
		 * much into memory.  In this particular case its something
		 * of a hack as we already had a type for getting the bes
		 * information so we just bundled that with a second interface
		 * for getting the job's endpoint.  This could have been done
		 * cleaner, but it works fine.
		 */
		Resolver resolver = new Resolver();
		
		/* Enqueue the worker into the outcall thread pool */
		_outcallThreadPool.enqueue(new JobUpdateWorker(
			this, resolver, resolver, _connectionPool, info, job));
	
	int newCount = _outcallThreadPool.size();
	
	_logger.debug(String.format(
		"Just finished enqueing a bunch of jobs into the thread pool " +
		"and the thread pool size grew from %d jobs to %d jobs.", 
		originalCount, newCount));
	}
	
	synchronized public Map<String, Long> summarizeToMap()
	{
		Map<String, Long> ret = new LinkedHashMap<String, Long>();
		
		long queued = 0;
		long running = 0;
		long starting = 0;
		long finished = 0;
		long error = 0;
		long requeued = 0;
		
		for (JobData jobData : _jobsByID.values())
		{
			QueueStates state = jobData.getJobState();
			if (state == QueueStates.QUEUED)
				queued++;
			else if (state == QueueStates.REQUEUED)
				requeued++;
			else if (state == QueueStates.STARTING)
				starting++;
			else if (state == QueueStates.RUNNING)
				running++;
			else if (state == QueueStates.ERROR)
				error++;
			else if (state == QueueStates.FINISHED)
				finished++;
		}
		
		ret.put("Queued", queued);
		ret.put("Re-queued", requeued);
		ret.put("Starting", starting);
		ret.put("Running", running);
		ret.put("Error", error);
		ret.put("Finished", finished);
		
		return ret;
	}
	
	/**
	 * This method is called periodically by a watcher thread to check on the
	 * current statuses of all jobs running in the queue (the poll'ing method
	 * of checking whether or not a job has run to completion or failed or is
	 * still running).
	 * 
	 * @param connection The database connection.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 * @throws ConfigurationException
	 * @throws GenesisIISecurityException
	 */
	synchronized public void checkJobStatuses(Connection connection)
		throws SQLException, ResourceException,
			GenesisIISecurityException
	{
		int originalCount = _outcallThreadPool.size();
		
		/* Iterate through all running jobs and enqueue a worker to
		 * check the status of that job.
		 */
		for (JobData job : _runningJobs.values())
		{
			if (job == null || job.getBESID() == null)
				continue;
			
			/* For convenience, we bundle together the id's of the
			 * job to check, and the bes container on which it is
			 * running.
			 */
			JobCommunicationInfo info = new JobCommunicationInfo(
				job.getJobID(), job.getBESID().longValue());
			
			/* As in other places, we use a callback mechanism to
			 * allow outcall threads to late bind "large" information
			 * at the last minute.  This keeps us from putting too
			 * much into memory.  In this particular case its something
			 * of a hack as we already had a type for getting the bes
			 * information so we just bundled that with a second interface
			 * for getting the job's endpoint.  This could have been done
			 * cleaner, but it works fine.
			 */
			Resolver resolver = new Resolver();
			
			/* Enqueue the worker into the outcall thread pool */
			_outcallThreadPool.enqueue(new JobUpdateWorker(
				this, resolver, resolver, _connectionPool, info, job));
		}
		
		int newCount = _outcallThreadPool.size();
		
		_logger.debug(String.format(
			"Just finished enqueing a bunch of jobs into the thread pool " +
			"and the thread pool size grew from %d jobs to %d jobs.", 
			originalCount, newCount));
	}
	
	/**
	 * Check to see whether or not there are any jobs in the queue 
	 * waiting to run.
	 * 
	 * @return True if there are any jobs still waiting to run.
	 */
	synchronized public boolean hasQueuedJobs()
	{
		return !_queuedJobs.isEmpty();
	}
	
	/**
	 * This method takes a list of slots (assumed to be an indication of
	 * total slots allocated to BES resources) and subtracts out the slots
	 * that are already in use.  We could have avoided this step by keeping
	 * more information about slot use in memory, but doing so exponentially
	 * increases the complexity of the Job Manager and the BES Manager and
	 * since all this work is done in memory, it is a relatively fast
	 * operation anyways.  It's an O(n) algorithm where n is equal to the
	 * number of jobs actively running at any given time.
	 * 
	 * @param slots A map of all of the slots available to the queue at this
	 * moment.
	 * 
	 * @throws ResourceException
	 */
	synchronized public void recordUsedSlots(HashMap<Long, ResourceSlots> slots)
		throws ResourceException
	{
		/* Iterate through all running jobs and reduce the slot count from
		 * resources that they are using.
		 */
		for (JobData job : _runningJobs.values())
		{
			/* Get the bes id that the job is running on */
			Long besID = job.getBESID();
			if (besID == null)
			{
				_logger.warn("A job is marked as running which isn't " +
				"assigned to a BES container.");
				continue;
			}
			
			ResourceSlots rs = slots.get(besID);
			if (rs != null)
			{
				/* Go ahead and "reserve" the slots for use */
				rs.reserveSlot();
				
				/* If the resource now has no slots available, remove it
				 * complete from the list.
				 */
				if (rs.slotsAvailable() <= 0)
					slots.remove(besID);
			}
		}
	}
	
	synchronized public void recordUsedSlots(Map<Long, SlotSummary> slots)
	{
		/* Iterate through all running jobs and reduce the slot count from
		 * resources that they are using.
		 */
		for (JobData job : _runningJobs.values())
		{
			/* Get the bes id that the job is running on */
			Long besID = job.getBESID();
			if (besID == null)
				continue;
			
			SlotSummary summary = slots.get(besID);
			if (summary != null)
				summary.add(-1, 1);
		}
	}
	
	synchronized public void addJobErrorInformation(Connection connection,
		long jobid, short attempt, Collection<String> errors)
	{
		try
		{
			_database.addError(connection, jobid, attempt, errors);
			connection.commit();
		}
		catch (Throwable cause)
		{
			_logger.warn(
				"An error occurred while trying to save error information.", 
				cause);
		}
	}
	
	/**
	 * Retrieves the list of currently queued jobs.
	 * 
	 * @return The list of currently queued jobs.
	 */
	synchronized public Collection<JobData> getQueuedJobs()
	{
		return _queuedJobs.values();
	}
	
	/**
	 * Given a list of resource matches, start the jobs indicated on the
	 * BES containers indicated.
	 * 
	 * @param connection The database connection to use.
	 * @param matches A list of matched resources and jobs.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized public void startJobs(Connection connection, 
		Collection<ResourceMatch> matches)
			throws SQLException, ResourceException
	{
		/* First, note in the database that we are starting a job.  In all
		 * honesty, if we restarted at this point, it would probably be
		 * ok if we didn't know this.  If we do restart, we won't be able
		 * to do anything with the job since we don't have an endpoint, but
		 * it is a state that we can keep so we do anyways.
		 */
		_database.markStarting(connection, matches);
		connection.commit();
		
		/* Iterate through the matches and enqueue a worker to start the jobs
		 * indicated there.
		 */
		for (ResourceMatch match : matches)
		{
			/* Find the job data for the match */
			JobData data = _jobsByID.get(new Long(match.getJobID()));
			
			/* Get the job off of the queued list and instead put him on the
			 * running list.  Also, note which bes the jobs is assigned to.
			 */
			_queuedJobs.remove(new SortableJobKey(data));
			data.setBESID(match.getBESID());
			data.setJobState(QueueStates.STARTING);
			_runningJobs.put(new Long(data.getJobID()), data);
			
			/*
			 * Finally, enqueue a worker to make the out call to the BES to
			 * start the job.  Until this worker completes, a restart of the
			 * container will cause this attempt at starting a job to fail.
			 */
			_outcallThreadPool.enqueue(new JobLauncher(this, match.getJobID(), 
				match.getBESID()));
		}
	}
	
	/**
	 * This is the resolver instance that is used to late bind job EPR,
	 * BES EPR, and Job Calling Context for outcall workers.  This class
	 * is implemented with a horrible hack in it.  Namely, that the call
	 * to createClientStub MUST come after a call to getJobEndpoint.  The
	 * main reason for this is that I'm now rushing to get this working and
	 * this hack can be managed client side.  We will at least check the
	 * condition though so that we will catch an invalid ordering if it
	 * ever happens.
	 * 
	 * @author mmm2a
	 */
	private class Resolver implements IBESPortTypeResolver, IJobEndpointResolver
	{
		private GeniiBESPortType _portType = null;
		private EndpointReferenceType _endpoint = null;
		
		/**
		 * Resolve the information in this resolver.  I.e., get the client stub
		 * and the job endpoint from the database.
		 * 
		 * @param connection The database connection to use.
		 * @param jobID The job's ID
		 * 
		 * @throws Throwable
		 */
		private void resolve(Connection connection, long jobID) throws Throwable
		{
			JobStatusInformation info = _database.getJobStatusInformation(
				connection, jobID);
			
			_endpoint = info.getJobEndpoint();
			_portType = ClientUtils.createProxy(
				GeniiBESPortType.class, info.getBESEndpoint(), 
				info.getCallingContext());
		}
		
		@Override
		public GeniiBESPortType createClientStub(Connection connection, long besID)
				throws Throwable
		{
			if (_portType == null)
				throw new IllegalStateException(
					"createClientStub called before getJobEndpoint.");
			
			return _portType;
		}

		@Override
		public EndpointReferenceType getJobEndpoint(Connection connection,
				long jobID) throws Throwable
		{
			resolve(connection, jobID);
			return _endpoint;
		}
		
		public String getBESName(long besID)
		{
			BESData data = _besManager.findBES(besID);
			if (data != null)
				return data.getName();
			
			return null;
		}
	}
	
	/**
	 * Kill all of the jobs indicated.  If the job is queued or requeued, we 
	 * simply move it to a final state.  If the job is starting or running, 
	 * we kill it.  If the job is already in a final state, we don't do
	 * anything.  This operation is also a "safe" operation meaning that
	 * the caller MUST own the job to kill it or it is considered an
	 * exception.  Also, unlike other similar operations, the list of
	 * tickets, when NULL, will not imply all jobs owned by the caller --
	 * instead we just will return.
	 * 
	 * @param connection The database connection to use.
	 * @param tickets The list of job tickets to kill.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized public void killJobs(Connection connection, String []tickets)
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		/* If we weren't given any tickets, then just ignore */
		if (tickets == null || tickets.length == 0)
			return;
		
		Collection<Long> jobsToKill = new LinkedList<Long>();
		HashMap<Long, PartialJobInfo> ownerMap;
		
		/* Iterate through all job tickets and get the associated in-memory
		 * job information.
		 */
		for (String jobTicket : tickets)
		{
			JobData data = _jobsByTicket.get(jobTicket);
			if (data == null)
				throw new ResourceException("Job \"" + jobTicket 
					+ "\" does not exist.");
			jobsToKill.add(new Long(data.getJobID()));
		}
		
		/* Retrieve the list of owners for all the jobs we 
		 * are going to kill. */
		ownerMap = _database.getPartialJobInfos(connection, jobsToKill);
		
		/* Iterate through the job information */
		for (Long jobID : ownerMap.keySet())
		{
			JobData jobData = _jobsByID.get(jobID);
			PartialJobInfo pji = ownerMap.get(jobID);
			
			/* If the caller doesn't own the job, it's 
			 * a security exception */
			if (!QueueSecurity.isOwner(pji.getOwners()))
				throw new GenesisIISecurityException(
					"Don't have permission to kill job \"" + 
						jobData.getJobTicket() + "\".");
			
			/* If the job is starting, we mark it as being killed.  Starting
			 * implies that another thread is about to try and start the thing
			 * up so it will have to check this flag and abort (or kill) as
			 * necessary.
			 */
			if (jobData.getJobState().equals(QueueStates.STARTING))
			{
				jobData.kill();
			} else if (jobData.getJobState().equals(QueueStates.RUNNING))
			{
				/* If the job is running, we have to finish the job (which 
				 * will kill it for us) */
				killJob(connection, jobID);
			} else if (!jobData.getJobState().isFinalState())
			{
				/* This won't kill the job (it isn't running), but it
				 * will move it to the correct lists, thus preventing it
				 * from ever being run.
				 */
				finishJob(jobID);
			}
		}
	}
	
	synchronized public void resetJobCommunicationAttempts(
		Connection connection, long jobid) throws SQLException
	{
		_database.setJobCommunicationAttempts(connection, jobid, 0);
	}
	
	synchronized public void addJobCommunicationAttempt(
		Connection connection, long jobid, long besid,
		Collection<GridLogTarget> logTargets) 
			throws SQLException, ResourceException
	{
		int commAttempts = _database.getJobCommunicationAttempts(
			connection, jobid);
		if (commAttempts > MAX_COMM_ATTEMPTS)
		{
			_jobLogger.log(logTargets,
				"Failed job communication attempts exceeds limit." +
				"  Re-starting job.");
			_logger.error(String.format(
				"Unable to communicate with job for %d attempts.  Re-starting it.",
				commAttempts));
			failJob(connection, jobid, false, false, false);
			_besManager.markBESAsMissed(besid, "Couldn't get job status.");
		} else
		{
			_database.setJobCommunicationAttempts(connection, jobid, 
				commAttempts + 1);
		}
	}
	
	/**
	 * This is the worker that is used to launch a new job
	 * 
	 * @author mmm2a
	 */
	private class JobLauncher implements OutcallHandler
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
		
		public boolean equals(JobLauncher other)
		{
			return (_jobID == other._jobID) && (_besID == other._besID) && 
				(_manager == other._manager);
		}
		
		@Override
		public boolean equals(OutcallHandler other)
		{
			if (other instanceof JobLauncher)
				return equals((JobLauncher)other);
			
			return false;
		}
		
		@Override
		public boolean equals(Object other)
		{
			if (other instanceof JobLauncher)
				return equals((JobLauncher)other);
			
			return false;
		}
		
		public void run()
		{
			Collection<GridLogTarget> logTargets = null;
			boolean isPermanent = false;
			ICallingContext startCtxt = null;
			Connection connection = null;
			EntryType entryType;
			HashMap<Long, EntryType> entries = new HashMap<Long, EntryType>();
			JobData data = null;
			
			try
			{
				/* Acquire a new database connection to use. */
				connection = _connectionPool.acquire(false);
				
				/* Get all of the information from the database required to
				 * start the job.
				 */
				JobStartInformation startInfo = _database.getStartInformation(
					connection, _jobID);
				connection.commit();
				
				logTargets = GridLogUtils.getTargets(
					startInfo.getCallingContext());
				
				SecurityUpdateResults checkResults = new SecurityUpdateResults();
				ClientUtils.checkAndRenewCredentials(startInfo.getCallingContext(),
					new Date(System.currentTimeMillis() + 1000l * 60 * 10),
					checkResults);
				if (checkResults.removedCredentials().size() > 0)
				{
					_jobLogger.log(logTargets, 
						"The security information for this job has expired!" +
						"  Failing the job permanently.");
					isPermanent = true;
					throw new GeneralSecurityException(
						"A job's credentials expired so we can't make " +
						"any further progress on it.  Failing it.");
				}
				
				/* Use the database's fillInBESEPRs function to get the EPR of
				 * the BES container that we are going to launch on.
				 */
				entries.put(new Long(_besID), entryType = new EntryType());
				_database.fillInBESEPRs(connection, entries);
				
				synchronized(_manager)
				{
					/* Get the in-memory information for the job */
					data = _jobsByID.get(new Long(_jobID));
					if (data == null)
					{
						_logger.warn("Job " + _jobID + 
							" dissappeared before it could be started.");
						return;
					}
					
					/* If the thing was marked as killed, then we simply won't
					 *  start it.  Instead, we will finish it early. */
					if (data.killed())
					{
						_jobLogger.log(logTargets,
							"The job was manually killed before it could be started.");
						finishJob(_jobID);
						return;
					}
				}
				
				String oldAction = data.setJobAction("Creating");
				if (oldAction != null)
				{
					_jobLogger.log(logTargets,
						"The job is currently being processed by the queue in" +
						" some other way and so we will not start it after all.");
					_logger.error(
						"We're trying to create an activity for a job that " +
						"is already under going some action:  " + oldAction);
					return;
				}
				
				CreateActivityResponseType resp;
				try
				{
					/* We need to start the job, so go ahead and create a proxy to
					 * call the container and then call it.
					 */
					startCtxt = startInfo.getCallingContext();
					GeniiBESPortType bes = ClientUtils.createProxy(GeniiBESPortType.class, 
						entryType.getEntry_reference(), 
						startCtxt);
					ClientUtils.setTimeout(bes, 120 * 1000);
					ActivityDocumentType adt = new ActivityDocumentType(
						startInfo.getJSDL(), null);
					EndpointReferenceType queueEPR = 
						_database.getQueueEPR(connection);
					if (queueEPR != null)
						BESUtils.addSubscription(adt, new Subscribe(
							new Token(
								WellknownTopics.BES_ACTIVITY_STATUS_CHANGE_FINAL),
							null,
							queueEPR,
							new UserDataType(new MessageElement[] {
								new MessageElement(
									QueueServiceImpl._JOBID_QNAME, 
									Long.toString(_jobID)) })));
					
					_jobLogger.log(logTargets,
						"Making grid outcall to start the job on the target BES container.");
					resp = bes.createActivity(
						new CreateActivityType(adt, null));
					
					EndpointReferenceType epr = resp.getActivityIdentifier();
					PostTargets.poster().post(JobEvent.jobLaunched(startCtxt, epr, 
						Long.toString(_jobID)));
				}
				finally
				{
					data.clearJobAction();
				}
				
				synchronized(_manager)
				{
					/* We successfully got back here, so mark the job as 
					 * started in the database. */
					_database.markRunning(connection, _jobID, 
						resp.getActivityIdentifier());
					connection.commit();
					
					/* Now it's stored in the database.  Note that it's started
					 * in memory as well.
					 */
					data = _jobsByID.get(new Long(_jobID));
					data.setJobState(
						QueueStates.RUNNING);
					
					/* Finally, we check one last time to see if it was 
					 * "killed" while we were starting it.  If so, then we
					 * will immediately kill it and finish it.
					 */
					if (data.killed())
						finishJob(_jobID);
				}
			}
			catch (Throwable cause)
			{
				_jobLogger.log(logTargets,
					"Unable to start the job on the remote BES.", cause);
				_logger.warn(String.format(
					"Unable to start job %d.  Exception class is %s.", 
					_jobID, cause.getClass().getName()), cause);
				
				boolean countAgainstJob;
				boolean countAgainstBES;
				
				if (cause instanceof NotAcceptingNewActivitiesFaultType)
				{
					countAgainstJob = false;
					countAgainstBES = true;
				} else if (cause instanceof InvalidRequestMessageFaultType)
				{
					countAgainstJob = false;
					countAgainstBES = true;
				} else if (cause instanceof UnsupportedFeatureFaultType)
				{
					countAgainstJob = true;
					countAgainstBES = false;
				} else if (cause instanceof NotAuthorizedFaultType)
				{
					countAgainstJob = true;
					countAgainstBES = false;
				} else if (cause instanceof GenesisIISecurityException)
				{
					countAgainstJob = true;
					countAgainstBES = false;
				} else if (cause instanceof GeneralSecurityException)
				{
					countAgainstJob = true;
					countAgainstBES = false;
				} else
				{
					countAgainstJob = true;
					countAgainstBES = true;
				}
				
				if (countAgainstBES)
				{
					_besManager.markBESAsUnavailable(_besID, 
						String.format("Exception during job start %s(%s)",
							cause.getClass().getName(), 
							cause.getLocalizedMessage()));
				}
				
				if (data != null && countAgainstJob)
					data.setNextValidRunTime(new Date());
				
				try 
				{
					/* We got an exception, so fail the job. */
					failJob(connection, _jobID, countAgainstJob,
						isPermanent, true);
					PostTargets.poster().post(JobEvent.jobFailed(
						startCtxt, Long.toString(_jobID)));
					_schedulingEvent.notifySchedulingEvent();
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
	
	/**
	 * A worker that can go to a bes container and terminate a bes activity.
	 * This worker is used to both kill jobs prematurely, and to clean up
	 * after the complete.
	 * 
	 * @author mmm2a
	 */
	private class JobKiller implements OutcallHandler
	{
		private boolean _outcallOnly;
		private boolean _attemptKill;
		private JobData _jobData;
		private QueueStates _newState;
		private Long _besID;
		
		public JobKiller(JobData jobData, QueueStates newState,
			boolean outcallOnly, boolean attemptKill, Long besID)
		{
			_outcallOnly = outcallOnly;
			_jobData = jobData;
			_newState = newState;
			_attemptKill = attemptKill;
			_besID = besID;
		}
		
		public boolean equals(JobKiller other)
		{
			return (_newState == other._newState) && 
				(_jobData.getJobID() == other._jobData.getJobID());
		}
		
		@Override
		public boolean equals(OutcallHandler other)
		{
			if (other instanceof JobKiller)
				return equals((JobKiller)other);
			
			return false;
		}
		
		@Override
		public boolean equals(Object other)
		{
			if (other instanceof JobKiller)
				return equals((JobKiller)other);
			
			return false;
		}
		
		/**
		 * Terminate the activity at the BES container.
		 * 
		 * @param connection The database connection to use.
		 * 
		 * @throws SQLException
		 * @throws ResourceException
		 */
		private ICallingContext terminateActivity(Connection connection,
			Collection<GridLogTarget> logTargets)
			throws SQLException, ResourceException
		{
			/* Ask the database for all information needed to
			 * terminate the activity at the BES container.
			 */
			KillInformation killInfo = _database.getKillInfo(
				connection, _jobData.getJobID(), _besID);
			connection.commit();
				
			String oldAction = _jobData.setJobAction("Terminating");
			if (oldAction != null)
			{
				_logger.error(
					"Attempted to kill an activity which was " +
					"already undergoing another action:  " + oldAction);
			}
			
			try
			{
				ICallingContext ctxt = killInfo.getCallingContext();
				
				if (_attemptKill)
				{
					PersistentOutcallJobKiller.killJob(
						killInfo.getBESEndpoint(), killInfo.getJobEndpoint(),
						killInfo.getCallingContext());
				}

				return ctxt;
			}
			catch (Throwable cause)
			{
				_jobLogger.log(logTargets,
					"Unable to make outcall to kill the activity.", cause);
				_logger.warn("Exception occurred while killing an activity.");
				return null;
			}
			finally
			{
				_jobData.clearJobAction();
			}
		}
		
		public void run()
		{
			Collection<GridLogTarget> logTargets =
				_jobData.gridLogTargets();
			
			ICallingContext ctxt = null;
			Connection connection = null;
			
			try
			{
				/* Acquire a connection to talk to the database with. */
				connection = _connectionPool.acquire(false);
				
				if (_outcallOnly)
				{
					terminateActivity(connection, logTargets);
					
					/* Ask the database to update the job state */
					_database.modifyJobState(connection, _jobData.getJobID(),
						_jobData.getRunAttempts(), _newState, new Date(), null, null, null);
					connection.commit();
					
					return;
				}
				
				/* If the job is running, then we have to terminate it */
				if (_jobData.getJobState().equals(QueueStates.RUNNING))
					ctxt = terminateActivity(connection, logTargets);
				
				/* Ask the database to update the job state */
				_database.modifyJobState(connection, _jobData.getJobID(),
					_jobData.getRunAttempts(), _newState, new Date(), null, null, null);
				connection.commit();
				
				/* Finally, note the new state in memory and clear the 
				 * old BES information. */
				_jobData.setJobState(_newState);
				_jobData.clearBESID();
				
				PostTarget pt = PostTargets.poster();
				String jobid = Long.toString(_jobData.getJobID()); 
				PostEvent event;
				
				/* If we were asked to re-queue the job, then put it back in 
				 * the queued jobs list. */
				if (_newState.equals(QueueStates.REQUEUED))
				{
					event = JobEvent.jobRequeued(ctxt, jobid);
					if (pt != null)
						pt.post(event);
					_logger.debug("Re-queing job " + _jobData.getJobTicket());
					synchronized (JobManager.this)
					{
						_queuedJobs.put(new SortableJobKey(_jobData), _jobData);
					}
				} else
				{
					/* Otherwise, we assume that he's already in 
					 * the right list */
					_logger.debug("Moving job \"" + _jobData.getJobTicket()
						+ "\" to the " + _newState + " state.");
				}
				
				/* Because a job was terminated (whether because it finished 
				 * or failed or whatnot) we have a new opportunity to 
				 * schedule a new job. */
				_schedulingEvent.notifySchedulingEvent();
			}
			catch (Throwable cause)
			{
				_logger.error("Error killing job " + _jobData.getJobTicket(), cause);
			}
			finally
			{
				_connectionPool.release(connection);
			}
		}
	}
}
