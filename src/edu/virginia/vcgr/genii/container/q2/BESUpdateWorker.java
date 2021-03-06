package edu.virginia.vcgr.genii.container.q2;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.GetFactoryAttributesDocumentResponseType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;

/**
 * This is a worker class which is enqueued into outcall thread pools to actually make the out call to a BES to see if it is responsive at the
 * moment.
 * 
 * @author mmm2a
 */
public class BESUpdateWorker implements OutcallHandler
{
	static private Log _logger = LogFactory.getLog(BESUpdateWorker.class);

	private String _besName;
	private ServerDatabaseConnectionPool _connectionPool;
	private BESManager _manager;
	private long _besID;
	private IBESPortTypeResolver _portTypeResolver;

	public BESUpdateWorker(ServerDatabaseConnectionPool connectionPool, BESManager manager, long besID, String besName,
		IBESPortTypeResolver clientStubResolver)
	{
		_connectionPool = connectionPool;
		_manager = manager;
		_besID = besID;
		_besName = besName;
		_portTypeResolver = clientStubResolver;
	}

	public boolean equals(BESUpdateWorker other)
	{
		return (_manager == other._manager) && (_besID == other._besID);
	}

	@Override
	public boolean equals(OutcallHandler other)
	{
		if (other instanceof BESUpdateWorker)
			return equals((BESUpdateWorker) other);

		return false;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof BESUpdateWorker)
			return equals((BESUpdateWorker) other);

		return false;
	}

	public void run()
	{
		Connection connection = null;

		try {
			/* Acquire a new database connection to use */
			connection = _connectionPool.acquire(false);

			/*
			 * Use the client stub resolver to finally load the EPR for the BES container from the database. Because we are in the "run"
			 * method of this class, this epr shouldn't get loaded until one of the threads from the outcall thread pool has been allocated to
			 * this worker task. This way we limit the number of EPRs in memory at any one time.
			 */
			GeniiBESPortType clientStub = _portTypeResolver.createClientStub(connection, _besID);
			try {
				connection.commit();
			} catch (Throwable c) {
			}
			ClientUtils.setTimeout(clientStub, 120 * 1000);

			/*
			 * Go ahead and Mark the BES as missed until we have actually communicated with it.
			 */
			_manager.markBESAsMissed(_besID, "Startup procedure");

			/*
			 * Make the out call to the BES object to get it's factory attributes
			 */
			GetFactoryAttributesDocumentResponseType resp = clientStub.getFactoryAttributesDocument(new GetFactoryAttributesDocumentType());

			/*
			 * If the bes container is currently accepting new activities, and it responded at all, we mark it as available.
			 */
			if (resp.getFactoryResourceAttributesDocument().isIsAcceptingNewActivities()) {
				_logger.info(String.format("Marking container %s as available.", _besName));
				_manager.markBESAsAvailable(_besID);
			} else {
				/* Otherwise, we mark it as unavailable */
				_manager.markBESAsUnavailable(_besID, "Not currently accepting activities");
			}
		} catch (Throwable cause) {
			/*
			 * If we couldn't talk to the container at all, then we mark it as missed.
			 */
			_logger.warn(String.format("Unable to update BES container %s(%d).", _besName, _besID), cause);
			_manager.markBESAsMissed(_besID,
				String.format("Exception during communication %s(%s)", cause.getClass().getName(), cause.getLocalizedMessage()));
		} finally {
			_connectionPool.release(connection);
		}
	}
}
