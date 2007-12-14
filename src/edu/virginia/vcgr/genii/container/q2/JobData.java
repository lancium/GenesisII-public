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
}