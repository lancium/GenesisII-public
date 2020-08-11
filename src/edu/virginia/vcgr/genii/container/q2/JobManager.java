package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.naming.ConfigurationException;

import org.apache.axis.types.UnsignedShort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityDocumentType;
import org.ggf.bes.factory.ActivityStateEnumeration;
import org.ggf.bes.factory.ActivityStatusType;
import org.ggf.bes.factory.CreateActivityResponseType;
import org.ggf.bes.factory.CreateActivityType;
import org.ggf.bes.factory.InvalidRequestMessageFaultType;
import org.ggf.bes.factory.NotAcceptingNewActivitiesFaultType;
import org.ggf.bes.factory.NotAuthorizedFaultType;
import org.ggf.bes.factory.UnsupportedFeatureFaultType;
import org.ggf.jsdl.JobDefinition_Type;
import org.morgan.util.GUID;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.cs.vcgr.genii.job_management.JobErrorPacket;
import edu.virginia.cs.vcgr.genii.job_management.JobInformationType;
import edu.virginia.cs.vcgr.genii.job_management.JobStateEnumerationType;
import edu.virginia.cs.vcgr.genii.job_management.ReducedJobInformationType;
import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.bes.BESUtils;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.history.SequenceNumber;
import edu.virginia.vcgr.genii.client.invoke.handlers.MyProxyCertificate;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLTransformer;
import edu.virginia.vcgr.genii.client.jsdl.JobRequest;
import edu.virginia.vcgr.genii.client.jsdl.JobRequestParser;
import edu.virginia.vcgr.genii.client.queue.QueueConstants;
import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.AbstractSubscriptionFactory;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.BESActivityTopics;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContainerService;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryEventToken;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryEventWriter;
import edu.virginia.vcgr.genii.container.cservices.history.InMemoryHistoryEventSink;
import edu.virginia.vcgr.genii.container.cservices.history.NullHistoryContext;
import edu.virginia.vcgr.genii.container.cservices.percall.BESActivityTerminatorActor;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.q2.iterator.QueueInMemoryIteratorEntry;
import edu.virginia.vcgr.genii.container.q2.summary.SlotSummary;
import edu.virginia.vcgr.genii.container.rns.LegacyEntryType;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.identity.Identity;
import edu.virginia.vcgr.genii.security.utils.SecurityUtilities;
import edu.virginia.vcgr.jsdl.sweep.SweepException;

/**
 * The Job Manager class is the main class to handle adding/removing/managing jobs in the queue. It DOES NOT handle scheduling of jobs (as
 * that is really a matching process between information stored in this manager and information stored in the BES manager) though it does help
 * the Scheduler with pieces of that function.
 * 
 * @author mmm2a
 */
public class JobManager implements Closeable
{
	static private Log _logger = LogFactory.getLog(JobManager.class);

	static public String PARAMETER_SWEEP_NAME_ADDITION = "Sweeper for ";

	/**
	 * How often we poll a running job (ms) to see if it is completed/failed or not. It would be great if we could avoid polling, but BES
	 * doesn't require notification in the spec. so we can't count on it.
	 */
	static final private long _STATUS_CHECK_FREQUENCY = 1000L * 60 * 5;

	/** One hour of non-communication */
	static final private long MAX_COMM_ATTEMPTS = (1000L * 60 * 60 * 1) / _STATUS_CHECK_FREQUENCY;

	/**
	 * The maximum number of times that we will allow a job to be started and failed before giving up.
	 */
	static final public short MAX_RUN_ATTEMPTS = 5;

	volatile private boolean _closed = false;

	private ThreadPool _outcallThreadPool;
	private QueueDatabase _database;
	private SchedulingEvent _schedulingEvent;
	private JobStatusChecker _statusChecker;
	private ServerDatabaseConnectionPool _connectionPool;
	private BESManager _besManager;
	private String _lastUserScheduled;

	// pending job status checks, performed during slack time.
	private volatile ArrayList<Long> _pendingChecks;
	// the time when we should check all the pending status notifications.
	private volatile Calendar _whenToProcessNotifications;
	// how frequently to check for notifications.
	private final int NOTIFICATION_CHECKING_DELAY = 1 * 500;
	
	private Date _lastUpdate = null;

	/**
	 * A map of all jobs in the queue based off of the job's key in the database.
	 */
	private Map<Long, JobData> _jobsByID = Collections.synchronizedMap(new HashMap<Long, JobData>());

	/**
	 * A map of all jobs in the queue based off of the job's human readable job ticket.
	 */
	private Map<String, JobData> _jobsByTicket = Collections.synchronizedMap(new HashMap<String, JobData>());

	/**
	 * All jobs in the queue that are waiting to run. This map is sorted by priority, then submit time, then job ID.
	 */
	//private TreeMap<SortableJobKey, JobData> _queuedJobs = new TreeMap<SortableJobKey, JobData>();
	private SortedMap<SortableJobKey, JobData> _queuedJobs = Collections.synchronizedSortedMap(new TreeMap<SortableJobKey, JobData>());

	/**
	 * All jobs in the queue, separated into lists for each user. Each user map is sorted like the full map above.
	 */
	private Map<String, TreeMap<SortableJobKey, JobData>> _usersWithJobs = Collections.synchronizedMap(new HashMap<String, TreeMap<SortableJobKey, JobData>>());

	/**
	 * All jobs in the queue, separated into lists for each user. Each user map is sorted like the full map above.
	 */
	private Map<String, TreeMap<SortableJobKey, JobData>> _usersNotReadyJobs = Collections.synchronizedMap(new HashMap<String, TreeMap<SortableJobKey, JobData>>());
	/*
	 * Map of string user id's to identity arrays. This is needed so we can identify more formally who owns the job to clients.
	 */
	private Map<String, byte[][]> _userIDs = Collections.synchronizedMap(new HashMap<String, byte[][]>());
	/**
	 * A map of all jobs currently running (or starting) in the queue.
	 */
	private Map<Long, JobData>  _runningJobs = Collections.synchronizedMap(new HashMap<Long, JobData>());

	public JobManager(ThreadPool outcallThreadPool, QueueDatabase database, SchedulingEvent schedulingEvent, BESManager manager,
		Connection connection, ServerDatabaseConnectionPool connectionPool) throws SQLException, ResourceException, GenesisIISecurityException
	{
		_connectionPool = connectionPool;
		_database = database;
		_schedulingEvent = schedulingEvent;
		_outcallThreadPool = outcallThreadPool;
		_besManager = manager;

		loadFromDatabase(connection);

		ContainerServices.findService(HistoryContainerService.class).loadQueue(connection);

		_whenToProcessNotifications = Calendar.getInstance();
		_whenToProcessNotifications.add(Calendar.MILLISECOND, NOTIFICATION_CHECKING_DELAY);
		_pendingChecks = new ArrayList<Long>();
		_statusChecker = new JobStatusChecker(connectionPool, this, _STATUS_CHECK_FREQUENCY);
		_lastUpdate = Calendar.getInstance().getTime();
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

	private void putInUserBucket(Map<String, TreeMap<SortableJobKey, JobData>> bucket, JobData job, String username)
	{
		// Also re-insert into user list
		SortableJobKey jobKey = new SortableJobKey(job.getJobID(), job.getPriority(), job.getSubmitTime());
		if (bucket.containsKey(username)) {
			bucket.get(username).put(jobKey, job);
		} else {
			TreeMap<SortableJobKey, JobData> userJobs = new TreeMap<SortableJobKey, JobData>();
			userJobs.put(jobKey, job);
			bucket.put(username, userJobs);
		}
	}

	private void removeFromUserBucket(Map<String, TreeMap<SortableJobKey, JobData>> bucket, JobData job)
	{
		SortableJobKey jobKey = new SortableJobKey(job);
		// Remove the job from _users with jobs, and move it to _usersNotReadyJobs
		for (TreeMap<SortableJobKey, JobData> user : bucket.values()) {
			user.remove(jobKey);
		}
	}

	private String updateJobData(Connection connection, JobData job) throws ResourceException, SQLException
	{
		// Get owner identities, and extract username of primary
		Collection<Long> jobID = new ArrayList<Long>();
		jobID.add(job.getJobID());
		HashMap<Long, PartialJobInfo> jobInfo = _database.getPartialJobInfos(connection, jobID);

		Collection<Identity> identities =
			SecurityUtilities.filterCredentials(jobInfo.get(job.getJobID()).getOwners(), SecurityUtilities.GROUP_TOKEN_PATTERN);

		identities = SecurityUtilities.filterCredentials(identities, SecurityUtilities.CLIENT_IDENTITY_PATTERN);

		String username = identities.iterator().next().toString();
		/*
		 * Added by ASG 4/25/2016. Add the username, and start/finish to the job record.
		 */
		job.setUserName(username);
		job.setStartTime(jobInfo.get(job.getJobID()).getStartTime());
		job.setFinishTime(jobInfo.get(job.getJobID()).getFinishTime());
		if (!_userIDs.containsKey(username)) {

			try {
				_userIDs.put(username, QueueSecurity.convert(jobInfo.get(job.getJobID()).getOwners()));
				// System.out.println("ADDing " + username +" to userIDs");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return username;
	}

	/**
	 * Load all jobs stored in the database into this manager. This operation should only be called once at the beginning of each web server
	 * start.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized private void loadFromDatabase(Connection connection) throws SQLException, ResourceException
	{
		if (_logger.isDebugEnabled())
			_logger.debug("Starting to reload jobs from the database.");

		/*
		 * Ask the database for a list of all jobs stored in the database for this queue.
		 */
		Collection<JobData> jobs = _database.loadAllJobs(connection);

		/* Loop through all jobs indicated by the queue */
		LinkedList<JobData> starting = new LinkedList<JobData>();
		for (JobData job : jobs) {
			/*
			 * Put the job into the two identity maps (the two that all jobs go into.
			 */
			_jobsByID.put(new Long(job.getJobID()), job);
			_jobsByTicket.put(job.getJobTicket(), job);
			String username = updateJobData(connection, job);
			/*
			 * Now, depending on the loaded job's state, we will perform some startup-load operations
			 */
			QueueStates jobState = job.getJobState();
			if (!jobState.isFinalState()) {
				/**
				 * If the job was in the queue and was listed as queued, or re-queued, then we need to put the job back into the "queued"
				 * list.
				 */
				if (jobState.equals(QueueStates.QUEUED) || jobState.equals(QueueStates.REQUEUED)) {
					// Get owner identities, and extract username of primary

					if (_logger.isTraceEnabled())
						_logger.debug("chose username using CLIENT_IDENTITY_PATTERN: " + username);
					SortableJobKey jobKey = new SortableJobKey(job.getJobID(), job.getPriority(), job.getSubmitTime());
					_queuedJobs.put(jobKey, job);

					// Also re-insert into user list
					putInUserBucket(_usersWithJobs, job, username);
				} else if (jobState.equals(QueueStates.RUNNING) || job.getBESID() != null) {
					// New by ASG, 2017-08-02
					JobStatusInformation info = _database.getJobStatusInformation(connection, job.getJobID());
					job.setJobEPR(info.getJobEndpoint());
					// End new stuff
					if (!jobState.equals(QueueStates.RUNNING)) {
						if (_logger.isDebugEnabled())
							_logger.debug("Found a job not marked as running in the database, but with a bes id.");
						job.setJobState(QueueStates.RUNNING);
					}
					/*
					 * Otherwise, if the job was listed as running, we need to put it into the running list.
					 */
					_runningJobs.put(new Long(job.getJobID()), job);
					putInUserBucket(_usersNotReadyJobs, job, username);
				} else {
					// If it isn't final, queued, and it isn't running, then we
					// loaded one marked as STARTING, which we can't resolve
					// (we don't have an EPR to access the job with -- it's
					// a leak). We will fail the job. There's a good chance
					// here that the bes container just leaked a job, but there
					// is no way to undo that unfortunately.
					starting.add(job);
					putInUserBucket(_usersNotReadyJobs, job, username);
				}
			} else { // Final job state
				putInUserBucket(_usersNotReadyJobs, job, username);
			}
		}

		if (_logger.isDebugEnabled())
			_logger.debug("Finished reloading jobs from the database.");

		/*
		 * Finally, we have to actually fail all of the jobs that were marked as starting.
		 */
		if (starting.size() > 0)
			_logger.debug(String.format("Failing %d jobs that were marked as starting when the " + "container went down.", starting.size()));
		for (JobData job : starting)
			failJob(connection, job.getJobID(), false, false, true);
	}

	/**
	 * Process a job that has failed for any reason.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param jobID
	 *            The ID of the job to fail.
	 * @param countAsAnAttempt
	 *            Indicate whether or not this failure should count against the job's maximum attempt count.
	 * @return True if the job was requeued, false otherwise.
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public boolean failJob(Connection connection, long jobID, boolean countAsAnAttempt, boolean isPermanent, boolean attemptKill)
		throws SQLException, ResourceException
	{
		boolean ret = false;
		/* Find the job's in-memory information */
		JobData job = _jobsByID.get(new Long(jobID));
		if (job == null) {
			// don't know where it went, but it's no longer our responsibility.
			_logger.warn(String.format("Couldn't find job %d to fail it.", jobID));
			return ret;
		}
		synchronized (job) {
		if (job.isSweepingJob()) {
			_logger.debug("in failJob, seeing attempt to fail on sweep job, ignoring.");
			return true;
		}

		if (_logger.isDebugEnabled())
			_logger.debug(String.format("Failing job %s(%s, %s, %s)", job,
				countAsAnAttempt ? "This failure counts against the job" : "This failure does NOT count against the job",
				isPermanent ? "Failure is permanent" : "Failure is NOT permanent",
				attemptKill ? "Attempting to kill the job" : "NOT attempting to kill the job"));

		/* Increment his attempt count if this counts against him. */
		if (countAsAnAttempt) {
			if (isPermanent) {
				job.history(HistoryEventCategory.Terminating).createErrorWriter("Permanently Failing Job")
					.format("We have determined that a job failure is permanent and "
						+ "are boosting the attempt number up to the max to reflect this.")
					.close();

				job.incrementRunAttempts(MAX_RUN_ATTEMPTS - job.getRunAttempts());
			} else {

				job.history(HistoryEventCategory.Terminating).createInfoWriter("Attempt %d Failed", job.getRunAttempts()).close();

				job.incrementRunAttempts();
			}

			job.setNextValidRunTime(new Date());
		} else {
			job.history(HistoryEventCategory.ReQueing).info("Retrying Attempt %d", job.getRunAttempts());
		}

		short attempts = job.getRunAttempts();
		QueueStates newState;

		/* If' he's already been started too many times, we fail him permanently */
		if (attempts >= MAX_RUN_ATTEMPTS) {
			job.history(HistoryEventCategory.Terminating).error("Maximum Attempts Reached");

			// We can't run this job any more.
			if (_logger.isDebugEnabled())
				_logger.debug(String.format("Moving job %s to ERROR state because we exceeded the maximum retry attempts.", job));
			newState = QueueStates.ERROR;
		} else {
			job.history(HistoryEventCategory.ReQueing).createTraceWriter("Re-queuing Job")
				.format("Re-queuing Job.  The job will " + "be removed from the runnable list until %tc.", job.getNextCanRun()).close();

			/* Otherwise, we'll just requeue him */
			if (_logger.isDebugEnabled())
				_logger.debug(String.format("Requeueing job %s because we have more attempts left.", job));
			newState = QueueStates.REQUEUED;
			removeFromUserBucket(_usersNotReadyJobs, job);
			putInUserBucket(_usersWithJobs, job, job.getUserName());

			ret = true;
		}

		if (_logger.isDebugEnabled())
			_logger.debug(String.format("Job %s's new states is %s", job, newState));

		// This is one of the few times we are going to break our pattern and
		// modify the in memory state before the database. The reason for this
		// is that we can't afford to forget that the BES container has a job
		// on it that it's managing. If we do, we will eventually leak memory
		// on that container.
		SortableJobKey jobKey = new SortableJobKey(job);
		_runningJobs.remove(new Long(jobID));
		_queuedJobs.remove(jobKey);
		// Remove the job from _users with jobs, and move it to _usersNotReadyJobs
		// The job should already be in notReady bucket
		// removeFromUserBucket(_usersWithJobs, job);
		// putInUserBucket(_usersNotReadyJobs, job, job.getUserName());

		/*
		 * In order to fail a job that was running, we need to make an outcall to this BES container. This is because, unless we terminate the
		 * activity through the bes container, the container will never garbage collect the information. This can lead to a database leak in
		 * the best case, and a memory leak in the worst.
		 * 
		 * To make the outcall, we create a new worker to enqueue onto the outcall thread queue. The major complication here is that because
		 * we can't update the database until after the job is killed, we will need to come back and update the database inside the outcall
		 * thread. This is why we took it out of the memory structures first. Now another thread won't try to do anything with it. It will be
		 * up to the outcall thread worker to put this guy back on the queued list (if that's where he's destined for) when the database has
		 * been updated and the outcall has been made).
		 */
		String besName = null;
		Long besID = job.getBESID();
		if (besID != null)
			besName = _besManager.getBESName(besID);

		job.history(HistoryEventCategory.Terminating).debug("Finishing Job with JobKiller");
		_outcallThreadPool.enqueue(new JobKiller(job, newState, false, attemptKill, besName, null));
		}
		return ret;
	}

	/**
	 * Similar to failing a job, this operation moves the job into a final state -- this one being a completed-successfully state.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param jobID
	 *            The job's ID.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public void finishJob(long jobID) throws SQLException, ResourceException
	{
		/* Find the job in the in-memory maps */
		JobData job = _jobsByID.get(new Long(jobID));
		if (job == null) {
			// don't know where it went, but it's no longer our responsibility.
			if (_logger.isDebugEnabled())
				_logger.debug(String.format("Couldn't find job for id %d, so I can't finish it.", jobID));
			return;
		}
		synchronized (job) {
		if (_logger.isDebugEnabled())
			_logger.debug("Finished job " + job);

		Connection connection = null;

		// get info about the BES if there is one.
		String besName = null;
		Long besID = job.getBESID();
		if (besID != null)
			besName = _besManager.getBESName(besID);
		// New 7/22/2017 by ASG. Just try the RPC, if it fails, then put it into the persistent caller DB
		connection = _connectionPool.acquire(false);
		// TEMP KillInformation killInfo = _database.getKillInfo(connection, job.getJobID(), besID);

		try {

			_database.incrementFinishCount(connection);


			if (besName == null) {
				/*
				 * handle a non-BES job by setting the state right now. this is because there is no job killer to deal with this later for us.
				 */
				_database.modifyJobState(connection, job.getJobID(), job.getRunAttempts(), QueueStates.FINISHED, new Date(), null, null,
					null);
			}

			connection.commit();
		} catch (SQLException sqe) {
			_logger.warn("Unable to update total jobs finished count.", sqe);
		} finally {
			_connectionPool.release(connection);
		}

		// This is one of the few times we are going to break our pattern and
		// modify the in memory state before the database. The reason for this
		// is that we can't afford to forget that the BES container has a job
		// on it that it's managing. If we do, we will eventually leak memory
		// on that container.
		SortableJobKey jobKey = new SortableJobKey(job);
		_queuedJobs.remove(jobKey);
		_runningJobs.remove(new Long(jobID));
		// removeFromUserBucket(_usersWithJobs, job);
		// putInUserBucket(_usersNotReadyJobs, job, job.getUserName());

		job.incrementRunAttempts();

		/*
		 * See failJob for a complete discussion of why we enqueue an outcall worker at this point -- the reasons are the same.
		 */
//		QueueStates _newState=QueueStates.FINISHED;
		if (besName != null) {
			if (_logger.isDebugEnabled())
				_logger.debug(String.format("Creating a JobKiller for finished job %s.", job));

			job.history(HistoryEventCategory.Terminating).debug("Finishing Job with JobKiller");
			_outcallThreadPool.enqueue(new JobKiller(job, QueueStates.FINISHED, false, true, besName, null));
/* TEMP boolean successfullCall=false;
			BESActivityTerminatorActor firstTry=new BESActivityTerminatorActor(_database.historyKey(job.getJobTicket()), 
				job.historyToken(), _besManager.getBESName(besID), killInfo.getJobEndpoint());
			try {
				successfullCall=firstTry.enactOutcall(killInfo.getCallingContext(), killInfo.getBESEndpoint(), null);
				}
			catch (Throwable e) {
			}
			if (!successfullCall) {
				_outcallThreadPool.enqueue(new JobKiller(job, QueueStates.FINISHED, false, true, besName, null));
				return;
			}
			else
			{
				_database.modifyJobState(connection, job.getJobID(), job.getRunAttempts(), _newState, new Date(), null, null, null);
				connection.commit();
				job.setJobState(_newState);
				job.clearBESID();
			}
			*/
		} else {
			if (_logger.isDebugEnabled())
				_logger.debug(String.format("Setting job %s to finished state without job killer (non-BES job).", job));

			job.setJobState(QueueStates.FINISHED);
			job.history(HistoryEventCategory.Terminating).debug("Finishing non-BES Job without JobKiller");
		}
		}
		// TEMP _schedulingEvent.notifySchedulingEvent();
	}

