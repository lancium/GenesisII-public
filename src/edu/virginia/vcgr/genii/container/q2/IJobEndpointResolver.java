package edu.virginia.vcgr.genii.container.q2;

import java.sql.Connection;

import org.ws.addressing.EndpointReferenceType;

/**
 * This interface is implemented by objects that have the ability to late bind job ID's to job EPRs.
 * This facilitates keeping the memory usage of the queue down by avoiding having too many EPRs in
 * memory at any given time.
 * 
 * @author mmm2a
 */
public interface IJobEndpointResolver
{
	/**
	 * Given the job ID, determine the job's endpoint (if it has one).
	 * 
	 * @param connection
	 *            The database connection to use to load EPRs from the database with.
	 * @param jobID
	 *            The ID of the job for which we want the EPR.
	 * 
	 * @return The EPR of the job indicated (if it has one).
	 * 
	 * @throws Throwable
	 */
	public EndpointReferenceType getJobEndpoint(Connection connection, long jobID) throws Throwable;
}