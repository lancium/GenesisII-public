package edu.virginia.vcgr.genii.container.q2;

import java.util.Date;

import edu.virginia.vcgr.genii.client.queue.QueueStates;

/**
 * This is the main data structure for keeping all information about a job
 * that is small enough to store in memory.
 * 
 * @author mmm2a
 */
public class JobData
{
	static private final long BACKOFF = 60 * 1000L;
	/**
	 * This variable is used internally by the queue to maintain the current
	 * "active" state of a job.  Is it in the process of being created or
	 * having it's status checked?  If the value is null, then no action is
	 * going on.
	 */
	private String _jobAction = null;
	
	private Object _jobActionLock = new Object();
	
	/**
	 * An internal flag used to indicate that the job has been killed and 
	 * needs to be cleaned up.
	 */
	private boolean _killed;
	
	/**
	 * The number of attempts that have unsuccessfully tried to run this
	 * job.
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
	 * The Job's priority.  Lower numbers imply a higher priority 
	 * (will run first).
	 */
	private short _priority;
	
	/**
	 * The current state of the job.
	 */
	private QueueStates _jobState;
	
	/**
	 * The time at which the job was submitted to the queue.  This information
	 * is kept so that we can ORDER jobs by submit time (after taking priority
	 * into account).
	 */
	private Date _submitTime;
	
	/**
	 * The BES ID associated with this job.  Notice that we use the wrapper
	 * class for longs here which allows this field to be null.  If it is
	 * null, then the job hasn't been matched to a resource yet.  If the
	 * besID is non-null, then it is in fact the bes key of the bes resource
	 * that we are running on (or starting on).
	 */
	private Long _besID;
	
	/**
	 * This date is used for exponential backoff.  If a job fails for a reason
	 * that increases the attempt number, then we exponentially back off the
	 * next time at which we can try running it.  This helps with resource
	 * contention.
	 */
	private Date _nextValidRunTime = null;
	
	public JobData(long jobID, String jobTicket, short priority,
		QueueStates jobState, Date submitTime, short runAttempts, Long besID)
	{
		_killed = false;
		_jobID = jobID;
		_jobTicket = jobTicket;
		_priority = priority;
		_jobState = jobState;
		_submitTime = submitTime;
		_besID = besID;
		_runAttempts = runAttempts;
	}
	
	public JobData(long jobID, String jobTicket, short priority,
		QueueStates jobState, Date submitTime, short runAttempts)
	{
		this(jobID, jobTicket, priority, jobState, submitTime, 
			runAttempts, null);
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
	
	/**
	 * Clear any associate with a BES container.
	 */
	public void clearBESID()
	{
		_besID = null;
	}
	
	public Long getBESID()
	{
		return _besID;
	}
	
	public short getRunAttempts()
	{
		return _runAttempts;
	}
	
	synchronized public void incrementRunAttempts()
	{
		_runAttempts++;
	}
	
	public String currentJobAction()
	{
		return _jobAction;
	}
	
	public String setJobAction(String newAction)
	{
		synchronized(_jobActionLock)
		{
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
		_nextValidRunTime = new Date(
			now.getTime() + (BACKOFF << _runAttempts));
	}
	
	public Date getNextCanRun()
	{
		return _nextValidRunTime;
	}
}