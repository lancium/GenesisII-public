package edu.virginia.vcgr.genii.client.queue;

import edu.virginia.cs.vcgr.genii.job_management.JobStateEnumerationType;

public enum QueueStates
{
	QUEUED,
	REQUEUED,
	STARTING,
	RUNNING,
	FINISHED,
	ERROR;
	
	static public QueueStates fromQueueStateType(JobStateEnumerationType state)
	{
		return QueueStates.valueOf(state.getValue());
	}
	
	public boolean isFinalState()
	{
		return equals(QueueStates.FINISHED) || equals(QueueStates.ERROR);
	}
}