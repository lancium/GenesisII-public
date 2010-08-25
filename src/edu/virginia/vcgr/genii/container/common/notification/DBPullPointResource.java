package edu.virginia.vcgr.genii.container.common.notification;

import java.sql.Connection;
import java.sql.SQLException;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class DBPullPointResource extends BasicDBResource
{
	public DBPullPointResource(String key, Connection connection)
	{
		super(key, connection);
	}
	
	public DBPullPointResource(ResourceKey parentKey,
		DatabaseConnectionPool connectionPool) throws SQLException
	{
		super(parentKey, connectionPool);
	}
}