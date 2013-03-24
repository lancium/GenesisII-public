package edu.virginia.vcgr.genii.container.notification;

import java.sql.Connection;
import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class NotificationBrokerDBResourceFactory extends BasicDBResourceFactory implements IResourceFactory
{

	public NotificationBrokerDBResourceFactory(DatabaseConnectionPool pool) throws SQLException
	{
		super(pool);
	}

	@Override
	protected void createTables() throws SQLException
	{
		Connection conn = null;
		super.createTables();
		try {
			conn = _pool.acquire(false);
			NotificationBrokerDatabase.createTables(conn);
			conn.commit();
		} finally {
			_pool.release(conn);
		}
	}

	@Override
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try {
			return new NotificationBrokerDBResource(parentKey, _pool);
		} catch (SQLException ex) {
			throw new ResourceException(ex.getLocalizedMessage(), ex);
		}
	}
}