	public void killJob(Connection connection, long jobID) throws SQLException, ResourceException
	{
		/* Find the job in the in-memory maps */
		JobData job = _jobsByID.get(new Long(jobID));
		
		if (job == null) {
			// don't know where it went, but it's no longer our responsibility.
			if (_logger.isDebugEnabled())
				_logger.debug(String.format("Couldn't find job for id %d, so I can't kill it.", jobID));
			return;
		}
		synchronized (job) {
		if (_logger.isDebugEnabled())
			_logger.debug("Killing a running job:" + jobID);
		
		//LAK: Mark the job as killed. This is important if early in the creation phase. This will stop the jobs from being created.
		job.kill();

		// This is one of the few times we are going to break our pattern and
		// modify the in memory state before the database. The reason for this
		// is that we can't afford to forget that the BES container has a job
		// on it that it's managing. If we do, we will eventually leak memory
		// on that container.
		SortableJobKey jobKey = new SortableJobKey(job);
		_queuedJobs.remove(jobKey);
		_runningJobs.remove(new Long(jobID));
		// removeFromUserBucket(_usersWithJobs, job);
		// putInUserBucket(_usersNotReadyJobs, job, job.getUserName());
		job.incrementRunAttempts();

		Long besID = job.getBESID();
		/* This shouldn't be necessary */
		/*
		 * _database.modifyJobState(connection, job.getJobID(), job.getRunAttempts(), QueueStates.FINISHED, new Date(), null, null, null);
		 * connection.commit();
		 */

		/*
		 * Finally, note the new state in memory and clear the old BES information.
		 */
		job.setJobState(QueueStates.FINISHED);
		job.clearBESID();

		/*
		 * Otherwise, we assume that he's already in the right list
		 */
		if (_logger.isDebugEnabled())
			_logger.debug("KillJob::Moving job \"" + job.getJobTicket() + "\" to the " + QueueStates.FINISHED + " state.");

		String besName = null;
		if (besID != null)
			besName = _besManager.getBESName(besID);

		_outcallThreadPool.enqueue(new JobKiller(job, QueueStates.FINISHED, true, true, besName, besID));
		_schedulingEvent.notifySchedulingEvent();
		}
	}

	/**
	 * Submit a new job into the queue.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param jsdl
	 *            The job's job description.
	 * @param priority
	 *            The job's priority.
	 * 
	 * @return The job ticket assigned to this job.
	 * 
	 * @throws SQLException
	 * @throws ConfigurationException
	 * @throws ResourceException
	 */
	public String submitJob(Connection connection, JobDefinition_Type jsdl, short priority) throws SQLException, ResourceException
	{
		/*
		 * First, generate a new ticket for the job. If we were being paranoid, we'd check to see if the ticket already exists, but the
		 * chances of that are astronomically slim (a fact that we count on for generating EPIs)
		 */
		String ticket = new GUID().toString();
		return submitJob(connection, jsdl, priority, ticket);
	}

