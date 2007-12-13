package edu.virginia.vcgr.genii.container.q2;

import java.sql.Connection;

import org.ws.addressing.EndpointReferenceType;

public interface IJobEndpointResolver
{
	public EndpointReferenceType getJobEndpoint(
		Connection connection, long jobID) throws Throwable;
}