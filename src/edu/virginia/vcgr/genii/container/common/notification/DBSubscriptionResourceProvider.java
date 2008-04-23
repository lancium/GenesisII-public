package edu.virginia.vcgr.genii.container.common.notification;

import java.sql.SQLException;
import java.util.Properties;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceProvider;

public class DBSubscriptionResourceProvider extends BasicDBResourceProvider
{
	public DBSubscriptionResourceProvider(Properties properties)
		throws ConfigurationException, SQLException
	{
		super(properties);
	}

	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
		throws SQLException
	{
		return new DBSubscriptionResourceFactory(pool, getTranslater());
	}
}