	/**
	 * Submit a new job into the queue.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param jsdl
	 *            The job's job description.
	 * @param priority
	 *            The job's priority.
	 * @param ticket
	 *            The previously constructed ticket for this job.
	 * 
	 * @return The job ticket passed in if all is successful.
	 */
	synchronized public String submitJob(Connection connection, JobDefinition_Type jsdl, short priority, String ticket)
		throws SQLException, ResourceException
	{
		// bump out the notification checking time, because this is a real queue job that we
		// want to give precedence.
		_whenToProcessNotifications = Calendar.getInstance();
		_whenToProcessNotifications.add(Calendar.MILLISECOND, NOTIFICATION_CHECKING_DELAY);

		// LAK: synchronized to keep this from running while createActivity is also running
		synchronized(_jobsByTicket)
		{
		try {

			/*
			 * Go ahead and get the current caller's calling context. We store this so that we can make outcalls in the future on his/her
			 * behalf.
			 */
			ICallingContext callingContext = ContextManager.getExistingContext();

			/*
			 * Similarly, get the current caller's security identity so that we can store that. This is used to protect different users of the
			 * queue from each other. We use this to check against others killing, getting the status of, or completing someone elses jobs.
			 */
			Collection<Identity> identities = QueueSecurity.getCallerIdentities(true);

			if (identities.size() <= 0)
				throw new ResourceException("Cannot submit a job with no non-group credentials.");

			/*
			 * The job starts out in the queued state and with the current time as it's submit time.
			 */
			QueueStates state = QueueStates.QUEUED;
			Date submitTime = new Date();

			HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.Default, _database.historyKey(ticket));

			/*
			 * Submit the job information into the queue (and get a new jobID from the database for it).
			 */

			int numOfCores = 1;
			JobRequest jobRequest = null;
			try {
				jobRequest = JobRequestParser.parse(jsdl);
				if ((jobRequest != null) && (jobRequest.getSPMDInformation() != null)) {
					numOfCores = jobRequest.getSPMDInformation().getNumberOfProcesses();
				}
			} catch (JSDLException e) {
				_logger.error("caught jsdl exception in submitJob", e);
			} catch (Exception ex) {
				_logger.error("caught exception in submitJob", ex);
			}

			long jobID = _database.submitJob(connection, ticket, priority, jsdl, callingContext, identities, state, submitTime, numOfCores);
			if (MyProxyCertificate.isAvailable())
				_database.setSecurityHeader(connection, jobID, MyProxyCertificate.getPEMString());

			connection.commit();

			if (MyProxyCertificate.isAvailable())
				MyProxyCertificate.reset();

			history.createInfoWriter("Queue Accepted Job").format("Queue accepted job with ticket %s (job-id = %d).", ticket, jobID).close();

			/*
			 * The data has been committed into the database so we can reload to this point from here on out.
			 */

			if (_logger.isDebugEnabled())
				_logger.debug("Submitted job \"" + ticket + "\" as job number " + jobID);

			/*
			 * Create a new data structure for the job's in memory information and put it into the in-memory lists. Get the SMPD information
			 * for the job
			 */

			JobData job =
				new JobData(jobID, QueueUtils.getJobName(jsdl), ticket, priority, state, submitTime, (short) 0, history, numOfCores, -1, -1, null);

			SortableJobKey jobKey = new SortableJobKey(jobID, priority, submitTime);



			/*
			 * As jobs are added to the primary list, they are also added to the appropriate user list (newly created, if needed)
			 */
			Collection<Identity> fullIdentities = identities;
			identities = SecurityUtilities.filterCredentials(identities, SecurityUtilities.CLIENT_IDENTITY_PATTERN);

			String username = identities.iterator().next().toString();

			if (_logger.isTraceEnabled())
				_logger.debug("chose username using CLIENT_IDENTITY_PATTERN: " + username);


			/*
			 * Added by ASG 4/25/2016. Add the username, and start/finish to the job record.
			 */
			job.setUserName(username);
			job.setStartTime(new Date());
			job.setFinishTime(new Date());
			if (!_userIDs.containsKey(username)) {

				try {
					_userIDs.put(username, QueueSecurity.convert(fullIdentities));
					// System.out.println("ADding " + username +" to userIDs");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			// End of _userIDs updates
			/*
			 * We've just added a new job to the queue, so we have a new scheduling opportunity.
			 */
			putInUserBucket(_usersWithJobs, job, username);
			_jobsByID.put(new Long(jobID), job);
			_jobsByTicket.put(ticket, job);
			_queuedJobs.put(jobKey, job);
			_schedulingEvent.notifySchedulingEvent();

			return ticket;
		} catch (IOException ioe) {
			if (_logger.isDebugEnabled())
				_logger.debug("Failed to submit job from jsdl: " + jsdl.toString());
			throw new ResourceException("Unable to submit job.", ioe);
		}
		}
	}

	synchronized public String submitJob(SweepingJob sweep, Connection connection, JobDefinition_Type jsdl, short priority)
		throws SQLException, ResourceException
	{
		// bump out the notification checking time, because this is a real queue job that we
		// want to give precedence.
		_whenToProcessNotifications = Calendar.getInstance();
		_whenToProcessNotifications.add(Calendar.MILLISECOND, NOTIFICATION_CHECKING_DELAY);
		ICallingContext callingContext = null;

		try {
			// format a guid plus a colon to distinguish from normal jobs.
			String tickynum = new GUID().toString() + ":";

			/*
			 * Go ahead and get the current caller's calling context. We store this so that we can make outcalls in the future on his/her
			 * behalf.
			 */
			callingContext = ContextManager.getExistingContext();

			/*
			 * Similarly, get the current caller's security identity so that we can store that. This is used to protect different users of the
			 * queue from each other. We use this to check against others killing, getting the status of, or completing someone elses jobs.
			 */
			Collection<Identity> identities = QueueSecurity.getCallerIdentities(true);
			Collection<Identity> fullIdentities = identities;

			if (identities.size() <= 0)
				throw new ResourceException("Cannot submit a job with no non-group credentials.");

			/*
			 * The job starts out in the queued state and with the current time as it's submit time.
			 */
			QueueStates state = QueueStates.RUNNING;
			Date submitTime = new Date();

			HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.Default, _database.historyKey(tickynum));

			// hook in the history context for the special sweep job.
			sweep.setHistory(history);

			/*
			 * Submit the job information into the queue (and get a new jobID from the database for it).
			 */

			int numOfCores = 1;
			// try {
			// JobRequest jobRequest = null;
			// jobRequest = JobRequestParser.parse(jsdl);
			// if ((jobRequest != null) && (jobRequest.getSPMDInformation() != null)) {
			// numOfCores = jobRequest.getSPMDInformation().getNumberOfProcesses();
			// }
			// } catch (JSDLException e) {
			// _logger.error("caught jsdl exception in submitJob", e);
			// } catch (Exception ex) {
			// _logger.error("caught exception in submitJob", ex);
			// }

			long jobID =
				_database.submitJob(sweep, connection, tickynum, priority, jsdl, callingContext, identities, state, submitTime, numOfCores);
			sweep.setJobId(jobID);

			if (MyProxyCertificate.isAvailable())
				_database.setSecurityHeader(connection, jobID, MyProxyCertificate.getPEMString());

			connection.commit();

			if (MyProxyCertificate.isAvailable())
				MyProxyCertificate.reset();

			history.createInfoWriter("Queue Accepted Sweep Job")
				.format("Queue accepted parameter sweep job with ticket %s (job-id = %d).", tickynum, jobID).close();

			/*
			 * The data has been committed into the database so we can reload to this point from here on out.
			 */

			if (_logger.isDebugEnabled())
				_logger.debug("Submitted sweeping job \"" + tickynum + "\" as job number " + jobID);

			/*
			 * Create a new data structure for the job's in memory information and put it into the in-memory lists.
			 */
			JobData job = new JobData(sweep, jobID, PARAMETER_SWEEP_NAME_ADDITION + QueueUtils.getJobName(jsdl), tickynum, priority, state,
				submitTime, (short) 0, history, numOfCores, -1, -1, null);

			SortableJobKey jobKey = new SortableJobKey(jobID, priority, submitTime);

			_jobsByID.put(new Long(jobID), job);
			_jobsByTicket.put(tickynum, job);
			_queuedJobs.put(jobKey, job);

			/*
			 * As jobs are added to the primary list, they are also added to the appropriate user list (newly created, if needed)
			 */
			identities = SecurityUtilities.filterCredentials(identities, SecurityUtilities.CLIENT_IDENTITY_PATTERN);

			String username = identities.iterator().next().toString();

			if (_logger.isTraceEnabled())
				_logger.debug("chose username using CLIENT_IDENTITY_PATTERN: " + username);

			putInUserBucket(_usersWithJobs, job, username);
			/*
			 * Added by ASG 4/25/2016. Add the username, and start/finish to the job record.
			 */

			job.setUserName(username);
			job.setStartTime(new Date());
			job.setFinishTime(new Date());
			if (!_userIDs.containsKey(username)) {

				try {
					_userIDs.put(username, QueueSecurity.convert(fullIdentities));
					// System.out.println("ADding " + username +" to userIDs");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			// End of _userIDs updates
			// now that the sweep has been set up with an id and is in the database, we can begin adding jobs based on it.
			boolean ret = false;
			try {
				ret = sweep.createSweep();
			} catch (SweepException e) {
				_logger.error("caught exception when creating sweep", e);
			}
			if (ret != true) {
				_logger.error("sweep startup failed.  sweeping job cannot progress.");
				job.setJobState(QueueStates.ERROR);
				return tickynum;
			}
			// and with it having begun the sweep, we can crank up its thread to monitor the state.
			sweep.start();

			/*
			 * We've just added a new job to the queue, so we have a new scheduling opportunity.
			 */
			_schedulingEvent.notifySchedulingEvent();

			return tickynum;
		} catch (IOException ioe) {
			if (_logger.isDebugEnabled())
				_logger.debug("Failed to submit sweeping job from jsdl: " + jsdl.toString());
			throw new ResourceException("Unable to submit sweeping job.", ioe);
		}
	}

	public EndpointReferenceType getActivityEPR(Connection connection, String jobTicket) throws ResourceException, SQLException
	{
		JobData jData = _jobsByTicket.get(jobTicket);
		// REMOVEJobStatusInformation info = _database.getJobStatusInformation(connection, jData.getJobID());
		// REMOVE return info.getJobEndpoint();
		return jData.getJobEPR();
	}

	/**
	 * List all jobs currently in the queue. This operation is considered "safe" from a security point of view and is not subject to verifying
	 * caller's identity (beyond basic ACL on the service operation itself).
	 * 
	 * @param connection
	 *            The database connection to use.
	 * 
	 * @return The list of all jobs contained in the queue.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized public Collection<ReducedJobInformationType> listJobs(Connection connection, String ticket)
		throws SQLException, ResourceException
	{
		Collection<ReducedJobInformationType> ret = new LinkedList<ReducedJobInformationType>();

		/*
		 * We have to ask the database for some information about the jobs that isn't kept in memory (the owner identities for example).
		 */
		Set<Long> keySet = null;
		if (ticket != null) {
			keySet = new HashSet<Long>();
			JobData data = _jobsByTicket.get(ticket);
			if (data != null)
				keySet.add(new Long(data.getJobID()));
		} else
			keySet = _jobsByID.keySet();

		/*
		 * 2016-04-29 by ASG. Updated logic to not use partial job info. Instead, use the newly updated JobData definition that has the
		 * partial job info in it already, eliminating the need to go to the database.
		 */
		try {
			/* For each job in the queue... */
			// for (Long jobID : ownerMap.keySet()) {
			for (Long jobID : keySet) {
				/* Get the in-memory data for the job */
				JobData jobData = _jobsByID.get(jobID.longValue());

				/* Find the corresponding database information */

				byte[][] owners = _userIDs.get(jobData.getUserName());
				if (owners == null) {
					// Big problem
					throw new ResourceException("Job owners not found.");
				}

				/* And add the sum of that too the return list */
				ret.add(new ReducedJobInformationType(jobData.getJobTicket(), owners,
					JobStateEnumerationType.fromString(jobData.getJobState().name())));
			}

			return ret;
		} catch (IOException ioe) {
			throw new ResourceException("Unable to serialize owner information.", ioe);
		}
	}

	public JobDefinition_Type getJSDL(long jobID) throws SQLException, ResourceException
	{
		Connection connection = null;

		try {
			connection = _connectionPool.acquire(true);
			return _database.getJSDL(connection, jobID);
		} finally {
			_connectionPool.release(connection);
		}
	}

	public void updateJSDL(JobDefinition_Type jsdl, long jobID) throws SQLException, ResourceException
	{
		Connection connection = null;

		try {
			connection = _connectionPool.acquire(true);
			_database.updateJSDL(connection, jsdl, jobID);
		} finally {
			_connectionPool.release(connection);
		}
	}

	public void updateJSDL(JobDefinition_Type jsdl, String jobID) throws SQLException, ResourceException
	{
		Connection connection = null;

		try {
			connection = _connectionPool.acquire(true);
			_database.updateJSDL(connection, jsdl, jobID);
		} finally {
			_connectionPool.release(connection);
		}
	}

	public EndpointReferenceType getLogEPR(String jobTicket) throws ResourceException, SQLException
	{
		return getLogEPR(_jobsByTicket.get(jobTicket).getJobID());
	}

	public EndpointReferenceType getLogEPR(long jobID) throws ResourceException, SQLException
	{
		Connection connection = null;

		try {
			connection = _connectionPool.acquire(true);
			return _database.getLogEPR(connection, jobID);
		} finally {
			_connectionPool.release(connection);
		}
	}

	public void printLog(long jobID, PrintStream out) throws IOException
	{
		InputStream in = null;

		try {
			EndpointReferenceType epr = getLogEPR(jobID);
			in = ByteIOStreamFactory.createInputStream(epr);
			StreamUtils.copyStream(in, out);
		} catch (SQLException e) {
			throw new IOException("Unable to print log for job.", e);
		} finally {
			StreamUtils.close(in);
		}
	}

	public JobDefinition_Type getJSDL(String ticket) throws ResourceException, SQLException
	{
		return getJSDL(_jobsByTicket.get(ticket).getJobID());
	}

	public void printLog(String ticket, PrintStream out) throws IOException
	{
		printLog(_jobsByTicket.get(ticket).getJobID(), out);
	}

	/**
	 * Get the job status for jobs in the queue. This operation IS subject to owner verification. This means that only the owner of a job is
	 * allowed to get the status of his or her job. This particular operation doesn't take a list of jobs to get the status of so it's assumed
	 * that the user wants status on ALL jobs that HE/SHE owns.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * 
	 * @return The list of statuses for all jobs owned by the caller.
	 * @throws GenesisIISecurityException
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */

	public Collection<JobInformationType> getJobStatus(Connection connection)
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		Collection<JobInformationType> ret = new LinkedList<JobInformationType>();

		/*
		 * We need to get a few pieces of information that only the database has, such as job owner.
		 */
		/*
		 * 2016-04-29 by ASG. Updated logic to not use partial job info. Instead, use the newly updated JobData definition that has the
		 * partial job info in it already, eliminating the need to go to the database.
		 */

		/*
		 * Look through all the jobs managed by this queue looking for the right ones.
		 */

		// Determine administrators of queue
		Collection<Identity> identities = QueueSecurity.getCallerIdentities(true);
		identities = SecurityUtilities.filterCredentials(identities, SecurityUtilities.CLIENT_IDENTITY_PATTERN);

		String uname = identities.iterator().next().toString();

		boolean isAdmin = QueueSecurity.isQueueAdmin();

		for (Long jobID : _jobsByID.keySet()) {

			/* Get the in-memory information for this job */
			String scheduledOn = null;
			JobData jobData = _jobsByID.get(jobID);
			String username = jobData.getUserName();

			BESData besData = _besManager.findBES(jobData.getBESID());
			if (besData != null)
				scheduledOn = besData.getName();
			/* Get the database information for this job */
			// Commented out by ASG 2016-4-29 PartialJobInfo pji = ownerMap.get(jobID);
			byte[][] owners = _userIDs.get(jobData.getUserName());
			if (isAdmin) {
				ret.add(
					new JobInformationType(jobData.getJobTicket(), owners, JobStateEnumerationType.fromString(jobData.getJobState().name()),
						(byte) jobData.getPriority(), QueueUtils.convert(jobData.getSubmitTime()), QueueUtils.convert(jobData.getStartTime()),
						QueueUtils.convert(jobData.getFinishTime()), new UnsignedShort(jobData.getRunAttempts()), scheduledOn,
						jobData.getBESActivityStatus(), jobData.jobName()));
			}

			/*
			 * If the caller owns this jobs, then add the status information for the job to the result list.
			 */
			else {
				// Here we want to only get jobs for this owner.

				if (username.equalsIgnoreCase(uname)) {
					ret.add(new JobInformationType(jobData.getJobTicket(), owners,
						JobStateEnumerationType.fromString(jobData.getJobState().name()), (byte) jobData.getPriority(),
						QueueUtils.convert(jobData.getSubmitTime()), QueueUtils.convert(jobData.getStartTime()),
						QueueUtils.convert(jobData.getFinishTime()), new UnsignedShort(jobData.getRunAttempts()), scheduledOn,
						jobData.getBESActivityStatus(), jobData.jobName()));
				}
			}

		}

		return ret;
	}

