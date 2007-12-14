package edu.virginia.vcgr.genii.container.q2;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;

/**
 * A simple data structure for bundling together information needed to call 
 * out to a bes container and kill a job.
 * @author mmm2a
 */
public class KillInformation
{
	private ICallingContext _callingContext;
	private EndpointReferenceType _jobEndpoint;
	private EndpointReferenceType _besEndpoint;
	
	public KillInformation(ICallingContext callingContext, 
		EndpointReferenceType jobEndpoint, EndpointReferenceType besEndpoint)
	{
		_callingContext = callingContext;
		_jobEndpoint = jobEndpoint;
		_besEndpoint = besEndpoint;
	}
	
	public ICallingContext getCallingContext()
	{
		return _callingContext;
	}
	
	public EndpointReferenceType getJobEndpoint()
	{
		return _jobEndpoint;
	}
	
	public EndpointReferenceType getBESEndpoint()
	{
		return _besEndpoint;
	}
}