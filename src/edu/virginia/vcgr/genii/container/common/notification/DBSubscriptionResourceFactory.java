package edu.virginia.vcgr.genii.container.common.notification;

import java.sql.Connection;
import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class DBSubscriptionResourceFactory extends BasicDBResourceFactory
{
	static private final String _CREATE_SUBSCRIPTIONS_TABLE =
		"CREATE TABLE subscriptions(" +
		"subscriptionid VARCHAR(128) PRIMARY KEY, sourcekey VARCHAR(128)," +
		"topic VARCHAR(256), targetendpoint BLOB(2G)," +
		"userdata VARCHAR (8192) FOR BIT DATA)";
	static private final String _CREATE_SUBSCRIPTIONS_SOURCE_INDEX =
		"CREATE INDEX subscriptionssourceidx ON subscriptions(sourcekey)";
	
	public DBSubscriptionResourceFactory(
			DatabaseConnectionPool pool)
		throws SQLException
	{
		super(pool);
	}
	
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new DBSubscriptionResource(parentKey, _pool);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}
	
	protected void createTables() throws SQLException
	{
		Connection conn = null;
		super.createTables();
		
		try
		{
			conn = _pool.acquire(false);
			DatabaseTableUtils.createTables(conn, false, 
				_CREATE_SUBSCRIPTIONS_TABLE,
				_CREATE_SUBSCRIPTIONS_SOURCE_INDEX);
			conn.commit();
		}
		finally
		{
			_pool.release(conn);
		}
	}
}