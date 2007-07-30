package edu.virginia.vcgr.genii.client.queue;

import java.util.Calendar;
import java.util.Collection;

import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;

public class JobInformation extends ReducedJobInformation
{
	private int _priority;
	private Calendar _submitTime;
	private Calendar _startTime;
	private Calendar _finishTime;
	private int _failedAttempts;
	
	public JobInformation(JobTicket ticket,
		Collection<Identity> owners, QueueStates state,
		int prioity, Calendar submitTime,
		Calendar startTime, Calendar finishTime,
		int failedAttempts)
	{
		super(ticket, owners, state);
		
		_priority = prioity;
		_submitTime = submitTime;
		_startTime = startTime;
		_finishTime = finishTime;
		_failedAttempts = failedAttempts;
	}
	
	public int getPriority()
	{
		return _priority;
	}
	
	public Calendar getSubmitTime()
	{
		return _submitTime;
	}
	
	public Calendar getStartTime()
	{
		return _startTime;
	}
	
	public Calendar getFinishTime()
	{
		return _finishTime;
	}
	
	public int getFailedAttempts()
	{
		return _failedAttempts;
	}
}