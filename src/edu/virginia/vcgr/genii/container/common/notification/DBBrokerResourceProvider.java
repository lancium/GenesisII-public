package edu.virginia.vcgr.genii.container.common.notification;

import java.sql.SQLException;
import java.util.Properties;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceProvider;

public class DBBrokerResourceProvider extends BasicDBResourceProvider
{
	@Override
	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
		throws SQLException
	{
		return new DBBrokerResourceFactory(pool);
	}
	
	public DBBrokerResourceProvider(Properties properties)
			throws SQLException
	{
		super(properties);
	}
}