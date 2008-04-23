package edu.virginia.vcgr.genii.container.replicatedExport.resolver;

import java.sql.SQLException;
import java.util.Properties;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceProvider;

public class RExportResolverFactoryDBResourceProvider  extends BasicDBResourceProvider
{
	public RExportResolverFactoryDBResourceProvider(Properties props)
		throws ConfigurationException, SQLException
	{
		super(props);
	}
	
	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
		throws SQLException
	{
		return new RExportResolverFactoryDBResourceFactory(pool, getTranslater());
	}
}