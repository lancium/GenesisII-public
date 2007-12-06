package edu.virginia.vcgr.genii.container.q2.resource;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class QueueDBResource extends BasicDBResource implements IQueueResource
{
	public QueueDBResource(ResourceKey parentKey, DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(parentKey, connectionPool);
	}
}