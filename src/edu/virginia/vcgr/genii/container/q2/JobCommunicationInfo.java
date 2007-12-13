package edu.virginia.vcgr.genii.container.q2;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;

public class JobCommunicationInfo
{
	private long _jobID;
	private ICallingContext _callContext;
	private EndpointReferenceType _jobEndpoint;
	private long _besID;
	private EndpointReferenceType _besEndpoint;
	
	public JobCommunicationInfo(long jobID, ICallingContext callContext,
		EndpointReferenceType jobEndpoint, long besID, 
		EndpointReferenceType besEndpoint)
	{
		_jobID = jobID;
		_callContext = callContext;
		_jobEndpoint = jobEndpoint;
		_besID = besID;
		_besEndpoint = besEndpoint;
	}
	
	public long getJobID()
	{
		return _jobID;
	}
	
	public ICallingContext getCallingContext()
	{
		return _callContext;
	}
	
	public EndpointReferenceType getJobEndpoint()
	{
		return _jobEndpoint;
	}
	
	public long getBESID()
	{
		return _besID;
	}
	
	public EndpointReferenceType getBESEndpoint()
	{
		return _besEndpoint;
	}
}