	public ActivityStatusType getBESActivityStatus(String ticket)
	{
		JobData data = _jobsByTicket.get(ticket);
		if (data == null)
			return null;

		ActivityStatusType ret = data.getBESActivityStatus();
		if (ret == null) {
			QueueStates state = data.getJobState();
			switch (state) {
				case ERROR:
					ret = new ActivityStatusType(null, ActivityStateEnumeration.Failed);
					break;

				case FINISHED:
					ret = new ActivityStatusType(null, ActivityStateEnumeration.Finished);
					break;

				case QUEUED:
					ret = new ActivityStatusType(null, ActivityStateEnumeration.Pending);
					break;

				case REQUEUED:
					ret = new ActivityStatusType(null, ActivityStateEnumeration.Pending);
					break;

				case RUNNING:
					ret = new ActivityStatusType(null, ActivityStateEnumeration.Running);
					break;

				case STARTING:
					ret = new ActivityStatusType(null, ActivityStateEnumeration.Pending);
					break;

			}
		}

		return ret;
	}

	/**
	 * Get the job status for jobs in the queue. This operation IS subject to owner verification. This means that only the owner of a job is
	 * allowed to get the status of his or her job. This particular operation takes a list of jobs to get status for. If the list is empty or
	 * null, then we get the status for all jobs owned by the caller. If the list is not empty, then we get status for all jobs listed unless
	 * one of them is not owned by the caller, in which case we'll throw an exception indicating that the caller doesn't have permission to
	 * get the status of one of the requested jobs.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param jobs
	 *            The list of jobs (by ticket) to get the status for.
	 * 
	 * @return The list of job statuses requested.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized public Collection<JobInformationType> getJobStatus(Connection connection, String[] jobs)
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		/*
		 * Check to see if any tickets were passed and call the "all jobs owned by me" version if none were.
		 */
		if (jobs == null || jobs.length == 0)
			return getJobStatus(connection);

		Collection<JobInformationType> ret = new LinkedList<JobInformationType>();

		/*
		 * First, get the job IDs of all jobs requested by the user.
		 */
		Collection<Long> jobIDs = new LinkedList<Long>();
		for (String ticket : jobs) {
			JobData data = _jobsByTicket.get(ticket);
			if (data == null)
				throw new ResourceException("Job \"" + ticket + "\" does not exist in queue.");

			jobIDs.add(new Long(data.getJobID()));
		}

		/*
		 * Now ask the database to fill in information about these jobs that we don't have in memory (like owner).
		 */
		HashMap<Long, PartialJobInfo> ownerMap = _database.getPartialJobInfos(connection, jobIDs);

