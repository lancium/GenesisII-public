package edu.virginia.vcgr.genii.container.scheduler.basic;

import java.sql.SQLException;
import java.util.Properties;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.rns.RNSDBResourceProvider;

public class BasicSchedulerDBResourceProvider extends RNSDBResourceProvider
{
	public BasicSchedulerDBResourceProvider(Properties props)
		throws ConfigurationException, SQLException
	{
		super(props);
	}
	
	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
		throws SQLException
	{
		return new BasicSchedulerDBResourceFactory(pool, getTranslater());
	}
}