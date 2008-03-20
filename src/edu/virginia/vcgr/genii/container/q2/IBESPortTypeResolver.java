package edu.virginia.vcgr.genii.container.q2;

import java.sql.Connection;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;

/**
 * This interface is used to indicate an object which is cable of
 * resolving a besID into a bes port type.  It's purpose is to
 * facilitate late-binding of besID's to EPRs so as to minimize
 * memory usage of the queue.
 * 
 * @author mmm2a
 */
public interface IBESPortTypeResolver
{
	/**
	 * Given the besID, create a new BESPortType client stub that can be used
	 * to communicate with the given BES container.
	 * 
	 * @param connection The database connection to use for loading EPRs from
	 * the database.
	 * @param besID The id of the bes container.
	 * 
	 * @return A new BESPortType client stub.
	 * 
	 * @throws Throwable
	 */
	public GeniiBESPortType createClientStub(
		Connection connection, long besID)
			throws Throwable;
}