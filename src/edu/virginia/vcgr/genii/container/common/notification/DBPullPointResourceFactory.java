package edu.virginia.vcgr.genii.container.common.notification;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class DBPullPointResourceFactory extends BasicDBResourceFactory
{
	@Override
	protected void createTables() throws SQLException
	{
	}

	public DBPullPointResourceFactory(ServerDatabaseConnectionPool pool) throws SQLException
	{
		super(pool);
	}

	@Override
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try {
			return new DBPullPointResource((ResourceKey) parentKey, _pool);
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}
}