		try {
			boolean isAdmin = QueueSecurity.isQueueAdmin();
			Collection<Identity> callers = QueueSecurity.getCallerIdentities(true);

			/*
			 * Loop through the jobs checking to make sure that they are all owned by the caller.
			 */
			for (Long jobID : ownerMap.keySet()) {
				JobData jobData = _jobsByID.get(jobID.longValue());
				BESData besData = _besManager.findBES(jobData.getBESID());
				String scheduledOn = null;
				if (besData != null)
					scheduledOn = besData.getName();

				PartialJobInfo pji = ownerMap.get(jobID);

				/*
				 * If the job is owned by the caller, add the job's status information to the result list.
				 */

				if (isAdmin) {
					ret.add(new JobInformationType(jobData.getJobTicket(), QueueSecurity.convert(pji.getOwners()),
						JobStateEnumerationType.fromString(jobData.getJobState().name()), (byte) jobData.getPriority(),
						QueueUtils.convert(jobData.getSubmitTime()), QueueUtils.convert(pji.getStartTime()),
						QueueUtils.convert(pji.getFinishTime()), new UnsignedShort(jobData.getRunAttempts()), scheduledOn,
						jobData.getBESActivityStatus(), jobData.jobName()));
				} else {

					if (QueueSecurity.isOwner(pji.getOwners(), callers)) {
						ret.add(new JobInformationType(jobData.getJobTicket(), QueueSecurity.convert(pji.getOwners()),
							JobStateEnumerationType.fromString(jobData.getJobState().name()), (byte) jobData.getPriority(),
							QueueUtils.convert(jobData.getSubmitTime()), QueueUtils.convert(pji.getStartTime()),
							QueueUtils.convert(pji.getFinishTime()), new UnsignedShort(jobData.getRunAttempts()), scheduledOn,
							jobData.getBESActivityStatus(), jobData.jobName()));
					}

					else {
						/*
						 * If the caller did not own a job, then we throw a security exception.
						 */
						throw new GenesisIISecurityException("Not permitted to get status of job \"" + jobData.getJobTicket() + "\".");
					}
				}
			}
			return ret;
		} catch (GenesisIISecurityException gse) {
			throw gse;
		} catch (IOException ioe) {
			throw new ResourceException("Unable to serialize owner information.", ioe);
		}
	}

	synchronized public JobInformationType getStatusFromID(Connection conn, Long jobID)
		throws ResourceException, SQLException, GenesisIISecurityException
	{

		if (conn == null || jobID == null)
			throw new ResourceException("Unable to query job status");

		JobData job = _jobsByID.get(jobID.longValue());
		Collection<Identity> callers = QueueSecurity.getCallerIdentities(true);
		boolean isAdmin = QueueSecurity.isQueueAdmin();
		callers = SecurityUtilities.filterCredentials(callers, SecurityUtilities.CLIENT_IDENTITY_PATTERN);
		String callerName = callers.iterator().next().toString();
		if (job == null) {
			throw new ResourceException("A queried job has been removed from the queue");
		}

		BESData besData = _besManager.findBES(job.getBESID());
		String scheduledOn = null;
		if (besData != null)
			scheduledOn = besData.getName();
		String username = job.getUserName();
		byte[][] owners = _userIDs.get(job.getUserName());
		if (isAdmin || (username.equalsIgnoreCase(callerName))) {
			return new JobInformationType(job.getJobTicket(), owners, JobStateEnumerationType.fromString(job.getJobState().name()),
				(byte) job.getPriority(), QueueUtils.convert(job.getSubmitTime()), QueueUtils.convert(job.getStartTime()),
				QueueUtils.convert(job.getFinishTime()), new UnsignedShort(job.getRunAttempts()), scheduledOn, job.getBESActivityStatus(),
				job.jobName());
		} else
			throw new GenesisIISecurityException("Not permitted to get status of job \"" + job.getJobTicket() + "\".");
	}

	synchronized public QueueInMemoryIteratorEntry getIterableJobStatus(Connection connection, String[] jobs)
		throws GenesisIISecurityException, ResourceException, SQLException
	{
		_logger.debug("Entering jobManager::getIterableJobStatus new");
		if (connection == null)
			throw new ResourceException("Unable to query job status");

		if (jobs == null || jobs.length == 0) {
			QueueInMemoryIteratorEntry temp=getIterableJobStatus(connection);
			_logger.debug("Exiting jobManager::getIterableJobStatus");
			return temp;
		}

		boolean isAdmin = QueueSecurity.isQueueAdmin();
		Collection<JobInformationType> ret = new LinkedList<JobInformationType>();
		Collection<String> toIterate = new LinkedList<String>();

		/*
		 * First, get the job IDs of all jobs requested by the user.
		 */
		Collection<Long> jobIDs = new LinkedList<Long>();

		for (String ticket : jobs) {
			JobData data = _jobsByTicket.get(ticket);
			if (data == null)
				throw new ResourceException("Job \"" + ticket + "\" does not exist in queue.");

			jobIDs.add(new Long(data.getJobID()));
		}

		if (isAdmin) {
			Collection<Long> batchSubset = new LinkedList<Long>();
			Iterator<Long> it = jobIDs.iterator();

			HashMap<Long, PartialJobInfo> ownerMap;

			// iterator has to be built
			if (jobIDs.size() > QueueConstants.PREFERRED_BATCH_SIZE) {
				for (int lcv = 0; lcv < QueueConstants.PREFERRED_BATCH_SIZE; lcv++) {
					batchSubset.add(it.next());
				}

				/*
				 * Now ask the database to fill in information about the subset jobs that we don't have in memory (like owner).
				 */
				ownerMap = _database.getPartialJobInfos(connection, batchSubset);
			} else {
				/*
				 * Now ask the database to fill in information about all jobs that we don't have in memory (like owner).
				 */
				ownerMap = _database.getPartialJobInfos(connection, jobIDs);
			}

			for (Long jobID : ownerMap.keySet()) {
				JobData jobData = _jobsByID.get(jobID.longValue());
				BESData besData = _besManager.findBES(jobData.getBESID());
				String scheduledOn = null;
				if (besData != null)
					scheduledOn = besData.getName();

				PartialJobInfo pji = ownerMap.get(jobID);
				try {
					ret.add(new JobInformationType(jobData.getJobTicket(), QueueSecurity.convert(pji.getOwners()),
						JobStateEnumerationType.fromString(jobData.getJobState().name()), (byte) jobData.getPriority(),
						QueueUtils.convert(jobData.getSubmitTime()), QueueUtils.convert(pji.getStartTime()),
						QueueUtils.convert(pji.getFinishTime()), new UnsignedShort(jobData.getRunAttempts()), scheduledOn,
						jobData.getBESActivityStatus(), jobData.jobName()));
				} catch (IOException ioe) {
					throw new ResourceException("Unable to serialize owner information.", ioe);
				}

			}

			if (jobIDs.size() > QueueConstants.PREFERRED_BATCH_SIZE) {
				while (it.hasNext()) {
					toIterate.add(it.next().toString());
				}
			}
			_logger.debug("Exiting jobManager::getIterableStatus");
			if (toIterate.size() == 0) // no iterator
				return new QueueInMemoryIteratorEntry(false, ret, toIterate);

			else
				// iterator needed
				return new QueueInMemoryIteratorEntry(true, ret, toIterate);

		}

		else // NotAdmin
		{
			HashMap<Long, PartialJobInfo> ownerMap = _database.getPartialJobInfos(connection, jobIDs);
			Iterator<Long> it = ownerMap.keySet().iterator();
			Collection<Identity> callers = QueueSecurity.getCallerIdentities(true);

			try {

				for (int lcv = 0; lcv < QueueConstants.PREFERRED_BATCH_SIZE; lcv++) {

					if (it.hasNext()) {
						Long jobID = it.next();

						JobData jobData = _jobsByID.get(jobID.longValue());
						BESData besData = _besManager.findBES(jobData.getBESID());
						String scheduledOn = null;
						if (besData != null)
							scheduledOn = besData.getName();

						PartialJobInfo pji = ownerMap.get(jobID);

						if (QueueSecurity.isOwner(pji.getOwners(), callers)) {
							ret.add(new JobInformationType(jobData.getJobTicket(), QueueSecurity.convert(pji.getOwners()),
								JobStateEnumerationType.fromString(jobData.getJobState().name()), (byte) jobData.getPriority(),
								QueueUtils.convert(jobData.getSubmitTime()), QueueUtils.convert(pji.getStartTime()),
								QueueUtils.convert(pji.getFinishTime()), new UnsignedShort(jobData.getRunAttempts()), scheduledOn,
								jobData.getBESActivityStatus(), jobData.jobName()));

						}

						else {
							/*
							 * If the caller did not own a job, then we throw a security exception.
							 */
							throw new GenesisIISecurityException("Not permitted to get status of job \"" + jobData.getJobTicket() + "\".");
						}

					}

					else
						// There are less than first batch-size # of jobs
						break;
				}
			}

			catch (IOException ioe) {
				throw new ResourceException("Unable to serialize owner information.", ioe);
			}

			while (it.hasNext()) // we might have to build iterator
			{

				Long jobID = it.next();
				PartialJobInfo pji = ownerMap.get(jobID);

				// is the caller the owner of this job?
				if (QueueSecurity.isOwner(pji.getOwners(), callers)) {
					toIterate.add(jobID.toString());
				}

				else
					throw new GenesisIISecurityException(
						"Not permitted to get status of job \"" + _jobsByID.get(jobID.longValue()).getJobTicket() + "\".");

			}
			_logger.debug("Exiting jobManager::getIterableStatus");
			if (toIterate.size() == 0) // no iterator
				return new QueueInMemoryIteratorEntry(false, ret, toIterate);
			else
				// iterator needed
				return new QueueInMemoryIteratorEntry(true, ret, toIterate);

		}

	}

	public QueueInMemoryIteratorEntry getIterableJobStatus(Connection connection)
		throws GenesisIISecurityException, ResourceException, SQLException
	{
		Collection<JobInformationType> ret = new ArrayList<JobInformationType>(_jobsByID.size() + 1);
		Collection<String> toIterate = new ArrayList<String>(_jobsByID.size() + 1); // Changed from linked list by ASG 2016-05-04
		Collection<Identity> callers = QueueSecurity.getCallerIdentities(true);
		Iterator<Long> it;
		if (connection == null)
			throw new ResourceException("Unable to query job status");
		_logger.debug("Entering jobManager::getIterableStatus-2");
		boolean isAdmin = QueueSecurity.isQueueAdmin();
		Collection<Long> batchSubset = new ArrayList<Long>(_jobsByID.size() + 1);

		if (!isAdmin) // is the caller qAdmin ?
		{
			callers = SecurityUtilities.filterCredentials(callers, SecurityUtilities.CLIENT_IDENTITY_PATTERN);
			String username = callers.iterator().next().toString();

			if (_usersWithJobs.keySet().contains(username) || _usersNotReadyJobs.keySet().contains(username)) {
				// Add the job to this user's set
				TreeMap<SortableJobKey, JobData> usersJobs = _usersWithJobs.get(username);
				if (usersJobs != null) {
					Iterator<SortableJobKey> jobKeys = usersJobs.keySet().iterator();
					while (jobKeys.hasNext()) {
						SortableJobKey job = jobKeys.next();
						batchSubset.add(job.getJobID());
					}
				}

				// Now add their not ready to run jobs, i.e., old jobs or running jobs
				usersJobs = _usersNotReadyJobs.get(username);
				if (usersJobs != null) {
					Iterator<SortableJobKey> jobKeys = usersJobs.keySet().iterator();

					while (jobKeys.hasNext()) {
						SortableJobKey job = jobKeys.next();
						batchSubset.add(job.getJobID());
					}
				}
			}
		} else {
			// Admin gets all the jobs
			it = _jobsByID.keySet().iterator();
			for (int i = 0; i < _jobsByID.size(); i++) {
				// grabs the first batch-size amount of jobs
				batchSubset.add(it.next());
			}
		}
		// We now have all of the jobs we want to iterate over in batchSubset
		Iterator<Long> thisBatch = batchSubset.iterator();
		int lcv = 0;
		while (thisBatch.hasNext() && (lcv < QueueConstants.PREFERRED_BATCH_SIZE)) {
			Long jobID = thisBatch.next();
			lcv++;
			/* Get the in-memory information for this job */
			String scheduledOn = null;
			JobData job = _jobsByID.get(jobID);
			if (job == null)
				continue; // This seems to happen sometimes.
			BESData besData = _besManager.findBES(job.getBESID());
			if (besData != null) {
				scheduledOn = besData.getName();
			}

			String username = job.getUserName();
			// System.out.println("##### Getting information on " + username + " for jobid " + jobID);
			byte[][] owners = _userIDs.get(job.getUserName());
			if (username.equalsIgnoreCase("Not Defined")) {
				System.out.println("##### Not Defined on " + job.jobName() + " for jobid " + jobID);
				continue;
			}
			ret.add(new JobInformationType(job.getJobTicket(), owners, JobStateEnumerationType.fromString(job.getJobState().name()),
				(byte) job.getPriority(), QueueUtils.convert(job.getSubmitTime()), QueueUtils.convert(job.getStartTime()),
				QueueUtils.convert(job.getFinishTime()), new UnsignedShort(job.getRunAttempts()), scheduledOn, job.getBESActivityStatus(),
				job.jobName()));
		}

		if (batchSubset.size() > QueueConstants.PREFERRED_BATCH_SIZE) {
			while (thisBatch.hasNext()) {
				toIterate.add(thisBatch.next().toString());
			}
		}
		_logger.debug("Exiting jobManager::getIterableStatus-2");
		if (toIterate.size() == 0) // no iterator
			return new QueueInMemoryIteratorEntry(false, ret, toIterate);

		else
			// iterator needed
			return new QueueInMemoryIteratorEntry(true, ret, toIterate);

	}

	public JobErrorPacket[] queryErrorInformation(Connection connection, String job)
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		JobData jobData = _jobsByTicket.get(job);
		if (jobData == null)
			throw new ResourceException(String.format("Unable to find job %s in queue.", job));

		/*
		 * Now ask the database to fill in information about these jobs that we don't have in memory (like owner).
		 */
		Collection<Long> jobList = new ArrayList<Long>(1);
		jobList.add(jobData.getJobID());
		HashMap<Long, PartialJobInfo> ownerMap = _database.getPartialJobInfos(connection, jobList);

		PartialJobInfo pji = ownerMap.get(jobData.getJobID());

		/*
		 * If the job is owned by the caller, add the job's status information to the result list.
		 */
		if (!QueueSecurity.isOwner(pji.getOwners())) {
			/*
			 * If the caller did not own a job, then we throw a security exception.
			 */
			throw new GenesisIISecurityException("Not permitted to get status of job \"" + jobData.getJobTicket() + "\".");
		}

		List<Collection<String>> errors = _database.getAttemptErrors(connection, jobData.getJobID());

		Collection<JobErrorPacket> packets = new LinkedList<JobErrorPacket>();

		for (int lcv = 0; lcv < errors.size(); lcv++) {
			Collection<String> strings = errors.get(lcv);
			if (strings != null) {
				packets.add(new JobErrorPacket(new UnsignedShort(lcv), strings.toArray(new String[0])));
			}
		}

		return packets.toArray(new JobErrorPacket[0]);
	}

	/**
	 * This method completes (or removes from the queue) all jobs which are owned by the caller and which are in a final state. Only jobs in a
	 * final state can be completed. To try and complete a job which is not in a final state is considered an error and an exception will be
	 * thrown. This operation is only called internally and it is assumed that the calling method has already checked the ownership and the
	 * final state.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param jobsToComplete
	 *            The list of jobs to complete.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	// Took out synchronized ... ASG
	private void completeJobs(Connection connection, Collection<Long> jobsToComplete) throws SQLException, ResourceException
	{
		_logger.debug("Entering jobManager::completeJobs 2");
		/* First, remove the job from the database and commit the changes. */
		_database.completeJobs(connection, jobsToComplete);
		connection.commit();

		/*
		 * Now that it's committed to the database, go through the in memory data structures and remove the job from all of those.
		 */
		for (Long jobID : jobsToComplete) {
			if (_logger.isDebugEnabled())
				_logger.debug(String.format("Completing job %d.", jobID));

			/*
			 * Get and remove the job information (which has the ticket needed to remove the job from the "byTickets" map).
			 */
			JobData data = _jobsByID.remove(jobID);
			if (data != null) {
				SortableJobKey jobKey = new SortableJobKey(data);
				/* Remove the job from all lists */
				_jobsByTicket.remove(data.getJobTicket());
				_queuedJobs.remove(jobKey);
				_runningJobs.remove(jobID);
				removeFromUserBucket(_usersWithJobs, data);
				removeFromUserBucket(_usersNotReadyJobs, data);

				HistoryContainerService service = ContainerServices.findService(HistoryContainerService.class);
				service.enqueue(_database.historyKey(data.getJobTicket()), connection);
			}
		}
		_logger.debug("Exiting jobManager::completeJobs 2");

	}

	synchronized private void rescheduleJobs(Connection connection, Collection<Long> jobs)
	{
		for (Long jobID : jobs) {
			try {
				if (_logger.isDebugEnabled())
					_logger.debug(String.format("Rescheduling job %d.", jobID));
				failJob(connection, jobID, false, false, true);
			} catch (Throwable cause) {
				_logger.warn(String.format("Unable to \"reschedule\" job %d.", jobID), cause);
			}
		}
	}

	/**
	 * Complete all jobs owned by the current caller which are already in a final state.
	 * 
	 * @param connection
	 *            The database connection.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 * @throws GenesisIISecurityException
	 */
	// Took out synchronized ... ASG
	private void completeJobs(Connection connection) throws SQLException, ResourceException, GenesisIISecurityException
	{
		Collection<Long> jobsToComplete = new LinkedList<Long>();
		HashMap<Long, PartialJobInfo> ownerMap;
		_logger.debug("Entering jobManager::completeJobs 3");
		/* Get information needed from the database (like owner id) */
		ownerMap = _database.getPartialJobInfos(connection, _jobsByID.keySet());

		/*
		 * Find all jobs that are owend by the caller and that are in a final state.
		 */
		boolean isAdmin = QueueSecurity.isQueueAdmin();
		Collection<Identity> callers = QueueSecurity.getCallerIdentities(true);

		for (Long jobID : ownerMap.keySet()) {
			JobData jobData = _jobsByID.get(jobID);
			PartialJobInfo pji = ownerMap.get(jobID);

			try {
				if (jobData.getJobState().isFinalState() && isAdmin) {
					jobsToComplete.add(jobID);
				} else {
					if (jobData.getJobState().isFinalState() && QueueSecurity.isOwner(pji.getOwners(), callers)) {
						jobsToComplete.add(jobID);
					}
				}
			} catch (AuthZSecurityException azse) {
				_logger.warn("Security exception caused us not to complete a job.", azse);
			}
		}

		// Go ahead and complete them
		completeJobs(connection, jobsToComplete);
		_logger.debug("Exiting jobManager::completeJobs 3");

	}

	/**
	 * This method completes (or removes from the queue) all jobs which are owned by the caller and which are in a final state. Only jobs in a
	 * final state can be completed. To try and complete a job which is not in a final state is considered an error and an exception will be
	 * thrown.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param jobs
	 *            The list of jobs to complete. If this is null or empty, then all jobs owned by the caller, and already in a final state,
	 *            will be completed.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 * @throws GenesisIISecurityException
	 */
	
	synchronized public void completeJobs(Connection connection, String[] jobs)
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		/*
		 * If no jobs were passed to us, then the caller wants us to complete all jobs that are in a final state and that are owned by
		 * him/her.
		 */
		if (jobs == null || jobs.length == 0) {
			completeJobs(connection);
			return;
		}
		_logger.debug("Entering jobManager::completeJobs 1");
		Collection<Long> jobsToComplete = new LinkedList<Long>();
		HashMap<Long, PartialJobInfo> ownerMap;

		/* Find the job IDs for all job tickets passed in */
		for (String jobTicket : jobs) {
			JobData data = _jobsByTicket.get(jobTicket);
			if (data == null)
				throw new ResourceException("Job \"" + jobTicket + "\" does not exist.");
			jobsToComplete.add(new Long(data.getJobID()));
		}

		/*
		 * Now, get the database information for all of the jobs passed in.
		 */
		ownerMap = _database.getPartialJobInfos(connection, jobsToComplete);

		boolean isAdmin = QueueSecurity.isQueueAdmin();
		Collection<Identity> callers = QueueSecurity.getCallerIdentities(true);

		/*
		 * Check that every job indicated is owned by the caller and is in a final state.
		 */
		for (Long jobID : ownerMap.keySet()) {
			JobData jobData = _jobsByID.get(jobID);
			PartialJobInfo pji = ownerMap.get(jobID);

			/* If the job isn't in a final state, throw an exception. */
			if (!jobData.getJobState().isFinalState())
				throw new ResourceException("Job \"" + jobData.getJobTicket() + "\" is not in a final state.");

			/* If the job isn't owned by the caller, throw an exception. */
			if (!isAdmin) {
				if (!QueueSecurity.isOwner(pji.getOwners(), callers))
					throw new GenesisIISecurityException("Don't have permission to complete job \"" + jobData.getJobTicket() + "\".");
			}
		}

		// Go ahead and complete them
		completeJobs(connection, jobsToComplete);
		_logger.debug("Exiting jobManager::completeJobs 1");

	}

	private Collection<Long> checkOwnershipAndGenerateJobID(Connection connection, String[] jobs, boolean skipCheck, String message)
			throws SQLException, ResourceException, GenesisIISecurityException
	{
		if (jobs == null || jobs.length == 0)
			return null;

		Collection<Long> jobsToReschedule = new LinkedList<Long>();
		HashMap<Long, PartialJobInfo> ownerMap;

		/* Find the job IDs for all job tickets passed in */
		for (String jobTicket : jobs) {
			JobData data = _jobsByTicket.get(jobTicket);
			if (data == null)
				throw new ResourceException("Job \"" + jobTicket + "\" does not exist.");
			jobsToReschedule.add(new Long(data.getJobID()));
		}

		/*
		 * Now, get the database information for all of the jobs passed in.
		 */
		ownerMap = _database.getPartialJobInfos(connection, jobsToReschedule);

		/*
		 * Check that every job indicated is owned by the caller and is in a final state.
		 */
		for (Long jobID : ownerMap.keySet()) {
			JobData jobData = _jobsByID.get(jobID);
			PartialJobInfo pji = ownerMap.get(jobID);

			QueueStates state = jobData.getJobState();
			if (!skipCheck) {
				if (!(state == QueueStates.RUNNING || state == QueueStates.STARTING)) {
					jobsToReschedule.remove(jobID);
					continue;
				}
			}

			/* If the job isn't owned by the caller, throw an exception. */
			if (!QueueSecurity.isOwner(pji.getOwners()))
				throw new GenesisIISecurityException(message + jobData.getJobTicket() + "\".");
		}
		return jobsToReschedule;

	}

	synchronized public void resetJobs(Connection connection, String[] jobs) 
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		Collection<Long> jobsToReset = checkOwnershipAndGenerateJobID(connection, jobs,true,"Don't have permission to reset job \"");
		for (Long jobID : jobsToReset) {
			JobData jobData = _jobsByID.get(jobID);

			QueueStates state = jobData.getJobState();
			if (state == QueueStates.RUNNING || state == QueueStates.STARTING) {	
				
				continue;
			}
			
			// Now we need to move the job between queues
			synchronized (jobData) {
				HistoryContext history = jobData.history(HistoryEventCategory.ResetCount);
				history.createTraceWriter("Resetting Job tries count to 0")
				.format("Resetting job.").close();
				jobData.resetJob();

				if (_logger.isDebugEnabled())
					_logger.debug(String.format("Resetting job %s", jobData));
				SortableJobKey jobKey = new SortableJobKey(jobData);
				_queuedJobs.put(jobKey, jobData);

				Connection connection2 = _connectionPool.acquire(false);

				/* Ask the database to update the job state */
				_database.modifyJobState(connection, jobData.getJobID(), jobData.getRunAttempts(), QueueStates.REQUEUED, new Date(), null, null,
						null);
				connection2.commit();
			}
			
		}
		_schedulingEvent.notifySchedulingEvent();
	}
	
	synchronized public void persistJobs(Connection connection, String[] jobs) 
			throws SQLException, ResourceException, GenesisIISecurityException
	{
		int originalCount = _outcallThreadPool.size();
		
		for (String jobTicket : jobs) 
		{
			JobData jobData = _jobsByTicket.get(jobTicket);
			if (jobData == null)
				throw new ResourceException("Job \"" + jobTicket + "\" does not exist.");

			if(jobData.getJobState() != QueueStates.RUNNING)
			{
				if(_logger.isErrorEnabled())
					_logger.error(String.format("%s is not currently running, cannot persist.", jobData));
				continue;
			}

			HistoryContext history = jobData.history(HistoryEventCategory.Persisting);

			history.createTraceWriter("Persisting Job")
			.format("Persisting the job due to request.").close();

			Resolver resolver = new Resolver();

			/* Enqueue the worker into the outcall thread pool */
			_outcallThreadPool.enqueue(new JobPersistWorker(resolver, resolver, _connectionPool, jobData));
		}

		_schedulingEvent.notifySchedulingEvent();

		int newCount = _outcallThreadPool.size();

		if (_logger.isDebugEnabled() && (originalCount != newCount)) {
			_logger.debug(String.format("%d jobs queued in thread pool (changed from %d).", newCount, originalCount));
		}
	}
	
	synchronized public void restartJobs(Connection connection, String[] jobs)
			throws SQLException, ResourceException, GenesisIISecurityException
	{
		int originalCount = _outcallThreadPool.size();

		for (String jobTicket : jobs)
		{
			JobData jobData = _jobsByTicket.get(jobTicket);
			if (jobData == null)
				throw new ResourceException("Job \"" + jobTicket + "\" does not exist.");

//			if(jobData.getJobState() != QueueStates.PERSISTED)
//			{
//				if(_logger.isErrorEnabled())
//					_logger.error(String.format("%s is not currently persisted, cannot restart.", jobData));
//				continue;
//			}
			
			HistoryContext history = jobData.history(HistoryEventCategory.Restarting);

			history.createTraceWriter("Restarting Execution of Job")
			.format("Restarting execution of the job due to request.").close();

			Resolver resolver = new Resolver();

			/* Enqueue the worker into the outcall thread pool */
			_outcallThreadPool.enqueue(new JobRestartWorker(resolver, resolver, _connectionPool, jobData));
		}

		_schedulingEvent.notifySchedulingEvent();

		int newCount = _outcallThreadPool.size();

		if (_logger.isDebugEnabled() && (originalCount != newCount)) {
			_logger.debug(String.format("%d jobs queued in thread pool (changed from %d).", newCount, originalCount));
		}
	}
	
	synchronized public void stopJobs(Connection connection, String[] jobs)
			throws SQLException, ResourceException, GenesisIISecurityException
	{
		int originalCount = _outcallThreadPool.size();
		
		for (String jobTicket : jobs)
		{
			JobData jobData = _jobsByTicket.get(jobTicket);
			if (jobData == null)
				throw new ResourceException("Job \"" + jobTicket + "\" does not exist.");

			if(jobData.getJobState() != QueueStates.RUNNING)
			{
				if(_logger.isErrorEnabled())
					_logger.error(String.format("%s is not currently running, cannot stop.", jobData));
				continue;
			}
			

			HistoryContext history = jobData.history(HistoryEventCategory.Stopping);

			history.createTraceWriter("Stopping Execution of Job")
			.format("Stopping execution of the job due to request.").close();

			Resolver resolver = new Resolver();

			/* Enqueue the worker into the outcall thread pool */
			_outcallThreadPool.enqueue(new JobStopWorker(resolver, resolver, _connectionPool, jobData));
			
			synchronized(jobData)
			{
				jobData.setJobState(QueueStates.STOPPED);
			}
		}

		_schedulingEvent.notifySchedulingEvent();
		
		int newCount = _outcallThreadPool.size();

		if (_logger.isDebugEnabled() && (originalCount != newCount)) {
			_logger.debug(String.format("%d jobs queued in thread pool (changed from %d).", newCount, originalCount));
		}
	}
	
	synchronized public void resumeJobs(Connection connection, String[] jobs)
			throws SQLException, ResourceException, GenesisIISecurityException
	{
		int originalCount = _outcallThreadPool.size();
		
		for (String jobTicket : jobs)
		{
			JobData jobData = _jobsByTicket.get(jobTicket);
			if (jobData == null)
				throw new ResourceException("Job \"" + jobTicket + "\" does not exist.");

			if(jobData.getJobState() != QueueStates.STOPPED)
			{
				if(_logger.isErrorEnabled())
					_logger.error(String.format("%s is not currently stopped, cannot resume.", jobData));
				continue;
			}
			
			HistoryContext history = jobData.history(HistoryEventCategory.Resuming);
			
			history.createTraceWriter("Resuming Execution of Job")
			.format("Resuming execution of the job due to request.").close();
			
			Resolver resolver = new Resolver();

			/* Enqueue the worker into the outcall thread pool */
			_outcallThreadPool.enqueue(new JobResumeWorker(resolver, resolver, _connectionPool, jobData));
			
			synchronized(jobData)
			{
				jobData.setJobState(QueueStates.RUNNING);
			}
		}

		_schedulingEvent.notifySchedulingEvent();
		
		int newCount = _outcallThreadPool.size();

		if (_logger.isDebugEnabled() && (originalCount != newCount)) {
			_logger.debug(String.format("%d jobs queued in thread pool (changed from %d).", newCount, originalCount));
		}
	}
	
	synchronized public void rescheduleJobs(Connection connection, String[] jobs)
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		Collection<Long> jobsToReschedule = checkOwnershipAndGenerateJobID(connection, jobs,false,"Don't have permission to reschedule \"");
		// Go ahead and complete them
		rescheduleJobs(connection, jobsToReschedule);
	}

	synchronized public void checkJobStatus(long jobID)
	{
		if (_logger.isDebugEnabled())
			_logger.debug("adding record to check on job status for: " + jobID);
		_pendingChecks.add(jobID);
	}

	// tends to notifications we've already heard about for jobs that may be complete.
	synchronized public void handlePendingJobStatusChecks()
	{
		if (_whenToProcessNotifications.after(Calendar.getInstance()))
			return; // not time yet.
		while (_pendingChecks.size() > 0) {
			long jobId = _pendingChecks.get(0);
			_pendingChecks.remove(0);
			if (_logger.isDebugEnabled())
				_logger.debug("JobManager: scheduling job status check on: " + jobId);
			scheduleAJobStatusCheck(jobId); 
		}
		// reset for the next time to check notified statuses.
		_whenToProcessNotifications = Calendar.getInstance();
		_whenToProcessNotifications.add(Calendar.MILLISECOND, NOTIFICATION_CHECKING_DELAY);
	}

	// adds an item to the queue to later perform a job status check on jobID.
	synchronized public void scheduleAJobStatusCheck(long jobID)
	{
		int originalCount = _outcallThreadPool.size();

		JobData job = _jobsByID.get(jobID);
		if (job == null || job.getBESID() == null)
			return;

		JobCommunicationInfo info = null;
		try {
			/*
			 * For convenience, we bundle together the id's of the job to check, and the bes container on which it is running.
			 */
			info = new JobCommunicationInfo(job.getJobID(), job.getBESID().longValue());
		} catch (Throwable cause) {
			_logger.error("Saw unexpected exception when creating job communication info for job: " + jobID, cause);
			return;
		}
		/*
		 * As in other places, we use a callback mechanism to allow outcall threads to late bind "large" information at the last minute. This
		 * keeps us from putting too much into memory. In this particular case its something of a hack as we already had a type for getting
		 * the bes information so we just bundled that with a second interface for getting the job's endpoint. This could have been done
		 * cleaner, but it works fine.
		 */
		Resolver resolver = new Resolver();

		/* Enqueue the worker into the outcall thread pool */
		_outcallThreadPool.enqueue(new JobUpdateWorker(this, resolver, resolver, _connectionPool, info, job, _lastUpdate));
		_lastUpdate = Calendar.getInstance().getTime();

		int newCount = _outcallThreadPool.size();

		if (_logger.isDebugEnabled() && (originalCount != newCount)) {
			_logger.debug(String.format("%d jobs queued in thread pool (changed from %d).", newCount, originalCount));
		}
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
		long stopped = 0;
		long persisted = 0;

		for (JobData jobData : _jobsByID.values()) {
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
			else if (state == QueueStates.STOPPED)
				stopped++;
			else if (state == QueueStates.PERSISTED)
				persisted++;
		}

		ret.put("Queued", queued);
		ret.put("Re-queued", requeued);
		ret.put("Starting", starting);
		ret.put("Running", running);
		ret.put("Error", error);
		ret.put("Finished", finished);
		ret.put("Stopped", stopped);
		ret.put("Persisted", persisted);

		return ret;
	}

	public long getJobCount()
	{
		return _jobsByID.size();
	}

	/**
	 * This method is called periodically by a watcher thread to check on the current statuses of all jobs running in the queue (the polling
	 * method of checking whether or not a job has run to completion or failed or is still running).
	 * 
	 * @param connection
	 *            The database connection.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 * @throws ConfigurationException
	 * @throws GenesisIISecurityException
	 */
	synchronized public void checkJobStatuses(Connection connection) throws SQLException, ResourceException, GenesisIISecurityException
	{
		// reset any stored notifications, since we're checking everything.
		// 2017-07-27 ASG. We cannot just delete the old checks .. there could be notifications in there
		if (_pendingChecks==null) _pendingChecks = new ArrayList<Long>();

		int originalCount = _outcallThreadPool.size();

		/*
		 * Iterate through all running jobs and enqueue a worker to check the status of that job.
		 */
		if (_logger.isDebugEnabled() && (_runningJobs.size() > 0)) {
			_logger.debug("checking on " + _runningJobs.size() + " running jobs.");
		}

		for (JobData job : _runningJobs.values()) {
			if (_logger.isDebugEnabled())
				_logger.debug("considering job: " + job);

			if (job == null || job.getBESID() == null) {
				if (job == null) {
					if (_logger.isDebugEnabled())
						_logger.debug(String.format("Can't check a job that is NULL."));
				} else {
					if (_logger.isDebugEnabled())
						_logger.debug(String.format(
							"Last minute decision not to check on job %s " + "status because it doesn't have a BES associated.", job));
					// Added 7/13/2017 by ASG  .. should have already been removed, but for some reason it is still there, so get rid of it.
					if (job.getJobState()==QueueStates.FINISHED) {
						SortableJobKey jobKey = new SortableJobKey(job);
						//FIXME wants a long not a jobkey object
						_runningJobs.remove(jobKey.getJobID());
					}
				}
				continue;
			} else {
				if (_logger.isDebugEnabled())
					_logger.debug(String.format("Starting process of checking status of running job %s", job));
			}

			HistoryContext history = job.history(HistoryEventCategory.Checking);

			history.createTraceWriter("Checking Running Job Status")
				.format("Checking job status as the result of a received asynchronous notification.").close();

			/*
			 * For convenience, we bundle together the id's of the job to check, and the bes container on which it is running.
			 */
			JobCommunicationInfo info = new JobCommunicationInfo(job.getJobID(), job.getBESID().longValue());

			/*
			 * As in other places, we use a callback mechanism to allow outcall threads to late bind "large" information at the last minute.
			 * This keeps us from putting too much into memory. In this particular case its something of a hack as we already had a type for
			 * getting the bes information so we just bundled that with a second interface for getting the job's endpoint. This could have
			 * been done cleaner, but it works fine.
			 */
			Resolver resolver = new Resolver();

			/* Enqueue the worker into the outcall thread pool */
			_outcallThreadPool.enqueue(new JobUpdateWorker(this, resolver, resolver, _connectionPool, info, job, _lastUpdate));
			_lastUpdate = Calendar.getInstance().getTime();
		}

		int newCount = _outcallThreadPool.size();

		if (_logger.isDebugEnabled() && (originalCount != newCount)) {
			_logger.debug(String.format("%d jobs queued in thread pool (changed from %d).", newCount, originalCount));
		}
	}

	/**
	 * Check to see whether or not there are any jobs in the queue waiting to run.
	 * 
	 * @return True if there are any jobs still waiting to run.
	 */
	public boolean hasQueuedJobs()
	{
		return !_queuedJobs.isEmpty();
	}

	/**
	 * This method takes a list of slots (assumed to be an indication of total slots allocated to BES resources) and subtracts out the slots
	 * that are already in use. We could have avoided this step by keeping more information about slot use in memory, but doing so
	 * exponentially increases the complexity of the Job Manager and the BES Manager and since all this work is done in memory, it is a
	 * relatively fast operation anyways. It's an O(n) algorithm where n is equal to the number of jobs actively running at any given time.
	 * 
	 * @param slots
	 *            A map of all of the slots available to the queue at this moment.
	 * 
	 * @throws ResourceException
	 */
	synchronized public void recordUsedSlots(HashMap<Long, ResourceSlots> slots) throws ResourceException
	{
		/*
		 * Iterate through all running jobs and reduce the slot count from resources that they are using.
		 */
		for (JobData job : _runningJobs.values()) {
			/* Get the bes id that the job is running on */
			Long besID = job.getBESID();
			if (besID == null) {
				_logger.warn(job + "is marked as running which isn't " + "assigned to a BES container.");
				continue;
			}

			ResourceSlots rs = slots.get(besID);
			if (rs != null) {
				/* Go ahead and "reserve" the slots for use */
				rs.reserveSlot();

				/*
				 * Update the available cores of the BES container.
				 */

				rs.reserveCores(job.getNumOfCores());

				/*
				 * If the resource now has no slots available, remove it complete from the list.
				 */
				if ((rs.slotsAvailable() <= 0) || (rs.coresAvailable() <= 0))
					slots.remove(besID);

			}
		}
	}

	synchronized public void recordUsedSlots(Map<Long, SlotSummary> slots)
	{
		/*
		 * Iterate through all running jobs and reduce the slot count from resources that they are using.
		 */
		for (JobData job : _runningJobs.values()) {
			/* Get the bes id that the job is running on */
			Long besID = job.getBESID();
			if (besID == null)
				continue;

			SlotSummary summary = slots.get(besID);
			if (summary != null) {
				summary.add(-1, 1);
				summary.addCores(-1, 1);
			}
		}
	}

	public void addJobErrorInformation(Connection connection, long jobid, short attempt, Collection<String> errors)
	{
		try {
			_database.addError(connection, jobid, attempt, errors);
			connection.commit();
		} catch (Throwable cause) {
			_logger.warn("An error occurred while trying to save error information.", cause);
		}
	}

	/**
	 * Retrieves the list of currently queued jobs.
	 * 
	 * @return The list of currently queued jobs.
	 */
	public Collection<JobData> getQueuedJobs()
	{
		/*
		 * Note: This method changes the meaning of the "Priority" of a job. Previously, setting a higher priority meant a user could push his
		 * job to the head of the line. Now, a high priority only moves the job to the head of the user's queue, but the user will still be
		 * scheduled in turn with other user's. This lets a user prioritize his own jobs without compromising the fairness of the overall
		 * queue scheduling.
		 */
		if (_usersWithJobs.size() > 1) {
			Collection<JobData> jobs = new ArrayList<JobData>();

			// Make a linked list to cycle through user names
			LinkedList<String> users = new LinkedList<String>(_usersWithJobs.keySet());

			// Move lastUserScheduled to the front
			if (users.indexOf(_lastUserScheduled) > 0) {
				while (!users.peekFirst().equals(_lastUserScheduled)) {
					users.addLast(users.removeFirst());
				}
				users.addLast(users.removeFirst());
				_lastUserScheduled = users.getFirst();
			} else
				_lastUserScheduled = users.getFirst();

			// Get a copy of the current job lists
			HashMap<String, Iterator<JobData>> userJobs = new HashMap<String, Iterator<JobData>>();
			for (String user : users) {
				userJobs.put(user, _usersWithJobs.get(user).values().iterator());
			}

			while (!users.isEmpty()) {
				// Get the next username from the list
				String thisUser = users.removeFirst();

				// If the user has more jobs to schedule...
				if (userJobs.get(thisUser).hasNext()) {
					// take the first one for this user, then put him back in line
					JobData nextJob = userJobs.get(thisUser).next();
					jobs.add(nextJob);
					users.addLast(thisUser);
				} else {
					// otherwise, take him out of the list
					users.remove(thisUser);
					userJobs.remove(thisUser);
				}
			}

			return jobs;
		} else
			return _queuedJobs.values();
	}

	/**
	 * Given a list of resource matches, start the jobs indicated on the BES containers indicated.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param matches
	 *            A list of matched resources and jobs.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public void startJobs(Connection connection, Collection<ResourceMatch> matches, List<Integer> selectedMatchIndices)
		throws SQLException, ResourceException
	{
		HashSet<ResourceMatch> badMatches = new HashSet<ResourceMatch>();

		int matchIndex = 0;

		/*
		 * Iterate through the matches and enqueue a worker to start the jobs indicated there.
		 */

		// System.out.println("Starting the job");

		for (ResourceMatch match : matches) {
			/*
			 * Retrieves the old JSDL with (possibly) multiple job descriptions
			 */
			JobDefinition_Type oldJSDL = getJSDL(match.getJobID());
			JobDefinition_Type newJSDL = JSDLTransformer.transform(oldJSDL, selectedMatchIndices.get(matchIndex));
			/*
			 * Transforms it into a JSDL with only one job description
			 */
			matchIndex++;

			// Updates the row with the old JSDL to hold the new one
			if (newJSDL != null)
				updateJSDL(newJSDL, match.getJobID());

			/* Find the job data for the match */
			JobData data = _jobsByID.get(new Long(match.getJobID()));
			synchronized (data) {

			if (data.isSweepingJob()) {
				_logger.debug("saw that job is sweeping type; not sending to bes.");
				badMatches.add(match);
				continue;
			}

			/*
			 * Get the job off of the queued list and instead put him on the running list. Also, note which bes the jobs is assigned to.
			 */
			SortableJobKey jobKey = new SortableJobKey(data);
			_queuedJobs.remove(jobKey);
			/*
			 * for (TreeMap<SortableJobKey, JobData> user : _usersWithJobs.values()) { user.remove(jobKey); }
			 */
			removeFromUserBucket(_usersWithJobs, data);
			putInUserBucket(_usersNotReadyJobs, data, data.getUserName());
			data.setBESID(match.getBESID());
			data.setJobState(QueueStates.STARTING);
			_runningJobs.put(new Long(data.getJobID()), data);
			}
			/*
			 * Finally, enqueue a worker to make the out call to the BES to start the job. Until this worker completes, a restart of the
			 * container will cause this attempt at starting a job to fail.
			 */
			_outcallThreadPool.enqueue(new JobLauncher(this, match.getJobID(), match.getBESID()));
		}

		/*
		 * this used to be done first, but now we wait until possibly pruning the matches list.
		 * 
		 * old notes: First (this used to be done first), note in the database that we are starting a job. In all honesty, if we restarted at
		 * this point, it would probably be ok if we didn't know this. If we do restart, we won't be able to do anything with the job since we
		 * don't have an endpoint, but it is a state that we can keep so we do anyways.
		 */
		_database.markStarting(connection, matches, badMatches);
		connection.commit();
	}

	/**
	 * This is the resolver instance that is used to late bind job EPR, BES EPR, and Job Calling Context for outcall workers. This class is
	 * implemented with a horrible hack in it. Namely, that the call to createClientStub MUST come after a call to getJobEndpoint. The main
	 * reason for this is that I'm now rushing to get this working and this hack can be managed client side. We will at least check the
	 * condition though so that we will catch an invalid ordering if it ever happens.
	 * 
	 * @author mmm2a
	 */
	private class Resolver implements IBESPortTypeResolver, IJobEndpointResolver
	{
		private GeniiBESPortType _portType = null;
		private EndpointReferenceType _endpoint = null;

		/**
		 * Resolve the information in this resolver. I.e., get the client stub and the job endpoint from the database.
		 * 
		 * @param connection
		 *            The database connection to use.
		 * @param jobID
		 *            The job's ID
		 * 
		 * @throws Throwable
		 */
		private void resolve(Connection connection, long jobID) throws Throwable
		{
			
			JobStatusInformation info = _database.getJobStatusInformation(connection, jobID);

			_endpoint = info.getJobEndpoint();
			
		//	EndpointReferenceType endpoint=_besManager.getBESEPR(connection,_jobsByID.get(jobID).getBESID());
			_portType = ClientUtils.createProxy(GeniiBESPortType.class, info.getBESEndpoint(), info.getCallingContext());
		}

		@Override
		public GeniiBESPortType createClientStub(Connection connection, long besID) throws Throwable
		{
			if (_portType == null)
				throw new IllegalStateException("createClientStub called before getJobEndpoint.");

			return _portType;
		}

		@Override
		public EndpointReferenceType getJobEndpoint(Connection connection, long jobID) throws Throwable
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
	 * Kill all of the jobs indicated. If the job is queued or requeued, we simply move it to a final state. If the job is starting or
	 * running, we kill it. If the job is already in a final state, we don't do anything. This operation is also a "safe" operation meaning
	 * that the caller MUST own the job to kill it or it is considered an exception. Also, unlike other similar operations, the list of
	 * tickets, when NULL, will not imply all jobs owned by the caller -- instead we just will return.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param tickets
	 *            The list of job tickets to kill.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	public void killJobs(Connection connection, String[] tickets)
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		/* If we weren't given any tickets, then just ignore */
		if (tickets == null || tickets.length == 0)
			return;

		Collection<Long> jobsToKill = new LinkedList<Long>();
		ArrayList<String> jobsToResume = new ArrayList<String>(0);
		HashMap<Long, PartialJobInfo> ownerMap;

		/*
		 * Iterate through all job tickets and get the associated in-memory job information.
		 */
		// LAK: Added mutex on _jobsByTickets to stop the race condition for killing a job while it is being created.
		synchronized (_jobsByTicket){
			for (String jobTicket : tickets) {
				JobData data = _jobsByTicket.get(jobTicket);
				if (data == null)
					throw new ResourceException("Job \"" + jobTicket + "\" does not exist.");

				data.history(HistoryEventCategory.Terminating).createInfoWriter("Job Termination Requested")
					.format("Request to terminate job from outside the queue").close();

				jobsToKill.add(new Long(data.getJobID()));
				if (data.getJobState() == QueueStates.STOPPED) {
					jobsToResume.add(jobTicket);
				}
			}
		
			if (jobsToResume.size() > 0) {
				resumeJobs(connection, jobsToResume.toArray(new String[0]));
			}
		}

		/*
		 * Retrieve the list of owners for all the jobs we are going to kill.
		 */
		ownerMap = _database.getPartialJobInfos(connection, jobsToKill);

		/* Iterate through the job information */
		for (Long jobID : ownerMap.keySet()) {
			JobData jobData = _jobsByID.get(jobID);
			synchronized (jobData) {
			PartialJobInfo pji = ownerMap.get(jobID);

			/*
			 * If the caller doesn't own the job, it's a security exception
			 */
			if (!QueueSecurity.isOwner(pji.getOwners())) {
				GenesisIISecurityException t =
					new GenesisIISecurityException("Don't have permission to kill job \"" + jobData.getJobTicket() + "\".");
				jobData.history(HistoryEventCategory.Terminating).createWarnWriter("Termination Request Denied")
					.format("Denying termination request.  Caller not authorized.").close();
				throw t;
			}

			/*
			 * If the job is starting, we mark it as being killed. Starting implies that another thread is about to try and start the thing up
			 * so it will have to check this flag and abort (or kill) as necessary.
			 */
			if (jobData.getJobState().equals(QueueStates.STARTING)) {
				jobData.kill();
			} else if (jobData.getJobState().equals(QueueStates.RUNNING)) {
				/*
				 * If the job is running, we have to finish the job (which will kill it for us)
				 */
				killJob(connection, jobID);
			} else if (!jobData.getJobState().isFinalState()) {
				/*
				 * This won't kill the job (it isn't running), but it will move it to the correct lists, thus preventing it from ever being
				 * run.
				 */
				finishJob(jobID);
			}
			}
		}
	}

	public void resetJobCommunicationAttempts(Connection connection, long jobid) throws SQLException
	{
		_database.setJobCommunicationAttempts(connection, jobid, 0);
	}

	public void addJobCommunicationAttempt(Connection connection, long jobid, long besid) throws SQLException, ResourceException
	{
		int commAttempts = _database.getJobCommunicationAttempts(connection, jobid);
		if (commAttempts > MAX_COMM_ATTEMPTS) {
			_logger.error(String.format("Unable to communicate with job for %d attempts.  Re-starting it.", commAttempts));
			failJob(connection, jobid, false, false, false);
			_besManager.markBESAsMissed(besid, "Couldn't get job status.");
		} else {
			_database.setJobCommunicationAttempts(connection, jobid, commAttempts + 1);
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
			return (_jobID == other._jobID) && (_besID == other._besID) && (_manager == other._manager);
		}

		@Override
		public boolean equals(OutcallHandler other)
		{
			if (other instanceof JobLauncher)
				return equals((JobLauncher) other);

			return false;
		}

		@Override
		public boolean equals(Object other)
		{
			if (other instanceof JobLauncher)
				return equals((JobLauncher) other);

			return false;
		}

		public void run()
		{
			boolean isPermanent = false;
			ICallingContext startCtxt = null;
			Connection connection = null;
			LegacyEntryType entryType;
			HashMap<Long, LegacyEntryType> entries = new HashMap<Long, LegacyEntryType>();
			JobData data = null;
			HistoryContext history = new NullHistoryContext();

			// REMOVE synchronized (_manager) {
				/* Get the in-memory information for the job */
				data = _jobsByID.get(new Long(_jobID));
				if (data == null) {
					_logger.warn("Job " + _jobID + " dissappeared before it could be started.");
					return;
				}
			// REMOVE }
			
			synchronized (data) {
				String oldAction = data.setJobAction("Creating");
				if (oldAction != null) {
					_logger.error("We're trying to create an activity for job " + data.getJobTicket() + " that " + "is already undergoing some action:  " + oldAction);
					data.setJobAction(oldAction);
					return;
				}
				
			try {
				/* Acquire a new database connection to use. */
				connection = _connectionPool.acquire(false);

				/*
				 * Get all of the information from the database required to start the job.
				 */
				JobStartInformation startInfo = _database.getStartInformation(connection, _jobID);
				connection.commit();


			
				////////////////////////////////////////////////////////////////////
				_logger.debug("Inside run ... Creating job " + data);
				////////////////////////////////////////////////////////////////////

				history = data.history(HistoryEventCategory.CreatingJob);

				SecurityUpdateResults checkResults = new SecurityUpdateResults();
				/*
				 * future: where does this 10 hour time limit come from? why block it like that with a constant? instead, we should grab the
				 * current credentials deadline and use that directly.
				 */
				ClientUtils.checkAndRenewCredentials(startInfo.getCallingContext(), new Date(System.currentTimeMillis() + 1000l * 60 * 10),
					checkResults);
				if (checkResults.removedCredentials().size() > 0) {
					PrintWriter hWriter = history.createErrorWriter("Job Credentials Expired");
					hWriter.println("The following credentials expired:");
					for (NuCredential cred : checkResults.removedCredentials())
						hWriter.format("\t%s\n", cred.describe(VerbosityLevel.OFF));
					hWriter.close();

					isPermanent = true;
					throw new AuthZSecurityException(
						"A job's credentials expired so we can't make " + "any further progress on it.  Failing it.");
				}

				/*
				 * Use the database's fillInBESEPRs function to get the EPR of the BES container that we are going to launch on.
				 */
				// REMOVE entries.put(new Long(_besID), entryType = new LegacyEntryType());
				// REMOVE _database.fillInBESEPRs(connection, entries);

				// TEMP synchronized (_manager) {
					/* Get the in-memory information for the job */
					// TEMP data = _jobsByID.get(new Long(_jobID));
					// TEMP if (data == null) {
					// TEMP 	_logger.warn("Job " + _jobID + " dissappeared before it could be started.");
					// TEMP 	return;
					// TEMP }

					/*
					 * If the thing was marked as killed, then we simply won't start it. Instead, we will finish it early.
					 */
					if (data.killed()) {
						history.debug("Job Terminated Before Create");

						finishJob(_jobID);
						return;
					}
				// TEMP }



				CreateActivityResponseType resp;

				try {
					/*
					 * We need to start the job, so go ahead and create a proxy to call the container and then call it.
					 */
					startCtxt = startInfo.getCallingContext();

					history.setProperty(QueueConstants.ATTEMPT_NUMBER_HISTORY_PROPERTY, Integer.toString(data.getRunAttempts()));
					history.setProperty(QueueConstants.QUEUE_STARTED_HISTORY_PROPERTY, "true");

					history.category(HistoryEventCategory.CreatingJob);

					HistoryEventToken historyToken = history.info("Creating Activity on %s", _besManager.getBESName(_besID));

					data.historyToken(historyToken);
					_database.historyToken(connection, _jobID, historyToken);
					connection.commit();

					startCtxt = history.setContextProperties(startCtxt);

				//	GeniiBESPortType bes = ClientUtils.createProxy(GeniiBESPortType.class, entryType.getEntry_reference(), startCtxt);
					GeniiBESPortType bes = ClientUtils.createProxy(GeniiBESPortType.class, _besManager.getBESEPR(connection, _besID), startCtxt);

					ClientUtils.setTimeout(bes, 120 * 1000);
					ActivityDocumentType adt = new ActivityDocumentType(startInfo.getJSDL(), null);
					EndpointReferenceType queueEPR = _database.getQueueEPR(connection);
					if (queueEPR != null)
						BESUtils.addSubscription(adt,
							AbstractSubscriptionFactory.createRequest(queueEPR,
								BESActivityTopics.ACTIVITY_STATE_CHANGED_TO_FINAL_TOPIC.asConcreteQueryExpression(), null,
								new JobCompletedAdditionUserData(_jobID)));

					////////////////////////////////////////////////////////////////////
					_logger.debug("Creating Activity");
					////////////////////////////////////////////////////////////////////

					HistoryEventWriter hWriter =
						history.createDebugWriter("Making CreateActivity Outcall on %s", _besManager.getBESName(_besID));

					hWriter.format("Making outcall to resource %s.", _besManager.getBESName(_besID)).close();
					historyToken = hWriter.getToken();

					String header = _database.getSecurityHeader(connection, _jobID);

					if (header != "")
						MyProxyCertificate.setPEMFormattedCertificate(header);

					resp = bes.createActivity(new CreateActivityType(adt, null));

					history.debug("CreateActivity Outcall Succeeded");

					SequenceNumber parentNumber = historyToken.retrieve();
					historyToken = InMemoryHistoryEventSink.wrapEvents(parentNumber, _besManager.getBESName(_besID), null,
						_database.historyKey(data.getJobTicket()), resp == null ? null : resp.get_any());

					data.historyToken(historyToken);
					_database.historyToken(connection, _jobID, historyToken);
					connection.commit();
				} finally {
					data.clearJobAction();
				}

				// TEMP synchronized (_manager) {
					/*
					 * We successfully got back here, so mark the job as started in the database.
					 */
					_database.markRunning(connection, _jobID, resp.getActivityIdentifier());
					connection.commit();

					/*
					 * Now it's stored in the database. Note that it's started in memory as well.
					 */
					// TEMP data = _jobsByID.get(new Long(_jobID));
					data.setJobState(QueueStates.RUNNING);

					/*
					 * Finally, we check one last time to see if it was "killed" while we were starting it. If so, then we will immediately
					 * kill it and finish it.
					 */
					if (data.killed())
						finishJob(_jobID);
					// TEMP }
			} catch (Throwable cause) {
				history.error(cause, "Exception Thrown During Create Activity");

				_logger.warn(String.format("Unable to start job %d.  Exception class is %s.", _jobID, cause.getClass().getName()), cause);

				boolean countAgainstJob;
				boolean countAgainstBES;

				if (cause instanceof NotAcceptingNewActivitiesFaultType) {
					countAgainstJob = false;
					countAgainstBES = true;
				} else if (cause instanceof InvalidRequestMessageFaultType) {
					countAgainstJob = false;
					countAgainstBES = true;
				} else if (cause instanceof UnsupportedFeatureFaultType) {
					countAgainstJob = true;
					countAgainstBES = false;
				} else if (cause instanceof NotAuthorizedFaultType) {
					countAgainstJob = true;
					countAgainstBES = false;
				} else if (cause instanceof GenesisIISecurityException) {
					countAgainstJob = true;
					countAgainstBES = false;
				} else if (cause instanceof GeneralSecurityException) {
					countAgainstJob = true;
					countAgainstBES = false;
				} else {
					countAgainstJob = true;
					countAgainstBES = true;
				}
				data.clearJobAction();
				if (countAgainstBES) {
					_besManager.markBESAsUnavailable(_besID,
						String.format("Exception during job start %s(%s)", cause.getClass().getName(), cause.getLocalizedMessage()));
				}

				if (data != null && countAgainstJob) {
					data.setNextValidRunTime(new Date());
					history.createInfoWriter("Job Being Re-queued")
						.format("An attempt to run the job failed.  The job will " + "be removed from the runnable list until %ty.",
							data.getNextCanRun())
						.close();
				} else {
					history.createInfoWriter("Job Being Re-queued")
						.format(
							"The job failed, but it wasn't deemed the job's fault." + "The job will be availble for immediate re-scheduling.")
						.close();
				}

				try {
					/* We got an exception, so fail the job. */
					failJob(connection, _jobID, countAgainstJob, isPermanent, true);
					_schedulingEvent.notifySchedulingEvent();
				} catch (Throwable cause2) {
					_logger.error("Unable to fail a job.", cause2);
				}
			} finally {
				_connectionPool.release(connection);
			}
		}
		}
	}

	/**
	 * A worker that can go to a bes container and terminate a bes activity. This worker is used to both kill jobs prematurely, and to clean
	 * up after the complete.
	 * 
	 * @author mmm2a
	 */
	private class JobKiller implements OutcallHandler
	{
		private boolean _outcallOnly;
		private boolean _attemptKill;
		private JobData _jobData;
		private QueueStates _newState;
		private String _besName;
		private Long _besID;

		public JobKiller(JobData jobData, QueueStates newState, boolean outcallOnly, boolean attemptKill, String besName, Long besID)
		{
			_outcallOnly = outcallOnly;
			_jobData = jobData;
			_newState = newState;
			_attemptKill = attemptKill;
			_besName = besName;
			_besID = besID;
		}

		public boolean equals(JobKiller other)
		{
			return (_newState == other._newState) && (_jobData.getJobID() == other._jobData.getJobID());
		}

		@Override
		public boolean equals(OutcallHandler other)
		{
			if (other instanceof JobKiller)
				return equals((JobKiller) other);

			return false;
		}

		@Override
		public boolean equals(Object other)
		{
			if (other instanceof JobKiller)
				return equals((JobKiller) other);

			return false;
		}

		/**
		 * Terminate the activity at the BES container.
		 * 
		 * @param connection
		 *            The database connection to use.
		 * 
		 * @throws SQLException
		 * @throws ResourceException
		 */
		private ICallingContext terminateActivity(Connection connection) throws SQLException, ResourceException
		{
			HistoryContext history = _jobData.history(HistoryEventCategory.Terminating);

			if (_logger.isDebugEnabled())
				_logger.debug(String.format("JobKiller in \"terminateActivity\" for %s.", _jobData));

			/*
			 * Ask the database for all information needed to terminate the activity at the BES container.
			 */
			KillInformation killInfo = _database.getKillInfo(connection, _jobData.getJobID(), _besID);
			connection.commit();

			String oldAction = _jobData.currentJobAction();
			while (oldAction != null) {
				_logger.error(String.format("JK:Terminate:Attempted to kill activity %s which was " + "already undergoing another action:  %s", _jobData,
					oldAction));
				// we cannot arbitrarily decide to keep going;
				// that's a violation of trust for the other actors
				// who check/use the action flag.
				try {
					this.wait(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			oldAction = _jobData.setJobAction("Terminating");
			if (oldAction != null) {
				_logger.error(String.format("Attempted to kill activity %s which was " + "already undergoing another action:  %s", _jobData,
					oldAction));
				// we cannot arbitrarily decide to keep going;
				// that's a violation of trust for the other actors
				// who check/use the action flag.
				return null;
			}

			try {
				history.category(HistoryEventCategory.Terminating);
				history.createInfoWriter("Killing BES Activity").format("Making a persistent outcall to kill BES activity.").close();

				ICallingContext ctxt = killInfo.getCallingContext();

				if (_attemptKill) {
					if (_logger.isDebugEnabled())
						_logger.debug(
							String.format("JobKiller::terminateActivity killing request for %s a persistent outcall.", _jobData));
					// New 7/22/2017 by ASG. Just try the RPC, if it fails, then put it into the persistent caller DB
					BESActivityTerminatorActor firstTry=new BESActivityTerminatorActor(_database.historyKey(_jobData.getJobTicket()), _jobData.historyToken(), _besName, killInfo.getJobEndpoint());
					if (!firstTry.enactOutcall(killInfo.getCallingContext(), killInfo.getBESEndpoint(), null)) {
						if (_logger.isDebugEnabled())
							_logger.debug(
								String.format("JobKiller::terminateActivity making a request for %s a persistent outcall.", _jobData));
						PersistentOutcallJobKiller.killJob(_besName, killInfo.getBESEndpoint(), _database.historyKey(_jobData.getJobTicket()),
							_jobData.historyToken(), killInfo.getJobEndpoint(), killInfo.getCallingContext());
					}


					_jobData.historyToken(null);
					_database.historyToken(connection, _jobData.getJobID(), null);
					connection.commit();
				}

				return ctxt;
			} catch (Throwable cause) {
				_logger.warn(String.format("Exception occurred while killing activity %s.", _jobData), cause);
				return null;
			} finally {
				_jobData.clearJobAction();
			}
		}

		public void run()
		{
			synchronized (_jobData) {
			HistoryContext history = _jobData.history(HistoryEventCategory.Terminating);

			if (_logger.isDebugEnabled())
				_logger.debug(String.format("JobKiller running for job %s", _jobData));

			Connection connection = null;

			try {
				/* Acquire a connection to talk to the database with. */
				connection = _connectionPool.acquire(false);

				if (_outcallOnly) {
					if (_logger.isDebugEnabled())
						_logger.debug(String.format("JobKiller using terminate on %s because we are flagged as \"outcallOnly\".", _jobData));
					terminateActivity(connection);

					/* Ask the database to update the job state */
					_database.modifyJobState(connection, _jobData.getJobID(), _jobData.getRunAttempts(), _newState, new Date(), null, null,
						null);
					connection.commit();

					return;
				} else {
					if (_logger.isDebugEnabled())
						_logger
							.debug(String.format("JobKiller not using terminate because we are not flagged as \"outcallOnly\".", _jobData));
				}

				/* If the job is running, then we have to terminate it */
				if (_jobData.getJobState().equals(QueueStates.RUNNING)) {
					if (_logger.isDebugEnabled())
						_logger.debug(String.format("JobKiller has to terminate %s because the job is marked as running.", _jobData));
					terminateActivity(connection);
				}

				/* Ask the database to update the job state */
				_database.modifyJobState(connection, _jobData.getJobID(), _jobData.getRunAttempts(), _newState, new Date(), null, null, null);
				connection.commit();

				/*
				 * Finally, note the new state in memory and clear the old BES information.
				 */
				_jobData.setJobState(_newState);
				_jobData.clearBESID();

				/*
				 * If we were asked to re-queue the job, then put it back in the queued jobs list.
				 */
				if (_newState.equals(QueueStates.REQUEUED)) {
					history.category(HistoryEventCategory.ReQueing);
					history.createInfoWriter("Re-queing Job").format("Next available run time is %tc.", _jobData.getNextCanRun()).close();

					if (_logger.isDebugEnabled())
						_logger.debug("Re-queing job " + _jobData.getJobTicket());
					synchronized (JobManager.this) {
						if (_logger.isDebugEnabled())
							_logger.debug(String.format("Re-queuing job %s in the JobKiller.", _jobData));

						// Retrieve owner identities and extract primary username
						Collection<Long> jobID = new ArrayList<Long>();
						jobID.add(_jobData.getJobID());
						HashMap<Long, PartialJobInfo> jobInfo = _database.getPartialJobInfos(connection, jobID);

						Collection<Identity> identities = SecurityUtilities.filterCredentials(jobInfo.get(_jobData.getJobID()).getOwners(),
							SecurityUtilities.GROUP_TOKEN_PATTERN);
						identities = SecurityUtilities.filterCredentials(identities, SecurityUtilities.CLIENT_IDENTITY_PATTERN);

						String username = identities.iterator().next().toString();
						if (_logger.isTraceEnabled())
							_logger.debug("chose username using CLIENT_IDENTITY_PATTERN: " + username);

						SortableJobKey jobKey = new SortableJobKey(_jobData);
						_queuedJobs.put(jobKey, _jobData);
						removeFromUserBucket(_usersNotReadyJobs, _jobData);
						putInUserBucket(_usersWithJobs, _jobData, _jobData.getUserName());

						// _usersWithJobs.get(username).put(jobKey, _jobData);
					}
				} else {
					/*
					 * Otherwise, we assume that he's already in the right list
					 */
					if (_logger.isDebugEnabled())
						_logger.debug("JobKiller::Run::Moving job \"" + _jobData + "\" to the " + _newState + " state.");
				}

				/*
				 * Because a job was terminated (whether because it finished or failed or whatnot) we have a new opportunity to schedule a new
				 * job.
				 */
				_schedulingEvent.notifySchedulingEvent();
			} catch (Throwable cause) {
				history.error(cause, "Error Killing Job");
				_logger.error("Error killing job " + _jobData, cause);
			} finally {
				_connectionPool.release(connection);
			}
		}
	}
	}
}
