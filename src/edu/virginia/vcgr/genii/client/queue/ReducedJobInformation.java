package edu.virginia.vcgr.genii.client.queue;

import java.util.Collection;

import edu.virginia.vcgr.genii.security.credentials.identity.Identity;

public class ReducedJobInformation
{
	private JobTicket _jobTicket;
	private Collection<Identity> _owners;
	private QueueStates _state;
	
	public ReducedJobInformation(JobTicket ticket,
		Collection<Identity> owners, QueueStates state)
	{
		_jobTicket = ticket;
		_owners = owners;
		_state = state;
	}
	
	public JobTicket getTicket()
	{
		return _jobTicket;
	}
	
	public Collection<Identity> getOwners()
	{
		return _owners;
	}
	
	public QueueStates getJobState()
	{
		return _state;
	}
}