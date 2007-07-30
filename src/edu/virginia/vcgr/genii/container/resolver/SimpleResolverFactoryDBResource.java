package edu.virginia.vcgr.genii.container.resolver;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class SimpleResolverFactoryDBResource extends BasicDBResource implements ISimpleResolverFactoryResource
{

	public SimpleResolverFactoryDBResource(ResourceKey parentKey, DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(parentKey, connectionPool);
	}
	
}