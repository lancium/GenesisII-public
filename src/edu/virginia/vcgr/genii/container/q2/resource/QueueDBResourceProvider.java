package edu.virginia.vcgr.genii.container.q2.resource;

import java.sql.SQLException;
import java.util.Properties;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceProvider;

public class QueueDBResourceProvider extends BasicDBResourceProvider
{
	public QueueDBResourceProvider(Properties props)
		throws SQLException
	{
		super(props);
	}
	
	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
		throws SQLException
	{
		return new QueueDBResourceFactory(pool);
	}
}