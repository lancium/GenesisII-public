package edu.virginia.vcgr.genii.container.q2;

import java.util.Date;

import edu.virginia.vcgr.genii.client.queue.QueueStates;

public class JobData
{
	private boolean _killed;
	private short _runAttempts;
	private long _jobID;
	private String _jobTicket;
	private short _priority;
	private QueueStates _jobState;
	private Date _submitTime;
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