package edu.virginia.vcgr.genii.container.q2;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;

/**
 * A simple data structure for bundling together information necessary to get a job's status (EPRs
 * and calling context).
 * 
 * @author mmm2a
 */
public class JobStatusInformation
{
	private EndpointReferenceType _jobEndpoint;
	private EndpointReferenceType _besEndpoint;
	private ICallingContext _jobCallingContext;

	public JobStatusInformation(EndpointReferenceType jobEndpoint, EndpointReferenceType besEndpoint,
		ICallingContext callingContext)
	{
		_jobEndpoint = jobEndpoint;
		_besEndpoint = besEndpoint;
		_jobCallingContext = callingContext;
	}

	public EndpointReferenceType getJobEndpoint()
	{
		return _jobEndpoint;
	}

	public EndpointReferenceType getBESEndpoint()
	{
		return _besEndpoint;
	}

	public ICallingContext getCallingContext()
	{
		return _jobCallingContext;
	}
}