package edu.virginia.vcgr.genii.container.q2;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStatusType;
import org.ggf.jsdl.JobDefinition_Type;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryEventToken;
import edu.virginia.vcgr.genii.container.q2.matching.JobResourceRequirements;

/**
 * This is the main data structure for keeping all information about a job that is small enough to store in memory.
 * 
 * @author mmm2a
 */
public class JobData
{
	static private Log _logger = LogFactory.getLog(JobData.class);

	static private final long BACKOFF = 60 * 1000L;

	private HistoryContext _history;
	private HistoryEventToken _historyToken = null;

	private String _jobName = null;

	/**
	 * This variable is used internally by the queue to maintain the current "active" state of a job. Is it in the process of being created or
	 * having it's status checked? If the value is null, then no action is going on.
	 */
	private String _jobAction = null;

	private Object _jobActionLock = new Object();

	/**
	 * An internal flag used to indicate that the job has been killed and needs to be cleaned up.
	 */
	private boolean _killed;

	/**
	 * The number of attempts that have unsuccessfully tried to run this job.
	 */
	private short _runAttempts;

	/**
	 * The job's ID in the database.
	 */
	private long _jobID;

	/**
	 * The human readable (GUID) ticket for the job.
	 */
	private String _jobTicket;

	/**
	 * The Job's priority. Lower numbers imply a higher priority (will run first).
	 */
	private short _priority;

	/**
	 * The current state of the job.
	 */
	private QueueStates _jobState;

	/**
	 * The time at which the job was submitted to the queue. This information is kept so that we can ORDER jobs by submit time (after taking
	 * priority into account).
	 */
	private Date _submitTime;

	/**
	 * The BES ID associated with this job. Notice that we use the wrapper class for longs here which allows this field to be null. If it is
	 * null, then the job hasn't been matched to a resource yet. If the besID is non-null, then it is in fact the bes key of the bes resource
	 * that we are running on (or starting on).
	 */
	private Long _besID;

	/**
	 * This date is used for exponential backoff. If a job fails for a reason that increases the attempt number, then we exponentially back
	 * off the next time at which we can try running it. This helps with resource contention.
	 */
	private Date _nextValidRunTime = null;

	private List<JobResourceRequirements> _resourceRequirements = null;

	private ActivityStatusType _besActivityStatus = null;

	// _sweepState is only set if this is a sweeping job rather than a normal bes-submitted job.
	private String _sweepState = null;

	private int _numOfCores;
	/*
	 * We need to keep track of the userID of the job so we can look up the identity information without going to the database
	 */
	private String _userName = null;
	private Date _startTime = null;
	private Date _finishTime = null;

	public JobData(long jobID, String jobName, String jobTicket, short priority, QueueStates jobState, Date submitTime, short runAttempts,
		Long besID, HistoryContext history, int numOfCores)
	{
		_jobName = jobName;
		_killed = false;
		_jobID = jobID;
		_jobTicket = jobTicket;
		_priority = priority;
		_jobState = jobState;
		_submitTime = submitTime;
		_besID = besID;
		_runAttempts = runAttempts;
		_history = history;
		_resourceRequirements = new ArrayList<JobResourceRequirements>();
		_numOfCores = numOfCores;
		_startTime = new Date();
		_finishTime = new Date();
		_userName = "Not Defined";
	}

	public JobData(long jobID, String jobName, String jobTicket, short priority, QueueStates jobState, Date submitTime, short runAttempts,
		HistoryContext history, int numOfCores)
	{
		this(jobID, jobName, jobTicket, priority, jobState, submitTime, runAttempts, null, history, numOfCores);
	}

	public JobData(SweepingJob sweep, long jobID, String jobName, String jobTicket, short priority, QueueStates jobState, Date submitTime,
		short runAttempts, HistoryContext history, int numOfCores)
	{
		this(jobID, jobName, jobTicket, priority, jobState, submitTime, runAttempts, null, history, numOfCores);
		if (sweep == null) {
			_logger.error("not adding sweep job since sweep object is null!");
			/* important to keep the state non-null, since this is supposedly a sweep even if broken. we don't want this sent to a BES. */
			_sweepState = "broken sweep";
			return;
		}
		_sweepState = sweep.getEncodedSweepState();
	}

	public void setUserName(String uname)
	{
		_userName = uname;
	}

	public String getUserName()
	{
		return _userName;
	}

