package edu.virginia.vcgr.genii.container.common.notification;

import java.sql.Connection;
import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class DBSubscriptionResourceFactory extends BasicDBResourceFactory
{
	@Override
	protected void createTables() throws SQLException
	{
		Connection conn = null;
		super.createTables();

		try {
			conn = _pool.acquire(false);
			SubscriptionsDatabase.createTables(conn);
			conn.commit();
		} finally {
			_pool.release(conn);
		}
	}

	public DBSubscriptionResourceFactory(DatabaseConnectionPool pool) throws SQLException
	{
		super(pool);
	}

	@Override
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try {
			return new DBSubscriptionResource(parentKey, _pool);
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}
}