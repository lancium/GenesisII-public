package edu.virginia.vcgr.genii.container.q2;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.BESPortType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentResponseType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentType;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class BESUpdateWorker implements Runnable
{
	static private Log _logger = LogFactory.getLog(BESUpdateWorker.class);
	
	private DatabaseConnectionPool _connectionPool;
	private BESManager _manager;
	private long _besID;
	private IBESPortTypeResolver _portTypeResolver;
	
	public BESUpdateWorker(DatabaseConnectionPool connectionPool,
		BESManager manager, long besID, IBESPortTypeResolver clientStubResolver)
	{
		_connectionPool = connectionPool;
		_manager = manager;
		_besID = besID;
		_portTypeResolver = clientStubResolver;
	}
	
	public void run()
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire();
			BESPortType clientStub = _portTypeResolver.createClientStub(
				connection, _besID);
			GetFactoryAttributesDocumentResponseType resp =
				clientStub.getFactoryAttributesDocument(
					new GetFactoryAttributesDocumentType());
			if (resp.getFactoryResourceAttributesDocument(
				).isIsAcceptingNewActivities())
			{
				_manager.markBESAsAvailable(_besID);
			} else
			{
				_manager.markBESAsUnavailable(_besID);
			}
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to update BES container " + _besID, cause);
			_manager.markBESAsUnavailable(_besID);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
}