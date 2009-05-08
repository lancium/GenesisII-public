package edu.virginia.vcgr.genii.container.q2.besinfo;

import java.sql.Connection;

import org.ggf.bes.factory.GetFactoryAttributesDocumentType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.utils.Duration;
import edu.virginia.vcgr.genii.container.cservices.infomgr.InformationEndpoint;
import edu.virginia.vcgr.genii.container.cservices.infomgr.InformationResolver;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class BESInformationResolver 
	implements InformationResolver<BESInformation>
{
	static private final long MAX_TIMEOUT = 1000L * 60 * 2;
	
	private DatabaseConnectionPool _connectionPool;
	
	public BESInformationResolver(DatabaseConnectionPool connectionPool)
	{
		_connectionPool = connectionPool;
	}
	
	@Override
	public BESInformation acquire(InformationEndpoint endpoint, 
		Duration timeout) throws Throwable
	{
		Connection connection = null;
		BESEndpoint bEndpoint = (BESEndpoint)endpoint;
		
		try
		{
			connection = _connectionPool.acquire();
			GeniiBESPortType bes = bEndpoint.getClientStub(connection);
			
			long timeoutValue = timeout.getTimeoutInMilliseconds();
			if (timeoutValue > MAX_TIMEOUT)
				timeoutValue = MAX_TIMEOUT;
			ClientUtils.setTimeout(bes, (int)timeoutValue);
			return new BESInformation(bes.getFactoryAttributesDocument(
				new GetFactoryAttributesDocumentType()));
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
}