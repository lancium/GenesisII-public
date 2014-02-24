package edu.virginia.vcgr.genii.container.resolver;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceProvider;

public class GeniiResolverDBResourceProvider extends BasicDBResourceProvider
{
	protected IResourceFactory instantiateResourceFactory(ServerDatabaseConnectionPool pool) throws SQLException
	{
		return new GeniiResolverDBResourceFactory(pool);
	}
}