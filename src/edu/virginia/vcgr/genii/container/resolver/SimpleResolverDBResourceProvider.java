package edu.virginia.vcgr.genii.container.resolver;

import java.sql.SQLException;
import java.util.Properties;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceProvider;

public class SimpleResolverDBResourceProvider extends BasicDBResourceProvider
{
	public SimpleResolverDBResourceProvider(Properties props)
		throws SQLException
	{
		super(props);
	}
	
	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
		throws SQLException
	{
		return new SimpleResolverDBResourceFactory(pool);
	}
}