	public void setStartTime(Date start)
	{
		_startTime = start;
	}

	public Date getStartTime()
	{
		return _startTime;
	}

	public void setFinishTime(Date endTime)
	{
		_finishTime = endTime;
	}

	public Date getFinishTime()
	{
		return _finishTime;
	}

	public boolean isSweepingJob()
	{
		return _sweepState != null;
	}

	final public String jobName()
	{
		return _jobName;
	}

	final public void jobName(String jobName)
	{
		_jobName = jobName;
	}

	final public HistoryContext history(HistoryEventCategory category)
	{
		HistoryContext ret = (HistoryContext) _history.clone();
		ret.category(category);
		return ret;
	}

	final public HistoryEventToken historyToken()
	{
		return _historyToken;
	}

	final public void historyToken(HistoryEventToken historyToken)
	{
		_historyToken = historyToken;
	}

	public boolean killed()
	{
		return _killed;
	}

	public void kill()
	{
		_killed = true;
	}

	public long getJobID()
	{
		return _jobID;
	}

	public String getJobTicket()
	{
		return _jobTicket;
	}

	public short getPriority()
	{
		return _priority;
	}

	public QueueStates getJobState()
	{
		return _jobState;
	}

	public void setJobState(QueueStates jobState)
	{
		_jobState = jobState;
	}

	public Date getSubmitTime()
	{
		return _submitTime;
	}

	public void setBESID(long besID)
	{
		_besID = new Long(besID);
	}

	public int getNumOfCores()
	{
		return _numOfCores;
	}

	public void setBESActivityStatus(ActivityStatusType ast)
	{
		// We go through all this rigamoral because we want to keep the status
		// in memory rather than in the DB, but if you aren't careful with
		// data structure pulled in from the wire, you can accidentally
		// keep live references to the entire SOAP message from whence they
		// came, inadvertently consuming too much memory. By serializing and
		// deserializing it, we guarantee that the SOAP message that this
		// came from is not referenced by the copy.
		try {
			QName tmp = new QName("http://tempuri.org", "tmp");
			StringWriter writer = new StringWriter();
			ObjectSerializer.serialize(writer, ast, tmp);
			StringReader reader = new StringReader(writer.toString());
			_besActivityStatus = (ActivityStatusType) ObjectDeserializer.deserialize(new InputSource(reader), ActivityStatusType.class);
		} catch (Throwable cause) {
			_besActivityStatus = null;
		}
	}

	public ActivityStatusType getBESActivityStatus()
	{
		return _besActivityStatus;
	}

	/**
	 * Clear any associate with a BES container.
	 */
	public void clearBESID()
	{
		_besID = null;
		_besActivityStatus = null;
	}

	public Long getBESID()
	{
		return _besID;
	}

	public short getRunAttempts()
	{
		return _runAttempts;
	}

	synchronized public void incrementRunAttempts(int incr)
	{
		_runAttempts += incr;
	}

	synchronized public void incrementRunAttempts()
	{
		incrementRunAttempts(1);
	}

	synchronized public JobResourceRequirements getResourceRequirements(JobManager _jobManager, int jobDescIndex)
		throws ResourceException, SQLException
	{
		int listSize = _resourceRequirements.size();
		if (jobDescIndex >= listSize) {
			JobDefinition_Type jsdl = _jobManager.getJSDL(_jobID);
			_resourceRequirements.add(jobDescIndex, new JobResourceRequirements(jsdl, jobDescIndex));
		}
		return _resourceRequirements.get(jobDescIndex);
	}

	public String currentJobAction()
	{
		return _jobAction;
	}

	public String setJobAction(String newAction)
	{
		synchronized (_jobActionLock) {
			if (_jobAction != null)
				return _jobAction;
			_jobAction = newAction;
		}

		return null;
	}

	public void clearJobAction()
	{
		_jobAction = null;
	}

	public boolean canRun(Date now)
	{
		if (_nextValidRunTime == null)
			return true;

		if (now.after(_nextValidRunTime))
			return true;

		return false;
	}

	public void setNextValidRunTime(Date now)
	{
		_nextValidRunTime = new Date(now.getTime() + (BACKOFF << _runAttempts));
	}

	public Date getNextCanRun()
	{
		return _nextValidRunTime;
	}

	@Override
	public String toString()
	{
		return String.format("Job %s (id = %d)", _jobTicket, _jobID);
	}
}